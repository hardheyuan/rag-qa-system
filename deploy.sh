#!/bin/bash
# =============================================================================
# RAG QA System - 服务器端部署脚本
# =============================================================================
# 此脚本在远程服务器上执行，负责：
# 1. 检查环境和配置
# 2. 部署 Docker 服务
# 3. 检查服务健康状态
# =============================================================================

set -e  # 遇到错误立即退出

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# =============================================================================
# 日志输出函数
# =============================================================================
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

log_step() {
    echo ""
    echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo -e "${BLUE}  $1${NC}"
    echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo ""
}

# =============================================================================
# 检查环境
# =============================================================================
check_environment() {
    log_step "检查部署环境"

    # 检查 Docker
    if ! command -v docker &> /dev/null; then
        log_error "Docker 未安装，请先安装 Docker"
        log_info "可以参考 DEPLOY.md 中的安装指南"
        exit 1
    fi

    # 检查 Docker Compose
    if ! command -v docker-compose &> /dev/null && ! docker compose version &> /dev/null; then
        log_error "Docker Compose 未安装，请先安装 Docker Compose"
        exit 1
    fi

    # 检查 .env 文件
    if [ ! -f ".env" ]; then
        log_warning ".env 文件不存在"
        if [ -f ".env.example" ]; then
            log_info "正在从 .env.example 创建 .env 文件..."
            cp .env.example .env
            log_warning "请编辑 .env 文件，填入实际的配置值后再运行部署脚本"
            log_info "需要配置的主要项："
            log_info "  - POSTGRES_PASSWORD: 数据库密码"
            log_info "  - MODELSCOPE_API_KEY: ModelScope API 密钥"
            log_info "  - JWT_SECRET: JWT 签名密钥"
            exit 1
        else
            log_error ".env.example 文件也不存在"
            exit 1
        fi
    fi

    # 检查 .env 文件中的占位符
    if grep -q "your_" .env; then
        log_warning ".env 文件中包含未修改的占位符值"
        log_info "请编辑 .env 文件，将 'your_xxx' 替换为实际值"
        exit 1
    fi

    # 检查 docker-compose.prod.yml 是否存在
    if [ ! -f "docker-compose.prod.yml" ]; then
        log_error "docker-compose.prod.yml 文件不存在"
        exit 1
    fi

    log_success "环境检查通过"
}

# =============================================================================
# 加载环境变量
# =============================================================================
load_env() {
    log_step "加载环境变量"

    # 加载 .env 文件
    set -a
    source .env
    set +a

    log_success "环境变量加载完成"
}

# =============================================================================
# 清理旧容器和镜像
# =============================================================================
cleanup() {
    log_step "清理旧容器和镜像"

    # 停止并删除相关容器
    if docker-compose -f docker-compose.prod.yml ps -q &> /dev/null; then
        log_info "停止现有容器..."
        docker-compose -f docker-compose.prod.yml down --remove-orphans 2>/dev/null || true
    fi

    # 删除 dangling 镜像
    log_info "清理 dangling 镜像..."
    docker image prune -f > /dev/null 2>&1 || true

    log_success "清理完成"
}

# =============================================================================
# 构建和启动服务
# =============================================================================
deploy() {
    log_step "构建和启动服务"

    log_info "开始构建 Docker 镜像并启动服务..."
    log_info "这可能需要 5-15 分钟，请耐心等待..."
    echo ""

    # 构建并启动
    if ! docker-compose -f docker-compose.prod.yml up --build -d; then
        log_error "部署失败"
        log_info "查看错误日志："
        docker-compose -f docker-compose.prod.yml logs --tail=100
        exit 1
    fi

    log_success "服务启动成功"
}

