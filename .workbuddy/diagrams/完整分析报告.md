# Silence-Job 接口 & 定时任务 & 单次运行 完整分析报告

> 生成时间: 2026-05-20
> 项目: silence-job-server

---

## 一、`/jobs/{id}/trigger` 接口分析

### 基本信息

| 项目 | 内容 |
|------|------|
| HTTP 方法 | `POST` |
| URL | `/api/v1/jobs/{id}/trigger` |
| 入参 | `JobTriggerVO`（可携带临时参数 `tmpArgsStr`） |
| Controller | `JobResource.trigger()` |
| 场景标记 | `scene = MANUAL_JOB` |

### 完整调用链

```
JobResource.trigger(id, jobTrigger)
  → JobService.trigger()
    → jobDao.selectById(id)                          // READ sj_job
    → groupConfigDao.selectCount()                   // 检查组是否开启 (group_status=true)
    → 转换 DTO: nextTriggerAt=now, scene=MANUAL_JOB
    → TerminalJobPrepareHandler.doHandle()
      → JobTaskBatchGenerator.generateJobTaskBatch()
        → jobTaskBatchDao.insert()                   // INSERT sj_job_task_batch (status=WAITING)
        → 无客户端? → status=CANCEL + 告警通知
        → JobTimerWheel.registerWithJob()            // 事务提交后注册时间轮
          → JobTimerTask.run() 到期触发 (delay≈0, 立即执行)
            → JobExecutorActor.doExecute()
              → ClusterJobExecutor.doExecute()
                → RequestClientActor.doExecute()      // RPC 分发到客户端执行
                  → 客户端执行 Job
                  → 回调结果 → 更新 sj_job_task_batch status
```

### 关键点

1. **trigger 设置 `nextTriggerAt=now`**，所以 delay ≈ 0，任务立即进入时间轮并执行
2. **只检查 `group_status`**，不检查 `jobStatus`，意味着即使 Job 被禁用也能手动触发
3. **可携带临时参数** `tmpArgsStr`，覆盖原有 job 参数
4. **无客户端时自动取消**，并发送告警通知

### 涉及数据库表

| 操作 | 表 | 说明 |
|------|-----|------|
| READ | sj_job | 查询 Job 信息 |
| READ | sj_group_config | 检查组是否开启 |
| INSERT | sj_job_task_batch | 创建任务批次 |
| UPDATE | sj_job_task_batch | 任务执行完成后更新状态 |

---

## 二、`/workflows/trigger` 接口分析

### 基本信息

| 项目 | 内容 |
|------|------|
| HTTP 方法 | `POST` |
| URL | `/api/v1/workflows/trigger` |
| 入参 | `WorkflowTriggerVO`（可携带工作流上下文 `tmpWfContext`） |
| Controller | `WorkflowResource.trigger()` |
| 场景标记 | `scene = MANUAL_WORKFLOW` |

### 完整调用链

```
WorkflowResource.trigger(triggerVO)
  → WorkflowService.trigger()
    → workflowDao.selectById()                        // READ sj_workflow
    → groupConfigDao 检查所有相关组                    // 循环检查每个 groupName, group_status=true
    → 转换 DTO: nextTriggerAt=now, scene=MANUAL_WORKFLOW
    → TerminalWorkflowPrepareHandler.doHandler()
      → WorkflowBatchGenerator.generateJobTaskBatch()
        → workflowTaskBatchDao.insert()               // INSERT sj_workflow_task_batch (status=WAITING)
        → JobTimerWheel.registerWithWorkflow()
          → WorkflowTimerTask.run() 到期触发 (delay≈0)
            → WorkflowExecutorActor.doExecutor()
              → 遍历 DAG 拓扑排序
              → 按节点类型分发:
                ├─ JOB_TASK → JobTaskWorkflowExecutor.execute()
                │     → JobTaskBatchGenerator.generateJobTaskBatch()
                │       → INSERT sj_job_task_batch
                │       → JobTimerWheel.registerWithJob()
                │         → JobExecutorActor → ClusterJobExecutor → RPC 分发
                ├─ DECISION → DecisionWorkflowExecutor (表达式评估, 决定下游分支)
                └─ CALLBACK → CallbackWorkflowExecutor (回调通知)
              → 节点完成: mergeAllWorkflowContext()    // 合并工作流上下文
              → 开启下游节点: openNextNode()           // 满足条件时开启
              → 所有节点完成 → UPDATE sj_workflow_task_batch status
```

