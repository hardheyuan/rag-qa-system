#!/bin/bash
# =============================================================================
# RAG QA System - 本地部署脚本（打包 + 上传）
# =============================================================================
# 此脚本在本地开发机执行，负责：
# 1. 清理临时文件
# 2. 打包项目（排除 node_modules、.git、target 等）
# 3. 上传到远程服务器
# 4. 触发远程部署
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
# 配置变量
# =============================================================================

# 远程服务器配置（可以通过环境变量或命令行参数传入）
REMOTE_HOST="${REMOTE_HOST:-}"      # 例如：1.2.3.4 或 your-domain.com
REMOTE_USER="${REMOTE_USER:-root}"  # 登录用户名
REMOTE_PORT="${REMOTE_PORT:-22}"    # SSH 端口
REMOTE_DIR="${REMOTE_DIR:-/opt/rag-qa-system}"  # 远程部署目录

# 本地打包配置
ARCHIVE_NAME="rag-qa-system-deploy.tar.gz"
EXCLUDE_PATTERNS=(
    "node_modules"
    ".git"
    "target"
    "*.log"
    ".idea"
    ".vscode"
    "*.iml"
    ".DS_Store"
    "Thumbs.db"
    "uploads/*"
    "demo1/uploads/*"
)

# =============================================================================
# 使用帮助
# =============================================================================
show_help() {
    cat << EOF
RAG QA System 本地部署脚本

用法: $0 [选项]

选项:
    -h, --help          显示此帮助信息
    -H, --host          远程服务器地址 (必需)
    -u, --user          远程登录用户名 (默认: root)
    -p, --port          SSH 端口 (默认: 22)
    -d, --dir           远程部署目录 (默认: /opt/rag-qa-system)
    -s, --skip-build    跳过打包步骤，直接上传现有压缩包
    -c, --clean         部署前清理远程服务器上的旧版本

环境变量:
    REMOTE_HOST         远程服务器地址
    REMOTE_USER         远程登录用户名
    REMOTE_PORT         SSH 端口
    REMOTE_DIR          远程部署目录

示例:
    # 基本用法
    $0 -H 1.2.3.4 -u root

    # 使用域名和自定义目录
    $0 -H your-domain.com -u ubuntu -d /var/www/rag-qa

    # 跳过打包，直接上传
    $0 -H 1.2.3.4 -s

EOF
}

# =============================================================================
# 解析命令行参数
# =============================================================================
parse_args() {
    while [[ $# -gt 0 ]]; do
        case $1 in
            -h|--help)
                show_help
                exit 0
                ;;
            -H|--host)
                REMOTE_HOST="$2"
                shift 2
                ;;
            -u|--user)
                REMOTE_USER="$2"
                shift 2
                ;;
            -p|--port)
                REMOTE_PORT="$2"
                shift 2
                ;;
            -d|--dir)
                REMOTE_DIR="$2"
                shift 2
                ;;
            -s|--skip-build)
                SKIP_BUILD=true
                shift
                ;;
            -c|--clean)
                CLEAN_REMOTE=true
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
# 验证配置
# =============================================================================
validate_config() {
    log_step "验证配置"

    # 检查必需参数
    if [[ -z "$REMOTE_HOST" ]]; then
        log_error "未指定远程服务器地址"
        log_info "请使用 -H 参数或设置 REMOTE_HOST 环境变量"
        exit 1
    fi

    # 检查本地是否在项目根目录
    if [[ ! -f "docker-compose.prod.yml" ]]; then
        log_error "当前目录不是项目根目录"
        log_info "请切换到项目根目录后再运行此脚本"
        exit 1
    fi

    # 检查 SSH 连接
    log_info "测试 SSH 连接到 ${REMOTE_USER}@${REMOTE_HOST}:${REMOTE_PORT}..."
    if ! ssh -o ConnectTimeout=5 -o StrictHostKeyChecking=no -p "${REMOTE_PORT}" "${REMOTE_USER}@${REMOTE_HOST}" "echo 'SSH connection successful'" > /dev/null 2>&1; then
        log_error "无法连接到远程服务器"
        log_info "请检查："
        log_info "  1. 服务器地址、端口、用户名是否正确"
        log_info "  2. SSH 服务是否已启动"
        log_info "  3. 防火墙是否允许 SSH 连接"
        log_info "  4. 是否已配置 SSH 密钥或密码"
        exit 1
    fi

    log_success "配置验证通过"
}

