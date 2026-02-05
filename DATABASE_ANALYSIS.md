# 数据库表结构分析报告

生成时间: 2026-02-05

## 📊 总体统计

| 类型 | 数量 |
|------|------|
| 实体类 (Model) | 24 个 |
| DAO 接口 | 25 个 |
| Mapper XML | 12 个 |

---

## ✅ 完整映射列表（Model - DAO - Mapper）

### 有 Mapper XML 的表（12 个）

| 序号 | 表名 | 实体类 | DAO | Mapper XML | 状态 |
|------|------|--------|-----|-----------|------|
| 1 | sj_job | Job | JobDao | ✅ JobMapper.xml | ✅ 完整 |
| 2 | sj_job_log_message | JobLogMessage | JobLogMessageDao | ✅ JobLogMessageMapper.xml | ✅ 完整 |
| 3 | sj_job_summary | JobSummary | JobSummaryDao | ✅ JobSummaryMapper.xml | ✅ 完整 |
| 4 | sj_job_task | JobTask | JobTaskDao | ✅ JobTaskMapper.xml | ✅ 完整 |
| 5 | sj_job_task_batch | JobTaskBatch | JobTaskBatchDao | ✅ JobTaskBatchMapper.xml | ✅ 完整 |
| 6 | sj_retry | Retry | RetryDao | ✅ RetryMapper.xml | ✅ 完整 |
| 7 | sj_retry_dead_letter | RetryDeadLetter | RetryDeadLetterDao | ✅ RetryDeadLetterMapper.xml | ✅ 完整 |
| 8 | sj_retry_summary | RetrySummary | RetrySummaryDao | ✅ RetrySummaryMapper.xml | ✅ 完整 |
| 9 | sj_retry_task_log_message | RetryTaskLogMessage | RetryTaskLogMessageDao | ✅ RetryTaskLogMessageMapper.xml | ✅ 完整 |
| 10 | sj_server_node | ServerNode | ServerNodeDao | ✅ ServerNodeMapper.xml | ✅ 完整 |
| 11 | sj_workflow | Workflow | WorkflowDao | ✅ WorkflowMapper.xml | ✅ 完整 |
| 12 | sj_workflow_task_batch | WorkflowTaskBatch | WorkflowTaskBatchDao | ✅ WorkflowTaskBatchMapper.xml | ✅ 完整 |

### 仅使用 MyBatis-Plus 注解的表（13 个）

这些表使用 MyBatis-Plus 的 BaseMapper，不需要自定义 XML：

| 序号 | 表名 | 实体类 | DAO | 使用方式 |
|------|------|--------|-----|----------|
| 13 | sj_distributed_lock | DistributedLock | DistributedLockDao | MyBatis-Plus 自动映射 |
| 14 | sj_group_config | GroupConfig | GroupConfigDao | MyBatis-Plus 自动映射 |
| 15 | sj_job_executor | JobExecutor | JobExecutorDao | MyBatis-Plus 自动映射 |
| 16 | sj_namespace | Namespace | NamespaceDao | MyBatis-Plus 自动映射 |
| 17 | sj_notify_config | NotifyConfig | NotifyConfigDao | MyBatis-Plus 自动映射 |
| 18 | sj_notify_recipient | NotifyRecipient | NotifyRecipientDao | MyBatis-Plus 自动映射 |
| 19 | sj_retry_scene_config | RetrySceneConfig | RetrySceneConfigDao | MyBatis-Plus 自动映射 |
| 20 | sj_retry_scene_config | RetrySceneConfig | **SceneConfigDao** ⚠️ | MyBatis-Plus 自动映射 |
| 21 | sj_retry_task | RetryTask | RetryTaskDao | MyBatis-Plus 自动映射 |
| 22 | sj_sequence_alloc | SequenceAlloc | SequenceAllocDao | MyBatis-Plus 自动映射 |
| 23 | sj_system_user | SystemUser | SystemUserDao | MyBatis-Plus 自动映射 |
| 24 | sj_system_user_permission | SystemUserPermission | SystemUserPermissionDao | MyBatis-Plus 自动映射 |
| 25 | sj_workflow_node | WorkflowNode | WorkflowNodeDao | MyBatis-Plus 自动映射 |

---

## ⚠️ 发现的问题

### 🔴 问题 1: DAO 重复映射同一张表

