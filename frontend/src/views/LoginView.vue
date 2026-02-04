<script setup>
import { ref } from 'vue'
import { useUserStore } from '../stores/user'
import { useRouter } from 'vue-router'

const userStore = useUserStore()
const router = useRouter()

const username = ref('')
const password = ref('')
const errorMessage = ref('')
const isLoading = ref(false)

// 初始账号提示
const demoAccounts = [
  { role: 'Student', username: 'student', password: '123456' },
  { role: 'Teacher', username: 'teacher', password: '123456' },
  { role: 'Admin', username: 'admin', password: '123456' }
]

async function handleLogin() {
  if (!username.value || !password.value) {
    errorMessage.value = '请输入用户名和密码'
    return
  }

  isLoading.value = true
  errorMessage.value = ''

  try {
    // 调用真实的登录API
    const result = await userStore.login({
      username: username.value,
      password: password.value
    })

    if (result.success) {
      // 根据后端返回的角色跳转
      const role = result.user.role
      if (role === 'STUDENT') {
        router.push('/')
      } else if (role === 'TEACHER') {
        router.push('/teacher')
      } else if (role === 'ADMIN') {
        router.push('/admin')
      }
    } else {
      errorMessage.value = result.error || '登录失败'
    }
  } catch (error) {
    errorMessage.value = '登录失败，请稍后重试'
    console.error('Login error:', error)
  } finally {
    isLoading.value = false
  }
}

function fillDemoAccount(account) {
  username.value = account.username
  password.value = account.password
}
</script>

<template>
  <div class="min-h-screen flex flex-col items-center justify-center p-4 sm:p-6 bg-background-light dark:bg-background-dark">
    <!-- Login Card -->
    <div class="w-full max-w-[440px] bg-white dark:bg-[#1a222c] rounded-xl shadow-[0_8px_30px_rgb(0,0,0,0.04)] overflow-hidden border border-transparent dark:border-white/5">
      <!-- Header Section -->
      <div class="pt-10 pb-4 px-8 flex flex-col items-center">
        <!-- Logo -->
        <div class="w-14 h-14 bg-primary rounded-xl flex items-center justify-center mb-5 text-white shadow-lg shadow-primary/30">
          <span class="material-symbols-outlined text-3xl">school</span>
        </div>
        <h3 class="text-[#111418] dark:text-white tracking-tight text-2xl font-bold leading-tight text-center">
          ScholarAI
        </h3>
        <p class="text-slate-500 dark:text-slate-400 text-base font-normal leading-normal pt-2 text-center px-4">
          Welcome back, please select your role to continue.
        </p>
      </div>

      <!-- Demo Accounts Hint -->
      <div class="px-8 py-3">
        <p class="text-xs text-slate-400 dark:text-slate-500 mb-2">测试账号（点击自动填充）:</p>
        <div class="flex gap-2 flex-wrap">
          <button
            v-for="account in demoAccounts"
            :key="account.role"
            @click="fillDemoAccount(account)"
            class="px-2 py-1 text-xs rounded bg-slate-100 dark:bg-slate-700 text-slate-600 dark:text-slate-300 hover:bg-slate-200 dark:hover:bg-slate-600 transition-colors"
          >
            {{ account.role }}: {{ account.username }}/{{ account.password }}
          </button>
        </div>
      </div>

      <!-- Login Form -->
      <div class="px-8 pb-10 flex flex-col gap-5 pt-2">
        <!-- Error Message -->
        <div v-if="errorMessage" class="p-3 rounded-lg bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 text-red-600 dark:text-red-400 text-sm">
          {{ errorMessage }}
        </div>

        <!-- Username Input -->
        <div class="flex flex-col gap-1.5">
          <label class="text-[#111418] dark:text-white text-sm font-medium leading-normal ml-0.5">
            Username
          </label>
          <div class="relative group">
            <span class="material-symbols-outlined absolute left-4 top-1/2 -translate-y-1/2 text-slate-400 group-focus-within:text-primary transition-colors text-[20px]">
              person
            </span>
            <input
              v-model="username"
              type="text"
              placeholder="Enter your username"
              class="w-full rounded-lg border border-[#dbe0e6] dark:border-slate-600 bg-white dark:bg-[#1a222c] h-12 pl-11 pr-4 text-[#111418] dark:text-white placeholder:text-slate-400 focus:outline-none focus:ring-0 focus:border-primary transition-all text-base font-normal leading-normal shadow-sm"
              @keyup.enter="handleLogin"
            />
          </div>
        </div>

        <!-- Password Input -->
        <div class="flex flex-col gap-1.5">
          <label class="text-[#111418] dark:text-white text-sm font-medium leading-normal ml-0.5">
            Password
          </label>
          <div class="relative group">
            <span class="material-symbols-outlined absolute left-4 top-1/2 -translate-y-1/2 text-slate-400 group-focus-within:text-primary transition-colors text-[20px]">
              lock
            </span>
            <input
              v-model="password"
              type="password"
              placeholder="••••••••"
              class="w-full rounded-lg border border-[#dbe0e6] dark:border-slate-600 bg-white dark:bg-[#1a222c] h-12 pl-11 pr-4 text-[#111418] dark:text-white placeholder:text-slate-400 focus:outline-none focus:ring-0 focus:border-primary transition-all text-base font-normal leading-normal shadow-sm"
              @keyup.enter="handleLogin"
            />
          </div>
        </div>

        <!-- Sign In Button -->
        <button
          @click="handleLogin"
          :disabled="isLoading"
          class="mt-2 w-full h-12 rounded-lg bg-primary text-white font-bold text-base hover:bg-blue-700 active:scale-[0.98] transition-all flex items-center justify-center gap-2 shadow-md shadow-primary/20 disabled:opacity-50 disabled:cursor-not-allowed"
        >
          <span v-if="!isLoading">Sign In</span>
          <span v-else>Signing in...</span>
          <span v-if="!isLoading" class="material-symbols-outlined text-[20px]">arrow_forward</span>
          <span v-else class="material-symbols-outlined text-[20px] animate-spin">progress_activity</span>
        </button>

        <!-- Footer Link -->
        <div class="pt-2 text-center">
          <a href="#" class="inline-flex items-center gap-1.5 text-sm text-slate-500 dark:text-slate-400 hover:text-primary dark:hover:text-primary transition-colors font-medium group/link">
            <span class="material-symbols-outlined text-[18px] text-slate-400 group-hover/link:text-primary transition-colors">help</span>
            Having trouble? Contact Support
          </a>
        </div>
      </div>
    </div>

    <!-- Background decorative elements -->
    <div class="fixed top-0 left-0 w-full h-full pointer-events-none -z-10 overflow-hidden">
      <div class="absolute top-[-10%] left-[-5%] w-[40%] h-[40%] bg-primary/5 rounded-full blur-[100px]"></div>
      <div class="absolute bottom-[-10%] right-[-5%] w-[40%] h-[40%] bg-blue-400/5 rounded-full blur-[100px]"></div>
    </div>
  </div>
</template>
