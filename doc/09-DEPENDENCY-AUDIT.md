# 依赖健康度审计报告

> 审计时间：2026-05-18
> 项目版本：3.0.0-SNAPSHOT
> Java 版本：21

---

## 一、SNAPSHOT 依赖清单（⚠️ 风险项）

### 1.1 内部集团包（全部为 SNAPSHOT）

| 依赖 | 当前版本 | 用途 | 风险级别 |
|------|---------|------|---------|
| `silence-job-common-*` | **1.8.0-SNAPSHOT** | 任务调度公共包（6个模块） | 🔴 P0 |
| `platform-data-commons` | **2.0.1-SNAPSHOT** | 平台数据公共包 | 🔴 P0 |
| `platform-data-mybatis-plus` | **2.0.1-SNAPSHOT** | MyBatis Plus 封装 | 🔴 P0 |
| `platform-cloud-config` | **2.0.1-SNAPSHOT** | 配置中心客户端 | 🔴 P0 |
| `platform-parent` | **2.0.1-SNAPSHOT** | 父 POM | 🔴 P0 |
| `silence-auth-center-client` | **2.0.1-SNAPSHOT** | 认证中心客户端 | 🔴 P0 |

**影响**：内部 SNAPSHOT 包随时可能变化，上线前必须锁定正式发布版本。

**建议**：
1. 联系 `silence-job-common` 团队，争取 `1.8.0` 正式发布
2. 或在 CI/CD 中固化集团内部 Maven 仓库的 SNAPSHOT 版本（通过 `mvn versions:resolve-ranges` 锁定）

### 1.2 项目自身版本

```
silence-job-server: 3.0.0-SNAPSHOT
```

**建议**：正式发版前将 `SNAPSHOT` 替换为正式版本号（如 `3.0.0.RELEASE`）。

---

## 二、过时 API / 过时依赖（🔴 P0 严重）

### 2.1 MapStruct 多 Artifact 问题（必须修复）

**问题位置**：`silence-job-server-common/pom.xml`

```xml
<!-- ❌ 已废弃，应该删除 -->
<dependency>
    <groupId>org.mapstruct</groupId>
    <artifactId>mapstruct-jdk8</artifactId>  <!-- MapStruct 1.3.0+ 已合并，无需此包 -->
</dependency>

<!-- ✅ 保留这两个 -->
<dependency>
    <groupId>org.mapstruct</groupId>
    <artifactId>mapstruct</artifactId>
</dependency>
<dependency>
    <groupId>org.mapstruct.extensions.spring</groupId>
    <artifactId>mapstruct-spring-annotations</artifactId>
</dependency>
<dependency>
    <groupId>org.mapstruct</groupId>
    <artifactId>mapstruct-processor</artifactId>
</dependency>
```

**原因**：`mapstruct-jdk8` 在 MapStruct **1.3.0** 发布时已合并入主 artifact，后续版本无需单独引入。保留会导致：
- 编译时产生 `duplicate class` 警告
- 潜在的类型绑定冲突

**修复方式**：从 `silence-job-server-common/pom.xml` 中删除 `mapstruct-jdk8` 依赖即可。

---

### 2.2 MyBatis Plus 注释提示版本过旧

**位置**：父 POM `platform-parent-2.0.1-SNAPSHOT.pom`

```xml
<!-- 父 POM 中注释：建议使用 3.5.6 或更高版本 -->
<artifactId>mybatis-plus-spring-boot3-starter</artifactId>
```

**建议**：确认当前使用的版本，如果低于 3.5.6，在升级集团内部包时一并升级。

---

## 三、版本过旧依赖（P1 建议处理）

### 3.1 Perf4j — 已停止维护

| 项目 | 版本 | 最新 | 状态 |
|------|------|------|------|
| `perf4j` | 0.9.16 | 无后续版本（~2012年停更） | ⚠️ 过时 |

**现状**：项目使用 Perf4j 记录性能日志。Perf4j 已多年无更新，但作为被动日志工具影响有限。

