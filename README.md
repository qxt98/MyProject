# 基于 Spring Boot 的药品信息智能化管理系统

毕业设计项目：药品信息智能化管理系统的设计与实现。

## 技术栈

| 层次     | 技术           | 说明 |
|----------|----------------|------|
| 前端     | Vue.js         | 响应式界面、组件化开发、Ajax 异步通信 |
| 后端     | Spring Boot    | 业务逻辑、REST 接口、安全控制 |
| 数据库   | MySQL          | 药品、库存、采购、处方、用户等数据存储 |
| 构建/工具 | Maven / Vite   | 后端构建 / 前端构建与开发服务器 |

## 项目结构

```
smart-pharma/
├── backend/                 # Spring Boot 后端
│   ├── src/main/java/       # Java 源码
│   ├── src/main/resources/  # 配置与静态资源
│   └── pom.xml
├── frontend/                # Vue.js 前端
│   ├── src/
│   │   ├── views/           # 页面视图
│   │   ├── components/      # 可复用组件
│   │   ├── api/             # 后端接口封装
│   │   └── router/          # 路由
│   └── package.json
├── database/                # 数据库脚本
│   └── schema.sql           # 建表与初始数据
├── docs/                    # 设计文档与开题报告
│   ├── 设计说明.md
│   ├── UML说明.md
│   └── 开题报告摘要.md
└── README.md
```

## 功能模块

1. **药品信息管理**：药品基础信息维护（名称、类别、剂型规格、适应症、厂家、供应商、价格、图片等）的增删改查。
2. **库存管理**：入库、出库、退库及库存变动记录，支持批次、生产日期、有效期管理。
3. **采购审批**：采购申请提交与审批流程（药品、数量、供应商、预算、理由等）。
4. **处方审核**：处方信息审查、剂量与配伍校验、风险提示。
5. **患者用药指导**：智能问答与用药建议（结合药品库与用药规范）；**对患者开放**，无需登录即可使用（前端 /guide 为公开页，登录页提供入口）。
6. **系统管理**：用户、角色权限（RBAC）、操作日志、系统参数配置。

## 快速开始

### 方式一：Docker Desktop（推荐）

已安装 [Docker Desktop](https://www.docker.com/products/docker-desktop/) 时，在项目根目录执行：

```bash
docker compose up -d --build
```

首次或代码变更后建议加 `--build` 以重新构建镜像。启动完成后：

- 浏览器访问：**http://localhost:3000**（前端 + Nginx 代理后端）。工作人员请**登录**后使用；**患者用药指导**无需登录，可直接访问 http://localhost:3000/guide 或从登录页点击「患者用药指导（无需登录）」进入。
- **默认管理员账号**：用户名 `admin`，密码 `123456`（生产环境请修改，见 `database/schema.sql`）。
- 后端 API：http://localhost:8080/api  
- **Swagger 接口文档**：http://localhost:8080/api/swagger-ui.html（或经前端代理 http://localhost:3000/api/swagger-ui.html）
- MySQL 端口：3306（如需用本机客户端连接）

可选：复制 `.env.example` 为 `.env`，配置 `GUIDE_AI_API_KEY` 可启用**患者用药指导**的 DeepSeek AI；不配置则使用规则兜底。详见 [开发与部署说明 - 患者用药指导 AI 配置](docs/开发与部署说明.md#六患者用药指导-ai-配置可选)。

详见 [开发与部署说明 - Docker 部署](docs/开发与部署说明.md#一使用-docker-desktop-部署推荐)。

### 方式二：本地环境

#### 环境要求

- JDK 17+
- Node.js 18+
- MySQL 8.0+
- Maven 3.8+

#### 数据库

```bash
# 创建数据库并执行脚本
mysql -u root -p -e "CREATE DATABASE smart_pharma DEFAULT CHARACTER SET utf8mb4;"
mysql -u root -p smart_pharma < database/schema.sql
```

### 后端

```bash
cd backend
# 修改 src/main/resources/application.yml 中的数据库连接
mvn spring-boot:run
```

默认端口：`8080`。

### 前端

```bash
cd frontend
npm install
npm run dev
```

默认端口：`5173`（或见控制台输出）。

### 接口与安全

- 后端提供 REST API，前端通过 Axios 等发起 Ajax 请求。
- **API 文档（Swagger）**：后端集成 springdoc-openapi，启动后访问 **http://localhost:8080/api/swagger-ui.html** 可查看与调试所有接口；OpenAPI JSON 地址为 `/api/v3/api-docs`。
- 权限采用基于角色的访问控制（RBAC），敏感数据加密存储与传输。

## 文档

- [需求文档](docs/需求文档.md)
- [设计说明](docs/设计说明.md)
- [系统设计文档](docs/系统设计文档.md)
- [产品文档](docs/产品文档.md)
- [逻辑文档](docs/逻辑文档.md)
- [UML 说明](docs/UML说明.md)
- [开题报告摘要](docs/开题报告摘要.md)
- [开发与部署说明](docs/开发与部署说明.md)（含 Docker 与本地部署）
- [Docker 使用说明](docs/Docker使用说明.md)（Docker 文件、命令、环境变量、常见问题）
- [角色与功能对应说明](docs/角色与功能对应说明.md)（RBAC 权限矩阵、单元测试与运行方式）

## 开发与测试

### 运行项目

| 方式 | 命令 | 说明 |
|------|------|------|
| Docker（推荐） | 项目根目录执行 `docker compose up -d --build` | 启动 MySQL、后端、前端；访问 http://localhost:3000，默认账号 admin / 123456 |
| 本地后端 | `cd backend` → `mvn spring-boot:run` | 需 JDK 17+、MySQL 已建库并执行 `database/schema.sql` |
| 本地前端 | `cd frontend` → `npm install` → `npm run dev` | 需 Node.js 18+，开发端口通常为 5173 |

### 运行单元测试

- **环境**：需 **JDK 17+**（与 Spring Boot 3.2 一致）。若本机为旧版 JDK，可用 Docker 运行测试。
- **本地**（已安装 JDK 17 与 Maven）：
  ```bash
  cd backend
  mvn test
  ```
- **仅运行权限相关单元测试**：
  ```bash
  cd backend
  mvn test -Dtest=PurchaseRequestControllerSecurityTest,PrescriptionControllerSecurityTest,GuideControllerSecurityTest
  ```
- **使用 Docker 运行全部测试**（不依赖本机 JDK 版本）：
  ```bash
  docker run --rm -v "%cd%\backend:/app" -w /app maven:3.9-eclipse-temurin-17 mvn test -B
  ```
  （Windows PowerShell 下路径可用 `e:\workspace\smart-pharma\backend` 等绝对路径替代 `%cd%\backend`。）

- 测试说明与角色权限对应关系见 [角色与功能对应说明](docs/角色与功能对应说明.md) 第 8 节。

- 开发采用迭代方式，结合 CI/CD 进行构建与部署；测试包括单元测试、集成测试、系统测试、用户验收测试（UAT）。

## 许可证

见 [LICENSE](LICENSE)。
