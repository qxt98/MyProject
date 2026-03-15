#!/bin/bash

# Smart Pharma 项目启动脚本（包含 ngrok）
# 使用方法: ./start-with-ngrok.sh

set -e

echo "=========================================="
echo "Smart Pharma 项目启动脚本"
echo "=========================================="

# 颜色定义
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# 检查 Docker 是否运行
echo -e "${YELLOW}检查 Docker 状态...${NC}"
if ! docker info > /dev/null 2>&1; then
    echo -e "${RED}错误: Docker 未运行，请先启动 Docker Desktop${NC}"
    exit 1
fi
echo -e "${GREEN}✓ Docker 正在运行${NC}"

# 检查 ngrok 是否安装
echo -e "${YELLOW}检查 ngrok...${NC}"
if ! command -v ngrok &> /dev/null; then
    echo -e "${RED}错误: ngrok 未安装${NC}"
    echo "请访问 https://ngrok.com/download 安装 ngrok"
    exit 1
fi
echo -e "${GREEN}✓ ngrok 已安装${NC}"

# 进入项目目录
cd "$(dirname "$0")"

# 停止可能存在的旧容器
echo -e "${YELLOW}停止现有容器...${NC}"
docker compose down 2>/dev/null || true

# 启动 Docker 服务
echo -e "${YELLOW}启动 Docker 服务...${NC}"

# 检查是否有网络问题
if docker compose up -d 2>&1 | grep -q "certificate\|tls\|failed to verify"; then
    echo -e "${RED}检测到网络证书问题${NC}"
    echo ""
    echo "检测到 Docker 配置了代理，这可能导致证书验证失败。"
    echo "请参考 'Docker网络问题解决方案.md' 文件解决此问题。"
    echo ""
    echo "快速解决方案："
    echo "1. 打开 Docker Desktop"
    echo "2. Settings > Resources > Proxies"
    echo "3. 临时禁用代理配置"
    echo "4. Apply & Restart"
    echo "5. 重新运行此脚本"
    echo ""
    exit 1
fi

if docker compose up -d; then
    echo -e "${GREEN}✓ Docker 服务启动成功${NC}"
else
    echo -e "${RED}错误: Docker 服务启动失败${NC}"
    echo "请检查错误信息并参考 'Docker网络问题解决方案.md'"
    exit 1
fi

# 等待服务启动
echo -e "${YELLOW}等待服务启动（30秒）...${NC}"
sleep 30

# 检查服务状态
echo -e "${YELLOW}检查服务状态...${NC}"
docker compose ps

# 启动 ngrok
echo -e "${YELLOW}启动 ngrok 隧道...${NC}"
echo -e "${GREEN}前端服务将暴露在: http://localhost:3000${NC}"
echo -e "${GREEN}ngrok 将创建公网访问地址${NC}"
echo ""
echo "按 Ctrl+C 停止 ngrok 和服务"
echo ""

# 在后台启动 ngrok
ngrok http 3000 --log=stdout
