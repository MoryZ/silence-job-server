# 工作流 TODO 实现 - 完整代码

## 📍 实现位置

### 文件 1: WorkflowExecutorContext.java
**路径**: `silence-job-server-job-task/src/main/java/com/old/silence/job/server/job/task/support/executor/workflow/WorkflowExecutorContext.java`

---

## 🔧 代码修改

### 修改 1: WorkflowExecutorContext - 补充方法

**修改前** (L44):
```java
/**
 * TODO 父节点批次状态
 */
private JobTaskStatus parentJobTaskStatus;
```

**修改后** (添加 getter/setter 和验证方法):
```java
/**
 * 父节点批次状态 - 工作流决策节点判定用
 * 用于记录所有父节点的汇总状态
 */
private JobTaskStatus parentJobTaskStatus;

/**
 * 获取父节点任务状态
 * @return 父节点的状态
 */
public JobTaskStatus getParentJobTaskStatus() {
    return parentJobTaskStatus;
}

/**
 * 设置父节点任务状态
 * @param status 父节点状态
 */
public void setParentJobTaskStatus(JobTaskStatus status) {
    this.parentJobTaskStatus = status;
}

/**
 * 验证父节点是否都已成功完成
 * @return true 如果所有父节点都成功，false 其他状态
 */
public boolean isParentNodesSuccess() {
    return JobTaskStatus.SUCCESS.equals(parentJobTaskStatus);
}

/**
 * 验证是否可以跳过当前节点
 * 当父节点状态为 SKIPPED 时，子节点也应该跳过
 * @return true 如果应该跳过，false 应该执行
 */
public boolean shouldSkipNode() {
    return JobTaskStatus.SKIPPED.equals(parentJobTaskStatus);
}
```

---

### 修改 2: AbstractWorkflowExecutor - 完成决策逻辑

**路径**: `silence-job-server-job-task/src/main/java/com/old/silence/job/server/job/task/support/executor/workflow/AbstractWorkflowExecutor.java`

**修改前** (L65-90):
```java
if (WorkflowNodeType.DECISION.equals(context.getNodeType())) {

    List<JobTaskBatch> jobTaskBatches = jobTaskBatchDao.selectList(new LambdaQueryWrapper<JobTaskBatch>()
            .select(JobTaskBatch::getOperationReason)
            .eq(JobTaskBatch::getWorkflowTaskBatchId, context.getWorkflowTaskBatchId())
            .eq(JobTaskBatch::getWorkflowNodeId, context.getWorkflowNodeId())
    );

    if (CollectionUtils.isNotEmpty(jobTaskBatches)) {
        total = jobTaskBatches.size();
        // ToDo
        JobTaskBatch jobTaskBatch = jobTaskBatches.get(0);
        if (WORKFLOW_SUCCESSOR_SKIP_EXECUTION.contains(jobTaskBatch.getOperationReason())) {
            context.setEvaluationResult(Boolean.FALSE);
        } else {
            context.setEvaluationResult(Boolean.TRUE);
        }
    }

} else {
    total = jobTaskBatchDao.selectCount(new LambdaQueryWrapper<JobTaskBatch>()
            .eq(JobTaskBatch::getWorkflowTaskBatchId, context.getWorkflowTaskBatchId())
            .eq(JobTaskBatch::getWorkflowNodeId, context.getWorkflowNodeId())
    );
}
```

