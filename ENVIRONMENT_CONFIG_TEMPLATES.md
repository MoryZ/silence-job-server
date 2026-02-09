# 环境配置模板

## 📋 系统环境配置

### 开发环境 (.env 文件)

**文件**: `.env` (添加到 .gitignore)

```properties
# ========================================
# 数据库配置 (MySQL)
# ========================================
MYSQL_USERNAME=root
MYSQL_PASSWORD=silenceopr@2026
MYSQL_HOST=localhost
MYSQL_PORT=3306
MYSQL_DATABASE=silence-platform

# ========================================
# Nacos 配置
# ========================================
NACOS_SERVER_ADDR=localhost:8848
NACOS_USERNAME=nacos
NACOS_PASSWORD=nacos
NACOS_NAMESPACE=
NACOS_GROUP=silence-job

# ========================================
# 邮件配置
# ========================================
MAIL_HOST=smtp.163.com
MAIL_PORT=465
MAIL_USERNAME=13611988536@163.com
MAIL_PASSWORD=PTsXDSWS8PqZarUA
MAIL_FROM=13611988536@163.com

# ========================================
# Spring 配置
# ========================================
SPRING_PROFILES_ACTIVE=dev
SPRING_APPLICATION_NAME=job-service
SERVER_PORT=8098
SERVER_SERVLET_CONTEXT_PATH=/

# ========================================
# 应用配置
# ========================================
SILENCE_JOB_RETRY_PULL_PAGE_SIZE=1000
SILENCE_JOB_JOB_PULL_PAGE_SIZE=1000
SILENCE_JOB_SERVER_PORT=17888
SILENCE_JOB_LOG_STORAGE=7
SILENCE_JOB_RPC_TYPE=grpc
```

### 生产环境 (.env.prod 文件)

**文件**: `.env.prod` (仅在 CI/CD 中引用，不提交到仓库)

```properties
# ========================================
# 数据库配置 (MySQL) - 生产
# ========================================
MYSQL_USERNAME=silenceopr
MYSQL_PASSWORD=520loveTmx@#
MYSQL_HOST=115.190.196.117
MYSQL_PORT=3306
MYSQL_DATABASE=silence-platform

# ========================================
# Nacos 配置 - 生产
# ========================================
NACOS_SERVER_ADDR=nacos.prod.example.com:8848
NACOS_USERNAME=nacos_prod_user
NACOS_PASSWORD=nacos_prod_password
NACOS_NAMESPACE=prod-namespace
NACOS_GROUP=silence-job-prod

# ========================================
# 邮件配置 - 生产
# ========================================
MAIL_HOST=smtp.prod.example.com
MAIL_PORT=465
MAIL_USERNAME=noreply@example.com
MAIL_PASSWORD=prod_email_password
MAIL_FROM=noreply@example.com

# ========================================
# Spring 配置 - 生产
# ========================================
SPRING_PROFILES_ACTIVE=prd
SPRING_APPLICATION_NAME=job-service
SERVER_PORT=8098
SERVER_SERVLET_CONTEXT_PATH=/

# ========================================
# 应用配置 - 生产
# ========================================
SILENCE_JOB_RETRY_PULL_PAGE_SIZE=5000
SILENCE_JOB_JOB_PULL_PAGE_SIZE=5000
SILENCE_JOB_SERVER_PORT=17888
SILENCE_JOB_LOG_STORAGE=30
SILENCE_JOB_RPC_TYPE=grpc
```

### 示例环境变量参考 (.env.example)

**文件**: `.env.example` (提交到仓库，供开发人员参考)

```properties
# 复制此文件到 .env 并填入实际值
# cp .env.example .env

# ========================================
# 数据库配置
# ========================================
MYSQL_USERNAME=your_db_user
MYSQL_PASSWORD=your_db_password
MYSQL_HOST=your_db_host
MYSQL_PORT=3306
MYSQL_DATABASE=silence-platform

# ========================================
# Nacos 配置
# ========================================
NACOS_SERVER_ADDR=your_nacos_server:8848
NACOS_USERNAME=your_nacos_user
NACOS_PASSWORD=your_nacos_password
NACOS_NAMESPACE=your_namespace_id
NACOS_GROUP=silence-job

# ========================================
# 邮件配置
# ========================================
MAIL_HOST=your_smtp_host
MAIL_PORT=465
MAIL_USERNAME=your_email@example.com
MAIL_PASSWORD=your_email_password
MAIL_FROM=your_email@example.com

# ========================================
# Spring 配置
# ========================================
SPRING_PROFILES_ACTIVE=dev
SPRING_APPLICATION_NAME=job-service
SERVER_PORT=8098
SERVER_SERVLET_CONTEXT_PATH=/
```

---

## 🐳 Docker 构建脚本

### Dockerfile

