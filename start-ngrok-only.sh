#!/bin/bash

# 仅启动 ngrok 脚本（假设 Docker 服务已在运行）
# 使用方法: ./start-ngrok-only.sh

set -e

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

echo "=========================================="
echo "启动 ngrok 隧道"
echo "=========================================="

# 检查服务是否运行
if ! docker ps | grep -q smart-pharma-frontend; then
    echo -e "${RED}错误: smart-pharma-frontend 容器未运行${NC}"
    echo "请先运行: docker compose up -d"
    exit 1
fi

# 检查端口是否可访问
if ! nc -z localhost 3000 2>/dev/null; then
    echo -e "${YELLOW}警告: 端口 3000 似乎不可访问${NC}"
    echo "请确认前端服务已完全启动"
    sleep 2
fi

# 检查 ngrok
if ! command -v ngrok &> /dev/null; then
    echo -e "${RED}错误: ngrok 未安装${NC}"
    exit 1
fi

echo -e "${GREEN}前端服务运行在: http://localhost:3000${NC}"
echo -e "${GREEN}启动 ngrok 隧道...${NC}"
echo ""
echo "ngrok 将创建一个公网访问地址"
echo "按 Ctrl+C 停止 ngrok"
echo ""

ngrok http 3000 --log=stdout
