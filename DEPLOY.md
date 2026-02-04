# RAG QA System - 生产环境部署指南

本文档详细介绍如何将 RAG QA System 部署到云服务器，使其成为可通过域名访问的真实网站。

## 部署流程概览

```
┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│  准备服务器  │ -> │ 安装 Docker │ -> │ 部署应用    │ -> │ 配置域名    │
└─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘
```

## 一、准备云服务器

### 1.1 服务器要求

| 配置项 | 最低要求 | 推荐配置 |
|--------|---------|---------|
| CPU | 2核 | 4核 |
| 内存 | 4GB | 8GB |
| 磁盘 | 40GB | 100GB SSD |
| 带宽 | 3Mbps | 5Mbps+ |
| 操作系统 | Ubuntu 20.04/22.04 LTS | Ubuntu 22.04 LTS |

### 1.2 推荐云服务商

- **国内**：阿里云 ECS、腾讯云 CVM、华为云 ECS
- **国外**：AWS EC2、Google Cloud Compute、DigitalOcean Droplet

### 1.3 服务器初始化

购买服务器后，记录以下信息：
- 公网 IP 地址
- 登录用户名（通常是 `root` 或 `ubuntu`）
- 登录密码或密钥文件

## 二、连接服务器并安装环境

### 2.1 连接服务器

**Windows 用户使用 PowerShell 或 Git Bash：**
```powershell
ssh root@你的服务器IP
```

**Mac/Linux 用户：**
```bash
ssh root@你的服务器IP
```

输入密码后即可登录。

### 2.2 安装 Docker 和 Docker Compose

创建安装脚本：

```bash
cat > install-docker.sh << 'EOF'
#!/bin/bash
set -e

echo "=== 开始安装 Docker 和 Docker Compose ==="

# 更新系统
apt-get update
apt-get upgrade -y

# 安装必要的工具
apt-get install -y \
    apt-transport-https \
    ca-certificates \
    curl \
    gnupg \
    lsb-release \
    git \
    vim

# 添加 Docker 官方 GPG 密钥
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg

# 添加 Docker 软件源
echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] https://download.docker.com/linux/ubuntu \
  $(lsb_release -cs) stable" | tee /etc/apt/sources.list.d/docker.list > /dev/null

# 安装 Docker
apt-get update
apt-get install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin

# 启动 Docker
systemctl start docker
systemctl enable docker

# 安装 Docker Compose
DOCKER_COMPOSE_VERSION=$(curl -s https://api.github.com/repos/docker/compose/releases/latest | grep -oP '"tag_name": "\K(.*)(?=")')
curl -L "https://github.com/docker/compose/releases/download/${DOCKER_COMPOSE_VERSION}/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
chmod +x /usr/local/bin/docker-compose

# 创建软链接
ln -sf /usr/local/bin/docker-compose /usr/bin/docker-compose

echo "=== Docker 和 Docker Compose 安装完成 ==="
echo "Docker 版本: $(docker --version)"
echo "Docker Compose 版本: $(docker-compose --version)"
EOF

chmod +x install-docker.sh
./install-docker.sh
```

### 2.3 配置防火墙

```bash
# 安装 UFW（如果未安装）
apt-get install -y ufw

# 默认拒绝所有入站连接
ufw default deny incoming

# 允许所有出站连接
ufw default allow outgoing

# 允许 SSH（22端口）
ufw allow 22/tcp

# 允许 HTTP（80端口）
ufw allow 80/tcp

# 允许 HTTPS（443端口）
ufw allow 443/tcp

# 启用防火墙
ufw --force enable

# 查看防火墙状态
ufw status
```

## 三、上传项目代码

### 3.1 方式一：使用 Git 克隆（推荐）

如果代码已推送到 Git 仓库：

```bash
# 安装 Git
apt-get install -y git

# 克隆代码到服务器
cd /opt
git clone <你的Git仓库地址> rag-qa-system

# 进入项目目录
cd rag-qa-system
```

### 3.2 方式二：使用 SCP 上传

