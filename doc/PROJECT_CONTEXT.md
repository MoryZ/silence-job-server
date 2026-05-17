# Silence Job Server - 项目上下文文档

## 项目概述
- **项目名称**：silence-job-server（分布式任务调度服务器）
- **Java 版本**：21
- **Parent POM**：platform-parent (2.0.1-SNAPSHOT)
- **公共依赖版本**：silence-job-common (1.8.0-SNAPSHOT)

## 项目结构
```
silence-job-server/
├── silence-job-server-app/              # 应用层（API、Controller）
├── silence-job-server-common/           # 本地公共工具类
├── silence-job-server-core/             # 核心业务逻辑、Mapper、Service
│   └── resources/mapper/                # MyBatis XML映射文件
├── silence-job-server-core-model/       # 数据模型
├── silence-job-server-core-repository/  # 数据访问层
├── silence-job-server-core-service/     # 业务服务层
├── silence-job-server-job-task/         # 工作任务处理
├── silence-job-server-retry-task/       # 重试任务处理
├── silence-job-server-rpc/              # RPC 通信
├── silence-job-server-scheduler/        # 调度器
├── silence-job-server-support/          # 支持组件
├── silence-job-server-task-common/      # 任务公共组件
└── silence-job-server-starter/          # Spring Boot Starter 入口
    └── resources/
        ├── application.yml              # 本地配置
        └── 其他配置文件
```

## 关键配置

### 数据库枚举处理
**问题**：枚举字段数据库转换失败  
**根本原因**：缺少 `platform-cloud-config` 依赖  
**解决方案**：
- 添加依赖：`platform-cloud-config` (2.0.1-SNAPSHOT)
- 该依赖通过 Spring Boot 自动配置加载 MyBatis Plus 的枚举处理器

**关键配置（来自 platform-cloud-config）**：
```yaml
mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true
    default-enum-type-handler: com.old.silence.data.commons.handler.GenericEnumTypeHandler
  type-handlers-package: com.old.silence.data.commons.handler
  global-config:
    db-config:
      id-type: auto
      logic-delete-field: is_deleted
      logic-not-delete-value: 0
      logic-delete-value: 1
```

**枚举处理规则**：
- 所有枚举必须实现 `EnumValue<?>` 接口（来自 silence-job-common）
- 数据库存储枚举的具体值（通过 EnumValue.getValue()）
- GenericEnumTypeHandler 自动完成 Java enum ↔ Database 转换

### 外部枚举源
- **来源**：`com.old.silence.job.common.enums`（silence-job-common 依赖）
- **包括**：ExecutorType, JobArgsType, JobState, RetryState, TaskState 等
- **这些枚举已实现 EnumValue 接口**

## 重要依赖关系
- `platform-parent`：父 POM，管理各种公共依赖
- `platform-cloud-config`：平台共享配置（必须）
- `platform-data-commons`：数据通用工具
- `platform-data-mybatis-plus`：MyBatis Plus 增强
- `silence-job-common-*`：框架通用模块

## MyBatis 配置
- **Mapper 位置**：`classpath:/mapper/**/*Mapper.xml`
- **数据源**：MySQL（配置在 application.yml）
- **Liquibase**：数据库版本管理（在 platform-cloud-config 中配置，本地开发关闭）

## 启动类
- 入口：`silence-job-server-starter` 模块
- Main Class：`com.old.silence.job.server.SilenceJobCenterApplication`
- 端口：8098（本地），38080（生产）

## 编码规范注意
1. **Mapper 文件命名**：遵循 `*Mapper.xml` 格式
2. **Mapper 接口命名**：遵循 `*Mapper` 格式，放在 `com.old.silence.job.server.*.mapper` 包下
3. **Service 类命名**：遵循 `*Service` 或 `*Impl` 格式
4. **枚举字段在数据库中存储值**，通过 TypeHandler 完成转换