**表名**: `sj_retry_scene_config`  
**实体类**: `RetrySceneConfig`  
**重复的 DAO**:
1. ✅ `RetrySceneConfigDao` - 正确命名，符合规范
2. ⚠️ `SceneConfigDao` - **命名不规范，应该删除**

**影响**:
- 代码混乱：两个 DAO 操作同一张表
- 命名不一致：违反项目命名规范（表名 `sj_retry_scene_config` 对应 DAO 应该是 `RetrySceneConfigDao`）
- 维护困难：容易导致使用混淆

**建议**:
```bash
# 删除不规范的 SceneConfigDao
rm ./silence-job-server-core/src/main/java/com/old/silence/job/server/infrastructure/persistence/dao/SceneConfigDao.java

# 检查所有使用 SceneConfigDao 的地方，替换为 RetrySceneConfigDao
find . -name "*.java" -type f -exec grep -l "SceneConfigDao" {} \;
```

---

## ✅ 没有发现的问题

### ✅ 无缺失的映射
- 所有 24 个实体类都有对应的 DAO
- 所有 DAO 都有对应的实体类
- 没有孤立的 Mapper XML 文件

### ✅ 命名规范一致
除了 `SceneConfigDao` 外，其他 24 个 DAO 的命名都符合规范：
- 表名: `sj_{entity_name}`
- 实体类: `{EntityName}`
- DAO: `{EntityName}Dao`

### ✅ 架构清晰
- 使用 MyBatis-Plus BaseMapper 提供基础 CRUD
- 复杂查询通过 XML Mapper 定制
- 分层合理：Model → DAO → Mapper XML

---

## 📝 建议的修复步骤

### Step 1: 确认 SceneConfigDao 使用情况 ✅

**已发现使用位置**:
- ✅ `AbstractGenerator.java` - 已使用 `RetrySceneConfigDao`（正确）
- ✅ `ManaSingleRetryGenerator.java` - 已使用 `RetrySceneConfigDao`（正确）
- ❌ `RetryExecutor.java` - **仍在使用 `SceneConfigDao`（需要修复）**

**具体问题**:
```java
// 文件: RetryExecutor.java
// Line 30: import com.old.silence.job.server.infrastructure.persistence.dao.SceneConfigDao;
// Line 53: private final SceneConfigDao sceneConfigDao;
// Line 58: SceneConfigDao sceneConfigDao 参数
// Line 62: this.sceneConfigDao = sceneConfigDao;
// Line 111: sceneConfigDao.selectOne(...)
```

### Step 2: 修复 RetryExecutor.java

需要修改 3 处：
1. 导入语句
2. 字段声明
3. 构造函数参数

### Step 3: 删除重复的 DAO 文件
```bash
rm ./silence-job-server-core/src/main/java/com/old/silence/job/server/infrastructure/persistence/dao/SceneConfigDao.java
```

### Step 4: 编译验证
```bash
mvn clean compile -DskipTests
```

---

## 📊 表分类统计

### 按业务模块分类

#### Job 相关（5 个表）
- sj_job
- sj_job_executor
- sj_job_log_message
- sj_job_summary
- sj_job_task
- sj_job_task_batch

#### Retry 相关（6 个表）
- sj_retry
- sj_retry_dead_letter
- sj_retry_scene_config
- sj_retry_summary
- sj_retry_task
- sj_retry_task_log_message

#### Workflow 相关（3 个表）
- sj_workflow
- sj_workflow_node
- sj_workflow_task_batch

#### 配置相关（4 个表）
- sj_group_config
- sj_namespace
- sj_notify_config
- sj_notify_recipient

#### 系统相关（6 个表）
- sj_distributed_lock
- sj_sequence_alloc
- sj_server_node
- sj_system_user
- sj_system_user_permission

---

## 🎯 总结

### 优点
✅ 表结构完整，覆盖所有业务场景  
✅ 命名规范统一（除了一个例外）  
✅ MyBatis-Plus + XML 混合使用，架构合理  
✅ 没有缺失的映射关系

### 待改进
⚠️ 存在 1 个重复 DAO：`SceneConfigDao` 与 `RetrySceneConfigDao` 重复  
⚠️ 需要统一删除不规范的 `SceneConfigDao`

### 优先级
🔴 **高优先级**: 修复 SceneConfigDao 重复问题（可能影响功能正确性）
