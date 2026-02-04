import api from './axios'
import { useUserStore } from '@/stores/user'
import router from '@/router'

// 用于标记是否正在刷新Token，防止重复请求
let isRefreshing = false
// 存储等待刷新完成的请求队列
let refreshSubscribers = []

/**
 * 将等待的请求加入队列
 */
function subscribeTokenRefresh(callback) {
  refreshSubscribers.push(callback)
}

/**
 * 刷新完成后，执行队列中的所有请求
 */
function onTokenRefreshed(newToken) {
  refreshSubscribers.forEach(callback => callback(newToken))
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
    
    // 如果不是401错误，或者没有配置，直接返回错误
    if (!error.response || error.response.status !== 401) {
      return Promise.reject(error)
    }

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
      return new Promise((resolve) => {
        subscribeTokenRefresh((newToken) => {
          originalRequest.headers.Authorization = `Bearer ${newToken}`
          resolve(api(originalRequest))
        })
      })
    }
    
    // 开始刷新Token
    isRefreshing = true
    
    try {
      // 调用刷新接口
      const response = await api.post('/auth/refresh', {
        refreshToken: userStore.refreshToken
      })
      
      const { accessToken } = response.data
      
      // 更新存储的Access Token
      userStore.setAccessToken(accessToken)
      
      // 通知等待的请求使用新Token
      onTokenRefreshed(accessToken)
      
      // 重试原始请求
      originalRequest.headers.Authorization = `Bearer ${accessToken}`
      return api(originalRequest)
      
    } catch (refreshError) {
      // 刷新失败，清除所有认证信息并跳转登录页
      userStore.clearAuth()
      router.push('/login')
      return Promise.reject(refreshError)
      
    } finally {
      isRefreshing = false
    }
  }
)

export default api