## 常见问题排查
1. **枚举转换异常** → 检查 platform-cloud-config 依赖是否添加
2. **找不到 Mapper** → 检查 XML 位置是否正确，XML namespace 是否对应接口全路径
3. **驼峰映射失败** → 检查 MyBatis Plus 配置中 map-underscore-to-camel-case 是否启用
4. **逻辑删除不生效** → 检查字段是否为 is_deleted，Entity 类是否用 @TableLogic 注解

## 运行命令
```bash
# 本地开发构建
mvn clean package -DskipTests

# 启动（通过 starter 模块）
mvn spring-boot:run -pl silence-job-server-starter
```

---
*最后更新*：2026-02-05

## 重构进度 - 清除 AccessTemplate 毒瘤

### 目标
删除 AccessTemplate（老项目遗留的过度抽象）和相关的 AbstractTaskAccess、各个 *Access 实现，简化为：
- **server-core**：DAO（直接继承 MyBatis Plus BaseMapper）
- **server-app**：Service（注入 DAO，处理业务逻辑）+ Resource（REST API）

### 完成进度 ✅

**已完成改造**：
- ✅ 删除了 server-core/domain/service 下的所有 Access 类及 AccessTemplate
- ✅ 改造了 common 模块的 5 个文件：
  - AbstractAlarm.java - 注入 NotifyConfigDao
  - AbstractRetryAlarm.java - 注入 RetrySceneConfigDao
  - CacheToken.java - 注入 GroupConfigDao
  - ConfigVersionSyncHandler.java - 注入 GroupConfigDao
  - ConfigHttpRequestHandler.java - 注入 GroupConfigDao
- ✅ 改造了 retry-task 的 3 个 Schedule 类：
  - AbstractRetryTaskAlarmSchedule.java - 注入 3 个 DAO
  - RetryTaskMoreThresholdAlarmSchedule.java - 继承改造
  - RetryErrorMoreThresholdAlarmSchedule.java - 继承改造

**编译状态**：❌ 失败（retry-task 还有 9 个文件未改造）

### 还需要改造的文件（retry-task 模块，共 9 个）

按优先级排序：
1. **CleanerSchedule.java** - 关键任务，数据清理
2. **AbstractGenerator.java** - 抽象基类，影响 3 个子类
3. **ClientReportRetryGenerator.java**
4. **ManaSingleRetryGenerator.java**
5. **ManaBatchRetryGenerator.java**
6. **ScanRetryActor.java** - Actor 模型
7. **RetrySuccessHandler.java** - 成功处理
8. **RetryFailureHandler.java** - 失败处理
9. **CallbackRetryTaskHandler.java** - 回调处理

### 改造策略总结

**核心思路**：
1. 查找 Access 的使用方法
2. 注入对应的 DAO（如 RetryDao, RetrySceneConfigDao 等）
3. 用 MyBatis Plus 的 Lambda 查询 替代 Access 的方法调用
4. 保留其他业务逻辑不变

**具体步骤示例**：
```java
// 改造前
TaskAccess<Retry> retryTaskAccess = accessTemplate.getRetryAccess();
List<Retry> list = retryTaskAccess.list(query);

// 改造后
@Autowired
private RetryDao retryDao;
List<Retry> list = retryDao.selectList(query);
```

### 注意事项

1. **DAO 自定义方法** - 某些 DAO 有自定义方法（如 `getConfigInfo`），需要：
   - 检查是否存在这个方法
   - 如果不存在，用 Lambda 查询替代或通过 JSON 手动转换
   
2. **DTO 转换** - ConfigDTO 等 DTO 可能没有 setter，需要用 new 创建或返回空对象

3. **Job/Workflow** 等 app 模块的 Service 层已经完全改为直接注入 DAO，无需再改

### 下一步建议

由于改造工作量较大（需要逐文件分析），建议：
1. 优先完成 CleanerSchedule 和 AbstractGenerator 这两个关键类
2. 其他 Handler/Generator 可后续逐步处理
3. 完成后再进行编译和集成测试