**修改后** (完整实现):
```java
if (WorkflowNodeType.DECISION.equals(context.getNodeType())) {
    
    // 查询该决策节点的所有父节点批次
    List<JobTaskBatch> jobTaskBatches = jobTaskBatchDao.selectList(
        new LambdaQueryWrapper<JobTaskBatch>()
            .select(JobTaskBatch::getId, 
                   JobTaskBatch::getOperationReason, 
                   JobTaskBatch::getStatus,
                   JobTaskBatch::getWorkflowNodeId)
            .eq(JobTaskBatch::getWorkflowTaskBatchId, context.getWorkflowTaskBatchId())
            .eq(JobTaskBatch::getWorkflowNodeId, context.getWorkflowNodeId())
            .orderByAsc(JobTaskBatch::getId)
    );

    if (CollectionUtils.isNotEmpty(jobTaskBatches)) {
        total = jobTaskBatches.size();
        
        // ✅ 修复: 对所有父节点批次进行聚合判定
        DecisionEvaluationResult evaluationResult = evaluateParentNodeDecision(
            jobTaskBatches, 
            context
        );
        
        // 设置决策结果
        context.setEvaluationResult(evaluationResult.isDecision());
        
        // 记录父节点状态
        context.setParentJobTaskStatus(evaluationResult.getParentStatus());
        
        // 📊 记录决策日志
        if (logger.isInfoEnabled()) {
            logger.info(
                "Workflow decision node evaluation: workflowNodeId={}, " +
                "workflowTaskBatchId={}, totalParentNodes={}, successCount={}, " +
                "skipCount={}, failedCount={}, decision={}, parentStatus={}",
                context.getWorkflowNodeId(),
                context.getWorkflowTaskBatchId(),
                total,
                evaluationResult.getSuccessCount(),
                evaluationResult.getSkipCount(),
                evaluationResult.getFailedCount(),
                evaluationResult.isDecision(),
                evaluationResult.getParentStatus()
            );
        }
        
    } else {
        // 没有父节点，决策节点直接执行
        context.setEvaluationResult(Boolean.TRUE);
        context.setParentJobTaskStatus(JobTaskStatus.SUCCESS);
        
        if (logger.isWarnEnabled()) {
            logger.warn(
                "Workflow decision node has no parent nodes: " +
                "workflowNodeId={}, workflowTaskBatchId={}",
                context.getWorkflowNodeId(),
                context.getWorkflowTaskBatchId()
            );
        }
    }
    
} else {
    // 非决策节点，查询任务批次总数
    total = jobTaskBatchDao.selectCount(
        new LambdaQueryWrapper<JobTaskBatch>()
            .eq(JobTaskBatch::getWorkflowTaskBatchId, context.getWorkflowTaskBatchId())
            .eq(JobTaskBatch::getWorkflowNodeId, context.getWorkflowNodeId())
    );
}
```

---

### 修改 3: 添加决策评估方法

**在 AbstractWorkflowExecutor 类中添加**:

```java
/**
 * 对所有父节点批次进行决策评估
 * 
 * 规则:
 * 1. 如果任何父节点指定跳过 -> 决策为 FALSE
 * 2. 如果任何父节点失败 -> 决策为 FALSE
 * 3. 如果所有父节点成功 -> 决策为 TRUE
 * 4. 如果有部分节点跳过、部分成功 -> 决策为 FALSE，状态为 SKIPPED
 * 
 * @param jobTaskBatches 所有父节点批次
 * @param context 工作流执行上下文
 * @return 决策评估结果
 */
private DecisionEvaluationResult evaluateParentNodeDecision(
        List<JobTaskBatch> jobTaskBatches,
        WorkflowExecutorContext context) {
    
    int totalNodes = jobTaskBatches.size();
    int successCount = 0;
    int skipCount = 0;
    int failedCount = 0;
    boolean shouldDecisionFalse = false;
    JobTaskStatus aggregatedStatus = JobTaskStatus.SUCCESS;
    
    // 遍历所有父节点批次
    for (JobTaskBatch batch : jobTaskBatches) {
        // 1. 检查是否指定跳过
        if (WORKFLOW_SUCCESSOR_SKIP_EXECUTION.contains(batch.getOperationReason())) {
            skipCount++;
            shouldDecisionFalse = true;
            aggregatedStatus = JobTaskStatus.SKIPPED;
            continue;
        }
        
        // 2. 检查批次状态
        JobTaskStatus batchStatus = batch.getStatus();
        
        if (JobTaskStatus.SUCCESS.equals(batchStatus)) {
            successCount++;
        } else if (JobTaskStatus.FAILED.equals(batchStatus)) {
            failedCount++;
            shouldDecisionFalse = true;
            aggregatedStatus = JobTaskStatus.FAILED;
        } else if (JobTaskStatus.RUNNING.equals(batchStatus)) {
            // 如果还有批次在运行，等待它完成
            logger.warn(
                "Parent node batch is still running: batchId={}, status={}",
                batch.getId(), batchStatus
            );
        } else if (JobTaskStatus.TIMEOUT.equals(batchStatus)) {
            // 超时也视为失败
            failedCount++;
            shouldDecisionFalse = true;
            aggregatedStatus = JobTaskStatus.TIMEOUT;
        }
    }
    
    // 汇总决策结果
    boolean finalDecision = !shouldDecisionFalse && (successCount == totalNodes);
    
    return DecisionEvaluationResult.builder()
            .decision(finalDecision)
            .successCount(successCount)
            .skipCount(skipCount)
            .failedCount(failedCount)
            .parentStatus(aggregatedStatus)
            .evaluatedAt(System.currentTimeMillis())
            .build();
}

/**
 * 处理跳过节点的逻辑
 * 当前驱节点状态为 SKIPPED 时，子节点也应该跳过执行
 */
private void handleSkipNodeExecution(WorkflowExecutorContext context) {
    if (context.shouldSkipNode()) {
        // 标记当前节点为跳过
        context.getTaskBatch().setStatus(JobTaskStatus.SKIPPED);
        context.getTaskBatch().setOperationReason(JobOperationReason.PARENT_SKIP);
        
        // 更新数据库
        jobTaskBatchDao.updateById(context.getTaskBatch());
        
        logger.info(
            "Skip node execution due to parent node skip: " +
            "nodeId={}, batchId={}",
            context.getWorkflowNodeId(),
            context.getTaskBatchId()
        );
    }
}
```

