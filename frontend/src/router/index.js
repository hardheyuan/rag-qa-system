import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'
import StudentView from '../views/StudentView.vue'
import TeacherView from '../views/TeacherView.vue'
import TeacherDocumentsView from '../views/TeacherDocumentsView.vue'
import TeacherStudentListView from '../views/teacher/TeacherStudentListView.vue'
import TeacherStudentDetailView from '../views/teacher/TeacherStudentDetailView.vue'
import LoginView from '../views/LoginView.vue'
import AdminUsersView from '../views/admin/AdminUsersView.vue'
import AdminDocumentsView from '../views/admin/AdminDocumentsView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: LoginView,
      meta: { public: true }
    },
    {
      path: '/',
      name: 'student',
      component: StudentView,
      meta: { requiresAuth: true }
    },
    {
      path: '/teacher',
      name: 'teacher',
      component: TeacherView,
      meta: { requiresAuth: true }
    },
    {
      path: '/teacher/documents',
      name: 'teacher-documents',
      component: TeacherDocumentsView,
      meta: { requiresAuth: true }
    },
    {
      path: '/teacher/students',
      name: 'teacher-students',
      component: TeacherStudentListView,
      meta: { requiresAuth: true }
    },
    {
      path: '/teacher/students/:id',
      name: 'teacher-student-detail',
      component: TeacherStudentDetailView,
      meta: { requiresAuth: true }
    },
    {
      path: '/admin',
      name: 'admin',
      component: AdminUsersView,
      meta: { requiresAuth: true }
    },
    {
      path: '/admin/users',
      name: 'admin-users',
      component: AdminUsersView,
      meta: { requiresAuth: true }
    },
    {
      path: '/admin/documents',
      name: 'admin-documents',
      component: AdminDocumentsView,
      meta: { requiresAuth: true }
    },
    {
      path: '/admin/ai-models',
      name: 'admin-ai-models',
      component: AdminUsersView,
      meta: { requiresAuth: true }
    },
    {
      path: '/admin/settings',
      name: 'admin-settings',
      component: AdminUsersView,
      meta: { requiresAuth: true }
    }
  ]
})

// Navigation guard
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()

  // Initialize auth state on first navigation
  if (!userStore.isAuthenticated && localStorage.getItem('accessToken')) {
    userStore.initAuth()
  }

  // If route requires auth and user is not authenticated, redirect to login
  if (to.meta.requiresAuth && !userStore.isAuthenticated) {
    next({ name: 'login' })
    return
  }

  // If user is authenticated and tries to access login page
  if (to.meta.public && userStore.isAuthenticated) {
    // Redirect based on user role
    if (userStore.user.role === 'TEACHER') {
      next({ name: 'teacher' })
    } else if (userStore.user.role === 'ADMIN') {
      next({ name: 'admin' })
    } else {
      next({ name: 'student' })
    }
    return
  }

  // Role-based route protection
  if (userStore.isAuthenticated) {
    // Teachers should not access student routes
    if (userStore.user.role === 'TEACHER' && to.name === 'student') {
      next({ name: 'teacher' })
      return
    }
    // Students should not access teacher routes
    if (userStore.user.role === 'STUDENT' && (to.name === 'teacher' || to.name === 'teacher-documents' || to.name === 'teacher-students' || to.path.startsWith('/teacher'))) {
      next({ name: 'student' })
      return
    }
    // Admin should only access admin routes
    if (userStore.user.role === 'ADMIN' && !to.path.startsWith('/admin')) {
      next({ name: 'admin' })
      return
    }
    // Non-admin should not access admin routes
    if (userStore.user.role !== 'ADMIN' && to.path.startsWith('/admin')) {
      if (userStore.user.role === 'TEACHER') {
        next({ name: 'teacher' })
      } else {
        next({ name: 'student' })
      }
      return
    }
  }

  next()
})

export default router
