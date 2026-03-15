# 药品信息智能化管理系统 - Docker 启动脚本
# 在 PowerShell 中执行：.\启动Docker.ps1
# 或直接在项目根目录执行：docker compose up -d --build

Set-Location $PSScriptRoot
Write-Host "正在构建并启动服务（首次可能需几分钟）..." -ForegroundColor Cyan
docker compose up -d --build
if ($LASTEXITCODE -eq 0) {
    Write-Host "`n启动完成。请访问：" -ForegroundColor Green
    Write-Host "  前端页面: http://localhost:3000" -ForegroundColor Yellow
    Write-Host "  后端 API:  http://localhost:8080/api" -ForegroundColor Yellow
    Write-Host "`n查看状态: docker compose ps" -ForegroundColor Gray
    Write-Host "查看日志: docker compose logs -f" -ForegroundColor Gray
} else {
    Write-Host "启动失败，请检查 Docker Desktop 是否已运行。" -ForegroundColor Red
}
