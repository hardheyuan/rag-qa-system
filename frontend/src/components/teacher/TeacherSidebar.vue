<template>
  <aside class="w-64 flex-shrink-0 flex flex-col bg-white dark:bg-[#111418] border-r border-slate-200 dark:border-slate-800 transition-all duration-300 h-full">
    <!-- Logo Area -->
    <div class="p-6 flex items-center gap-3">
      <div class="size-10 rounded-lg bg-gradient-to-br from-primary to-blue-600 flex items-center justify-center shadow-lg shadow-blue-900/20">
        <span class="material-symbols-outlined text-white text-2xl">school</span>
      </div>
      <div class="flex flex-col">
        <h1 class="text-lg font-bold tracking-tight text-slate-900 dark:text-white leading-none">EduAI</h1>
        <p class="text-slate-500 dark:text-slate-400 text-xs font-medium mt-1">教师门户</p>
      </div>
    </div>

    <!-- Navigation Menu -->
    <nav class="flex-1 px-4 py-4 gap-2 flex flex-col overflow-y-auto">
      <a 
        v-for="item in menuItems" 
        :key="item.name"
        @click="navigateTo(item.route)"
        :class="[
          'flex items-center gap-3 px-3 py-2.5 rounded-lg transition-colors group cursor-pointer',
          isActive(item.route)
            ? 'bg-primary/10 text-primary' 
            : 'text-slate-600 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-800'
        ]"
      >
        <span 
          class="material-symbols-outlined transition-colors"
          :class="isActive(item.route) ? 'fill-1' : 'group-hover:text-primary'"
        >
          {{ item.icon }}
        </span>
        <span class="font-medium text-sm">{{ item.label }}</span>
        <span 
          v-if="item.badge" 
          class="ml-auto flex items-center justify-center h-5 px-1.5 rounded-full bg-red-500 text-[10px] font-bold text-white shadow-sm shadow-red-500/20"
        >
          {{ item.badge }}
        </span>
      </a>

      <div class="my-2 border-t border-slate-200 dark:border-slate-800"></div>

      <a 
        @click="selectMenu('settings')"
        :class="[
          'flex items-center gap-3 px-3 py-2.5 rounded-lg transition-colors group cursor-pointer',
          activeMenu === 'settings' 
            ? 'bg-primary/10 text-primary' 
            : 'text-slate-600 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-800'
        ]"
      >
        <span class="material-symbols-outlined group-hover:text-primary transition-colors">settings</span>
        <span class="font-medium text-sm">设置</span>
      </a>
    </nav>

    <!-- User Profile -->
    <div class="p-4 border-t border-slate-200 dark:border-slate-800">
      <div class="flex items-center gap-3 px-2 group cursor-pointer">
        <div 
          class="size-9 rounded-full bg-slate-200 dark:bg-slate-700 bg-cover bg-center shrink-0"
          :style="`background-image: url('${userStore.user.avatar}')`"
        ></div>
        <div class="flex flex-col flex-1 min-w-0">
          <p class="text-sm font-semibold text-slate-900 dark:text-white truncate">{{ userStore.user.name }}</p>
          <p class="text-xs text-slate-500 dark:text-slate-400 truncate">{{ userStore.user.department || '物理系' }}</p>
        </div>
        <span 
          class="material-symbols-outlined text-slate-400 group-hover:text-primary cursor-pointer" 
          @click="userStore.toggleDarkMode()"
          title="切换主题"
        >
          {{ userStore.isDarkMode ? 'light_mode' : 'dark_mode' }}
        </span>
        <span 
          class="material-symbols-outlined text-slate-400 hover:text-red-500 cursor-pointer ml-1" 
          @click="handleLogout" 
          title="退出登录"
        >
          logout
        </span>
      </div>
    </div>
  </aside>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '../../stores/user'

const userStore = useUserStore()
const router = useRouter()
const route = useRoute()
const activeMenu = ref('analytics')

const menuItems = [
  { name: 'dashboard', label: '概览面板', icon: 'dashboard', route: '/teacher', badge: null },
  { name: 'documents', label: '文档管理', icon: 'description', route: '/teacher/documents', badge: null },
  { name: 'analytics', label: '数据分析', icon: 'analytics', route: '/teacher', badge: null },
  { name: 'students', label: '学生管理', icon: 'groups', route: '/teacher', badge: 3 },
]

// Check if a route is currently active
function isActive(itemRoute) {
  return route.path === itemRoute
}

// Navigate to route
function navigateTo(routePath) {
  if (route.path !== routePath) {
    router.push(routePath)
  }
}

function selectMenu(menuName) {
  activeMenu.value = menuName
  console.log('Selected menu:', menuName)
}

function handleLogout() {
  userStore.logout()
  router.push('/login')
}
</script>
