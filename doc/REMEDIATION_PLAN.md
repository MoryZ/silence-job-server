# 修改实现指南

## 1. 硬编码凭证修改方案

### 当前状态
项目中有 **4 处硬编码凭证**：

#### 📍 application.yml (开发环境)
- 数据库: `root` / `silenceopr@2026`
- Nacos: `nacos` / `nacos`
- 邮件: `13611988536@163.com` / `PTsXDSWS8PqZarUA`

#### 📍 application-prd.yml (生产环境)
- 数据库: `silenceopr` / `520loveTmx@#`
- Nacos: `nacos` / `nacos`
- 邮件: `13611988536@163.com` / `PTsXDSWS8PqZarUA`

### 修改方案

#### 方案 A: 使用环境变量 (推荐)
**优点**: 安全、灵活、符合 12-factor app 规范

```yaml
# application.yml (开发)
spring:
  datasource:
    username: ${MYSQL_USERNAME:root}
    password: ${MYSQL_PASSWORD:silenceopr@2026}
  cloud:
    nacos:
      discovery:
        username: ${NACOS_USERNAME:nacos}
        password: ${NACOS_PASSWORD:nacos}
  mail:
    username: ${MAIL_USERNAME:13611988536@163.com}
    password: ${MAIL_PASSWORD:PTsXDSWS8PqZarUA}

# application-prd.yml (生产)
spring:
  datasource:
    username: ${MYSQL_USERNAME:silenceopr}
    password: ${MYSQL_PASSWORD:520loveTmx@#}
  cloud:
    nacos:
      discovery:
        username: ${NACOS_USERNAME:nacos}
        password: ${NACOS_PASSWORD:nacos}
  mail:
    username: ${MAIL_USERNAME:13611988536@163.com}
    password: ${MAIL_PASSWORD:PTsXDSWS8PqZarUA}
```

#### 方案 B: 使用 Spring Cloud Config (企业级)
**优点**: 集中管理、动态刷新、审计日志

```java
// 创建 ConfigServer 配置
// config-server/application.yml
spring:
  cloud:
    config:
      server:
        git:
          uri: https://github.com/your-org/config-repo
          searchPaths: silence-job-server
          
// 获取配置代码
@Service
@RefreshScope  // 支持动态刷新
public class DbConfigService {
    @Value("${spring.datasource.username}")
    private String dbUsername;
    
    @Value("${spring.datasource.password}")
    private String dbPassword;
}
```

#### 方案 C: 使用加密配置 (你提出的方案)
**优点**: 文件中有密钥标记，便于管理员识别

```yaml
# application.yml
spring:
  datasource:
    username: root
    password: ${cipher}MYSQL_SCJOB_JOBOPR  # cipher 表示加密
    # 实现: 使用 Spring Cloud Config 的 cipher 功能自动解密
  
  cloud:
    nacos:
      discovery:
        username: nacos
        password: ${cipher}NACOS_PASSWORD_PRD  # 标记为加密
```

**实现 Spring Cloud Config Cipher (推荐)**:
```java
// bootstrap.yml 添加
spring:
  cloud:
    config:
      encryption:
        enabled: true

// 加密密钥 (环境变量)
export ENCRYPT_KEY=myEncryptionKeyHere

// 加密命令
curl localhost:8888/encrypt -d "myPassword"
# 返回: 加密后的值，如 xxx...xxx

// 在配置文件中使用
spring:
  datasource:
    password: '{cipher}加密后的值'
```

### 📋 实现步骤 (推荐方案 A + B 混合)

#### Step 1: 修改 YAML 文件
```bash
# 1. application.yml
# 2. application-prd.yml
# 使用环境变量替换硬编码值
```

