# 硬编码凭证 - 具体修改步骤

## 📍 文件修改清单

### 1. application.yml (开发环境)
**路径**: `/Users/moryzang/IdeaProjects/silence-job-server/silence-job-server-starter/src/main/resources/application.yml`

**修改前**:
```yaml
spring:
  datasource:
    username: root
    password: silenceopr@2026
  cloud:
    nacos:
      discovery:
        username: nacos
        password: nacos
  mail:
    username: 13611988536@163.com
    password: PTsXDSWS8PqZarUA
```

**修改后** (方案 A - 环境变量):
```yaml
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
```

**格式说明**: `${ENV_VAR:defaultValue}`
- `ENV_VAR`: 环境变量名称
- `defaultValue`: 当环境变量不存在时的默认值

---

### 2. application-prd.yml (生产环境)
**路径**: `/Users/moryzang/IdeaProjects/silence-job-server/silence-job-server-starter/src/main/resources/application-prd.yml`

**修改前**:
```yaml
spring:
  datasource:
    username: silenceopr
    password: 520loveTmx@#
  cloud:
    nacos:
      discovery:
        username: nacos
        password: nacos
  mail:
    username: 13611988536@163.com
    password: PTsXDSWS8PqZarUA
```

**修改后** (生产也使用环保变量):
```yaml
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

---

## 🚀 运行环境配置

### 方案 1: 系统环境变量

#### macOS / Linux
```bash
# 临时设置 (仅当前终端会话)
export MYSQL_USERNAME=root
export MYSQL_PASSWORD=silenceopr@2026
export NACOS_USERNAME=nacos
export NACOS_PASSWORD=nacos
export MAIL_USERNAME=13611988536@163.com
export MAIL_PASSWORD=PTsXDSWS8PqZarUA

# 启动应用
java -jar app.jar

# 永久设置 (添加到 ~/.bash_profile 或 ~/.zshrc)
echo 'export MYSQL_USERNAME=root' >> ~/.zshrc
echo 'export MYSQL_PASSWORD=silenceopr@2026' >> ~/.zshrc
# ... 其他变量
source ~/.zshrc
```

#### Windows (PowerShell)
```powershell
$env:MYSQL_USERNAME = "root"
$env:MYSQL_PASSWORD = "silenceopr@2026"
$env:NACOS_USERNAME = "nacos"
$env:NACOS_PASSWORD = "nacos"
$env:MAIL_USERNAME = "13611988536@163.com"
$env:MAIL_PASSWORD = "PTsXDSWS8PqZarUA"

# 启动应用
java -jar app.jar
```

---

### 方案 2: Java 启动参数

```bash
java \
  -DMYSQL_USERNAME=root \
  -DMYSQL_PASSWORD=silenceopr@2026 \
  -DNACOS_USERNAME=nacos \
  -DNACOS_PASSWORD=nacos \
  -DMAIL_USERNAME=13611988536@163.com \
  -DMAIL_PASSWORD=PTsXDSWS8PqZarUA \
  -jar app.jar
```

**YAML 配置中使用**:
```yaml
spring:
  datasource:
    username: ${MYSQL_USERNAME}
    password: ${MYSQL_PASSWORD}
```

---

### 方案 3: Docker 容器

#### Dockerfile
```dockerfile
FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

COPY target/silence-job-server-starter-1.0.0.jar app.jar

# 环境变量 - 通过 docker run 或 docker-compose 覆盖
ENV MYSQL_USERNAME=root
ENV MYSQL_PASSWORD=silenceopr@2026
ENV NACOS_USERNAME=nacos
ENV NACOS_PASSWORD=nacos
ENV MAIL_USERNAME=13611988536@163.com
ENV MAIL_PASSWORD=PTsXDSWS8PqZarUA

EXPOSE 8098

ENTRYPOINT ["java", "-jar", "app.jar"]
```

#### docker-compose.yml
```yaml
version: '3.8'

services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: silenceopr@2026
      MYSQL_DATABASE: silence-platform
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql

  nacos:
    image: nacos/nacos-server:v2.2.0
    environment:
      MODE: standalone
      NACOS_AUTH_IDENTITY_KEY: nacos
      NACOS_AUTH_IDENTITY_VALUE: nacos
    ports:
      - "8848:8848"

  job-server:
    build: .
    ports:
      - "8098:8098"
    environment:
      # 数据库配置
      MYSQL_USERNAME: ${MYSQL_USERNAME:-root}
      MYSQL_PASSWORD: ${MYSQL_PASSWORD:-silenceopr@2026}
      
      # Nacos 配置
      NACOS_USERNAME: ${NACOS_USERNAME:-nacos}
      NACOS_PASSWORD: ${NACOS_PASSWORD:-nacos}
      
      # 邮件配置
      MAIL_USERNAME: ${MAIL_USERNAME:-13611988536@163.com}
      MAIL_PASSWORD: ${MAIL_PASSWORD:-PTsXDSWS8PqZarUA}
      
      # Spring profiles
      SPRING_PROFILES_ACTIVE: prd
      
    depends_on:
      - mysql
      - nacos

volumes:
  mysql_data:
```

**启动命令**:
```bash
# 开发环境
docker-compose up

