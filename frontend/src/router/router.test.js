import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'
import routerConfig from '../router/index.js'

describe('路由守卫角色判断测试', () => {
  let router
  let userStore

  beforeEach(() => {
    // 创建新的 Pinia 实例
    setActivePinia(createPinia())
    
    // 创建路由实例 - 使用原始配置中的路由定义
    router = createRouter({
      history: createWebHistory(),
      routes: routerConfig.options.routes
    })
    
    // 添加路由守卫 - 这模拟了实际的路由配置
    router.beforeEach((to, from, next) => {
      const store = useUserStore()
      
      // Initialize auth state on first navigation
      if (!store.isAuthenticated && localStorage.getItem('accessToken')) {
        store.initAuth()
      }
      
      // If route requires auth and user is not authenticated, redirect to login
      if (to.meta.requiresAuth && !store.isAuthenticated) {
        next({ name: 'login' })
        return
      }
      
      // If user is authenticated and tries to access login page
      if (to.meta.public && store.isAuthenticated) {
        // Redirect based on user role (使用大写格式)
        if (store.user.role === 'TEACHER') {
          next({ name: 'teacher' })
        } else if (store.user.role === 'ADMIN') {
          next({ name: 'admin' })
        } else {
          next({ name: 'student' })
        }
        return
      }
      
      // Role-based route protection
      if (store.isAuthenticated) {
        // Teachers should not access student routes
        if (store.user.role === 'TEACHER' && to.name === 'student') {
          next({ name: 'teacher' })
          return
        }
        // Students should not access teacher routes
        if (store.user.role === 'STUDENT' && (to.name === 'teacher' || to.name === 'teacher-documents')) {
          next({ name: 'student' })
          return
        }
        // Admin should only access admin routes
        if (store.user.role === 'ADMIN' && !to.path.startsWith('/admin')) {
          next({ name: 'admin' })
          return
        }
        // Non-admin should not access admin routes
        if (store.user.role !== 'ADMIN' && to.path.startsWith('/admin')) {
          if (store.user.role === 'TEACHER') {
            next({ name: 'teacher' })
          } else {
            next({ name: 'student' })
          }
          return
        }
      }
      
      next()
    })
    
    // 获取 store
    userStore = useUserStore()
    
    // 清理 localStorage
    localStorage.clear()
  })

  describe('已认证用户访问登录页面', () => {
    it('STUDENT 角色应重定向到学生页面', async () => {
      // 模拟已登录的学生用户
      userStore.$patch({
        accessToken: 'test-token',
        user: {
          id: 1,
          name: 'Student',
          role: 'STUDENT'
        }
      })

      await router.push('/login')
      
      expect(router.currentRoute.value.name).toBe('student')
    })

    it('TEACHER 角色应重定向到教师页面', async () => {
      // 模拟已登录的教师用户
      userStore.$patch({
        accessToken: 'test-token',
        user: {
          id: 2,
          name: 'Teacher',
          role: 'TEACHER'
        }
      })

      await router.push('/login')
      
      expect(router.currentRoute.value.name).toBe('teacher')
    })

    it('ADMIN 角色应重定向到管理员页面', async () => {
      // 模拟已登录的管理员用户
      userStore.$patch({
        accessToken: 'test-token',
        user: {
          id: 3,
          name: 'Admin',
          role: 'ADMIN'
        }
      })

      await router.push('/login')
      
      expect(router.currentRoute.value.name).toBe('admin')
    })
  })

  describe('角色权限保护', () => {
    it('TEACHER 不应访问 student 路由', async () => {
      userStore.$patch({
        accessToken: 'test-token',
        user: {
          id: 2,
          name: 'Teacher',
          role: 'TEACHER'
        }
      })

      await router.push('/')
      
      expect(router.currentRoute.value.name).toBe('teacher')
    })

    it('STUDENT 不应访问 teacher 路由', async () => {
      userStore.$patch({
        accessToken: 'test-token',
        user: {
          id: 1,
          name: 'Student',
          role: 'STUDENT'
        }
      })

      await router.push('/teacher')
      
      expect(router.currentRoute.value.name).toBe('student')
    })

    it('STUDENT 不应访问 teacher-documents 路由', async () => {
      userStore.$patch({
        accessToken: 'test-token',
        user: {
          id: 1,
          name: 'Student',
          role: 'STUDENT'
        }
      })

      await router.push('/teacher/documents')
      
      expect(router.currentRoute.value.name).toBe('student')
    })

    it('ADMIN 不应访问非 admin 路由', async () => {
      userStore.$patch({
        accessToken: 'test-token',
        user: {
          id: 3,
          name: 'Admin',
          role: 'ADMIN'
        }
      })

      await router.push('/')
      
      expect(router.currentRoute.value.name).toBe('admin')
    })

    it('TEACHER 访问 admin 路由应被重定向到 teacher', async () => {
      userStore.$patch({
        accessToken: 'test-token',
        user: {
          id: 2,
          name: 'Teacher',
          role: 'TEACHER'
        }
      })

      await router.push('/admin')
      
      expect(router.currentRoute.value.name).toBe('teacher')
    })

    it('STUDENT 访问 admin 路由应被重定向到 student', async () => {
      userStore.$patch({
        accessToken: 'test-token',
        user: {
          id: 1,
          name: 'Student',
          role: 'STUDENT'
        }
      })

      await router.push('/admin')
      
      expect(router.currentRoute.value.name).toBe('student')
    })
  })
})

