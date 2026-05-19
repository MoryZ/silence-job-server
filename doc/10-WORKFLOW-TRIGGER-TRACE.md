# /workflows/trigger Endpoint Trace Report

## Overview
This document traces the complete execution path of the `POST /api/v1/workflows/trigger` HTTP endpoint through the silence-job-server codebase.

---

## 1. Controller Layer

### File: `/Users/moryzang/IdeaProjects/silence-job-server/silence-job-server-app/src/main/java/com/old/silence/job/server/api/WorkflowResource.java`

```java
@RestController
@RequestMapping("/api/v1")
public class WorkflowResource {
    
    @PostMapping("/workflows/trigger")
    public Boolean trigger(@RequestBody @Validated WorkflowTriggerVO triggerVO) {
        return workflowService.trigger(triggerVO);
    }
}
```

**Method Signature:** `public Boolean trigger(@RequestBody @Validated WorkflowTriggerVO triggerVO)`

---

## 2. Service Layer

### File: `/Users/moryzang/IdeaProjects/silence-job-server/silence-job-server-app/src/main/java/com/old/silence/job/server/domain/service/WorkflowService.java`

```java
public Boolean trigger(WorkflowTriggerVO triggerVO) {
    // 1. Fetch workflow by ID
    Workflow workflow = workflowDao.selectById(triggerVO.getWorkflowId());
    Assert.notNull(workflow, () -> new SilenceJobServerException("workflow can not be null."));

    // 2. Validate group is enabled
    long count = groupConfigDao.selectCount(
            new LambdaQueryWrapper<GroupConfig>()
                    .eq(GroupConfig::getGroupName, workflow.getGroupName())
                    .eq(GroupConfig::getNamespaceId, workflow.getNamespaceId())
                    .eq(GroupConfig::getGroupStatus, true)
    );
    Assert.isTrue(count > 0,
            () -> new SilenceJobServerException("组:[{}]已经关闭，不支持手动执行.", workflow.getGroupName()));

    // 3. Prepare DTO for task execution
    WorkflowTaskPrepareDTO prepareDTO = WorkflowTaskConverter.INSTANCE.toWorkflowTaskPrepareDTO(workflow);
    prepareDTO.setNextTriggerAt(DateUtils.toNowMilli());  // Immediate execution
    prepareDTO.setTaskExecutorScene(JobTaskExecutorScene.MANUAL_WORKFLOW);
    
    String tmpWfContext = triggerVO.getTmpWfContext();
    if (StrUtil.isNotBlank(tmpWfContext) && !JSON.isValid(tmpWfContext)){
        prepareDTO.setWfContext(tmpWfContext);
    }
    
    // 4. Delegate to handler
    terminalWorkflowPrepareHandler.handler(prepareDTO);
    return Boolean.TRUE;
}
```

---

## 3. Handler Layer

### File: `/Users/moryzang/IdeaProjects/silence-job-server/silence-job-server-job-task/src/main/java/com/old/silence/job/server/job/task/support/prepare/workflow/TerminalWorkflowPrepareHandler.java`

```java
@Component
public class TerminalWorkflowPrepareHandler extends AbstractWorkflowPrePareHandler {
    
    @Override
    protected void doHandler(WorkflowTaskPrepareDTO jobPrepareDTO) {
        log.debug("无处理中的工作流数据. workflowId:[{}]", jobPrepareDTO.getWorkflowId());
        workflowBatchGenerator.generateJobTaskBatch(
            WorkflowTaskConverter.INSTANCE.toWorkflowTaskBatchGeneratorContext(jobPrepareDTO)
        );
    }
}
```

---

## 4. Batch Generator Layer

### File: `/Users/moryzang/IdeaProjects/silence-job-server/silence-job-server-job-task/src/main/java/com/old/silence/job/server/job/task/support/generator/batch/WorkflowBatchGenerator.java`