# =============================================================================
# 检查服务健康状态
# =============================================================================
check_health() {
    log_step "检查服务健康状态"

    local max_attempts=30
    local attempt=1
    local all_healthy=false

    log_info "等待服务启动（最多等待 5 分钟）..."
    echo ""

    while [ $attempt -le $max_attempts ]; do
        # 检查后端健康
        local backend_status=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/api/system/health 2>/dev/null || echo "000")

        # 检查前端
        local frontend_status=$(curl -s -o /dev/null -w "%{http_code}" http://localhost 2>/dev/null || echo "000")

        # 显示进度
        printf "\r  尝试 %2d/%d - 后端: %s, 前端: %s" "$attempt" "$max_attempts" "$backend_status" "$frontend_status"

        # 检查是否都正常
        if [[ "$backend_status" == "200" && "$frontend_status" == "200" ]]; then
            all_healthy=true
            break
        fi

        attempt=$((attempt + 1))
        sleep 10
    done

    echo ""  # 换行
    echo ""

    if [[ "$all_healthy" == "true" ]]; then
        log_success "所有服务运行正常！"
        return 0
    else
        log_warning "服务启动超时或异常"
        log_info "查看详细日志："
        log_info "  docker-compose -f docker-compose.prod.yml logs -f"
        return 1
    fi
}

# =============================================================================
# 显示部署信息
# =============================================================================
show_info() {
    # 获取服务器 IP 或域名
    local server_ip=$(curl -s ifconfig.me 2>/dev/null || echo "your-server-ip")

    echo ""
    echo "================================================================================"
    echo "                     RAG QA System 部署完成"
    echo "================================================================================"
    echo ""
    echo "  🌐 访问地址:"
    echo "     - 网站首页: http://${server_ip}"
    echo "     - API 地址: http://${server_ip}/api/system/health"
    echo ""
    echo "  📋 常用命令:"
    echo "     查看日志:  docker-compose -f docker-compose.prod.yml logs -f"
    echo "     停止服务:  docker-compose -f docker-compose.prod.yml down"
    echo "     重启服务:  docker-compose -f docker-compose.prod.yml restart"
    echo ""
    echo "  📁 数据持久化:"
    echo "     数据库数据: postgres_data (Docker Volume)"
    echo "     上传文件:  backend_uploads (Docker Volume)"
    echo ""
    echo "================================================================================"
    echo ""
}

# =============================================================================
# 使用帮助
# =============================================================================
show_help() {
    cat << EOF
RAG QA System 服务器端部署脚本

用法: $0 [选项]

选项:
    -h, --help       显示此帮助信息
    --cleanup-only   仅执行清理，不部署
    --no-cleanup     跳过清理步骤
    --skip-health    跳过健康检查

示例:
    # 标准部署
    $0

    # 仅清理旧容器
    $0 --cleanup-only

    # 保留旧数据，直接部署
    $0 --no-cleanup

EOF
}

# =============================================================================
# 解析命令行参数（服务器端）
# =============================================================================
parse_server_args() {
    CLEANUP_ONLY=false
    NO_CLEANUP=false
    SKIP_HEALTH=false

    while [[ $# -gt 0 ]]; do
        case $1 in
            -h|--help)
                show_help
                exit 0
                ;;
            --cleanup-only)
                CLEANUP_ONLY=true
                shift
                ;;
            --no-cleanup)
                NO_CLEANUP=true
                shift
                ;;
            --skip-health)
                SKIP_HEALTH=true
                shift
                ;;
            *)
                log_error "未知参数: $1"
                show_help
                exit 1
                ;;
        esac
    done
}

# =============================================================================
# 服务器端主函数
# =============================================================================
main_server() {
    # 解析命令行参数
    parse_server_args "$@"

    # 如果只执行清理
    if [[ "$CLEANUP_ONLY" == "true" ]]; then
        log_step "仅执行清理"
        check_environment
        cleanup
        log_success "清理完成"
        exit 0
    fi

    # 标准部署流程
    echo "================================================================================"
    echo "              RAG QA System - 服务器端部署脚本"
    echo "================================================================================"
    echo ""

    check_environment
    load_env

    if [[ "$NO_CLEANUP" != "true" ]]; then
        cleanup
    fi

    deploy

    if [[ "$SKIP_HEALTH" != "true" ]]; then
        check_health
    fi

    show_info

    log_success "部署完成！"
}

# 捕获 Ctrl+C
trap 'log_warning "部署被中断"; exit 1' INT

# 判断是本地部署还是服务器端部署
# 如果是从本地通过 SSH 触发，环境变量 REMOTE_MODE 会被设置
if [[ "$REMOTE_MODE" == "server" ]]; then
    # 服务器端部署模式
    main_server "$@"
else
    # 本地部署模式（默认）
    main "$@"
fi