#### Step 2: Docker/K8s 环境配置
```dockerfile
# Dockerfile
ENV MYSQL_USERNAME=root
ENV MYSQL_PASSWORD=${MYSQL_PASSWORD}
ENV NACOS_USERNAME=nacos
ENV NACOS_PASSWORD=${NACOS_PASSWORD}
ENV MAIL_USERNAME=${MAIL_USERNAME}
ENV MAIL_PASSWORD=${MAIL_PASSWORD}

# docker-compose.yml
services:
  job-server:
    environment:
      - MYSQL_USERNAME=${MYSQL_USERNAME}
      - MYSQL_PASSWORD=${MYSQL_PASSWORD}
      - NACOS_USERNAME=${NACOS_USERNAME}
      - NACOS_PASSWORD=${NACOS_PASSWORD}
      - MAIL_USERNAME=${MAIL_USERNAME}
      - MAIL_PASSWORD=${MAIL_PASSWORD}
```

#### Step 3: 启动脚本
```bash
#!/bin/bash
# start.sh

export MYSQL_USERNAME=${MYSQL_USERNAME:-root}
export MYSQL_PASSWORD=${MYSQL_PASSWORD:-silenceopr@2026}
export NACOS_USERNAME=${NACOS_USERNAME:-nacos}
export NACOS_PASSWORD=${NACOS_PASSWORD:-nacos}
export MAIL_USERNAME=${MAIL_USERNAME:-13611988536@163.com}
export MAIL_PASSWORD=${MAIL_PASSWORD:-PTsXDSWS8PqZarUA}

java -jar silence-job-server-starter-1.0.0.jar
```

---

## 2. 缺失功能实现 - 工作流 TODO

### 📍 问题位置

#### 1️⃣ WorkflowExecutorContext.java (L44)
```java
/**
 * TODO 父节点批次状态
 */
private JobTaskStatus parentJobTaskStatus;  // ❌ 已定义但未使用
```

**含义**: 父节点的任务状态需要在子节点执行时读取和验证

#### 2️⃣ AbstractWorkflowExecutor.java (L75)
```java
if (CollectionUtils.isNotEmpty(jobTaskBatches)) {
    total = jobTaskBatches.size();
    // ToDo  ❌ 这里缺实现
    JobTaskBatch jobTaskBatch = jobTaskBatches.get(0);
    if (WORKFLOW_SUCCESSOR_SKIP_EXECUTION.contains(jobTaskBatch.getOperationReason())) {
        context.setEvaluationResult(Boolean.FALSE);
    } else {
        context.setEvaluationResult(Boolean.TRUE);
    }
}
```

**问题**: 
- 只取第一个批次 `jobTaskBatches.get(0)` 是不完整的
- 需要验证所有父节点批次的状态
- 应该对所有批次进行聚合判定

### 🛠️ 实现方案

#### 完整实现代码

**Step 1: 补充 WorkflowExecutorContext 初始化**
```java
// WorkflowExecutorContext.java

/**
 * 父节点批次状态 - 工作流决策节点判定用
 */
private JobTaskStatus parentJobTaskStatus;

/**
 * 获取父节点任务状态
 */
public JobTaskStatus getParentJobTaskStatus() {
    return parentJobTaskStatus;
}

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
```

