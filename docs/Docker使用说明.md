# 药品信息智能化管理系统 — Docker 使用说明

本文档说明如何使用 Docker / Docker Desktop 运行本项目的数据库、后端与前端服务。

---

## 一、Docker 相关文件说明

| 文件/目录 | 说明 |
|-----------|------|
| `docker-compose.yml` | 项目根目录下的编排文件，定义 MySQL、backend、frontend 三个服务及其依赖、端口、环境变量、数据卷。 |
| `backend/Dockerfile` | 后端镜像构建：Maven 构建 Spring Boot jar → JRE 17 运行。 |
| `frontend/Dockerfile` | 前端镜像构建：Node 构建 Vue 静态资源 → Nginx 提供页面并代理 `/api` 到后端。 |
| `frontend/nginx.conf` | 前端容器内 Nginx 配置：静态资源、SPA 路由、`/api` 反向代理。 |
| `backend/src/main/resources/application-docker.yml` | Docker 环境下 Spring Boot 配置（数据源等由环境变量覆盖）。 |
| `.env.example` | 环境变量示例；复制为 `.env` 后可自定义数据库密码、患者用药指导 AI Key 等。`.env` 已加入 `.gitignore`，勿提交。 |
| `backend/.dockerignore`、`frontend/.dockerignore` | 构建时忽略的文件，减小上下文、加快构建。 |
| `启动Docker.ps1` | PowerShell 启动脚本（Windows），执行后构建并启动所有服务。 |

---

## 二、环境要求

