# silence-job-server 项目全面分析计划

> 目标：仅分析、不修改任何文件；逐模块梳理核心业务功能、职责边界与依赖关系。

---

## 一、项目总体概况

| 属性 | 值 |
|------|-----|
| groupId | com.old.silence |
| artifactId | silence-job-server |
| version | 3.0.0-SNAPSHOT |
| Java 版本 | 21 |
| Spring Boot 主类 | `SilenceJobCenterApplication` |
| 系统定位 | 分布式任务调度与重试平台（Server 端） |
| Java 文件总量 | 624 个 |

---

## 二、模块层次与依赖关系

### 2.1 模块分层图（从底到顶）

```
┌─────────────────────────────────────────────┐
│           silence-job-server-starter          │ ← 启动入口（可执行 fat-jar）
│  依赖: app / core / common / job-task / retry │
└────────────────────┬────────────────────────┘
                     │
        ┌────────────┴────────────┐
        ▼                         ▼
┌───────────────┐         ┌───────────────────┐
│  server-app   │         │ (直接聚合 job/retry)│
│  REST API层   │         └───────────────────┘
└───────┬───────┘
        │ 依赖
        ▼
┌────────────────────┐   ┌───────────────────────┐
│  server-job-task   │◄──│  server-retry-task     │
│  定时任务执行引擎  │──►│  重试任务执行引擎      │
└────────┬───────────┘   └──────────┬────────────┘
         │ 均依赖                     │
         └──────────┬───────────────┘
                    ▼
          ┌──────────────────────┐
          │  server-task-common   │ ← 任务公共层（阻塞策略/时间轮/幂等）
          └──────────┬───────────┘
                     │ 依赖
          ┌──────────┴───────────┐
          │    server-common      │ ← 基础设施层（gRPC/Pekko/负载均衡/RPC/缓存）
          └──────────┬───────────┘
                     │ 依赖
          ┌──────────┴───────────┐
          │     server-core       │ ← 最底层：领域模型 + MyBatis Plus DAO
          └──────────────────────┘
```

### 2.2 关键依赖注意点

| 问题 | 说明 |
|------|------|
| ⚠️ job-task ↔ retry-task 逻辑耦合 | job-task 依赖 retry-task；retry-task 也被 job-task 聚合，形成高耦合 |
| ⚠️ 硬编码版本 | `silence-job-server-core:3.0.0-SNAPSHOT`、`silence-auth-center-client:2.0.1-SNAPSHOT`、`jakarta.persistence-api:3.1.0` 未走 dependencyManagement |
| ⚠️ testkit 进生产 | `pekko-actor-testkit-typed_2.13` 未标 test scope |
| ⚠️ Scala 运行时 | scala-library:2.13.12 传递依赖，影响 fat-jar 大小 |

---

## 三、各子模块详细分析

---

### 3.1 `silence-job-server-core`（持久化层）

**职责：** 领域模型定义 + MyBatis Plus DAO，是所有上层模块的数据访问基础，无任何业务逻辑。

**核心依赖：**
- `platform-data-mybatis-plus`（ORM 框架）
- `silence-job-common-server-api`（接口契约）
- `mysql-connector-j`（数据库驱动）

**领域模型（domain/model 下 20 个实体类）：**

| 实体 | 对应业务 |
|------|---------|
| `GroupConfig` | 执行器组配置（命名空间下的业务分组） |
| `Job` | 定时任务定义（触发规则、执行器类型、参数等） |
| `JobExecutor` | 执行器注册信息 |
| `JobTask` | 单次任务实例 |
| `JobTaskBatch` | 任务批次（一次触发对应一个批次） |
| `JobLogMessage` | 任务执行日志 |
| `JobSummary` | 任务执行统计汇总 |
| `JobNotifyConfigRelation` | 任务与通知配置关联 |
| `Retry` | 重试任务配置（来自客户端上报） |
| `RetryTask` | 重试任务实例 |
| `RetryDeadLetter` | 死信任务（重试耗尽后转移） |
| `RetrySceneConfig` | 重试场景配置 |
| `RetrySummary` | 重试统计汇总 |
| `RetryTaskLogMessage` | 重试日志 |
| `RetrySceneConfigNotifyConfigRelation` | 重试场景与通知关联 |
| `ServerNode` | Server 节点注册信息（用于集群负载） |
| `DistributedLock` | DB 分布式锁记录 |
| `SequenceAlloc` | 分段 ID 分配表（美团 Leaf 方案） |
| `Namespace` | 命名空间（多租户隔离） |
| `Workflow` / `WorkflowNode` / `WorkflowTaskBatch` | 工作流 DAG 相关 |
| `SystemUser` / `SystemUserPermission` | 系统用户与权限 |
| `NotifyConfig` / `NotifyRecipient` / `NotifyConfigRecipientRelation` | 告警通知配置 |