# =============================================================================
# 清理本地临时文件
# =============================================================================
cleanup_local() {
    log_step "清理本地临时文件"

    # 删除旧的压缩包
    if [[ -f "$ARCHIVE_NAME" ]]; then
        log_info "删除旧的压缩包: $ARCHIVE_NAME"
        rm -f "$ARCHIVE_NAME"
    fi

    # 删除前端 node_modules（减少包大小）
    if [[ -d "frontend/node_modules" ]]; then
        log_info "删除前端 node_modules"
        rm -rf frontend/node_modules
    fi

    # 删除后端 target 目录
    if [[ -d "demo1/target" ]]; then
        log_info "删除后端 target 目录"
        rm -rf demo1/target
    fi

    log_success "本地清理完成"
}

# =============================================================================
# 打包项目
# =============================================================================
build_package() {
    log_step "打包项目"

    # 构建排除参数
    local exclude_args=""
    for pattern in "${EXCLUDE_PATTERNS[@]}"; do
        exclude_args="$exclude_args --exclude=$pattern"
    done

    # 创建压缩包
    log_info "正在创建压缩包: $ARCHIVE_NAME"
    log_info "排除的目录: ${EXCLUDE_PATTERNS[*]}"

    # 使用 tar 命令创建压缩包
    tar -czf "$ARCHIVE_NAME" $exclude_args \
        --exclude="*.tar.gz" \
        .

    # 检查压缩包是否创建成功
    if [[ ! -f "$ARCHIVE_NAME" ]]; then
        log_error "压缩包创建失败"
        exit 1
    fi

    # 显示压缩包信息
    local size=$(du -h "$ARCHIVE_NAME" | cut -f1)
    log_success "压缩包创建完成: $ARCHIVE_NAME ($size)"
}

# =============================================================================
# 上传文件到远程服务器
# =============================================================================
upload_to_remote() {
    log_step "上传文件到远程服务器"

    local remote_host="${REMOTE_USER}@${REMOTE_HOST}"
    local ssh_opts="-o StrictHostKeyChecking=no -o ConnectTimeout=30"

    # 创建远程目录
    log_info "创建远程目录: ${REMOTE_DIR}"
    ssh ${ssh_opts} -p "${REMOTE_PORT}" "${remote_host}" "mkdir -p ${REMOTE_DIR}"

    # 如果需要清理远程目录
    if [[ "$CLEAN_REMOTE" == "true" ]]; then
        log_warning "清理远程目录: ${REMOTE_DIR}"
        ssh ${ssh_opts} -p "${REMOTE_PORT}" "${remote_host}" "cd ${REMOTE_DIR} && rm -rf *"
    fi

    # 上传压缩包
    log_info "上传压缩包到服务器..."
    scp ${ssh_opts} -P "${REMOTE_PORT}" "$ARCHIVE_NAME" "${remote_host}:${REMOTE_DIR}/"

    log_success "文件上传完成"
}

# =============================================================================
# 在远程服务器上解压和部署
# =============================================================================
deploy_on_remote() {
    log_step "在远程服务器上部署"

    local remote_host="${REMOTE_USER}@${REMOTE_HOST}"
    local ssh_opts="-o StrictHostKeyChecking=no -o ConnectTimeout=30"

    # 在远程服务器上执行部署命令
    log_info "在远程服务器上解压和部署..."

    ssh ${ssh_opts} -p "${REMOTE_PORT}" "${remote_host}" << EOF
        set -e

        echo "=== 进入部署目录 ==="
        cd ${REMOTE_DIR}

        echo "=== 解压项目文件 ==="
        tar -xzf ${ARCHIVE_NAME}

        echo "=== 删除压缩包 ==="
        rm -f ${ARCHIVE_NAME}

        echo "=== 检查 .env 文件 ==="
        if [ ! -f ".env" ]; then
            if [ -f ".env.example" ]; then
                echo "从 .env.example 创建 .env"
                cp .env.example .env
                echo "警告: 请编辑 .env 文件填入实际配置"
                exit 1
            fi
        fi

        echo "=== 执行部署脚本 ==="
        chmod +x deploy.sh
        ./deploy.sh
EOF

    if [ $? -eq 0 ]; then
        log_success "远程部署完成"
    else
        log_error "远程部署失败"
        exit 1
    fi
}

