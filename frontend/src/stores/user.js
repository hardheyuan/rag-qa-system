import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi } from '@/api/auth'

/**
 * 用户状态管理 Store
 * 
 * 管理用户认证状态、Token和用户信息
 * 集成真实后端API进行登录认证
 */
export const useUserStore = defineStore('user', () => {
  // ========== State ==========
  const user = ref({
    id: null,
    name: '',
    email: '',
    role: 'Student',
    roleDisplayName: '',
    avatar: '',
    department: ''
  })
  
  const accessToken = ref(null)
  const refreshToken = ref(null)
  const isDarkMode = ref(false)
  
  // ========== Getters ==========
  const isAuthenticated = computed(() => !!accessToken.value)
  const isLoggedIn = computed(() => isAuthenticated.value)
  const userRole = computed(() => user.value.role)
  
  // ========== Actions ==========
  
  /**
   * 用户登录
   * @param {Object} credentials 登录凭证 { username, password }
   * @returns {Promise<{success: boolean, error?: string, user?: Object}>}
   */
  async function login(credentials) {
    try {
      const response = await authApi.login(credentials.username, credentials.password)
      const { accessToken: newAccessToken, refreshToken: newRefreshToken, user: userInfo } = response.data
      
      // 保存Token
      accessToken.value = newAccessToken
      refreshToken.value = newRefreshToken
      
      // 保存用户信息
      user.value = {
        id: userInfo.id,
        name: userInfo.username,
        email: userInfo.email || '',
        role: userInfo.role,
        roleDisplayName: userInfo.roleDisplayName,
        avatar: getDefaultAvatar(userInfo.role),
        department: userInfo.role === 'TEACHER' ? '教学部门' : ''
      }
      
      // 持久化到localStorage
      saveToStorage()
      
      return { success: true, user: user.value }
    } catch (error) {
      console.error('Login failed:', error)
      const errorMessage = error.response?.data?.message || '登录失败，请检查用户名和密码'
      return { success: false, error: errorMessage }
    }
  }
  
  /**
   * 用户登出
   */
  async function logout() {
    try {
      // 调用后端登出接口
      if (accessToken.value) {
        await authApi.logout(accessToken.value)
      }
    } catch (error) {
      console.error('Logout API error:', error)
    } finally {
      // 清除所有状态
      clearState()
    }
  }

  /**
   * 强制清除认证状态（不调用后端）
   */
  function clearAuth() {
    clearState()
  }
  
  /**
   * 初始化认证状态
   * 从localStorage恢复登录状态
   */
  function initAuth() {
    const savedAccessToken = localStorage.getItem('accessToken')
    const savedRefreshToken = localStorage.getItem('refreshToken')
    const savedUser = localStorage.getItem('user')
    
    if (savedAccessToken && savedUser) {
      try {
        accessToken.value = savedAccessToken
        refreshToken.value = savedRefreshToken
        user.value = JSON.parse(savedUser)
      } catch (e) {
        console.error('Failed to parse saved user data:', e)
        clearState()
      }
    }
  }
  
  /**
   * 更新Access Token
   * 用于Token刷新后更新
   * @param {string} token 新的Access Token
   */
  function setAccessToken(token) {
    accessToken.value = token
    localStorage.setItem('accessToken', token)
  }
  
  /**
   * 获取当前登录用户信息
   * @returns {Promise<{success: boolean, user?: Object, error?: string}>}
   */
  async function fetchCurrentUser() {
    try {
      const response = await authApi.getCurrentUser()
      const userInfo = response.data
      
      // 更新用户信息
      user.value = {
        ...user.value,
        id: userInfo.id,
        name: userInfo.username,
        email: userInfo.email || '',
        role: userInfo.role,
        roleDisplayName: userInfo.roleDisplayName
      }
      
      saveToStorage()
      return { success: true, user: user.value }
    } catch (error) {
      console.error('Failed to fetch user info:', error)
      return { success: false, error: error.response?.data?.message || '获取用户信息失败' }
    }
  }
  
  /**
   * 切换深色模式
   */
  function toggleDarkMode() {
    isDarkMode.value = !isDarkMode.value
    if (isDarkMode.value) {
      document.documentElement.classList.add('dark')
    } else {
      document.documentElement.classList.remove('dark')
    }
    localStorage.setItem('darkMode', isDarkMode.value)
  }
  
  /**
   * 初始化深色模式
   */
  function initDarkMode() {
    const saved = localStorage.getItem('darkMode')
    if (saved === 'true') {
      isDarkMode.value = true
      document.documentElement.classList.add('dark')
    }
  }
  
  // ========== Helper Functions ==========
  
  /**
   * 保存状态到localStorage
   */
  function saveToStorage() {
    localStorage.setItem('accessToken', accessToken.value)
    localStorage.setItem('refreshToken', refreshToken.value)
    localStorage.setItem('user', JSON.stringify(user.value))
  }
  
  /**
   * 清除所有状态
   */
  function clearState() {
    accessToken.value = null
    refreshToken.value = null
    user.value = {
      id: null,
      name: '',
      email: '',
      role: 'Student',
      roleDisplayName: '',
      avatar: '',
      department: ''
    }
    
    // 清除localStorage
    localStorage.removeItem('accessToken')
    localStorage.removeItem('refreshToken')
    localStorage.removeItem('user')
  }
  
  /**
   * 根据角色获取默认头像
   * @param {string} role 角色
   * @returns {string} 头像URL
   */
  function getDefaultAvatar(role) {
    const avatars = {
      'STUDENT': 'https://ui-avatars.com/api/?name=Student&background=0D8ABC&color=fff',
      'TEACHER': 'https://ui-avatars.com/api/?name=Teacher&background=27ae60&color=fff',
      'ADMIN': 'https://ui-avatars.com/api/?name=Admin&background=e74c3c&color=fff'
    }
    return avatars[role] || avatars['STUDENT']
  }
  
  // ========== Return ==========
  return {
    // State
    user,
    accessToken,
    refreshToken,
    isDarkMode,
    // Getters
    isAuthenticated,
    isLoggedIn,
    userRole,
    // Actions
    login,
    logout,
    clearAuth,
    initAuth,
    setAccessToken,
    fetchCurrentUser,
    toggleDarkMode,
    initDarkMode
  }
})