**DAO 层：** 共 20+ 个 Mapper 接口，全部继承 `BaseMapper<T>`，含部分自定义 SQL（如多表 Join 的 JobBatch/WorkflowBatch 查询）。

**职责边界：** 只做数据存取，不含任何调度/业务判断逻辑。

---

### 3.2 `silence-job-server-common`（基础设施层）

**职责：** 提供全项目通用的基础能力：RPC 通信、Actor 框架集成、负载均衡、节点注册、分布式锁、ID 生成、告警抽象、缓存管理。

**关键子包与功能：**

| 子包 | 功能描述 |
|------|---------|
| `rpc/server` | gRPC Server + Netty HTTP Server 双模式，统一接收客户端 RPC 请求 |
| `rpc/client` | gRPC Client + Netty HTTP Client，调用执行器客户端 |
| `rpc/okhttp` | OkHttp3 封装，支持 HTTP 外部调用 |
| `pekko` | Pekko（Akka 开源分支）Actor 系统集成，`ActorGenerator` 统一创建 Actor |
| `allocate/server` | `AllocateMessageQueueAveragely`：Server 节点间平均分桶（借鉴 RocketMQ） |
| `allocate/client` | 客户端负载均衡：Random/Round/LRU/First/Last/ConsistentHash 6 种策略 |
| `handler/ServerNodeBalance` | **集群核心**：定时扫 DB 节点、检测变化、触发 rebalance 重新分配 Bucket |
| `handler/ClientNodeAllocateHandler` | 客户端节点分配处理 |
| `handler/ConfigVersionSyncHandler` | 配置版本同步（客户端配置热更新） |
| `register/ServerRegister` | 本节点注册到 DB，维持心跳 |
| `register/ClientRegister` | 跟踪客户端执行器节点 |
| `cache/CacheRegisterTable` | 全局内存注册表（groupName → hostId → RegisterNodeInfo） |
| `cache/CacheNotifyRateLimiter` | 告警限流（Guava Rate Limiter） |
| `lock/` | 分布式锁：JDBC 持久化锁（`JdbcLockProvider`）+ 内存一次性锁 |
| `generator/id` | 分布式 ID：Snowflake + 分段式（Segment，双缓冲 SegmentBuffer） |
| `alarm/` | 告警抽象（Job/Retry/Workflow 三类告警基类） |
| `dto/DistributeInstance` | **全局单例**：持有当前节点分配的 Bucket 集合 + rebalance 状态标志 |

**关键配置：** `SystemProperties`（`silence.job.server` 前缀）：
- `bucketTotal`（默认 128）：分桶总数
- `loadBalanceCycleTime`：rebalance 检查周期
- `rpcType`：RPC 类型（grpc/netty）

---

### 3.3 `silence-job-server-task-common`（任务公共逻辑层）

**职责：** 为 job-task 和 retry-task 提供共享的任务执行抽象，不含具体的调度驱动。

| 组件 | 功能 |
|------|------|
| `BlockStrategy` / `BlockStrategyFactory` | 阻塞策略抽象（丢弃/并发/覆盖） |
| `AbstractBlockStrategy` / `BlockStrategyContext` | 阻塞策略模板方法 |
| `TimerIdempotent` | 时间轮任务幂等（防重复触发） |
| `AbstractLogStorage` | 日志存储抽象基类 |
| `LogMergeUtils` | 日志合并聚合工具 |
| `AbstractTimerWheel` | 时间轮基类（封装 Netty HashedWheelTimer） |
| `TimerWheelConfig` | 时间轮配置（精度、轮次等） |
| `AbstractLogActor` | Pekko 日志 Actor 基类 |

---

### 3.4 `silence-job-server-retry-task`（重试任务执行引擎）

**职责：** 负责将客户端上报的失败任务在服务端进行重试调度，支持多种阻塞策略、限流、死信、告警。

