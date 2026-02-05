import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '@/api/interceptor'

// Mock data for development (当后端不可用时使用)
const mockUsers = [
  {
    id: '1',
    name: '系统管理员',
    email: 'admin@eduai.com',
    role: 'ADMIN',
    status: 'Active',
    lastLogin: '2026-02-02 14:30:00',
    avatar: '',
    department: ''
  },
  {
    id: '2',
    name: '张老师',
    email: 'zhang.teacher@eduai.com',
    role: 'TEACHER',
    status: 'Active',
    lastLogin: '2026-02-02 11:15:00',
    avatar: '',
    department: '物理系'
  },
  {
    id: '3',
    name: '李老师',
    email: 'li.teacher@eduai.com',
    role: 'TEACHER',
    status: 'Active',
    lastLogin: '2026-02-01 16:45:00',
    avatar: '',
    department: '数学系'
  },
  {
    id: '4',
    name: '王同学',
    email: 'wang.student@eduai.com',
    role: 'STUDENT',
    status: 'Active',
    lastLogin: '2026-02-02 09:20:00',
    avatar: '',
    department: ''
  },
  {
    id: '5',
    name: '陈同学',
    email: 'chen.student@eduai.com',
    role: 'STUDENT',
    status: 'Inactive',
    lastLogin: '2026-01-28 15:30:00',
    avatar: '',
    department: ''
  },
  {
    id: '6',
    name: '刘老师',
    email: 'liu.teacher@eduai.com',
    role: 'TEACHER',
    status: 'Active',
    lastLogin: '2026-02-01 10:00:00',
    avatar: '',
    department: '化学系'
  },
  {
    id: '7',
    name: '赵同学',
    email: 'zhao.student@eduai.com',
    role: 'STUDENT',
    status: 'Active',
    lastLogin: '2026-02-02 08:45:00',
    avatar: '',
    department: ''
  },
  {
    id: '8',
    name: '孙老师',
    email: 'sun.teacher@eduai.com',
    role: 'TEACHER',
    status: 'Inactive',
    lastLogin: '2026-01-25 14:20:00',
    avatar: '',
    department: '英语系'
  }
]

export const useUsersStore = defineStore('users', () => {
  // State
  const users = ref([])
  const loading = ref(false)
  const error = ref(null)
  const searchQuery = ref('')
  const roleFilter = ref('All') // 'All' | 'ADMIN' | 'TEACHER' | 'STUDENT'
  const backendAvailable = ref(false) // Will detect if backend API is available

  // Getters
  const filteredUsers = computed(() => {
    let result = users.value

    // Apply role filter (使用大写格式)
    if (roleFilter.value !== 'All') {
      result = result.filter(user => user.role === roleFilter.value)
    }

    // Apply search filter
    if (searchQuery.value.trim()) {
      const query = searchQuery.value.toLowerCase()
      result = result.filter(user => 
        user.name.toLowerCase().includes(query) ||
        user.email.toLowerCase().includes(query)
      )
    }

    return result
  })

  const userStats = computed(() => {
    return {
      total: users.value.length,
      admin: users.value.filter(u => u.role === 'ADMIN').length,
      teacher: users.value.filter(u => u.role === 'TEACHER').length,
      student: users.value.filter(u => u.role === 'STUDENT').length,
      active: users.value.filter(u => u.status === 'Active').length,
      inactive: users.value.filter(u => u.status === 'Inactive').length
    }
  })

  // Actions
  async function fetchUsers() {
    loading.value = true
    error.value = null

    try {
      // 使用配置了JWT拦截器的 api 实例
      const response = await api.get('/users')
      const data = response.data
      users.value = data.data || data
      backendAvailable.value = true
    } catch (err) {
      console.error('Failed to fetch users:', err)
      error.value = err.message
      backendAvailable.value = false
      // Fallback to mock data
      await new Promise(resolve => setTimeout(resolve, 500))
      users.value = [...mockUsers]
    } finally {
      loading.value = false
    }
  }

  async function createUser(userData) {
    loading.value = true
    error.value = null

    try {
      if (backendAvailable.value) {
        // 调用注册API，需要将字段映射到后端期望的格式
        const registerData = {
          username: userData.name, // 使用name作为username
          email: userData.email,
          password: userData.password,
          role: userData.role.toUpperCase() // 转换为大写：ADMIN, TEACHER, STUDENT
        }
        
        const response = await api.post('/auth/register', registerData)
        const data = response.data
        
        // 将返回的用户数据添加到列表
        const newUser = {
          id: data.user?.id || String(Date.now()),
          name: data.user?.username || userData.name,
          email: data.user?.email || userData.email,
          role: data.user?.role || userData.role.toUpperCase(),
          status: userData.status || 'Active',
          lastLogin: '-',
          avatar: '',
          department: userData.department || ''
        }
        users.value.push(newUser)
        return newUser
      }

      // Mock creation
      await new Promise(resolve => setTimeout(resolve, 300))
      const newUser = {
        id: String(Date.now()),
        name: userData.name,
        email: userData.email,
        role: userData.role.toUpperCase(),
        status: userData.status || 'Active',
        lastLogin: '-',
        avatar: '',
        department: userData.department || ''
      }
      users.value.push(newUser)
      return newUser
    } catch (err) {
      console.error('Failed to create user:', err)
      error.value = err.response?.data?.message || err.message
      throw err
    } finally {
      loading.value = false
    }
  }

  async function updateUser(userId, updates) {
    loading.value = true
    error.value = null

    try {
      if (backendAvailable.value) {
        // 使用配置了JWT拦截器的 api 实例
        const response = await api.put(`/users/${userId}`, updates)
        const data = response.data
        const index = users.value.findIndex(u => u.id === userId)
        if (index !== -1) {
          users.value[index] = { ...users.value[index], ...(data.data || data) }
        }
        return data.data || data
      }

      // Mock update
      await new Promise(resolve => setTimeout(resolve, 300))
      const index = users.value.findIndex(u => u.id === userId)
      if (index !== -1) {
        users.value[index] = { ...users.value[index], ...updates }
        return users.value[index]
      }
      throw new Error('User not found')
    } catch (err) {
      console.error('Failed to update user:', err)
      error.value = err.message
      throw err
    } finally {
      loading.value = false
    }
  }

  async function deleteUser(userId) {
    loading.value = true
    error.value = null

    try {
      if (backendAvailable.value) {
        // 使用配置了JWT拦截器的 api 实例
        await api.delete(`/users/${userId}`)
      }

      // Mock delete
      await new Promise(resolve => setTimeout(resolve, 300))
      const index = users.value.findIndex(u => u.id === userId)
      if (index !== -1) {
        users.value.splice(index, 1)
      }
    } catch (err) {
      console.error('Failed to delete user:', err)
      error.value = err.message
      throw err
    } finally {
      loading.value = false
    }
  }

  function setSearchQuery(query) {
    searchQuery.value = query
  }

  function setRoleFilter(role) {
    roleFilter.value = role
  }

  // Check backend availability
  async function checkBackend() {
    try {
      await api.get('/users', { timeout: 3000 })
      backendAvailable.value = true
    } catch {
      backendAvailable.value = false
    }
  }

  // Initialize store
  function init() {
    checkBackend()
    fetchUsers()
  }

  return {
    users,
    loading,
    error,
    searchQuery,
    roleFilter,
    filteredUsers,
    userStats,
    fetchUsers,
    createUser,
    updateUser,
    deleteUser,
    setSearchQuery,
    setRoleFilter,
    init
  }
})