```java
public void generateJobTaskBatch(WorkflowTaskBatchGeneratorContext context) {
    // 1. Create workflow task batch entity
    WorkflowTaskBatch workflowTaskBatch = WorkflowTaskConverter.INSTANCE.toWorkflowTaskBatch(context);
    workflowTaskBatch.setTaskBatchStatus(Optional.ofNullable(context.getTaskBatchStatus())
        .orElse(JobTaskBatchStatus.WAITING));
    workflowTaskBatch.setOperationReason(context.getOperationReason());
    workflowTaskBatch.setWfContext(context.getWfContext());

    // 2. INSERT into database - sj_workflow_task_batch table
    Assert.isTrue(1 == workflowTaskBatchDao.insert(workflowTaskBatch), 
        () -> new SilenceJobServerException("新增调度任务失败."));

    // 3. Register timer task for immediate execution
    if (JobTaskBatchStatus.WAITING != workflowTaskBatch.getTaskBatchStatus()) {
        return;
    }

    long delay = context.getNextTriggerAt().longValue() - DateUtils.toNowMilli();
    WorkflowTimerTaskDTO workflowTimerTaskDTO = new WorkflowTimerTaskDTO();
    workflowTimerTaskDTO.setWorkflowTaskBatchId(workflowTaskBatch.getId());
    workflowTimerTaskDTO.setWorkflowId(context.getWorkflowId());
    workflowTimerTaskDTO.setTaskExecutorScene(context.getTaskExecutorScene());

    // 4. Register with time wheel for delayed execution
    JobTimerWheel.registerWithWorkflow(() -> new WorkflowTimerTask(workflowTimerTaskDTO), 
        Duration.ofMillis(delay));
}
```

---

## 5. Timer Task Layer (Actor Message)

### File: `/Users/moryzang/IdeaProjects/silence-job-server/silence-job-server-job-task/src/main/java/com/old/silence/job/server/job/task/support/timer/WorkflowTimerTask.java`

```java
public class WorkflowTimerTask implements TimerTask<String> {
    
    @Override
    public void run(Timeout timeout) throws Exception {
        try {
            WorkflowNodeTaskExecuteDTO taskExecuteDTO = new WorkflowNodeTaskExecuteDTO();
            taskExecuteDTO.setWorkflowTaskBatchId(workflowTimerTaskDTO.getWorkflowTaskBatchId());
            taskExecuteDTO.setTaskExecutorScene(workflowTimerTaskDTO.getTaskExecutorScene());
            taskExecuteDTO.setParentId(SystemConstants.ROOT);
            
            // ACTOR MESSAGE SENT HERE - Tell WorkflowExecutorActor
            ActorRef actorRef = ActorGenerator.workflowTaskExecutorActor();
            actorRef.tell(taskExecuteDTO, actorRef);
            
        } catch (Exception e) {
            SilenceJobLog.LOCAL.error("任务调度执行失败", e);
        }
    }
}
```

**Actor Message Sent:**
- `ActorRef`: `ActorGenerator.workflowTaskExecutorActor()` 
- Message Type: `WorkflowNodeTaskExecuteDTO`
- Actor Name: `WorkflowExecutorActor`

---

## 6. Workflow Executor Actor

### File: `/Users/moryzang/IdeaProjects/silence-job-server/silence-job-server-job-task/src/main/java/com/old/silence/job/server/job/task/support/dispatch/WorkflowExecutorActor.java`