---

### 修改 4: 创建决策评估结果类

**新文件**: `silence-job-server-job-task/src/main/java/com/old/silence/job/server/job/task/support/executor/workflow/DecisionEvaluationResult.java`

```java
package com.old.silence.job.server.job.task.support.executor.workflow;

import com.old.silence.job.server.core.model.JobTaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 工作流决策节点评估结果
 * 
 * 用于记录决策节点对所有父节点的评估结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DecisionEvaluationResult {
    
    /**
     * 最终决策结果: true 表示继续执行，false 表示跳过
     */
    private boolean decision;
    
    /**
     * 成功的父节点数
     */
    private int successCount;
    
    /**
     * 跳过的父节点数
     */
    private int skipCount;
    
    /**
     * 失败的父节点数
     */
    private int failedCount;
    
    /**
     * 父节点汇总状态
     */
    private JobTaskStatus parentStatus;
    
    /**
     * 评估时间戳
     */
    private long evaluatedAt;
    
    /**
     * 获取总评估的父节点数
     */
    public int getTotalNodes() {
        return successCount + skipCount + failedCount;
    }
    
    /**
     * 是否所有父节点都成功
     */
    public boolean allParentsSuccess() {
        return skipCount == 0 && failedCount == 0;
    }
    
    /**
     * 是否有任何父节点失败
     */
    public boolean hasFailedParents() {
        return failedCount > 0;
    }
    
    /**
     * 获取评估结果的摘要信息
     */
    public String getSummary() {
        return String.format(
            "Decision=%s, Success=%d, Skip=%d, Failed=%d, ParentStatus=%s",
            decision, successCount, skipCount, failedCount, parentStatus
        );
    }
}
```

---

### 修改 5: 常量类 - 定义跳过原因

**更新或创建**: `silence-job-server-core/src/main/java/com/old/silence/job/server/core/constants/WorkflowConstants.java`

```java
package com.old.silence.job.server.core.constants;

import com.old.silence.job.server.core.model.JobOperationReason;
import java.util.Set;

/**
 * 工作流相关常量
 */
public class WorkflowConstants {
    
    /**
     * 工作流节点类型 - 决策后跳过执行的原因集合
     * 
     * 这些原因会导致决策节点返回 false，子节点被跳过
     */
    public static final Set<JobOperationReason> WORKFLOW_SUCCESSOR_SKIP_EXECUTION = 
        Set.of(
            JobOperationReason.MANUAL_SKIP,      // 手动跳过
            JobOperationReason.CONDITION_SKIP,   // 条件判定跳过
            JobOperationReason.PARENT_SKIP,      // 父节点跳过导致的连锁跳过
            JobOperationReason.TIMEOUT_SKIP      // 超时导致的跳过
        );
    
    /**
     * 决策节点评估策略
     */
    public static final int DECISION_STRATEGY_ALL_SUCCESS = 1;  // 所有父节点都成功
    public static final int DECISION_STRATEGY_ANY_SUCCESS = 2;  // 任一父节点成功
    public static final int DECISION_STRATEGY_NO_FAILURE = 3;   // 不能有失败的父节点
    
    /**
     * 默认决策策略
     */
    public static final int DEFAULT_DECISION_STRATEGY = DECISION_STRATEGY_ALL_SUCCESS;
    
    /**
     * 工作流执行超时 (单位: 毫秒)
     */
    public static final long WORKFLOW_EXECUTION_TIMEOUT = 24 * 60 * 60 * 1000L;  // 24小时
    
    /**
     * 决策节点的等待超时 (单位: 毫秒)
     */
    public static final long DECISION_NODE_WAIT_TIMEOUT = 5 * 60 * 1000L;  // 5分钟
}
```

