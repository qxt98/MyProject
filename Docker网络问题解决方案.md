# Docker 网络问题解决方案

## 问题描述
Docker 在拉取镜像时出现证书验证错误，错误信息显示证书是为 Facebook 域名签发的。

## 原因
检测到 Docker 配置了代理：`http.docker.internal:3128`，这可能导致证书验证问题。

## 解决方案

### 方案 1: 临时禁用 Docker 代理（推荐）

1. 打开 Docker Desktop
2. 进入 Settings (设置) > Resources > Proxies
3. 取消勾选 "Manual proxy configuration" 或清空代理设置
4. 点击 "Apply & Restart"
5. 重新运行启动脚本

### 方案 2: 修复代理配置

如果必须使用代理，请确保代理服务器正常工作：

```bash
# 测试代理是否可用
curl -v --proxy http://http.docker.internal:3128 https://registry-1.docker.io/v2/
```

### 方案 3: 使用国内镜像源（如果在中国）

如果在中国大陆，可以使用 Docker 镜像加速器：

1. 打开 Docker Desktop
2. 进入 Settings > Docker Engine
3. 添加以下配置：

```json
{
  "registry-mirrors": [
    "https://docker.mirrors.ustc.edu.cn",
    "http://hub-mirror.c.163.com",
    "https://docker.m.daocloud.io"
  ]
}
```

4. 点击 "Apply & Restart"

### 方案 4: 手动构建（如果网络问题持续）

如果上述方案都不行，可以尝试：

1. 确保基础镜像已下载：
```bash
docker pull mysql:8.0
docker pull maven:3.9-eclipse-temurin-17
docker pull node:20-alpine
docker pull nginx:alpine
docker pull eclipse-temurin:17-jre
```

2. 然后运行：
```bash
docker compose build --no-cache
docker compose up -d
```

## 验证服务

启动后，检查服务状态：

```bash
docker compose ps
```

应该看到三个服务都在运行：
- smart-pharma-mysql
- smart-pharma-backend  
- smart-pharma-frontend

访问：
- 前端: http://localhost:3000
- 后端 API: http://localhost:8080