**核心调度链路：**
```
ScanRetryActor（扫描 Bucket 中待重试任务）
  → RetryTaskPrepareActor（任务预处理，按状态分发）
    → WaitRetryPrepareHandler / RunningRetryPrepareHandler / TerminalRetryPrepareHandler
      → RetryExecutor（真正执行，通过 RPC 调用客户端）
        → RequestRetryClientActor / RequestCallbackClientActor / RequestStopClientActor
          → RetryResultActor（处理执行结果）
            → RetrySuccessHandler / RetryFailureHandler / RetryStopHandler
```

**关键子组件：**

| 组件 | 功能 |
|------|------|
| `RetryTimerWheel` | Netty 时间轮，管理重试超时检测 |
| `RetryTimeoutCheckTask` | 超时检测任务 |
| `BlockStrategy` (Concurrency/Discard/Overlay) | 3 种阻塞策略 |
| `CacheGroupRateLimiter` | 分组级别限流（Caffeine + Guava RateLimiter） |
| `IdempotentHolder` | 重试任务幂等防重 |
| `TaskGenerator` (ClientReport/ManaSSingle/ManaBatch) | 三种重试任务生成方式：客户端上报、管理端单个、管理端批量 |
| `RetryLogStorage` | 重试日志落库 |
| `CleanerSchedule` | 定期清理已完成/过期任务 |
| `RetrySummarySchedule` | 统计汇总定时任务 |
| `RetryErrorMoreThresholdAlarmSchedule` | 重试错误超阈值告警 |
| `RetryLogMergeSchedule` | 日志合并落库 |
| `RetryDeadLetterConverter` | 超出重试次数转死信队列 |
| `RateLimiterHandler` | 限流拦截链 |
| `ReportRetryInfoHttpRequestHandler` | 处理客户端上报重试信息的 HTTP 请求 |
| `ReportDispatchResultHttpRequestHandler` | 处理客户端回传重试执行结果 |

---

### 3.5 `silence-job-server-job-task`（定时任务执行引擎）

**职责：** 定时任务的完整生命周期管理：触发、分配、执行（Cluster/Broadcast/Sharding/Map/MapReduce）、回调、停止、工作流 DAG 编排。

**核心调度链路（两条）：**

#### Job 链路：
```
ScanJobTaskActor（按 Bucket 扫描待触发 Job）
  → JobTaskPrepareActor（预处理/阻塞策略判断）
    → WaitJobPrepareHandler / RunningJobPrepareHandler / TerminalJobPrepareHandler
      → JobExecutorActor（选择执行器，按类型分发）
        → ClusterJobExecutor / BroadcastJobExecutor / ShardingJobExecutor / MapJobExecutor / MapReduceJobExecutor
          → RequestClientActor（RPC 调用客户端）
            → JobExecutorResultActor（回调结果处理）
              → ClusterJobExecutorHandler / BroadcastJobExecutorHandler / ...
```

#### Workflow 链路：
```
ScanWorkflowTaskActor
  → WorkflowTaskPrepareActor
    → WorkflowExecutorActor
      → JobTaskWorkflowExecutor / DecisionWorkflowExecutor / CallbackWorkflowExecutor
        → ReduceActor（MapReduce 归约）
```

**关键子组件：**

| 组件 | 功能 |
|------|------|
| `JobTimerWheel` | Netty 时间轮（常驻任务，触发间隔 <10s 走时间轮代替 DB 扫描） |
| `ResidentJobTimerTask` | 常驻任务触发器（高频任务优化） |
| `ResidentTaskCache` | 常驻任务本地缓存（Caffeine） |
| `MutableGraphCache` | 工作流 DAG 图缓存 |
| `JobBlockStrategyFactory` (4 种) | Job 阻塞策略：丢弃/并发/覆盖/恢复 |
| `WorkflowBlockStrategyFactory` (4 种) | Workflow 阻塞策略 |
| `LockExecutor` / `DistributedLockHandler` | 任务执行期间的分布式锁防并发 |
| `JobTaskBatchGenerator` / `WorkflowBatchGenerator` | 批次生成 |
| `JobTaskGeneratorFactory` (6 种) | 任务生成：Cluster/Broadcast/Sharding/Map/MapReduce/Workflow |
| `ClientCallbackFactory` (5 种) | 按执行类型回调处理：Cluster/Broadcast/Sharding/Map/MapReduce |
| `JobTaskStopFactory` (5 种) | 停止任务：按类型分发停止逻辑 |
| `ExpressionInvocationHandler` | Aviator/QLExpress 表达式引擎代理（Workflow 决策节点条件判断） |
| `JobClearLogSchedule` | 定期清理 Job 日志 |
| `JobSummarySchedule` | Job 执行统计汇总 |
| `OpenApiXxx RequestHandler` | 开放 API 处理器（增删改查 Job/Workflow） |
| `MapTaskPostHttpRequestHandler` | Map 任务的子任务分发回调 |