### 关键点

1. **Workflow 是 DAG 结构**，触发后按拓扑排序逐节点执行
2. **三种节点类型**：JOB_TASK（执行Job）、DECISION（条件分支）、CALLBACK（回调通知）
3. **同样只检查 `group_status`**，不检查 `workflowStatus`
4. **工作流上下文可在节点间传递和合并**
5. **每个 JOB_TASK 节点实际会创建一个 sj_job_task_batch**，走 Job 的执行链路

### 与 Job Trigger 的区别

| 维度 | Job Trigger | Workflow Trigger |
|------|------------|-----------------|
| 粒度 | 单个 Job | 整个 DAG 工作流 |
| 执行方式 | 直接创建一个 TaskBatch | 创建 WorkflowBatch → 逐节点创建 TaskBatch |
| 节点类型 | 无 | JOB_TASK / DECISION / CALLBACK |
| 上下文 | tmpArgsStr 临时参数 | tmpWfContext 工作流上下文 |
| 完成判断 | 单次执行完成 | 所有 DAG 节点执行完成 |

### 涉及数据库表

| 操作 | 表 | 说明 |
|------|-----|------|
| READ | sj_workflow | 查询 Workflow 信息 |
| READ | sj_group_config | 检查所有相关组是否开启 |
| INSERT | sj_workflow_task_batch | 创建工作流批次 |
| INSERT | sj_job_task_batch | 每个 JOB_TASK 节点创建 |
| UPDATE | sj_job_task_batch | Job 执行完成更新状态 |
| UPDATE | sj_workflow_task_batch | 工作流完成更新状态 |

---

## 三、项目定时任务汇总

### 调度架构

项目定时任务分为两大调度体系：

1. **Pekko Actor 定时调度**（核心调度链，SCHEDULE_PERIOD=30s）
   - `DispatchService` → `ConsumerBucketActor` → 3 个扫描 Actor

2. **Spring TaskScheduler**（`AbstractSchedule` 基类 + `ShedLock` 分布式锁）
   - 统一的 cron 配置方式
   - `lockAtMostFor` / `lockAtLeastFor` 防止多节点重复执行

### 核心调度任务（4 个）

| 任务 | 频率 | 作用 | 涉及表 | 关键过滤条件 |
|------|------|------|--------|-------------|
| DispatchService | 30s | 定时分发 bucket 任务，驱动任务执行引擎 | - | Pekko Timer 驱动 |
| ScanJobTaskActor | 30s | 扫描到期 Job，创建任务批次 | sj_job, sj_group_config | `job_status=true AND next_trigger_at<=now+30s` |
| ScanWorkflowTaskActor | 30s | 扫描到期 Workflow，创建工作流批次 | sj_workflow, sj_group_config | `workflow_status=true AND next_trigger_at<=now+30s` |
| ScanRetryActor | 30s | 扫描到期重试任务，重新分发 | sj_retry | `next_trigger_at<=now AND retry_count<max` |

### 节点管理任务（2 个）

| 任务 | 频率 | 作用 | 涉及表 |
|------|------|------|--------|
| OfflineNodeSchedule | 5s | 清理过期下线节点，释放任务槽位 | sj_server_node |
| ClientRegister.RefreshNodeSchedule | 5s | 客户端节点心跳续签，保持在线状态 | sj_group_config |

### 统计汇总任务（3 个）

| 任务 | 频率 | 作用 | 涉及表 |
|------|------|------|--------|
| JobSummarySchedule | 1min | Job Dashboard 统计汇总 | sj_job_summary |
| WorkflowJobSummarySchedule | 1min | Workflow Dashboard 统计汇总 | sj_job_summary |
| RetrySummarySchedule | 1min | Retry Dashboard 统计汇总 | sj_job_summary |

### 日志处理任务（2 个）

| 任务 | 频率 | 作用 | 涉及表 |
|------|------|------|--------|
| JobLogMergeSchedule | 1min | Job 日志合并归档，减少碎片 | sj_log_message |
| RetryLogMergeSchedule | 1h | Retry 日志合并归档 | sj_log_message |