```java
@Component(ActorGenerator.WORKFLOW_EXECUTOR_ACTOR)  // "WorkflowExecutorActor"
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class WorkflowExecutorActor extends AbstractActor {
    
    @Override
    public Receive createReceive() {
        return receiveBuilder().match(WorkflowNodeTaskExecuteDTO.class, taskExecute -> {
            log.info("工作流开始执行. [{}]", JSON.toJSONString(taskExecute));
            try {
                doExecutor(taskExecute);
            } catch (Exception e) {
                // Error handling and alarm event publication
            }
        }).build();
    }
    
    private void doExecutor(WorkflowNodeTaskExecuteDTO taskExecute) {
        // 1. SELECT workflow task batch
        WorkflowTaskBatch workflowTaskBatch = workflowTaskBatchDao.selectById(taskExecute.getWorkflowTaskBatchId());
        
        // 2. UPDATE task batch status to RUNNING
        if (SystemConstants.ROOT.equals(taskExecute.getParentId())
                && JobTaskBatchStatus.WAITING == workflowTaskBatch.getTaskBatchStatus()) {
            handlerTaskBatch(taskExecute, JobTaskBatchStatus.RUNNING, JobOperationReason.NONE);
            
            // 3. SELECT workflow details
            Workflow workflow = workflowDao.selectById(workflowTaskBatch.getWorkflowId());
            
            // 4. Register timeout check task
            JobTimerWheel.registerWithWorkflow(() -> new WorkflowTimeoutCheckTask(taskExecute.getWorkflowTaskBatchId()),
                    Duration.ofSeconds(workflow.getExecutorTimeout()));
        }
        
        // 5. Get DAG graph from cache
        String flowInfo = workflowTaskBatch.getFlowInfo();
        MutableGraph<BigInteger> graph = MutableGraphCache.getOrDefault(workflowTaskBatch.getId(), flowInfo);
        
        // 6. SELECT job task batches for workflow nodes
        List<JobTaskBatch> allJobTaskBatchList = jobTaskBatchDao.selectList(...);
        
        // 7. SELECT workflow nodes
        List<WorkflowNode> workflowNodes = workflowNodeDao.selectList(...);
        
        // 8. SELECT jobs for workflow nodes
        List<Job> jobs = jobDao.selectBatchIds(...);
        
        // 9. Execute each workflow node via WorkflowExecutor
        for (WorkflowNode workflowNode : workflowNodes) {
            WorkflowExecutor workflowExecutor = WorkflowExecutorFactory.getWorkflowExecutor(workflowNode.getNodeType());
            WorkflowExecutorContext context = ...;
            workflowExecutor.execute(context);
        }
    }
}
```

---

## 7. Additional Actor Messages

### WorkflowBatchHandler
**File:** `/Users/moryzang/IdeaProjects/silence-job-server/silence-job-server-job-task/src/main/java/com/old/silence/job/server/job/task/support/handler/WorkflowBatchHandler.java`

```java
// Line 335-336 - recoveryWorkflowExecutor method
ActorRef actorRef = ActorGenerator.jobTaskPrepareActor();
actorRef.tell(jobTaskPrepare, actorRef);

// Line 365-366 - tellWorkflowTaskExecutor method  
ActorRef actorRef = ActorGenerator.workflowTaskExecutorActor();
actorRef.tell(taskExecuteDTO, actorRef);
```

### WorkflowTaskPrepareActor
**File:** `/Users/moryzang/IdeaProjects/silence-job-server/silence-job-server-job-task/src/main/java/com/old/silence/job/server/job/task/support/dispatch/WorkflowTaskPrepareActor.java`

```java
// Handles WorkflowTaskPrepareDTO messages and delegates to WorkflowPrePareHandler
```

---

## 8. Database Operations Summary

### Tables Involved:
1. **sj_workflow_task_batch** - Workflow task batch records
2. **sj_workflow** - Workflow definitions
3. **sj_group_config** - Group configuration (validation)
4. **sj_workflow_node** - Workflow DAG nodes
5. **sj_job** - Job definitions
6. **sj_job_task_batch** - Job task batches

### DAO Classes:
| DAO Class | File Path |
|-----------|-----------|
| `WorkflowDao` | `silence-job-server-core/.../infrastructure/persistence/dao/WorkflowDao.java` |
| `WorkflowTaskBatchDao` | `silence-job-server-core/.../infrastructure/persistence/dao/WorkflowTaskBatchDao.java` |
| `GroupConfigDao` | `silence-job-server-core/.../infrastructure/persistence/dao/GroupConfigDao.java` |
| `WorkflowNodeDao` | `silence-job-server-core/.../infrastructure/persistence/dao/WorkflowNodeDao.java` |
| `JobDao` | `silence-job-server-core/.../infrastructure/persistence/dao/JobDao.java` |
| `JobTaskBatchDao` | `silence-job-server-core/.../infrastructure/persistence/dao/JobTaskBatchDao.java` |

