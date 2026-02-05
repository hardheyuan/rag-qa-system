import { ref, readonly } from 'vue'

/**
 * 全局 Toast 通知系统
 * 
 * 提供统一的通知显示功能，支持成功、错误、警告和信息类型
 * 通知会在指定时间后自动消失
 */

// 全局状态 - 在所有组件间共享
const toasts = ref([])
let toastId = 0

/**
 * Toast 类型枚举
 */
export const ToastType = {
  SUCCESS: 'success',
  ERROR: 'error',
  WARNING: 'warning',
  INFO: 'info'
}

/**
 * 默认配置
 */
const DEFAULT_DURATION = 3000 // 3秒后自动消失
const MAX_TOASTS = 5 // 最多同时显示5个通知

/**
 * 添加一个 Toast 通知
 * @param {Object} options Toast 配置
 * @param {string} options.message 消息内容
 * @param {string} options.type Toast 类型 (success/error/warning/info)
 * @param {number} options.duration 显示时长（毫秒），0 表示不自动消失
 * @returns {number} Toast ID
 */
function addToast({ message, type = ToastType.INFO, duration = DEFAULT_DURATION }) {
  const id = ++toastId
  
  const toast = {
    id,
    message,
    type,
    show: true
  }
  
  // 限制最大数量，移除最早的通知
  if (toasts.value.length >= MAX_TOASTS) {
    toasts.value.shift()
  }
  
  toasts.value.push(toast)
  
  // 自动消失
  if (duration > 0) {
    setTimeout(() => {
      removeToast(id)
    }, duration)
  }
  
  return id
}

/**
 * 移除指定的 Toast
 * @param {number} id Toast ID
 */
function removeToast(id) {
  const index = toasts.value.findIndex(t => t.id === id)
  if (index !== -1) {
    toasts.value.splice(index, 1)
  }
}

/**
 * 清除所有 Toast
 */
function clearAllToasts() {
  toasts.value = []
}

/**
 * 显示成功通知
 * @param {string} message 消息内容
 * @param {number} duration 显示时长
 * @returns {number} Toast ID
 */
function success(message, duration = DEFAULT_DURATION) {
  return addToast({ message, type: ToastType.SUCCESS, duration })
}

/**
 * 显示错误通知
 * @param {string} message 消息内容
 * @param {number} duration 显示时长
 * @returns {number} Toast ID
 */
function error(message, duration = DEFAULT_DURATION) {
  return addToast({ message, type: ToastType.ERROR, duration })
}

/**
 * 显示警告通知
 * @param {string} message 消息内容
 * @param {number} duration 显示时长
 * @returns {number} Toast ID
 */
function warning(message, duration = DEFAULT_DURATION) {
  return addToast({ message, type: ToastType.WARNING, duration })
}

/**
 * 显示信息通知
 * @param {string} message 消息内容
 * @param {number} duration 显示时长
 * @returns {number} Toast ID
 */
function info(message, duration = DEFAULT_DURATION) {
  return addToast({ message, type: ToastType.INFO, duration })
}

/**
 * useToast composable
 * 
 * 使用示例:
 * ```javascript
 * import { useToast } from '@/composables/useToast'
 * 
 * const toast = useToast()
 * 
 * // 显示成功通知
 * toast.success('操作成功')
 * 
 * // 显示错误通知
 * toast.error('操作失败')
 * 
 * // 在模板中使用
 * <ToastContainer />
 * ```
 */
export function useToast() {
  return {
    toasts: readonly(toasts),
    addToast,
    removeToast,
    clearAllToasts,
    success,
    error,
    warning,
    info
  }
}

// 导出单例方法，供非组件代码使用（如 API 拦截器）
export const toast = {
  success,
  error,
  warning,
  info,
  addToast,
  removeToast,
  clearAllToasts
}

export default useToast
