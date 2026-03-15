# 项目启动助手脚本 - 简化版
# 功能：检查Docker状态并启动项目服务

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "Smart Pharma 项目启动助手" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

# 检查Docker状态
Write-Host "检查Docker状态..." -ForegroundColor Yellow
$dockerCheck = docker info 2>&1

if ($LASTEXITCODE -ne 0) {
    Write-Host "✗ Docker 未运行或未启动" -ForegroundColor Red
    Write-Host ""
    Write-Host "请按以下步骤操作：" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "1. 手动启动 Docker Desktop：" -ForegroundColor White
    Write-Host "   - 点击Windows开始菜单" -ForegroundColor Gray
    Write-Host "   - 搜索 'Docker Desktop' 并点击打开" -ForegroundColor Gray
    Write-Host "   - 或双击桌面上的Docker Desktop图标" -ForegroundColor Gray
    Write-Host ""
    Write-Host "2. 等待 Docker Desktop 完全启动：" -ForegroundColor White
    Write-Host "   - 系统托盘（右下角）会出现Docker鲸鱼图标" -ForegroundColor Gray
    Write-Host "   - 图标变为绿色表示已就绪" -ForegroundColor Gray
    Write-Host "   - 首次启动可能需要1-2分钟" -ForegroundColor Gray
    Write-Host ""
    Write-Host "3. Docker启动后，重新运行此脚本或执行：" -ForegroundColor Green
    Write-Host "   docker compose up -d" -ForegroundColor White
    Write-Host ""
    Write-Host "按任意键退出..." -ForegroundColor White
    $null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
    exit 1
}

Write-Host "✓ Docker 正在运行" -ForegroundColor Green
Write-Host ""

# 启动项目服务
Write-Host "开始启动项目服务..." -ForegroundColor Green
Write-Host "正在执行: docker compose up -d" -ForegroundColor Gray
Write-Host "首次启动可能需要几分钟下载镜像，请耐心等待..." -ForegroundColor Yellow
Write-Host ""

# 执行启动命令
docker compose up -d

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "✓ 服务启动成功！" -ForegroundColor Green
    Write-Host "等待服务初始化（30秒）..." -ForegroundColor Yellow
    Start-Sleep -Seconds 30
    
    Write-Host ""
    Write-Host "==========================================" -ForegroundColor Cyan
    Write-Host "服务启动完成" -ForegroundColor Cyan
    Write-Host "==========================================" -ForegroundColor Cyan
    Write-Host ""
    
    # 显示服务状态
    Write-Host "服务状态:" -ForegroundColor Green
    docker compose ps
    
    Write-Host ""
    Write-Host "访问地址:" -ForegroundColor Green
    Write-Host "  前端页面: http://localhost:3000" -ForegroundColor Yellow
    Write-Host "  后端 API:  http://localhost:8080" -ForegroundColor Yellow
    Write-Host "  API文档:   http://localhost:8080/swagger-ui.html" -ForegroundColor Yellow
    Write-Host "  MySQL数据库: localhost:3306" -ForegroundColor Yellow
    Write-Host ""
    
    Write-Host "管理命令:" -ForegroundColor Green
    Write-Host "  查看日志: docker compose logs -f" -ForegroundColor Gray
    Write-Host "  停止服务: docker compose stop" -ForegroundColor Gray
    Write-Host "  删除服务: docker compose down" -ForegroundColor Gray
    Write-Host "  重启服务: docker compose restart" -ForegroundColor Gray
    Write-Host ""
    
    Write-Host "测试账号（用于登录系统）:" -ForegroundColor Green
    Write-Host "  管理员: admin / admin123" -ForegroundColor Gray
    Write-Host "  药师:   pharmacist / pharma123" -ForegroundColor Gray
    Write-Host "  护士:   nurse / nurse123" -ForegroundColor Gray
    Write-Host "  采购员: purchaser / purchase123" -ForegroundColor Gray
    Write-Host ""
    
    Write-Host "注意：护士账号已按需求修改，无法访问库存管理功能" -ForegroundColor Magenta
    Write-Host ""
    
} else {
    Write-Host ""
    Write-Host "✗ 服务启动失败" -ForegroundColor Red
    Write-Host "错误信息:" -ForegroundColor Yellow
    Write-Host $dockerCheck -ForegroundColor Gray
    Write-Host ""
    Write-Host "请尝试以下解决方案：" -ForegroundColor Yellow
    Write-Host "1. 检查端口是否被占用（3000, 8080, 3306）" -ForegroundColor Gray
    Write-Host "2. 检查网络连接，确保可以下载Docker镜像" -ForegroundColor Gray
    Write-Host "3. 查看详细日志: docker compose logs" -ForegroundColor Gray
    Write-Host "4. 重启Docker Desktop后重试" -ForegroundColor Gray
    Write-Host ""
}

Write-Host "按任意键退出..." -ForegroundColor White
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")