```dockerfile
# 使用 JDK 17 作为基础镜像
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /build

# 复制项目文件
COPY . .

# 构建应用
RUN ./mvnw clean package -DskipTests

# 运行阶段
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# 从 builder 阶段复制构建后的 JAR
COPY --from=builder /build/silence-job-server-starter/target/silence-job-server-starter-*.jar app.jar

# 创建非 root 用户
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# 暴露端口
EXPOSE 8098 17888

# 设置用户
USER appuser

# 环境变量默认值
ENV MYSQL_USERNAME=root \
    MYSQL_PASSWORD=silenceopr@2026 \
    NACOS_USERNAME=nacos \
    NACOS_PASSWORD=nacos \
    MAIL_USERNAME=13611988536@163.com \
    MAIL_PASSWORD=PTsXDSWS8PqZarUA \
    SPRING_PROFILES_ACTIVE=dev \
    SERVER_PORT=8098

# 启动脚本
ENTRYPOINT ["java", "-jar", "app.jar"]

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8098/actuator/health || exit 1
```

### docker-compose.yml

```yaml
version: '3.8'

services:
  mysql:
    image: mysql:8.0.32
    container_name: silence-mysql
    environment:
      MYSQL_ROOT_PASSWORD: ${MYSQL_PASSWORD:-silenceopr@2026}
      MYSQL_DATABASE: ${MYSQL_DATABASE:-silence-platform}
      TZ: Asia/Shanghai
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
      - ./init.sql:/docker-entrypoint-initdb.d/init.sql
    networks:
      - silence-network
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 10s
      timeout: 5s
      retries: 5

  nacos:
    image: nacos/nacos-server:v2.2.0
    container_name: silence-nacos
    environment:
      MODE: standalone
      PREFER_HOST_MODE: hostname
      NACOS_AUTH_IDENTITY_KEY: ${NACOS_USERNAME:-nacos}
      NACOS_AUTH_IDENTITY_VALUE: ${NACOS_PASSWORD:-nacos}
      SPRING_DATASOURCE_PLATFORM: mysql
      MYSQL_SERVICE_HOST: mysql
      MYSQL_SERVICE_PORT: 3306
      MYSQL_SERVICE_DB_NAME: nacos
      MYSQL_SERVICE_USER: root
      MYSQL_SERVICE_PASSWORD: ${MYSQL_PASSWORD:-silenceopr@2026}
    ports:
      - "8848:8848"
      - "9848:9848"
    depends_on:
      mysql:
        condition: service_healthy
    networks:
      - silence-network
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8848/nacos/"]
      interval: 10s
      timeout: 5s
      retries: 5

  job-server:
    build:
      context: .
      dockerfile: Dockerfile
    container_name: silence-job-server
    ports:
      - "8098:8098"
      - "17888:17888"
    environment:
      MYSQL_USERNAME: ${MYSQL_USERNAME:-root}
      MYSQL_PASSWORD: ${MYSQL_PASSWORD:-silenceopr@2026}
      MYSQL_HOST: mysql
      MYSQL_PORT: 3306
      MYSQL_DATABASE: ${MYSQL_DATABASE:-silence-platform}
      
      NACOS_SERVER_ADDR: nacos:8848
      NACOS_USERNAME: ${NACOS_USERNAME:-nacos}
      NACOS_PASSWORD: ${NACOS_PASSWORD:-nacos}
      NACOS_NAMESPACE: ${NACOS_NAMESPACE:-}
      
      MAIL_HOST: ${MAIL_HOST:-smtp.163.com}
      MAIL_PORT: ${MAIL_PORT:-465}
      MAIL_USERNAME: ${MAIL_USERNAME:-13611988536@163.com}
      MAIL_PASSWORD: ${MAIL_PASSWORD:-PTsXDSWS8PqZarUA}
      
      SPRING_PROFILES_ACTIVE: ${SPRING_PROFILES_ACTIVE:-dev}
      SERVER_PORT: ${SERVER_PORT:-8098}
      
    depends_on:
      mysql:
        condition: service_healthy
      nacos:
        condition: service_healthy
    networks:
      - silence-network
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8098/"]
      interval: 30s
      timeout: 10s
      retries: 5
      start_period: 40s

volumes:
  mysql_data:
    driver: local

networks:
  silence-network:
    driver: bridge
```

### docker-compose.prod.yml (生产覆盖配置)

```yaml
version: '3.8'

services:
  job-server:
    environment:
      MYSQL_USERNAME: ${MYSQL_USERNAME}
      MYSQL_PASSWORD: ${MYSQL_PASSWORD}
      MYSQL_HOST: ${MYSQL_HOST}
      MYSQL_PORT: ${MYSQL_PORT}
      MYSQL_DATABASE: ${MYSQL_DATABASE}
      
      NACOS_SERVER_ADDR: ${NACOS_SERVER_ADDR}
      NACOS_USERNAME: ${NACOS_USERNAME}
      NACOS_PASSWORD: ${NACOS_PASSWORD}
      
      MAIL_HOST: ${MAIL_HOST}
      MAIL_PORT: ${MAIL_PORT}
      MAIL_USERNAME: ${MAIL_USERNAME}
      MAIL_PASSWORD: ${MAIL_PASSWORD}
      
      SPRING_PROFILES_ACTIVE: prd
    
    restart: always
    
    # 资源限制
    resources:
      limits:
        cpus: '2'
        memory: 2G
      reservations:
        cpus: '1'
        memory: 1G
    
    # 日志驱动
    logging:
      driver: "json-file"
      options:
        max-size: "100m"
        max-file: "3"
```