### 告警任务（2 个）

| 任务 | 频率 | 作用 | 涉及表 |
|------|------|------|--------|
| RetryTaskMoreThresholdAlarmSchedule | 10min | 重试超量告警 | sj_retry |
| RetryErrorMoreThresholdAlarmSchedule | 10min | 重试失败告警 | sj_retry |

### 日志清理任务（2 个）

| 任务 | 频率 | 作用 | 涉及表 |
|------|------|------|--------|
| JobClearLogSchedule | 4h | Job 日志定期清理 | sj_log_message, sj_job_task_batch |
| CleanerSchedule | 4h | Retry 清理 + 死信迁移 | sj_retry, sj_retry_dead_letter |

---

## 四、定时任务和工作流能否单次运行？

### 结论：当前代码已原生支持，无需修改！

### 核心逻辑

```
定时扫描路径:  Scan → 检查 status=true? → 是 → 创建批次 → 执行 → 更新 nextTriggerAt
                              ↓ 否
                           跳过（不执行）

手动触发路径:  Trigger → 只检查 group_status → 创建批次 (nextTriggerAt=now) → 立即执行 → 一次性完成
```

### 代码证据

| 路径 | 文件 | 行号 | 逻辑 |
|------|------|------|------|
| 定时扫描过滤 Job | `ScanJobTaskActor.java` | 174 | `.eq(Job::getJobStatus, true)` — 必须启用才扫描 |
| 定时扫描过滤 Workflow | `ScanWorkflowTaskActor.java` | 137 | `.eq(Workflow::getWorkflowStatus, true)` — 必须启用才扫描 |
| Trigger 不检查 Job 状态 | `JobService.trigger()` | 176-198 | 只查 group_status，不查 jobStatus |
| Trigger 不检查 Workflow 状态 | `WorkflowService.trigger()` | 263-288 | 只查 group_status，不查 workflowStatus |
| OpenAPI 同样不检查 | `OpenApiTriggerJobRequestHandler` | - | 只检查组状态，不检查 job_status |

### 操作步骤

**Job 单次运行**：

| 步骤 | 操作 | API |
|------|------|-----|
| 1. 停止自动执行 | 禁用 Job | `PUT /api/v1/jobs/{id}/disable` |
| 2. 手动触发一次 | 触发执行 | `POST /api/v1/jobs/{id}/trigger` |
| 3. 恢复自动执行 | 启用 Job | `PUT /api/v1/jobs/{id}/enable` |

**Workflow 单次运行**：

| 步骤 | 操作 | API |
|------|------|-----|
| 1. 停止自动执行 | 禁用 Workflow | `PUT /api/v1/workflows/{id}/disable` |
| 2. 手动触发一次 | 触发执行 | `POST /api/v1/workflows/trigger` |
| 3. 恢复自动执行 | 启用 Workflow | `PUT /api/v1/workflows/{id}/enable` |

### 注意事项

1. **disable 后 `next_trigger_at` 不会被更新**，重新 enable 后需要等下一个 Scan 周期（30s）才会触发
2. **OpenAPI 触发也不检查 status**，外部系统同样可以触发停用状态的任务
3. **如果需要"停用后也禁止手动触发"**，需要在 trigger 方法中额外加状态检查
4. **手动触发时 `scene` 不同**：MANUAL_JOB / MANUAL_WORKFLOW，与定时触发的场景标记不同
5. **手动触发可携带临时参数**：Job 可带 `tmpArgsStr`，Workflow 可带 `tmpWfContext`，覆盖原有配置
6. **手动触发不会更新 `next_trigger_at`**，所以不会影响后续的定时调度周期

---

## 附录：图表文件

以下分析图表保存在 `.workbuddy/diagrams/` 目录：

| 文件 | 内容 |
|------|------|
| `job-trigger-sequence.svg` | Job Trigger 时序图 |
| `workflow-trigger-sequence.svg` | Workflow Trigger 时序图 |
| `scheduled-tasks-overview.svg` | 项目定时任务全景图 |
| `manual-trigger-feasibility.svg` | 定时任务单次运行可行性分析 |
