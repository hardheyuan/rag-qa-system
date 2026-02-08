import api from './axios'
import { useUserStore } from '@/stores/user'
import router from '@/router'
import { handleApiError, getErrorMessage } from '@/utils/errorHandler'

// 用于标记是否正在刷新Token，防止重复请求
let isRefreshing = false
// 存储等待刷新完成的请求队列
let refreshSubscribers = []

/**
 * 需要静默处理的错误（不显示全局 Toast）
 * 这些错误由各组件自行处理
 */
const SILENT_ERROR_URLS = [
  '/teacher/students', // 教师学生管理相关 API 由组件处理
  '/teacher/feedback', // 教师反馈相关 API 由组件处理
  '/student/feedback', // 学生反馈相关 API 由组件处理
  '/auth/login',       // 登录错误由登录页面处理
  '/auth/register'     // 注册错误由注册页面处理
]

/**
 * 检查是否应该静默处理错误
 * @param {string} url 请求 URL
 * @returns {boolean} 是否静默处理
 */
function shouldSilentError(url) {
  return SILENT_ERROR_URLS.some(path => url?.includes(path))
}

/**
 * 将等待的请求加入队列
 */
function subscribeTokenRefresh(callback) {
  refreshSubscribers.push(callback)
}

/**
 * 刷新完成后，执行队列中的所有请求
 */
function onTokenRefreshed(newToken, error = null) {
  refreshSubscribers.forEach(({ resolve, reject }) => {
    if (error) {
      reject(error)
      return
    }
    resolve(newToken)
  })
  refreshSubscribers = []
}

/**
 * 请求拦截器
 * 
 * 在每个请求发送前自动附加 Access Token
 */
api.interceptors.request.use(
  (config) => {
    const userStore = useUserStore()
    const token = userStore.accessToken
    
    // 如果有token，添加到请求头
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

/**
 * 响应拦截器
 * 
 * 处理Token过期情况，自动使用Refresh Token换取新Token
 * 处理常见的 HTTP 错误码，显示友好的中文错误消息
 */
api.interceptors.response.use(
  (response) => {
    // 正常响应，直接返回
    return response
  },
  async (error) => {
    const originalRequest = error.config

    const requestUrl = originalRequest?.url || ''
    const isAuthRequest = requestUrl.startsWith('/auth/')
    const isLogoutRequest = requestUrl === '/auth/logout'
    
    // 获取响应状态码
    const status = error.response?.status
    
    // 处理网络错误（无响应）
    if (!error.response) {
      // 只对非静默 URL 显示全局错误
      if (!shouldSilentError(requestUrl)) {
        handleApiError(error, { showToast: true })
      }
      return Promise.reject(error)
    }

    // 处理 401 未授权错误
    if (status === 401) {
      // 登录/注册/刷新/登出请求不做刷新处理，避免循环
      if (isAuthRequest) {
        if (isLogoutRequest) {
          const userStore = useUserStore()
          userStore.clearAuth()
          router.push('/login')
        }
        return Promise.reject(error)
      }
      
      // 如果请求已经尝试过刷新Token，不再重复尝试
      if (originalRequest._retry) {
        // 清除认证状态并跳转到登录页
        const userStore = useUserStore()
        userStore.clearAuth()
        router.push('/login')
        return Promise.reject(error)
      }
      
      // 标记请求已经尝试过刷新
      originalRequest._retry = true
      
      const userStore = useUserStore()
      
      // 如果没有Refresh Token，直接登出
      if (!userStore.refreshToken) {
        userStore.clearAuth()
        router.push('/login')
        return Promise.reject(error)
      }
      
      // 如果正在刷新Token，将请求加入队列等待
      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          subscribeTokenRefresh({ resolve, reject })
        }).then((newToken) => {
          originalRequest.headers = originalRequest.headers || {}
          originalRequest.headers.Authorization = `Bearer ${newToken}`
          return api(originalRequest)
        })
      }
      
      // 开始刷新Token
      isRefreshing = true
      
      try {
        // 调用刷新接口
        const response = await api.post('/auth/refresh', {
          refreshToken: userStore.refreshToken
        })
        
        const responseData = response?.data || {}
        const accessToken = responseData.accessToken || responseData?.data?.accessToken

        if (!accessToken) {
          throw new Error('刷新 token 响应缺少 accessToken')
        }
        
        // 更新存储的Access Token
        userStore.setAccessToken(accessToken)
        
        // 通知等待的请求使用新Token
        onTokenRefreshed(accessToken)
        
        // 重试原始请求
        originalRequest.headers.Authorization = `Bearer ${accessToken}`
        return api(originalRequest)
        
      } catch (refreshError) {
        onTokenRefreshed(null, refreshError)
        // 刷新失败，清除所有认证信息并跳转登录页
        userStore.clearAuth()
        router.push('/login')
        return Promise.reject(refreshError)
        
      } finally {
        isRefreshing = false
      }
    }
    
    // 处理 403 禁止访问错误
    if (status === 403) {
      // 只对非静默 URL 显示全局错误
      if (!shouldSilentError(requestUrl)) {
        handleApiError(error, { showToast: true })
      }
      return Promise.reject(error)
    }
    
    // 处理 404 资源不存在错误
    if (status === 404) {
      // 只对非静默 URL 显示全局错误
      if (!shouldSilentError(requestUrl)) {
        handleApiError(error, { showToast: true })
      }
      return Promise.reject(error)
    }
    
    // 处理 400 请求参数错误
    if (status === 400) {
      // 只对非静默 URL 显示全局错误
      if (!shouldSilentError(requestUrl)) {
        handleApiError(error, { showToast: true })
      }
      return Promise.reject(error)
    }
    
    // 处理 500 服务器错误
    if (status >= 500) {
      // 服务器错误始终显示全局提示
      handleApiError(error, { showToast: true })
      return Promise.reject(error)
    }
    
    // 其他错误
    if (!shouldSilentError(requestUrl)) {
      handleApiError(error, { showToast: true })
    }
    
    return Promise.reject(error)
  }
)

export default api
