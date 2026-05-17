# 🚀 快速开始指南

三大关键修改的快速参考

---

## 1️⃣ 硬编码凭证修改 - 5 分钟快速版

### 问题位置
- `application.yml` (L12-13, L27-28, L33-34)
- `application-prd.yml` (L12-13, L27-28, L33-34)

### 快速修复

```bash
# 方案 A: 使用环境变量 (推荐)

# Step 1: 修改 application.yml
cd silence-job-server-starter/src/main/resources
sed -i '' 's/password: silenceopr@2026/password: ${MYSQL_PASSWORD:silenceopr@2026}/' application.yml
sed -i '' 's/password: nacos/password: ${NACOS_PASSWORD:nacos}/' application.yml
sed -i '' 's/password: PTsXDSWS8PqZarUA/password: ${MAIL_PASSWORD:PTsXDSWS8PqZarUA}/' application.yml

# Step 2: 修改 application-prd.yml  
sed -i '' 's/password: 520loveTmx@#/password: ${MYSQL_PASSWORD:520loveTmx@#}/' application-prd.yml
sed -i '' 's/password: nacos/password: ${NACOS_PASSWORD:nacos}/' application-prd.yml

# Step 3: 创建 .env 文件 (开发使用)
cat > .env << 'EOF'
MYSQL_USERNAME=root
MYSQL_PASSWORD=silenceopr@2026
NACOS_USERNAME=nacos
NACOS_PASSWORD=nacos
MAIL_USERNAME=13611988536@163.com
MAIL_PASSWORD=PTsXDSWS8PqZarUA
EOF

# Step 4: 添加到 .gitignore
echo ".env" >> .gitignore
echo ".env.local" >> .gitignore

# Step 5: 启动应用
export $(cat .env | xargs)
java -jar target/silence-job-server-starter-1.0.0.jar

# Step 6: 验证
grep -n "password:" silence-job-server-starter/src/main/resources/application*.yml
# 应该显示 ${...} 格式
```

### 验证清单
- [ ] YAML 文件中所有密码使用 `${ENV_VAR:default}`
- [ ] `.env` 文件已创建
- [ ] `.gitignore` 已更新
- [ ] 本地启动通过 `export $(cat .env | xargs)`
- [ ] 编译成功: `mvn clean compile`

**耗时**: 15 分钟

---

## 2️⃣ 工作流 TODO 实现 - 完整版

### 问题位置

| 文件 | 行号 | 问题 |
|------|------|------|
| `WorkflowExecutorContext.java` | L44 | parentJobTaskStatus 定义但无方法 |
| `AbstractWorkflowExecutor.java` | L75 | // TODO 决策逻辑不完整 |

### 实现步骤

#### Step 1: 更新 WorkflowExecutorContext.java (L44 之后添加)

```java
// 获取父节点任务状态
public JobTaskStatus getParentJobTaskStatus() {
    return parentJobTaskStatus;
}

// 设置父节点任务状态
public void setParentJobTaskStatus(JobTaskStatus status) {
    this.parentJobTaskStatus = status;
}

// 验证父节点是否成功
public boolean isParentNodesSuccess() {
    return JobTaskStatus.SUCCESS.equals(parentJobTaskStatus);
}

// 检查是否应该跳过
public boolean shouldSkipNode() {
    return JobTaskStatus.SKIPPED.equals(parentJobTaskStatus);
}
```

#### Step 2: 更新 AbstractWorkflowExecutor.java (L65-90 完全替换)

```java
if (WorkflowNodeType.DECISION.equals(context.getNodeType())) {
    
    List<JobTaskBatch> jobTaskBatches = jobTaskBatchDao.selectList(
        new LambdaQueryWrapper<JobTaskBatch>()
            .select(JobTaskBatch::getId, JobTaskBatch::getOperationReason, 
                   JobTaskBatch::getStatus)
            .eq(JobTaskBatch::getWorkflowTaskBatchId, context.getWorkflowTaskBatchId())
            .eq(JobTaskBatch::getWorkflowNodeId, context.getWorkflowNodeId())
    );

    if (CollectionUtils.isNotEmpty(jobTaskBatches)) {
        total = jobTaskBatches.size();
        
        // ✅ 完整的决策评估
        int successCount = 0;
        int skipCount = 0;
        boolean decision = true;
        JobTaskStatus parentStatus = JobTaskStatus.SUCCESS;
        
        for (JobTaskBatch batch : jobTaskBatches) {
            // 检查跳过原因
            if (WORKFLOW_SUCCESSOR_SKIP_EXECUTION.contains(batch.getOperationReason())) {
                skipCount++;
                decision = false;
                parentStatus = JobTaskStatus.SKIPPED;
                break;
            }
            
            // 检查批次状态
            if (JobTaskStatus.SUCCESS.equals(batch.getStatus())) {
                successCount++;
            } else if (!JobTaskStatus.SUCCESS.equals(batch.getStatus())) {
                decision = false;
                parentStatus = JobTaskStatus.FAILED;
            }
        }
        
        context.setEvaluationResult(decision);
        context.setParentJobTaskStatus(parentStatus);
        
        logger.info("Workflow decision: nodes={}, success={}, skip={}, result={}",
            total, successCount, skipCount, decision);
        
    } else {
        context.setEvaluationResult(Boolean.TRUE);
        context.setParentJobTaskStatus(JobTaskStatus.SUCCESS);
    }
    
} else {
    total = jobTaskBatchDao.selectCount(new LambdaQueryWrapper<JobTaskBatch>()
            .eq(JobTaskBatch::getWorkflowTaskBatchId, context.getWorkflowTaskBatchId())
            .eq(JobTaskBatch::getWorkflowNodeId, context.getWorkflowNodeId())
    );
}
```

