========================================
Smart Pharma 项目启动指南
========================================

由于检测到 Docker 网络配置问题（代理证书错误），请按以下步骤操作：

【步骤 1】解决 Docker 网络问题
--------------------------------
1. 打开 Docker Desktop
2. 进入 Settings (设置) > Resources > Proxies  
3. 临时禁用或清空代理配置
4. 点击 "Apply & Restart"
5. 等待 Docker 重启完成

详细说明请查看: Docker网络问题解决方案.md

【步骤 2】启动 Docker 服务
--------------------------------
在项目根目录执行：

    docker compose up -d

等待所有服务启动（约 30-60 秒）

验证服务状态：

    docker compose ps

应该看到三个服务都在运行：
  - smart-pharma-mysql
  - smart-pharma-backend
  - smart-pharma-frontend

【步骤 3】启动 ngrok
--------------------------------
方法 1 - 使用脚本：

    ./start-ngrok-only.sh

方法 2 - 手动启动：

    ngrok http 3000

【步骤 4】获取外网访问地址
--------------------------------
ngrok 启动后会显示类似信息：

    Forwarding   https://xxxx-xx-xx-xx-xx.ngrok-free.app -> http://localhost:3000

使用这个 https 地址即可在外网访问项目。

注意：
- 免费版会显示警告页面，点击 "Visit Site" 继续
- 地址每次启动都会变化
- 如需固定域名，需要升级 ngrok 账户

【停止服务】
--------------------------------
停止 Docker 服务：
    docker compose down

停止 ngrok：
    在运行 ngrok 的终端按 Ctrl+C

【更多帮助】
--------------------------------
查看详细文档: 启动说明.md
查看网络问题解决方案: Docker网络问题解决方案.md