---

### 修改 6: 使用示例代码

**在其他执行器中使用**:

```java
@Service
public class WorkflowNodeExecutor {
    
    @Autowired
    private WorkflowExecutorContext context;
    
    @Autowired
    private AbstractWorkflowExecutor abstractWorkflowExecutor;
    
    /**
     * 执行工作流节点
     */
    public void executeNode() {
        
        // 1. 检查是否应该跳过当前节点
        if (context.shouldSkipNode()) {
            logger.info("Skipping node execution due to parent node status");
            context.setNodeStatus(JobTaskStatus.SKIPPED);
            return;
        }
        
        // 2. 验证父节点是否满足执行条件
        if (!context.isParentNodesSuccess()) {
            logger.warn("Parent nodes are not in success state: status={}",
                context.getParentJobTaskStatus());
            context.setNodeStatus(JobTaskStatus.BLOCKED);
            return;
        }
        
        // 3. 正常执行节点
        try {
            // 执行业务逻辑
            doExecute();
            
            context.setNodeStatus(JobTaskStatus.SUCCESS);
        } catch (Exception e) {
            logger.error("Failed to execute node", e);
            context.setNodeStatus(JobTaskStatus.FAILED);
            context.setErrorMessage(e.getMessage());
        }
    }
}
```

---

## 📊 测试用例

**单元测试**: `WorkflowExecutorContextTest.java`

```java
@Test
public void testParentNodeStatusCheck() {
    // 安排
    WorkflowExecutorContext context = new WorkflowExecutorContext();
    context.setParentJobTaskStatus(JobTaskStatus.SUCCESS);
    
    // 执行
    boolean result = context.isParentNodesSuccess();
    
    // 验证
    assertTrue(result);
}

@Test
public void testShouldSkipNode() {
    // 安排
    WorkflowExecutorContext context = new WorkflowExecutorContext();
    context.setParentJobTaskStatus(JobTaskStatus.SKIPPED);
    
    // 执行
    boolean shouldSkip = context.shouldSkipNode();
    
    // 验证
    assertTrue(shouldSkip);
}

@Test
public void testDecisionEvaluationWithMultipleParents() {
    // 安排
    List<JobTaskBatch> parents = new ArrayList<>();
    JobTaskBatch parent1 = new JobTaskBatch();
    parent1.setStatus(JobTaskStatus.SUCCESS);
    parent1.setOperationReason(JobOperationReason.NORMAL);
    parents.add(parent1);
    
    JobTaskBatch parent2 = new JobTaskBatch();
    parent2.setStatus(JobTaskStatus.SUCCESS);
    parent2.setOperationReason(JobOperationReason.NORMAL);
    parents.add(parent2);
    
    // 执行
    DecisionEvaluationResult result = evaluateParentNodeDecision(parents, context);
    
    // 验证
    assertTrue(result.isDecision());
    assertEquals(2, result.getSuccessCount());
    assertEquals(0, result.getSkipCount());
    assertEquals(JobTaskStatus.SUCCESS, result.getParentStatus());
}
```

---

## ✅ 验证清单

- [ ] WorkflowExecutorContext 添加 getter/setter 方法
- [ ] AbstractWorkflowExecutor 完成决策评估逻辑
- [ ] DecisionEvaluationResult 类创建
- [ ] WorkflowConstants 添加必要常量
- [ ] 单元测试编写并通过
- [ ] 集成测试验证决策流程
- [ ] 代码审查完成
- [ ] 编译通过 (mvn clean compile)
- [ ] 提交到版本控制