**建议**：如需现代可观测性方案，可考虑：
- Micrometer + Prometheus（更主流）
- 当前方案可维持，不强制升级

### 3.2 Spring Cloud Alibaba — 补丁版本可升级

| 项目 | 当前 | 最新可用 |
|------|------|---------|
| `spring-cloud-alibaba-dependencies` | 2023.0.3.2 | **2023.0.3.3** |

**影响**：版本差异极小（仅补丁修复），**非紧急**，但建议在常规迭代中同步升级。

---

## 四、版本落后依赖（P2 可延后）

> 以下依赖当前版本可用，但有新版本。升级需要测试验证，不紧急。

| 依赖 | 当前 | 最新 | 升级风险 |
|------|------|------|---------|
| **gRPC** (`grpc-netty-shaded`) | 1.58.0 | **1.68.0** | 🔴 高（API 变化，需全面回归） |
| **Protobuf** | 3.25.1 | **3.25.5** | 🟡 中（补丁版本，较小风险） |
| **Guava** | 32.1.2-jre | **33.4.0-jre** | 🟡 中（API 兼容，但涉及移除的 deprecated 方法） |
| **Aviator** | 5.3.3 | **5.5.0+** | 🔴 高（表达式引擎，语法/行为可能变化） |
| **QLExpress** | 3.3.1 | **3.3.2+** | 🟡 中（补丁版本） |
| **Apache Pekka** | 1.0.2 | **1.1.x** | 🔴 高（Actor API 变化） |

**⚠️ 特别提醒**：`Aviator` 和 `Pekka` 升级风险最高，因为：
- **Aviator**：表达式语法和内置函数在次版本间可能有变化，影响 Workflow 条件判断的求值结果
- **Pekka**：Actor 的 behavior/message 处理 API 在 1.1.x 有 breaking changes

**建议策略**：
1. `Protobuf 3.25.1 → 3.25.5`：低风险，补丁升级，可在常规迭代中处理
2. `gRPC 1.58.0 → 1.68.0`：高风险，需要：
   - 更新所有 `.proto` 文件对应的 generated 代码
   - 回归测试所有 RPC 接口
   - 建议作为专项重构处理
3. `Guava`：需要扫描代码中是否使用了已移除的 deprecated API（可用 `mvn dependency:analyze`）

---

## 五、汇总：优先级与行动计划

| 优先级 | 问题 | 修复方式 | 建议时间 |
|--------|------|---------|---------|
| 🔴 **P0** | `mapstruct-jdk8` 废弃依赖 | 删除 `silence-job-server-common/pom.xml` 中对应条目 | **立即** |
| 🔴 **P0** | 内部 SNAPSHOT 包不稳定 | 联系集团内包负责人，争取正式发布版；或 CI 锁定版本 | 上线前 |
| 🔴 **P0** | 项目自身版本 SNAPSHOT | 发版前替换为正式版本号 | 发版前 |
| 🟡 **P1** | Spring Cloud Alibaba 补丁 | 升级至 2023.0.3.3 | 下个迭代 |
| 🟡 **P1** | MyBatis Plus 版本确认 | 确认当前版本是否 ≥ 3.5.6 | 下个迭代 |
| 🟢 **P2** | Perf4j 停止维护 | 可选迁移至 Micrometer，不紧急 | 后续规划 |
| 🟢 **P2** | gRPC / Guava 版本落后 | 评估后计划升级路径，避开发版窗口 | 中期规划 |
| 🟢 **P2** | Aviator / Pekka 版本落后 | 高风险，暂缓，待业务稳定后专项处理 | 长期规划 |

---

## 六、快速验证命令

```bash
# 1. 检查 SNAPSHOT 依赖
mvn dependency:list | grep SNAPSHOT

# 2. 检查过期依赖（需要 maven versions plugin）
mvn versions:display-dependency-updates

# 3. 检查重复依赖
mvn dependency:analyze | grep "Used undeclared" -A 5

# 4. 检查 mapstruct-jdk8 是否被传递引用
mvn dependency:tree -Dincludes=org.mapstruct:* | grep jdk8
```