- 已安装 **Docker** 或 **Docker Desktop**（[下载](https://www.docker.com/products/docker-desktop/)）
- Docker 引擎已启动（Docker Desktop 需处于运行状态）
- 建议为 Docker 分配至少 **4GB** 内存
- 首次运行需联网（拉取 MySQL、Maven、Node、Nginx 等基础镜像）

---

## 三、服务与端口

| 服务 | 容器名 | 镜像/构建 | 宿主机端口 | 说明 |
|------|--------|-----------|------------|------|
| mysql | smart-pharma-mysql | mysql:8.0 | 3306 | MySQL 数据库，首次启动自动创建库 `smart_pharma` 并执行 `database/schema.sql` |
| backend | smart-pharma-backend | 从 backend/Dockerfile 构建 | 8080 | Spring Boot 应用，上下文路径 `/api`，依赖 MySQL 健康后启动 |
| frontend | smart-pharma-frontend | 从 frontend/Dockerfile 构建 | **3000** | Nginx 提供前端页面，并将 `/api` 代理到 backend:8080 |

**访问地址：**

- **前端（浏览器）**：http://localhost:3000  
- **后端 API**：http://localhost:8080/api  
- **Swagger 接口文档**：请使用 **http://localhost:8080/api/swagger-ui.html**（不要用 `/swagger-ui/index.html`）。经前端代理：http://localhost:3000/api/swagger-ui.html。OpenAPI JSON：http://localhost:8080/api/v3/api-docs  

> 若本机 80 端口空闲，可在 `docker-compose.yml` 中将 frontend 的 `ports` 改为 `"80:80"`，即可通过 http://localhost 访问前端。

---

## 四、启动与停止

### 4.1 首次启动（构建并启动）

在项目根目录执行：

```bash
docker compose up -d --build
```

- `-d`：后台运行  
- `--build`：构建或重新构建镜像  

首次会拉取基础镜像并构建后端、前端，可能需要数分钟。

### 4.2 后续启动（不重新构建）

```bash
docker compose up -d
```

### 4.3 停止所有服务

```bash
docker compose down
```

不会删除 MySQL 数据卷，再次 `up` 时数据仍在。

### 4.4 停止并删除数据卷（清空数据库）

```bash
docker compose down -v
```

下次 `up` 时会重新创建库并执行 `database/schema.sql`。

---

## 五、常用命令

| 操作 | 命令 |
|------|------|
| 查看运行状态 | `docker compose ps` |
| 查看所有服务日志 | `docker compose logs -f` |
| 仅查看后端日志 | `docker compose logs -f backend` |
| 仅查看 MySQL 日志 | `docker compose logs -f mysql` |
| 重新构建并启动 | `docker compose up -d --build` |
| 仅重新构建后端 | `docker compose build backend` |
| 仅重新构建前端 | `docker compose build frontend` |
| 进入 MySQL 命令行 | `docker compose exec mysql mysql -uroot -p smart_pharma`（密码见 `.env` 或默认 `root`） |

---

## 六、环境变量

在项目根目录创建 `.env` 文件（可复制 `.env.example` 后修改），用于覆盖默认配置：

| 变量名 | 默认值 | 说明 |
|--------|--------|------|
| MYSQL_ROOT_PASSWORD | root | MySQL root 密码，backend 通过该账号连接 |
| MYSQL_USER | app | 可选，MySQL 应用账号 |
| MYSQL_PASSWORD | app123 | 可选，MySQL 应用账号密码 |
| GUIDE_AI_API_KEY | （空） | 可选，患者用药指导 DeepSeek API Key；不填则使用规则兜底 |

修改 `.env` 后需执行 `docker compose up -d` 使新配置生效（若仅改密码，可先 `docker compose down` 再 `up -d`；**注意**：若已有数据卷，改 root 密码后需保证 backend 使用的密码与之一致）。

---

## 七、数据持久化

- **MySQL 数据**：通过 Docker 卷 `mysql_data` 持久化，位置由 Docker 管理。执行 `docker compose down` 不会删除该卷，因此数据库数据会保留。
- **清空数据**：执行 `docker compose down -v` 会删除 `mysql_data`，下次 `docker compose up -d` 时 MySQL 会重新初始化并执行 `database/schema.sql`。

---

## 八、Dockerfile 与构建说明

### 8.1 后端（backend/Dockerfile）

- **构建阶段**：使用 `maven:3.9-eclipse-temurin-17`，复制 `pom.xml` 与 `src`，执行 `mvn package -DskipTests`，得到 jar。
- **运行阶段**：使用 `eclipse-temurin:17-jre`，仅复制 jar，暴露 8080，启动命令 `java -jar app.jar`。
- 运行时会加载 Spring profile `docker`（见 `application-docker.yml`），数据源地址为 `mysql:3306`。

### 8.2 前端（frontend/Dockerfile）

- **构建阶段**：使用 `node:20-alpine`，安装依赖并执行 `npm run build`，产物在 `dist/`。
- **运行阶段**：使用 `nginx:alpine`，将 `dist` 复制到 `/usr/share/nginx/html`，使用项目中的 `nginx.conf` 作为默认站点配置，容器内监听 80 端口；宿主机映射为 3000（见 `docker-compose.yml`）。

---

## 九、常见问题

### 9.1 端口被占用

- **80 端口**：Windows 上 80 可能被系统或 IIS 占用，当前 compose 已改为将前端映射到 **3000**。若需使用 80，请先释放该端口再修改 `docker-compose.yml` 中 frontend 的 `ports` 为 `"80:80"`。
- **3306 / 8080 / 3000**：若被其他程序占用，可在 `docker-compose.yml` 中修改对应服务的 `ports`（如 `"3307:3306"`），并注意后端连接数据库时仍使用容器内端口 3306。

### 9.2 构建失败

- **后端 Maven 失败**：检查 `backend/pom.xml` 语法，确保本机或 Docker 能访问 Maven 中央仓库。
- **前端 npm build 失败**：检查 `frontend/package.json` 与 Node 版本（Dockerfile 使用 Node 20）；若缺少 `package-lock.json`，Dockerfile 中已使用 `npm install` 作为回退。

### 9.3 后端启动报错连不上 MySQL

- 确保先启动 MySQL 且健康检查通过（compose 中 backend 已设置 `depends_on: mysql: condition: service_healthy`）。
- 若曾修改过 `MYSQL_ROOT_PASSWORD`，请确认 backend 的 `SPRING_DATASOURCE_PASSWORD` 与之一致（compose 中已通过 `${MYSQL_ROOT_PASSWORD:-root}` 传递）。

### 9.4 前端能打开但接口 404 或报错

- 确认 backend 容器已正常运行：`docker compose ps`，并访问 http://localhost:8080/api/drugs 测试。
- 前端通过 Nginx 将 `/api` 代理到 `backend:8080`，仅在通过 http://localhost:3000 访问页面时生效；若直接打开前端静态文件，需自行解决跨域或代理。

### 9.5 访问 /api/auth/login 等接口出现 502 Bad Gateway

502 表示 Nginx 代理无法从后端得到正常响应，常见原因与处理见上文“直连后端验证”与“后端容器异常”排查。

### 9.6 后端容器 Up 但访问 http://localhost:8080/api/ 或 Swagger 无数据（“didn’t send any data”）

说明请求已到宿主机但未得到正常响应，可按下面步骤排查：

1. **确认进程是否真的在监听**  
   在项目根目录执行：
   ```bash
   docker compose logs backend --tail 80
   ```
   看最后是否有 `Started SmartPharmaApplication` 或类似启动成功日志；若有 **Exception**、**Error**、**Failed**，说明应用启动失败，需按报错修复（如数据库连接、JWT 配置等）。

2. **用 curl 测根路径**  
   ```bash
   curl -v http://localhost:8080/api/
   ```
   - 若返回 JSON（如 `{"code":200,"data":{...}}`），说明后端正常，再试 Swagger。
   - 若 `Connection refused`，说明本机 8080 未映射或后端未监听；若无输出或连接立即关闭，多为应用未启动成功，以步骤 1 的日志为准。

3. **再测 Swagger**  
   使用 **http://localhost:8080/api/swagger-ui.html**（不要用 `/swagger-ui/index.html`）。若根路径正常而 Swagger 仍无数据，请重新构建后端使最新 Security 配置生效：
   ```bash
   docker compose up -d --build backend
   ```
   等待约 30 秒后再访问 Swagger。

4. **端口与防火墙**  
   确认本机 8080 未被其他程序占用，且 Docker Desktop 端口映射正常（容器 8080 已映射到 host 8080）。

---

## 十、与文档的对应关系

- **开发与部署说明**（`开发与部署说明.md`）：包含 Docker 快速开始与本地/生产部署的总览，详细 Docker 说明以本文档为准。
- **README.md**：项目概览与快速开始中已注明通过 Docker 访问 http://localhost:3000。

---

**文档版本**：V1.0  
**适用项目**：基于 Spring Boot 的药品信息智能化管理系统
