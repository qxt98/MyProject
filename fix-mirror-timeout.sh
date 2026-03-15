#!/bin/bash

# 修复镜像源超时问题的脚本

set -e

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m'

echo "=========================================="
echo "修复 Docker 镜像源超时问题"
echo "=========================================="

# 检查 Docker
if ! docker info > /dev/null 2>&1; then
    echo -e "${RED}错误: Docker 未运行${NC}"
    exit 1
fi

echo -e "${GREEN}✓ Docker 正在运行${NC}"

# 显示当前镜像源配置
echo ""
echo -e "${YELLOW}当前镜像源配置:${NC}"
docker info 2>&1 | grep -A 5 "Registry Mirrors" || echo "  无镜像源配置"

echo ""
echo -e "${BLUE}检测到镜像源超时问题${NC}"
echo ""
echo "解决方案："
echo ""
echo "1. 打开 Docker Desktop"
echo "2. 进入 Settings > Docker Engine"
echo "3. 编辑 registry-mirrors 配置"
echo ""
echo "推荐配置（移除有问题的 docker.domys.cc）："
echo ""
cat << 'EOF'
{
  "registry-mirrors": [
    "https://docker.mirrors.ustc.edu.cn/",
    "https://hub-mirror.c.163.com/"
  ]
}
EOF
echo ""
echo "或者如果网络允许，可以临时使用官方源："
echo ""
cat << 'EOF'
{
  "registry-mirrors": []
}
EOF
echo ""
echo "4. 点击 'Apply & Restart'"
echo "5. 等待 Docker 重启后，重新运行: docker compose up -d"
echo ""

# 尝试测试镜像源连接
echo -e "${YELLOW}测试镜像源连接...${NC}"
echo ""

test_mirror() {
    local url=$1
    local name=$2
    echo -n "测试 $name... "
    if curl -s --max-time 5 -o /dev/null -w "%{http_code}" "$url" 2>/dev/null | grep -q "200\|401\|404"; then
        echo -e "${GREEN}✓ 可用${NC}"
        return 0
    else
        echo -e "${RED}✗ 超时或不可用${NC}"
        return 1
    fi
}

test_mirror "https://docker.mirrors.ustc.edu.cn/v2/" "USTC 镜像源"
test_mirror "https://hub-mirror.c.163.com/v2/" "163 镜像源"
test_mirror "https://registry-1.docker.io/v2/" "Docker Hub 官方"

echo ""
echo "如果所有镜像源都不可用，请检查网络连接。"
