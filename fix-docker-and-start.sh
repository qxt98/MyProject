#!/bin/bash

# 修复 Docker 网络问题并启动服务的脚本

set -e

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m'

echo "=========================================="
echo "修复 Docker 网络问题并启动服务"
echo "=========================================="

# 检查 Docker
if ! docker info > /dev/null 2>&1; then
    echo -e "${RED}错误: Docker 未运行，请先启动 Docker Desktop${NC}"
    exit 1
fi

echo -e "${GREEN}✓ Docker 正在运行${NC}"

# 检查代理配置
echo ""
echo -e "${YELLOW}检查 Docker 代理配置...${NC}"
PROXY_INFO=$(docker info 2>&1 | grep -i "HTTP Proxy" || echo "")
if [ -n "$PROXY_INFO" ]; then
    echo -e "${YELLOW}检测到代理配置:${NC}"
    echo "$PROXY_INFO"
    echo ""
    echo -e "${BLUE}提示: 如果遇到证书错误，建议在 Docker Desktop 中禁用代理${NC}"
    echo "路径: Settings > Resources > Proxies"
    echo ""
fi

# 尝试方法 1: 直接启动（如果镜像已存在）
echo -e "${YELLOW}尝试启动服务...${NC}"
if docker compose up -d 2>&1 | tee /tmp/docker-output.log; then
    echo -e "${GREEN}✓ 服务启动成功！${NC}"
    echo ""
    echo "等待服务就绪（30秒）..."
    sleep 30
    
    echo ""
    echo -e "${GREEN}服务状态:${NC}"
    docker compose ps
    
    echo ""
    echo -e "${GREEN}访问地址:${NC}"
    echo "  前端: http://localhost:3000"
    echo "  后端: http://localhost:8080"
    echo ""
    echo "启动 ngrok: ./start-ngrok-only.sh"
    exit 0
fi

# 如果失败，检查是否是证书错误
if grep -q "certificate\|tls\|failed to verify" /tmp/docker-output.log; then
    echo ""
    echo -e "${RED}检测到证书验证错误${NC}"
    echo ""
    echo "解决方案："
    echo "1. 打开 Docker Desktop"
    echo "2. 进入 Settings > Resources > Proxies"
    echo "3. 取消勾选 'Manual proxy configuration' 或清空代理设置"
    echo "4. 点击 'Apply & Restart'"
    echo "5. 等待 Docker 重启后，重新运行此脚本"
    echo ""
    echo "或者尝试手动拉取镜像（如果网络允许）："
    echo "  docker pull mysql:8.0"
    echo "  docker pull maven:3.9-eclipse-temurin-17"
    echo "  docker pull node:20-alpine"
    echo "  docker pull nginx:alpine"
    echo "  docker pull eclipse-temurin:17-jre"
    echo "  然后运行: docker compose build && docker compose up -d"
    exit 1
fi

echo -e "${RED}启动失败，请查看上方错误信息${NC}"
exit 1