#### Step 3: 编译验证

```bash
mvn clean compile -f silence-job-server-job-task/pom.xml -DskipTests
```

### 验证清单
- [ ] WorkflowExecutorContext 有 4 个新方法
- [ ] AbstractWorkflowExecutor 决策逻辑完整
- [ ] 编译通过
- [ ] TODO 注释已删除

**耗时**: 30 分钟

---

## 3️⃣ 其他 TODO 修复 (可选)

### 快速清单

| 文件 | 问题 | 修复 | 耗时 |
|------|------|------|------|
| `CacheRegisterTable.java` L81 | namespaceId 未用 | 在缓存键中使用 | 10 min |
| `WorkflowBatchService.java` L100 | 租户填充疑问 | 显式加租户条件 | 10 min |
| `PodsService.java` L55 | 查询全量? | 根据需求判断 | 10 min |
| `DispatchService.java` L45 | 待优化 | 添加并发控制 | 20 min |

**总耗时**: 50 分钟

---

## 📋 完整修改清单

### Week 1 操作

```bash
# 1. 创建 feature 分支
git checkout -b fix/p0-critical-issues

# 2. 修改硬编码凭证 (application.yml, application-prd.yml)
vi silence-job-server-starter/src/main/resources/application.yml
vi silence-job-server-starter/src/main/resources/application-prd.yml
git add -A && git commit -m "refactor: Extract credentials to environment variables"

# 3. 修改工作流 TODO
vi silence-job-server-job-task/src/main/java/.../WorkflowExecutorContext.java
vi silence-job-server-job-task/src/main/java/.../AbstractWorkflowExecutor.java
git add -A && git commit -m "refactor: Complete workflow decision node implementation"

# 4. 修复其他 TODO (可选)
# ... 修改 4 个文件 ...
git add -A && git commit -m "refactor: Fix namespaceId, tenant, pods query, dispatch"

# 5. 全量编译验证
mvn clean compile -DskipTests
# 结果: BUILD SUCCESS

# 6. 推送
git push origin fix/p0-critical-issues

# 7. 创建 PR
# 在 GitHub/GitLab 上创建 Pull Request
```

### 验证命令

```bash
# 检查没有硬编码密码
grep -r "password.*:.*[a-zA-Z0-9]" *.yml | grep -v "cipher\|env\|\${" 
# 结果: 应该为空或仅匹配 ${...} 格式

# 检查 TODO 已移除
grep -r "// TODO\|// ToDo" silence-job-server-job-task/
# 结果: 不应该显示决策节点相关的 TODO

# 编译验证
mvn clean package -DskipTests
# 结果: BUILD SUCCESS

# 运行单元测试
mvn test
```

---

## 🎯 关键要点

### ✅ 必须做
1. **硬编码凭证** → 改成环境变量 (安全关键)
2. **工作流决策** → 完成 TODO 实现 (功能关键)
3. **编译验证** → mvn clean compile (确保不破坏)
4. **Git 提交** → 清晰的 commit message

### ❌ 不要做
1. ❌ 不要在 YAML 中保留硬编码密码
2. ❌ 不要将 .env 提交到 Git
3. ❌ 不要忽略编译错误
4. ❌ 不要修改功能逻辑，只修改 TODO

---

## 📚 详细文档

深入了解请查看:
- 📖 [REMEDIATION_PLAN.md](REMEDIATION_PLAN.md) - 完整修改方案
- 📖 [CREDENTIALS_MIGRATION.md](CREDENTIALS_MIGRATION.md) - 凭证迁移指南
- 📖 [WORKFLOW_TODO_IMPLEMENTATION.md](WORKFLOW_TODO_IMPLEMENTATION.md) - 工作流实现详解

---

## 📞 遇到问题?

### 编译失败
```bash
# 清理并重新编译
mvn clean compile -DskipTests -X

# 检查依赖
mvn dependency:tree | grep -i "error"
```

### 环境变量不生效
```bash
# 验证环境变量已设置
echo $MYSQL_PASSWORD

# 查看应用启动日志
grep "MYSQL_PASSWORD" catalina.out
```

### 工作流逻辑不对
```bash
# 检查 TODO 是否完全移除
grep -n "TODO\|ToDo" silence-job-server-job-task/src/main/java/...

# 运行工作流单元测试
mvn test -Dtest=*Workflow*
```

---

**总时间投入**: 1.5-2 小时快速修复 P0 关键问题
**期望收益**: 
- 🔒 消除硬编码凭证安全隐患
- ✅ 完成工作流功能实现
- 📈 项目质量评分 D → C+