# =============================================================================
# 显示部署信息
# =============================================================================
show_info() {
    echo ""
    echo "================================================================================"
    echo "                     RAG QA System 部署完成"
    echo "================================================================================"
    echo ""
    echo "  🌐 服务器地址: ${REMOTE_HOST}"
    echo "  📂 部署目录: ${REMOTE_DIR}"
    echo ""
    echo "  访问地址:"
    echo "    - 网站首页: http://${REMOTE_HOST}"
    echo "    - API 文档: http://${REMOTE_HOST}/api/system/health"
    echo ""
    echo "  📋 远程服务器常用命令:"
    echo "    ssh ${REMOTE_USER}@${REMOTE_HOST}"
    echo "    cd ${REMOTE_DIR}"
    echo "    docker-compose -f docker-compose.prod.yml logs -f"
    echo "    docker-compose -f docker-compose.prod.yml restart"
    echo ""
    echo "================================================================================"
    echo ""

    log_success "部署成功！"
    log_info "如有问题，请查看服务器日志："
    log_info "  ssh ${REMOTE_USER}@${REMOTE_HOST} 'cd ${REMOTE_DIR} && docker-compose -f docker-compose.prod.yml logs -f'"
}

# =============================================================================
# 主函数
# =============================================================================
main() {
    echo "================================================================================"
    echo "              RAG QA System - 本地部署脚本"
    echo "================================================================================"
    echo ""
    echo "  此脚本将自动完成以下操作："
    echo "    1. 清理本地临时文件"
    echo "    2. 打包项目（自动排除 node_modules、.git、target 等）"
    echo "    3. 上传压缩包到远程服务器"
    echo "    4. 在服务器上解压并执行部署"
    echo ""
    echo "================================================================================"
    echo ""

    # 解析命令行参数
    parse_args "$@"

    # 验证配置
    validate_config

    # 询问是否继续
    if [[ "$SKIP_CONFIRM" != "true" ]]; then
        echo ""
        read -p "确认开始部署到 ${REMOTE_HOST}? (y/N): " choice
        if [[ ! $choice =~ ^[Yy]$ ]]; then
            log_info "部署已取消"
            exit 0
        fi
    fi

    # 执行部署步骤
    if [[ "$SKIP_BUILD" != "true" ]]; then
        cleanup_local
        build_package
    else
        log_info "跳过打包步骤，使用现有压缩包: $ARCHIVE_NAME"
        if [[ ! -f "$ARCHIVE_NAME" ]]; then
            log_error "压缩包不存在: $ARCHIVE_NAME"
            exit 1
        fi
    fi

    upload_to_remote
    deploy_on_remote
    show_info

    log_success "全部完成！"
}

# =============================================================================
# 解析命令行参数（覆盖之前的定义，添加更多选项）
# =============================================================================
parse_args() {
    SKIP_CONFIRM=false

    while [[ $# -gt 0 ]]; do
        case $1 in
            -h|--help)
                show_help
                exit 0
                ;;
            -H|--host)
                REMOTE_HOST="$2"
                shift 2
                ;;
            -u|--user)
                REMOTE_USER="$2"
                shift 2
                ;;
            -p|--port)
                REMOTE_PORT="$2"
                shift 2
                ;;
            -d|--dir)
                REMOTE_DIR="$2"
                shift 2
                ;;
            -s|--skip-build)
                SKIP_BUILD=true
                shift
                ;;
            -c|--clean)
                CLEAN_REMOTE=true
                shift
                ;;
            -y|--yes)
                SKIP_CONFIRM=true
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

# 更新帮助信息
show_help() {
    cat << EOF
RAG QA System 本地部署脚本

用法: $0 [选项]

选项:
    -h, --help          显示此帮助信息
    -H, --host          远程服务器地址 (必需)
    -u, --user          远程登录用户名 (默认: root)
    -p, --port          SSH 端口 (默认: 22)
    -d, --dir           远程部署目录 (默认: /opt/rag-qa-system)
    -s, --skip-build    跳过打包步骤，使用现有压缩包
    -c, --clean         部署前清理远程目录
    -y, --yes           跳过确认提示，直接部署

环境变量:
    REMOTE_HOST         远程服务器地址
    REMOTE_USER         远程登录用户名
    REMOTE_PORT         SSH 端口
    REMOTE_DIR          远程部署目录

示例:
    # 基本用法
    $0 -H 1.2.3.4 -u root

    # 使用域名和自定义目录
    $0 -H your-domain.com -u ubuntu -d /var/www/rag-qa

    # 跳过确认，自动部署
    $0 -H 1.2.3.4 -y

EOF
}

# 捕获 Ctrl+C
trap 'log_warning "部署被中断"; exit 1' INT

# 执行主函数
main "$@"
