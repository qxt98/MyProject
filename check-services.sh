#!/bin/bash

# 检查服务状态的脚本

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

echo "=========================================="
echo "检查 Smart Pharma 服务状态"
echo "=========================================="

# 检查 Docker
echo -n "检查 Docker... "
if docker info > /dev/null 2>&1; then
    echo -e "${GREEN}✓ 运行中${NC}"
else
    echo -e "${RED}✗ 未运行${NC}"
    exit 1
fi

# 检查容器
echo ""
echo "检查容器状态:"
docker compose ps

echo ""
echo "检查端口:"
echo -n "  前端 (3000)... "
if nc -z localhost 3000 2>/dev/null; then
    echo -e "${GREEN}✓ 可访问${NC}"
else
    echo -e "${RED}✗ 不可访问${NC}"
fi

echo -n "  后端 (8080)... "
if nc -z localhost 8080 2>/dev/null; then
    echo -e "${GREEN}✓ 可访问${NC}"
else
    echo -e "${RED}✗ 不可访问${NC}"
fi

echo -n "  MySQL (3306)... "
if nc -z localhost 3306 2>/dev/null; then
    echo -e "${GREEN}✓ 可访问${NC}"
else
    echo -e "${RED}✗ 不可访问${NC}"
fi

echo ""
echo "访问地址:"
echo "  前端: http://localhost:3000"
echo "  后端: http://localhost:8080"