**任务执行类型说明：**

| 类型 | 说明 |
|------|------|
| Cluster | 集群中单节点执行（负载均衡选一台） |
| Broadcast | 广播所有客户端节点执行 |
| Sharding | 分片执行（任务参数按分片数分发给多个节点） |
| Map | 父任务生成子任务分发（MapReduce 的 Map 阶段） |
| MapReduce | Map + 归约（ReduceActor 合并结果） |

---

### 3.6 `silence-job-server-app`（Web API 层）

**职责：** 提供管理端 REST API，包含认证鉴权、多租户隔离、VO/命令对象转换。

**REST 接口（Resource 类）一览：**

| Resource | 管理对象 |
|----------|---------|
| `GroupConfigResource` | 执行器组配置（CRUD + 状态变更 + 导出） |
| `JobResource` | 定时任务 CRUD + 手动触发 + 导出 |
| `JobTaskResource` | 任务实例查询 |
| `JobBatchResource` | 任务批次查询 |
| `JobLogResource` | 任务日志查询 |
| `JobExecutorResource` | 执行器节点管理 |
| `RetryResource` | 重试任务管理 |
| `RetryTaskResource` | 重试任务实例 |
| `RetryDeadLetterResource` | 死信队列管理（回滚/批量删除） |
| `SceneConfigResource` | 重试场景配置 |
| `WorkflowResource` | 工作流 CRUD |
| `WorkflowNodeResource` | 工作流节点管理 |
| `WorkflowBatchResource` | 工作流批次查询 |
| `NamespaceResource` | 命名空间（多租户）管理 |
| `NotifyConfigResource` | 告警通知配置 |
| `NotifyRecipientResource` | 告警接收人管理 |
| `DashboardResource` | 仪表盘数据（统计折线图/卡片） |
| `SystemUserResource` | 系统用户 + 权限管理 |
| `OnlinePodsResource` / `PodsResource` | 在线节点查询 |
| `PartitionResource` | 分区/Bucket 状态查询 |
| `WorkbenchResource` | 工作台（快捷入口） |
| `SystemInfoResource` | 系统信息 |

**横切关注点：**
- `TenantInterceptor` + `TenantContext`：多租户拦截，从 Header 解析租户信息
- `AuditorAwareConfiguration`：JPA 审计（createBy/updateBy 自动填充）
- `WebMvcConfig`：注册拦截器
- Spring Security（`spring-security-core/web/config`）+ `silence-auth-center-client`：认证鉴权
- MapStruct（`assembler` 包中 20+ Mapper）：VO/DO/Command 对象转换

**Service 层（domain/service）：** 14 个 Service，直接操作 core 层 DAO，并调用 job-task/retry-task 的执行逻辑（如手动触发、停止任务）。

---

### 3.7 `silence-job-server-starter`（启动聚合层）

**职责：** Spring Boot 启动入口，聚合所有模块，管理应用生命周期，初始化 Netty/gRPC 服务器，接入 Nacos 服务发现。

**核心文件：**

| 文件 | 功能 |
|------|------|
| `SilenceJobCenterApplication` | `@SpringBootApplication` 主类，启动入口 |
| `StartListener` | `ApplicationStartedEvent` 监听：按序调用 `Lifecycle.start()`（ServerRegister → ServerNodeBalance → DispatchService 等） |
| `EndListener` | `ContextClosedEvent` 监听：优雅停机，反向调用 `Lifecycle.close()` |
| `DispatchService` | **调度心跳**：单线程定时器，每 N 秒获取当前节点分配的 Bucket 集合，发消息给 `ConsumerBucketActor` |
| `ConsumerBucketActor` | Pekko Actor，接收 Bucket 集合后分发给 ScanJobTaskActor + ScanRetryActor |
| `ConsumerBucket` | Bucket 集合消息体 |
| `JobServerNettyAutoConfiguration` | 注册 Netty/gRPC Handler（整合 job-task 和 retry-task 的 HTTP 请求处理器） |
| `OfflineNodeSchedule` | 定期清理长时间未心跳的下线节点 |
| `PlatformStartupFailureListener` | 启动失败事件监听，安全退出 |