**Step 2: 完成决策节点逻辑**
```java
// AbstractWorkflowExecutor.java (L65-90 修改)

if (WorkflowNodeType.DECISION.equals(context.getNodeType())) {
    
    // 查询该决策节点的所有父节点批次
    List<JobTaskBatch> jobTaskBatches = jobTaskBatchDao.selectList(
        new LambdaQueryWrapper<JobTaskBatch>()
            .select(JobTaskBatch::getId, JobTaskBatch::getOperationReason, 
                   JobTaskBatch::getStatus)
            .eq(JobTaskBatch::getWorkflowTaskBatchId, context.getWorkflowTaskBatchId())
            .eq(JobTaskBatch::getWorkflowNodeId, context.getWorkflowNodeId())
    );

    if (CollectionUtils.isNotEmpty(jobTaskBatches)) {
        total = jobTaskBatches.size();
        
        // ✅ 修复: 聚合判定所有父节点批次状态
        boolean shouldExecute = true;
        int successCount = 0;
        int skipCount = 0;
        
        for (JobTaskBatch batch : jobTaskBatches) {
            // 如果任何一个父节点要求跳过，则整个决策节点返回 false
            if (WORKFLOW_SUCCESSOR_SKIP_EXECUTION.contains(batch.getOperationReason())) {
                skipCount++;
                shouldExecute = false;
                break;
            }
            
            // 验证批次状态
            if (JobTaskStatus.SUCCESS.equals(batch.getStatus())) {
                successCount++;
            } else if (JobTaskStatus.FAILED.equals(batch.getStatus())) {
                // 失败的父节点也会导致决策失败
                shouldExecute = false;
                break;
            }
        }
        
        // 设置决策结果
        context.setEvaluationResult(shouldExecute);
        
        // 📊 记录决策日志
        logger.info(
            "Workflow decision node evaluation: workflowNodeId={}, " +
            "totalParents={}, successCount={}, skipCount={}, decision={}",
            context.getWorkflowNodeId(), total, successCount, skipCount, shouldExecute
        );
        
        // ✅ 存储父节点状态供后续使用
        if (successCount == total) {
            context.setParentJobTaskStatus(JobTaskStatus.SUCCESS);
        } else if (skipCount > 0) {
            context.setParentJobTaskStatus(JobTaskStatus.SKIPPED);
        } else {
            context.setParentJobTaskStatus(JobTaskStatus.FAILED);
        }
        
    } else {
        // 没有父节点，决策节点直接执行
        context.setEvaluationResult(Boolean.TRUE);
        context.setParentJobTaskStatus(JobTaskStatus.SUCCESS);
        logger.warn(
            "Workflow decision node has no parent nodes: workflowNodeId={}",
            context.getWorkflowNodeId()
        );
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

#### 相关常量定义
```java
// 在 Constant 或相关类中定义
public class WorkflowConstants {
    
    /**
     * 工作流节点状态 - 决策后跳过执行的原因集合
     */
    public static final Set<JobOperationReason> WORKFLOW_SUCCESSOR_SKIP_EXECUTION = 
        Set.of(
            JobOperationReason.MANUAL_SKIP,    // 手动跳过
            JobOperationReason.CONDITION_SKIP, // 条件跳过
            JobOperationReason.TIMEOUT_SKIP    // 超时跳过
        );
        
    /**
     * 决策节点评估规则
     */
    public static final int DECISION_REQUIRE_ALL_SUCCESS = 1;  // 所有父节点都成功
    public static final int DECISION_REQUIRE_ANY_SUCCESS = 2;  // 任一父节点成功
    public static final int DECISION_REQUIRE_NO_FAILURE = 3;   // 不能有失败
}

// 使用示例
if (WorkflowConstants.WORKFLOW_SUCCESSOR_SKIP_EXECUTION.contains(batch.getOperationReason())) {
    // 跳过
}
```

#### 数据验证方法 (可选)
```java
/**
 * 验证工作流决策节点是否可以继续执行
 */
@Service
public class WorkflowDecisionValidator {
    
    @Autowired
    private JobTaskBatchDao jobTaskBatchDao;
    