从本地电脑上传：

**Windows (PowerShell):**
```powershell
# 压缩项目文件
Compress-Archive -Path ".\*" -DestinationPath "rag-qa-system.zip"

# 上传到服务器
scp rag-qa-system.zip root@你的服务器IP:/opt/
```

**Mac/Linux:**
```bash
# 压缩项目
zip -r rag-qa-system.zip . -x "*.git*" -x "node_modules/*" -x "target/*"

# 上传到服务器
scp rag-qa-system.zip root@你的服务器IP:/opt/
```

然后在服务器上解压：
```bash
cd /opt
unzip rag-qa-system.zip -d rag-qa-system
cd rag-qa-system
```

## 四、配置和部署

### 4.1 配置环境变量

```bash
# 复制示例配置文件
cp .env.example .env

# 编辑配置文件
vim .env
```

**必须修改的配置项：**

```bash
# 数据库密码（使用强密码）
POSTGRES_PASSWORD=YourSecurePassword123!

# ModelScope API Key
# 从 https://www.modelscope.cn/my/myaccesstoken 获取
MODELSCOPE_API_KEY=your-actual-api-key

# JWT Secret（生成命令：openssl rand -base64 32）
JWT_SECRET=your-generated-jwt-secret
```

保存并退出（Vim: `:wq`）。

### 4.2 运行部署脚本

```bash
# 给脚本执行权限
chmod +x deploy.sh

# 执行部署
./deploy.sh
```

部署过程大约需要 5-10 分钟，具体取决于服务器性能和网络速度。

### 4.3 验证部署

部署完成后，访问服务器 IP 地址：

```
http://你的服务器IP
```

如果看到登录页面，说明部署成功！

## 五、配置域名和 HTTPS（推荐）

### 5.1 购买域名

推荐域名注册商：
- 阿里云（万网）
- 腾讯云（DNSPod）
- Cloudflare

### 5.2 配置 DNS 解析

在域名管理后台添加 A 记录：

| 主机记录 | 记录类型 | 记录值 | TTL |
|---------|---------|--------|-----|
| @ | A | 你的服务器IP | 600 |
| www | A | 你的服务器IP | 600 |

### 5.3 配置 Nginx 和 SSL（Let's Encrypt）

```bash
# 安装 Certbot
apt-get install -y certbot python3-certbot-nginx

# 停止 Nginx（释放 80 端口）
docker-compose -f docker-compose.prod.yml stop frontend

# 申请证书
certbot certonly --standalone -d your-domain.com -d www.your-domain.com

# 创建证书目录并复制证书
mkdir -p ./nginx/ssl
cp /etc/letsencrypt/live/your-domain.com/fullchain.pem ./nginx/ssl/cert.crt
cp /etc/letsencrypt/live/your-domain.com/privkey.pem ./nginx/ssl/cert.key

# 修改 nginx.conf 启用 HTTPS（参考 nginx-ssl.conf）
```

### 5.4 自动续期证书

添加定时任务：

```bash
crontab -e

# 添加以下行（每月 1 号凌晨 3 点检查续期）
0 3 1 * * /usr/bin/certbot renew --quiet --deploy-hook "docker-compose -f /opt/rag-qa-system/docker-compose.prod.yml restart frontend"
```

## 六、日常运维

### 6.1 常用命令

```bash
# 查看容器状态
docker-compose -f docker-compose.prod.yml ps

# 查看日志
docker-compose -f docker-compose.prod.yml logs -f
docker-compose -f docker-compose.prod.yml logs -f backend
docker-compose -f docker-compose.prod.yml logs -f frontend

# 重启服务
docker-compose -f docker-compose.prod.yml restart

# 停止服务
docker-compose -f docker-compose.prod.yml down

# 停止并删除数据卷（危险！会删除所有数据）
docker-compose -f docker-compose.prod.yml down -v
```

### 6.2 备份数据