---

## 9. Actor System Configuration

**File:** `/Users/moryzang/IdeaProjects/silence-job-server/silence-job-server-common/src/main/java/com/old/silence/job/server/common/pekko/ActorGenerator.java`

Key Actor references:
- `workflowTaskExecutorActor()` -> `"WorkflowExecutorActor"` in `jobActorSystem`
- `workflowTaskPrepareActor()` -> `"WorkflowTaskPrepareActor"` in `jobActorSystem`
- `jobTaskPrepareActor()` -> `"JobTaskPrepareActor"` in `jobActorSystem`

---

## 10. Execution Flow Diagram

```
POST /api/v1/workflows/trigger
         |
         v
WorkflowResource.trigger()
         |
         v
WorkflowService.trigger()
         |-- workflowDao.selectById() [READ]
         |-- groupConfigDao.selectCount() [READ]
         |
         v
TerminalWorkflowPrepareHandler.doHandler()
         |
         v
WorkflowBatchGenerator.generateJobTaskBatch()
         |-- workflowTaskBatchDao.insert() [INSERT]
         |
         v
JobTimerWheel.registerWithWorkflow()
         |
         v (Timer expires - async)
WorkflowTimerTask.run()
         |-- ActorGenerator.workflowTaskExecutorActor().tell()
         |
         v (Akka Actor)
WorkflowExecutorActor.doExecutor()
         |-- workflowTaskBatchDao.selectById() [READ]
         |-- workflowTaskBatchDao.updateById() [UPDATE]
         |-- workflowDao.selectById() [READ]
         |-- jobTaskBatchDao.selectList() [READ]
         |-- workflowNodeDao.selectList() [READ]
         |-- jobDao.selectBatchIds() [READ]
         |
         v
WorkflowBatchHandler.openNextNode()
         |-- ActorGenerator.workflowTaskExecutorActor().tell()
```

---

## Key Files Summary

| Component | File Path |
|-----------|-----------|
| Controller | `/Users/moryzang/IdeaProjects/silence-job-server/silence-job-server-app/src/main/java/com/old/silence/job/server/api/WorkflowResource.java` |
| Service | `/Users/moryzang/IdeaProjects/silence-job-server/silence-job-server-app/src/main/java/com/old/silence/job/server/domain/service/WorkflowService.java` |
| Terminal Handler | `/Users/moryzang/IdeaProjects/silence-job-server/silence-job-server-job-task/src/main/java/com/old/silence/job/server/job/task/support/prepare/workflow/TerminalWorkflowPrepareHandler.java` |
| Batch Generator | `/Users/moryzang/IdeaProjects/silence-job-server/silence-job-server-job-task/src/main/java/com/old/silence/job/server/job/task/support/generator/batch/WorkflowBatchGenerator.java` |
| Timer Task | `/Users/moryzang/IdeaProjects/silence-job-server/silence-job-server-job-task/src/main/java/com/old/silence/job/server/job/task/support/timer/WorkflowTimerTask.java` |
| Executor Actor | `/Users/moryzang/IdeaProjects/silence-job-server/silence-job-server-job-task/src/main/java/com/old/silence/job/server/job/task/support/dispatch/WorkflowExecutorActor.java` |
| Batch Handler | `/Users/moryzang/IdeaProjects/silence-job-server/silence-job-server-job-task/src/main/java/com/old/silence/job/server/job/task/support/handler/WorkflowBatchHandler.java` |
| Actor Generator | `/Users/moryzang/IdeaProjects/silence-job-server/silence-job-server-common/src/main/java/com/old/silence/job/server/common/pekko/ActorGenerator.java` |
| DTO | `/Users/moryzang/IdeaProjects/silence-job-server/silence-job-server-app/src/main/java/com/old/silence/job/server/dto/WorkflowTriggerVO.java` |
| Entity | `/Users/moryzang/IdeaProjects/silence-job-server/silence-job-server-core/src/main/java/com/old/silence/job/server/domain/model/WorkflowTaskBatch.java` |
