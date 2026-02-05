import { toast } from '@/composables/useToast'

/**
 * 全局错误处理工具
 * 
 * 提供统一的 API 错误处理和用户友好的中文错误消息
 * 
 * 错误响应格式（来自后端）:
 * {
 *   "code": 400,
 *   "message": "目标用户不是学生角色",
 *   "data": null,
 *   "timestamp": "2024-01-15T10:30:00"
 * }
 */

/**
 * HTTP 状态码对应的默认中文错误消息
 */
const HTTP_ERROR_MESSAGES = {
  400: '请求参数错误',
  401: '登录已过期，请重新登录',
  403: '无权限执行此操作',
  404: '请求的资源不存在',
  405: '请求方法不允许',
  408: '请求超时，请稍后重试',
  409: '数据冲突，请刷新后重试',
  413: '上传文件过大',
  415: '不支持的文件格式',
  422: '请求数据验证失败',
  429: '请求过于频繁，请稍后重试',
  500: '服务器内部错误，请稍后重试',
  502: '网关错误，请稍后重试',
  503: '服务暂时不可用，请稍后重试',
  504: '网关超时，请稍后重试'
}

/**
 * 业务错误码对应的中文错误消息
 * 这些是后端返回的业务错误码
 */
const BUSINESS_ERROR_MESSAGES = {
  // 通用错误
  'VALIDATION_ERROR': '数据验证失败',
  'UNAUTHORIZED': '未授权访问',
  'FORBIDDEN': '无权限访问',
  'NOT_FOUND': '资源不存在',
  
  // 教师学生管理相关错误
  'USER_NOT_FOUND': '用户不存在',
  'STUDENT_NOT_FOUND': '学生不存在',
  'ASSOCIATION_NOT_FOUND': '关联不存在',
  'NOT_STUDENT_ROLE': '目标用户不是学生角色',
  'DUPLICATE_ASSOCIATION': '该学生已在您的班级中',
  'INVALID_FILE_FORMAT': '文件格式不正确',
  'FILE_PARSE_ERROR': '文件解析失败'
}

/**
 * 特定 API 路径的错误消息映射
 * 用于提供更具体的错误提示
 */
const API_ERROR_MESSAGES = {
  '/teacher/students': {
    403: '无权限访问该学生',
    404: '学生不存在'
  },
  '/teacher/students/batch': {
    400: '文件格式不正确或内容无效'
  },
  '/auth/login': {
    401: '用户名或密码错误'
  },
  '/auth/register': {
    409: '用户名或邮箱已存在'
  }
}

/**
 * 从错误响应中提取错误消息
 * @param {Error} error Axios 错误对象
 * @returns {string} 用户友好的中文错误消息
 */
export function getErrorMessage(error) {
  // 网络错误（无响应）
  if (!error.response) {
    if (error.code === 'ECONNABORTED') {
      return '请求超时，请检查网络连接'
    }
    if (error.message === 'Network Error') {
      return '网络连接失败，请检查网络设置'
    }
    return '网络错误，请稍后重试'
  }

  const { status, data, config } = error.response
  const requestUrl = config?.url || ''

  // 1. 优先使用后端返回的错误消息（如果是中文）
  if (data?.message && isChinese(data.message)) {
    return data.message
  }

  // 2. 检查是否有特定 API 路径的错误消息
  for (const [path, messages] of Object.entries(API_ERROR_MESSAGES)) {
    if (requestUrl.includes(path) && messages[status]) {
      return messages[status]
    }
  }

  // 3. 检查业务错误码
  if (data?.code && BUSINESS_ERROR_MESSAGES[data.code]) {
    return BUSINESS_ERROR_MESSAGES[data.code]
  }

  // 4. 使用 HTTP 状态码对应的默认消息
  if (HTTP_ERROR_MESSAGES[status]) {
    return HTTP_ERROR_MESSAGES[status]
  }

  // 5. 后端返回的非中文消息，尝试翻译
  if (data?.message) {
    return translateErrorMessage(data.message)
  }

  // 6. 默认错误消息
  return '操作失败，请稍后重试'
}

/**
 * 检查字符串是否包含中文字符
 * @param {string} str 要检查的字符串
 * @returns {boolean} 是否包含中文
 */
function isChinese(str) {
  return /[\u4e00-\u9fa5]/.test(str)
}

/**
 * 翻译常见的英文错误消息为中文
 * @param {string} message 英文错误消息
 * @returns {string} 中文错误消息
 */
function translateErrorMessage(message) {
  const translations = {
    'User not found': '用户不存在',
    'Student not found': '学生不存在',
    'Association not found': '关联不存在',
    'Target user is not a student': '目标用户不是学生角色',
    'Student already in your class': '该学生已在您的班级中',
    'Invalid file format': '文件格式不正确',
    'File parse error': '文件解析失败',
    'Access denied': '无权限访问',
    'Unauthorized': '未授权访问',
    'Forbidden': '无权限执行此操作',
    'Bad request': '请求参数错误',
    'Internal server error': '服务器内部错误',
    'Validation failed': '数据验证失败'
  }

  // 尝试精确匹配
  if (translations[message]) {
    return translations[message]
  }

  // 尝试部分匹配
  for (const [eng, chn] of Object.entries(translations)) {
    if (message.toLowerCase().includes(eng.toLowerCase())) {
      return chn
    }
  }

  return message
}

/**
 * 处理 API 错误并显示 Toast 通知
 * @param {Error} error Axios 错误对象
 * @param {Object} options 配置选项
 * @param {boolean} options.showToast 是否显示 Toast 通知，默认 true
 * @param {string} options.defaultMessage 默认错误消息
 * @returns {string} 错误消息
 */
export function handleApiError(error, options = {}) {
  const { showToast = true, defaultMessage } = options
  
  const message = defaultMessage || getErrorMessage(error)
  
  if (showToast) {
    toast.error(message)
  }
  
  // 记录错误日志（开发环境）
  if (import.meta.env.DEV) {
    console.error('API Error:', {
      url: error.config?.url,
      method: error.config?.method,
      status: error.response?.status,
      message: error.response?.data?.message,
      displayMessage: message
    })
  }
  
  return message
}

/**
 * 显示成功通知
 * @param {string} message 成功消息
 */
export function showSuccess(message) {
  toast.success(message)
}

/**
 * 显示错误通知
 * @param {string} message 错误消息
 */
export function showError(message) {
  toast.error(message)
}

/**
 * 显示警告通知
 * @param {string} message 警告消息
 */
export function showWarning(message) {
  toast.warning(message)
}

/**
 * 显示信息通知
 * @param {string} message 信息消息
 */
export function showInfo(message) {
  toast.info(message)
}

export default {
  getErrorMessage,
  handleApiError,
  showSuccess,
  showError,
  showWarning,
  showInfo
}