# 生产环境 - 通过 .env 文件注入凭证
export MYSQL_PASSWORD=520loveTmx@#
export NACOS_PASSWORD=prod_nacos_pass
export MAIL_PASSWORD=prod_mail_pass
docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d
```

---

### 方案 4: .env 文件 (推荐用于开发)

#### .env 文件
```properties
# .env (添加到 .gitignore)
MYSQL_USERNAME=root
MYSQL_PASSWORD=silenceopr@2026
NACOS_USERNAME=nacos
NACOS_PASSWORD=nacos
MAIL_USERNAME=13611988536@163.com
MAIL_PASSWORD=PTsXDSWS8PqZarUA
```

#### .env.example (提交到仓库)
```properties
# .env.example (开发人员参考)
MYSQL_USERNAME=root
MYSQL_PASSWORD=your_mysql_password_here
NACOS_USERNAME=nacos
NACOS_PASSWORD=nacos
MAIL_USERNAME=your_email@163.com
MAIL_PASSWORD=your_email_password_here
```

#### Maven pom.xml 支持
```xml
<!-- 在 pom.xml 中添加 maven-dotenv-plugin -->
<plugin>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-maven-plugin</artifactId>
    <version>3.0.0</version>
    <configuration>
        <systemPropertyVariables>
            <!-- 从 .env 读取 -->
            <env.file>.env</env.file>
        </systemPropertyVariables>
    </configuration>
</plugin>
```

---

### 方案 5: IDE 运行配置 (IDEA)

#### IntelliJ IDEA 配置

1. **Run → Edit Configurations**

2. **添加 VM options** (在 `Run` 或 `Debug` 配置中):
```
-DMYSQL_USERNAME=root
-DMYSQL_PASSWORD=silenceopr@2026
-DNACOS_USERNAME=nacos
-DNACOS_PASSWORD=nacos
-DMAIL_USERNAME=13611988536@163.com
-DMAIL_PASSWORD=PTsXDSWS8PqZarUA
```

3. **设置环境变量**:
```
MYSQL_USERNAME=root;MYSQL_PASSWORD=silenceopr@2026;NACOS_USERNAME=nacos;NACOS_PASSWORD=nacos;MAIL_USERNAME=13611988536@163.com;MAIL_PASSWORD=PTsXDSWS8PqZarUA
```

4. **设置 Active profiles**:
```
dev
```

---

## 🔐 安全最佳实践

### ✅ DO
```bash
# ✅ 环境变量中存储敏感信息
export MYSQL_PASSWORD=actualPassword

# ✅ 使用 .env 文件 (需要在 .gitignore 中)
cat .env  # 仅在本地查看

# ✅ 在 CI/CD 中使用 Secrets
# GitHub Actions, GitLab CI, Jenkins 等

# ✅ 配置文件中使用占位符
password: ${MYSQL_PASSWORD:defaultPassword}

# ✅ 定期轮换凭证
# 每个季度更换一次密码
```

### ❌ DON'T
```yaml
# ❌ 不要在配置文件中硬编码
password: silenceopr@2026

# ❌ 不要将 .env 提交到 Git
git add .env  # ❌ 危险!

# ❌ 不要在日志中打印敏感信息
logger.info("Password is: {}", password);  // ❌ 危险!

# ❌ 不要在注释中写密码
// MySQL password: silenceopr@2026  ❌ 危险!

# ❌ 不要使用默认密码
password: 123456
```

---

## 📝 提交更改

### Git 操作
```bash
# 1. 备份当前配置 (可选)
cp silence-job-server-starter/src/main/resources/application.yml \
   silence-job-server-starter/src/main/resources/application.yml.backup

# 2. 修改 YAML 文件
vi silence-job-server-starter/src/main/resources/application.yml
vi silence-job-server-starter/src/main/resources/application-prd.yml

# 3. 确保 .env 在 .gitignore
echo ".env" >> .gitignore
echo ".env.local" >> .gitignore

# 4. 提交更改
git add .gitignore \
        silence-job-server-starter/src/main/resources/application.yml \
        silence-job-server-starter/src/main/resources/application-prd.yml

git commit -m "refactor: Extract hardcoded credentials to environment variables

- Replace hardcoded MySQL password with \${MYSQL_PASSWORD} env variable
- Replace hardcoded Nacos credentials with env variables
- Replace hardcoded mail credentials with env variables
- Add .env and .env.local to .gitignore
- Add REMEDIATION_PLAN.md with environment configuration guide

Security improvement: Credentials no longer hardcoded in source files"

# 5. 验证编译
mvn clean compile -DskipTests

# 6. 推送
git push origin feature-refactor-ddd
```

---

## ✅ 验证清单

- [ ] application.yml 使用环境变量
- [ ] application-prd.yml 使用环境变量
- [ ] .env 文件创建 (开发使用)
- [ ] .env.example 文件创建 (供参考)
- [ ] .gitignore 更新 (忽略 .env)
- [ ] Docker/docker-compose 配置完成
- [ ] IDE 运行配置更新
- [ ] 本地测试通过 (使用环境变量启动)
- [ ] 编译成功 (mvn clean compile)
- [ ] 提交并推送到版本控制