---

## 🚀 启动脚本

### Linux/macOS: start.sh

```bash
#!/bin/bash

set -e

# 加载环境变量
if [ -f .env ]; then
    echo "Loading environment variables from .env"
    export $(cat .env | xargs)
else
    echo "Warning: .env file not found, using defaults"
fi

# 设置默认值
: ${MYSQL_USERNAME:=root}
: ${MYSQL_PASSWORD:=silenceopr@2026}
: ${NACOS_USERNAME:=nacos}
: ${NACOS_PASSWORD:=nacos}
: ${MAIL_USERNAME:=13611988536@163.com}
: ${MAIL_PASSWORD:=PTsXDSWS8PqZarUA}
: ${SPRING_PROFILES_ACTIVE:=dev}
: ${SERVER_PORT:=8098}

echo "========================================="
echo "Starting Silence Job Server"
echo "========================================="
echo "MySQL User: $MYSQL_USERNAME"
echo "Nacos Server: $NACOS_SERVER_ADDR"
echo "Spring Profile: $SPRING_PROFILES_ACTIVE"
echo "Server Port: $SERVER_PORT"
echo "========================================="

# 启动应用
java \
    -server \
    -Xmx1024m \
    -Xms512m \
    -XX:+UseG1GC \
    -XX:MaxGCPauseMillis=200 \
    -DMYSQL_USERNAME="$MYSQL_USERNAME" \
    -DMYSQL_PASSWORD="$MYSQL_PASSWORD" \
    -DNACOS_USERNAME="$NACOS_USERNAME" \
    -DNACOS_PASSWORD="$NACOS_PASSWORD" \
    -DMAIL_USERNAME="$MAIL_USERNAME" \
    -DMAIL_PASSWORD="$MAIL_PASSWORD" \
    -DSPRING_PROFILES_ACTIVE="$SPRING_PROFILES_ACTIVE" \
    -DSERVER_PORT="$SERVER_PORT" \
    -jar target/silence-job-server-starter-1.0.0.jar

```

### Windows: start.bat

```batch
@echo off
REM 加载环境变量
if exist .env (
    echo Loading environment variables from .env
    for /f "tokens=*" %%a in (.env) do set "%%a"
) else (
    echo Warning: .env file not found, using defaults
)

REM 设置默认值
if not defined MYSQL_USERNAME set MYSQL_USERNAME=root
if not defined MYSQL_PASSWORD set MYSQL_PASSWORD=silenceopr@2026
if not defined NACOS_USERNAME set NACOS_USERNAME=nacos
if not defined NACOS_PASSWORD set NACOS_PASSWORD=nacos
if not defined MAIL_USERNAME set MAIL_USERNAME=13611988536@163.com
if not defined MAIL_PASSWORD set MAIL_PASSWORD=PTsXDSWS8PqZarUA
if not defined SPRING_PROFILES_ACTIVE set SPRING_PROFILES_ACTIVE=dev
if not defined SERVER_PORT set SERVER_PORT=8098

echo =========================================
echo Starting Silence Job Server
echo =========================================
echo MySQL User: %MYSQL_USERNAME%
echo Spring Profile: %SPRING_PROFILES_ACTIVE%
echo Server Port: %SERVER_PORT%
echo =========================================

REM 启动应用
java -Xmx1024m -Xms512m ^
    -DMYSQL_USERNAME=%MYSQL_USERNAME% ^
    -DMYSQL_PASSWORD=%MYSQL_PASSWORD% ^
    -DNACOS_USERNAME=%NACOS_USERNAME% ^
    -DNACOS_PASSWORD=%NACOS_PASSWORD% ^
    -DMAIL_USERNAME=%MAIL_USERNAME% ^
    -DMAIL_PASSWORD=%MAIL_PASSWORD% ^
    -DSPRING_PROFILES_ACTIVE=%SPRING_PROFILES_ACTIVE% ^
    -DSERVER_PORT=%SERVER_PORT% ^
    -jar target\silence-job-server-starter-1.0.0.jar

pause
```

---

## ✅ 配置检查清单

- [ ] `.env` 文件已创建 (本地开发)
- [ ] `.env` 已添加到 `.gitignore`
- [ ] `.env.example` 已创建 (参考文件)
- [ ] Dockerfile 已创建
- [ ] docker-compose.yml 已创建
- [ ] 启动脚本 (start.sh/start.bat) 已创建
- [ ] 所有环境变量使用 `${VAR_NAME:default_value}` 格式
- [ ] 可以通过 `source .env` 加载环境变量
- [ ] 可以通过 Docker Compose 启动完整栈

---

## 🧪 验证命令

```bash
# 1. 验证 YAML 配置
grep -E "\$\{.*\}" silence-job-server-starter/src/main/resources/application*.yml

# 2. 验证环境变量
cat .env

# 3. 测试本地启动
source .env
mvn spring-boot:run

# 4. Docker 启动
docker-compose up -d

# 5. 查看日志
docker-compose logs -f job-server

# 6. 健康检查
curl http://localhost:8098/actuator/health
```

