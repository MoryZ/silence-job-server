# SilenceJob Server

分布式任务调度系统的服务端实现，提供任务管理、调度、监控和重试服务。

## 📦 模块说明

### silence-job-server-starter
Spring Boot 自动配置模块，开箱即用的部署方案：
- 应用自动启动配置
- 数据库初始化
- RPC 服务暴露

### silence-job-server-app
应用启动模块，包含 Spring Boot 主类和配置：
- 应用入口点
- 配置文件管理
- 依赖集成

### silence-job-server-common
服务端公共模块：
- 通用常量和枚举
- 工具类
- 基础组件

### silence-job-server-core
核心业务模块：
- 任务调度引擎
- 分布式锁
- 负载均衡算法
- 任务分配策略

### silence-job-server-core-model
数据模型模块：
- 实体类定义
- DTO 对象
- VO 对象

### silence-job-server-core-repository
数据访问层：
- MyBatis Mapper
- 数据库操作
- 缓存管理

### silence-job-server-core-service
服务层：
- 业务逻辑实现
- 任务管理服务
- 调度服务
- 监控服务

### silence-job-server-job-task
任务执行模块：
- 任务触发器
- 执行器管理
- 结果处理

### silence-job-server-retry-task
重试任务模块：
- 重试调度
- 退避策略
- 失败处理

### silence-job-server-rpc
RPC 通信模块：
- gRPC 服务实现
- HTTP 接口
- 客户端通信

### silence-job-server-scheduler
调度器模块：
- Cron 表达式解析
- 时间轮算法
- 定时任务管理

### silence-job-server-support
支持模块：
- 告警通知
- 日志收集
- 监控指标

### silence-job-server-task-common
任务公共模块：
- 任务基础接口
- 任务上下文
- 任务工具类

## 🚀 快速开始

### Maven 依赖

```xml
<dependency>
    <groupId>com.old.silence</groupId>
    <artifactId>silence-job-server-starter</artifactId>
    <version>3.0.0-SNAPSHOT</version>
</dependency>
```

### 配置示例

```yaml
silence:
  job:
    server:
      port: 8080
      storage:
        type: mysql
        url: jdbc:mysql://localhost:3306/silence_job
```

## 📖 依赖关系

本项目依赖：
- `silence-job-common` v1.0.0 - 公共组件库

## 📄 许可证

Apache License 2.0
