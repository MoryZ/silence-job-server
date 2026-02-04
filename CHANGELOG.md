# Changelog

All notable changes to this project will be documented in this file.

## [3.0.0-SNAPSHOT] - 2024-02-04

### Features
- Spring Boot Starter for zero-configuration setup
- 任务调度引擎：支持 Cron、固定延迟、固定频率
- 分布式锁：基于 Redis/数据库
- 负载均衡：轮询、随机、一致性哈希、最少活跃
- 任务类型：单机、广播、分片、MapReduce、工作流
- 重试机制：多种退避策略
- 告警通知：钉钉、企业微信、邮件、Webhook
- 监控指标：任务执行统计、性能分析
- gRPC/HTTP 双协议支持

### Dependencies
- silence-job-common 1.0.0
- Spring Boot 2.7.x
- MyBatis-Plus 3.5.x
- gRPC Java 1.58.0