```bash
#!/bin/bash
# backup.sh - 数据库备份脚本

BACKUP_DIR="/opt/backups"
DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_FILE="rag_qa_backup_${DATE}.sql"

# 创建备份目录
mkdir -p ${BACKUP_DIR}

# 执行备份
docker exec rag-postgres pg_dump -U postgres rag_qa_system > ${BACKUP_DIR}/${BACKUP_FILE}

# 压缩备份文件
gzip ${BACKUP_DIR}/${BACKUP_FILE}

# 删除 30 天前的备份
find ${BACKUP_DIR} -name "rag_qa_backup_*.sql.gz" -mtime +30 -delete

echo "备份完成: ${BACKUP_DIR}/${BACKUP_FILE}.gz"
```

添加定时任务：

```bash
# 每天凌晨 2 点执行备份
0 2 * * * /opt/rag-qa-system/backup.sh >> /var/log/rag-backup.log 2>&1
```

### 6.3 恢复数据

```bash
# 解压备份文件
gunzip rag_qa_backup_20240101_020000.sql.gz

# 复制到容器内
docker cp rag_qa_backup_20240101_020000.sql rag-postgres:/tmp/

# 进入容器执行恢复
docker exec -it rag-postgres psql -U postgres -d rag_qa_system -f /tmp/rag_qa_backup_20240101_020000.sql
```

## 七、故障排查

### 7.1 常见问题

**1. 容器无法启动**

```bash
# 查看详细错误信息
docker-compose -f docker-compose.prod.yml logs service-name

# 检查端口占用
netstat -tlnp | grep 80
netstat -tlnp | grep 443
```

**2. 数据库连接失败**

```bash
# 检查数据库容器状态
docker-compose -f docker-compose.prod.yml ps postgres

# 进入数据库查看
docker exec -it rag-postgres psql -U postgres -d rag_qa_system
```

**3. 前端无法访问后端 API**

检查浏览器开发者工具 Network 标签：
- 确认请求地址正确
- 检查 CORS 错误
- 确认 Nginx 代理配置正确

### 7.2 性能优化

**1. 增加 JVM 内存**

修改 `docker-compose.prod.yml` 中 backend 的环境变量：
```yaml
environment:
  JAVA_OPTS: "-Xms1g -Xmx4g"
```

**2. 数据库优化**

```sql
-- 连接数和缓冲区优化
ALTER SYSTEM SET max_connections = '200';
ALTER SYSTEM SET shared_buffers = '1GB';
ALTER SYSTEM SET effective_cache_size = '3GB';
```

**3. Nginx 性能调优**

```nginx
# 在 nginx.conf 中添加
worker_processes auto;
worker_connections 4096;

# 开启高效文件传输
sendfile on;
tcp_nopush on;
tcp_nodelay on;
```

## 八、安全建议

### 8.1 必须完成的安全措施

1. **修改所有默认密码**（数据库、JWT Secret）
2. **配置防火墙**，只开放必要的端口（80、443、22）
3. **启用 HTTPS**，使用 SSL 证书
4. **禁用 root 登录**，使用普通用户 + sudo
5. **定期更新系统和依赖包**

### 8.2 安全配置示例

**SSH 安全加固** `/etc/ssh/sshd_config`：
```
# 禁用 root 登录
PermitRootLogin no

# 禁用密码认证，使用密钥
PasswordAuthentication no
PubkeyAuthentication yes

# 修改默认端口（可选）
Port 2222
```

**UFW 防火墙配置**：
```bash
# 默认拒绝所有入站
ufw default deny incoming

# 允许所有出站
ufw default allow outgoing

# 允许 SSH（如果修改了端口，使用新端口）
ufw allow 22/tcp

# 允许 HTTP 和 HTTPS
ufw allow 80/tcp
ufw allow 443/tcp

# 启用防火墙
ufw enable
```

---

**恭喜！** 按照以上步骤，你就可以成功将 RAG QA System 部署到云服务器，使其成为一个可通过域名访问的真实网站。

如果在部署过程中遇到问题，请查看日志信息或参考故障排查章节。