**应用配置（application.yml）：**
- 端口：8080（HTTP Web）+ 17888（gRPC）
- Nacos 服务发现
- 数据库连接（MySQL）
- Silence Job Server 专属配置（bucketTotal=128、loadBalanceCycleTime 等）

---

## 四、核心业务机制梳理

### 4.1 分桶分布式调度机制

```
总 128 个 Bucket（可配置）
Job/RetryTask 按 hash(groupName + jobName/retryScene) % 128 分配到某个 Bucket

集群中每个 Server 节点通过 AllocateMessageQueueAveragely（类 RocketMQ 均分算法）
分配 128/N 个 Bucket（N = 在线 Server 节点数）

ServerNodeBalance 持续轮询 DB，检测节点变化 → 触发 rebalance

DispatchService 每隔 PERIOD 秒，将本节点负责的 Bucket 集合
发送给 ConsumerBucketActor → 触发 Scan（Job + Retry 两条链路）
```

### 4.2 常驻任务时间轮优化

```
触发间隔 < 10s 的 Job 定义为"常驻任务"
不走 DB 扫描，改用 JobTimerWheel（Netty HashedWheelTimer）
ResidentTaskCache 本地缓存任务定义
ResidentJobTimerTask 到时自动触发，避免高频 DB 扫描
```

### 4.3 双 RPC 模式

```
SystemProperties.rpcType 控制：
- grpc（默认）：GrpcServer（端口 17888）+ GrpcClientInvokeHandler
- netty：NettyHttpServer + NettyHttpConnectClient
两种模式均通过 Pekko Actor 异步处理请求（GrpcRequestHandlerActor / RequestHandlerActor）
```

### 4.4 重试生命周期

```
客户端任务失败 → 上报 Server（ReportRetryInfoHttpRequestHandler）
→ TaskGenerator 生成 RetryTask（状态：WAIT）
→ ScanRetryActor 扫描 → RetryExecutor 执行 → 回传结果
→ RetrySuccessHandler（成功删除）/ RetryFailureHandler（失败重新入队，+1次数）
→ 超出最大次数 → RetryDeadLetterConverter 转死信
→ 死信可从管理端手动回滚
```

### 4.5 工作流 DAG 编排

```
Workflow 由多个 WorkflowNode 组成有向无环图
GraphUtils 解析 DAG 拓扑，MutableGraphCache 缓存图结构
DecisionWorkflowExecutor 调用 Aviator/QLExpress 表达式引擎计算决策节点条件
节点类型：JobTask（触发定时任务）/ Decision（条件分支）/ Callback（回调确认）
```

---

## 五、潜在问题与优化点（仅分析，不修改）

| 问题类型 | 具体描述 | 涉及模块 |
|---------|---------|---------|
| 循环依赖风险 | job-task 依赖 retry-task，两模块高度耦合，未来扩展可能引发问题 | job-task / retry-task |
| 版本硬编码 | 3 处依赖未通过 dependencyManagement 管理 | pom.xml |
| testkit 污染生产 | pekko-actor-testkit 无 test scope | common/pom.xml |
| TODO 待优化 | DispatchService.start() 中 `// TODO待优化` 注释，ActorRef 获取方式待改进 | starter |
| 日志 appender 自定义 | `SilenceJobServerLogbackAppender` 自定义日志 Appender，需确认与 logback 兼容性 | common |
| DB 分布式锁 | 使用数据库实现分布式锁（`JdbcLockProvider`），高并发场景下性能有限 | common/lock |
| ServerNodeBalance close() 日志错误 | `close()` 方法中日志写的是 "start" 而非 "close"（第 126 行） | common |

---

## 六、后续分析步骤

1. ✅ 父 POM + 各模块 POM 依赖分析
2. ✅ 模块层次与依赖关系梳理
3. ✅ 各模块核心业务功能分析
4. ✅ 核心机制（分桶/时间轮/RPC/重试/工作流）梳理
5. ✅ 潜在问题汇总
6. 📌 （可按需）深入单模块某条具体执行链路的源码走读
7. 📌 （可按需）数据库表结构与领域模型对应关系分析
8. 📌 （可按需）告警通知链路详细分析