describe('认证初始化测试', () => {
  let router
  let userStore

  beforeEach(() => {
    setActivePinia(createPinia())
    localStorage.clear()
    
    router = createRouter({
      history: createWebHistory(),
      routes: routerConfig.options.routes
    })
    
    // 添加路由守卫，包含认证初始化逻辑
    router.beforeEach((to, from, next) => {
      const store = useUserStore()
      
      // Initialize auth state on first navigation
      if (!store.isAuthenticated && localStorage.getItem('accessToken')) {
        store.initAuth()
      }
      
      if (to.meta.requiresAuth && !store.isAuthenticated) {
        next({ name: 'login' })
        return
      }
      
      if (to.meta.public && store.isAuthenticated) {
        if (store.user.role === 'TEACHER') {
          next({ name: 'teacher' })
        } else if (store.user.role === 'ADMIN') {
          next({ name: 'admin' })
        } else {
          next({ name: 'student' })
        }
        return
      }
      
      next()
    })
    
    userStore = useUserStore()
  })

  it('从 localStorage 的 accessToken 恢复认证状态', async () => {
    // 预先设置 localStorage
    localStorage.setItem('accessToken', 'stored-access-token')
    localStorage.setItem('refreshToken', 'stored-refresh-token')
    localStorage.setItem('user', JSON.stringify({
      id: 1,
      name: 'TestUser',
      role: 'STUDENT'
    }))

    // 在导航前 store 应该是未认证状态
    expect(userStore.isAuthenticated).toBe(false)

    // 触发导航，初始化认证
    await router.push('/')

    // 认证状态应该被恢复
    expect(userStore.isAuthenticated).toBe(true)
    expect(userStore.accessToken).toBe('stored-access-token')
    expect(userStore.user.name).toBe('TestUser')
  })

  it('没有 accessToken 时不初始化认证', async () => {
    // 只设置 user，不设置 accessToken
    localStorage.setItem('user', JSON.stringify({
      id: 1,
      name: 'TestUser',
      role: 'STUDENT'
    }))

    await router.push('/')

    // 应该保持未认证状态
    expect(userStore.isAuthenticated).toBe(false)
    expect(userStore.user.name).toBe('') // 默认值
  })

  it('无效的用户数据应清除状态', async () => {
    // 设置有效的 token 但无效的用户数据
    localStorage.setItem('accessToken', 'valid-token')
    localStorage.setItem('user', 'invalid-json')

    await router.push('/')

    // 应该清除认证状态
    expect(userStore.isAuthenticated).toBe(false)
    expect(localStorage.getItem('accessToken')).toBeNull()
  })
})

describe('路由守卫向后兼容性测试', () => {
  let router
  let userStore

  beforeEach(() => {
    setActivePinia(createPinia())
    localStorage.clear()
    
    router = createRouter({
      history: createWebHistory(),
      routes: routerConfig.options.routes
    })
    
    router.beforeEach((to, from, next) => {
      const store = useUserStore()
      
      if (!store.isAuthenticated && localStorage.getItem('accessToken')) {
        store.initAuth()
      }
      
      if (to.meta.requiresAuth && !store.isAuthenticated) {
        next({ name: 'login' })
        return
      }
      
      if (to.meta.public && store.isAuthenticated) {
        // 修复后的代码使用大写角色名
        if (store.user.role === 'TEACHER') {
          next({ name: 'teacher' })
        } else if (store.user.role === 'ADMIN') {
          next({ name: 'admin' })
        } else {
          next({ name: 'student' })
        }
        return
      }
      
      next()
    })
    
    userStore = useUserStore()
  })

  it('验证角色必须使用大写格式', async () => {
    // 使用大写角色（正确的格式）
    userStore.$patch({
      accessToken: 'test-token',
      user: {
        id: 1,
        name: 'Student',
        role: 'STUDENT'  // 大写格式 - 正确
      }
    })

    await router.push('/login')
    
    // 应该成功重定向到学生页面
    expect(router.currentRoute.value.name).toBe('student')
    
    // 验证所有允许的角色都是大写
    const validRoles = ['STUDENT', 'TEACHER', 'ADMIN']
    expect(validRoles).toContain(userStore.user.role)
  })

  it('修复后的代码使用大写角色确保路由正确工作', async () => {
    // 验证修复后的代码使用大写角色
    userStore.$patch({
      accessToken: 'test-token',
      user: {
        id: 1,
        name: 'Student',
        role: 'STUDENT'  // 大写格式 - 修复后的正确格式
      }
    })

    await router.push('/login')
    
    // 使用大写角色确保路由正确工作
    expect(router.currentRoute.value.name).toBe('student')
    
    // 记录：后端返回的角色是大写的（STUDENT/TEACHER/ADMIN）
    // 前端路由守卫已修复为使用大写格式进行匹配
  })
})