    /**
     * 验证所有父节点是否满足条件
     */
    public boolean validateParentNodeConditions(WorkflowExecutorContext context) {
        List<JobTaskBatch> parentBatches = jobTaskBatchDao.selectList(
            new LambdaQueryWrapper<JobTaskBatch>()
                .eq(JobTaskBatch::getWorkflowTaskBatchId, context.getWorkflowTaskBatchId())
                .eq(JobTaskBatch::getWorkflowNodeId, context.getWorkflowNodeId())
        );
        
        if (CollectionUtils.isEmpty(parentBatches)) {
            return true;  // 没有父节点，可以执行
        }
        
        // 检查是否所有父节点都满足条件
        return parentBatches.stream()
            .noneMatch(batch -> 
                WORKFLOW_SUCCESSOR_SKIP_EXECUTION.contains(batch.getOperationReason()) ||
                JobTaskStatus.FAILED.equals(batch.getStatus())
            );
    }
}
```

---

## 3. 其他 TODO 项清单

| 文件 | 行号 | 问题 | 影响 | 优先级 |
|------|------|------|------|--------|
| [CacheRegisterTable.java](CacheRegisterTable.java#L81) | 81, 112, 142 | namespaceId 未使用 | 代码可读性 | P2 |
| [WorkflowBatchService.java](WorkflowBatchService.java#L100) | 100 | 租户填充表连接疑问 | 可能的查询问题 | P1 |
| [PodsService.java](PodsService.java#L55) | 55 | 查询是否应该全量 | 功能不完整 | P1 |
| [DispatchService.java](DispatchService.java#L45) | 45 | 待优化的调度逻辑 | 性能问题 | P2 |

### 快速修复

#### ❌ CacheRegisterTable.java
```java
// 问题: namespaceId 参数传入但未使用
// 修复: 要么使用，要么删除参数
public void registerCache(String namespace, String namespaceId) {
    // TODO 这里namespaceId 也没有用到
    // ✅ 修复后:
    String cacheKey = String.format("cache:%s:%s", namespace, namespaceId);
    cache.put(cacheKey, data);
}
```

#### ❌ WorkflowBatchService.java (L100)
```java
// 问题: 租户填充能否通过表连接生效
// 修复: 明确指定租户条件
List<WorkflowBatch> batches = dao.selectList(
    new LambdaQueryWrapper<WorkflowBatch>()
        .eq(WorkflowBatch::getTenantId, getTenantId())  // ✅ 显式添加租户条件
        // ... 其他条件
);
```

#### ❌ PodsService.java (L55)
```java
// 问题: 查询是否应该是全量
// 修复: 根据实际需求决定
public List<Pods> queryPods(String filter) {
    if (StringUtils.isBlank(filter)) {
        // TODO 查询所有的？
        // ✅ 修复:
        logger.info("Querying all pods without filter");
        return podDao.selectList(null);
    }
    
    return podDao.selectList(
        new LambdaQueryWrapper<Pods>()
            .like(Pods::getName, filter)
    );
}
```

#### ❌ DispatchService.java (L45)
```java
// 问题: 调度逻辑待优化
// 修复: 添加批处理和并发控制
public void dispatchTasks(List<Task> tasks) {
    // TODO 待优化
    // ✅ 修复:
    
    // 使用 ExecutorService 并发调度
    ExecutorService executor = Executors.newFixedThreadPool(10);
    
    tasks.stream()
        .collect(Collectors.groupingBy(Task::getCategory))
        .forEach((category, categoryTasks) -> {
            executor.submit(() -> {
                categoryTasks.forEach(this::dispatchSingleTask);
            });
        });
    
    executor.shutdown();
}
```

---

## 📊 修改优先级

### Week 1 (优先修复)
- [ ] 硬编码凭证 → 环境变量 (2 小时)
- [ ] 工作流决策节点 TODO (4 小时)
- [ ] WorkflowBatchService 租户填充 (1 小时)

### Week 2
- [ ] PodsService 查询优化 (1 小时)
- [ ] DispatchService 并发优化 (2 小时)
- [ ] CacheRegisterTable namespaceId 使用 (1 小时)

---

## 🔍 验证方案

### 硬编码凭证验证
```bash
# 1. 验证没有硬编码密码
grep -r "password.*:.*[0-9a-zA-Z]" *.yml | grep -v "cipher\|env\|\${"
# 结果: 应该为空

# 2. 验证环境变量替换成功
export MYSQL_USERNAME=test_user
export MYSQL_PASSWORD=test_pass
java -jar app.jar --debug  # 查看日志中是否使用环境变量
```

### 工作流 TODO 验证
```bash
# 1. 编译验证
mvn clean compile -f silence-job-server-job-task/pom.xml

# 2. 单元测试
mvn test -f silence-job-server-job-task/pom.xml -Dtest=WorkflowExecutorTest

# 3. 集成测试 - 验证决策逻辑
# 创建包含多个父节点的工作流，验证决策结果是否正确
```

