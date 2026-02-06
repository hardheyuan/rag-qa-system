<template>
  <div class="fixed inset-0 bg-black/50 flex items-center justify-center z-50 overflow-y-auto py-4" @click="$emit('close')">
    <div class="bg-white dark:bg-[#1a222c] rounded-lg w-full max-w-md mx-4 max-h-[85vh] flex flex-col" @click.stop>
      <!-- Header -->
      <div class="px-6 py-4 border-b border-slate-200 dark:border-slate-700 flex-shrink-0">
        <h3 class="text-lg font-semibold text-slate-900 dark:text-white">编辑用户</h3>
        <p class="text-sm text-slate-500 dark:text-slate-400 mt-1">修改用户 {{ user.name }} 的信息</p>
      </div>

      <!-- Form -->
      <div class="p-6 space-y-4 overflow-y-auto flex-1">
        <!-- Name -->
        <div>
          <label class="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-1.5">
            姓名 <span class="text-red-500">*</span>
          </label>
          <input
            v-model="form.name"
            type="text"
            placeholder="请输入用户姓名"
            class="w-full h-11 px-4 rounded-lg border border-slate-200 dark:border-slate-700 bg-white dark:bg-[#111418] text-slate-900 dark:text-white placeholder:text-slate-400 focus:outline-none focus:border-primary transition-all"
          />
        </div>

        <!-- Email -->
        <div>
          <label class="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-1.5">
            邮箱 <span class="text-red-500">*</span>
          </label>
          <input
            v-model="form.email"
            type="email"
            placeholder="user@example.com"
            class="w-full h-11 px-4 rounded-lg border border-slate-200 dark:border-slate-700 bg-white dark:bg-[#111418] text-slate-900 dark:text-white placeholder:text-slate-400 focus:outline-none focus:border-primary transition-all"
          />
        </div>

        <!-- Role -->
        <div>
          <label class="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-1.5">
            角色 <span class="text-red-500">*</span>
          </label>
          <div class="flex gap-3">
            <button
              v-for="role in roles"
              :key="role.value"
              type="button"
              @click="form.role = role.value"
              :class="[
                'flex-1 h-11 rounded-lg border text-sm font-medium transition-all',
                form.role === role.value
                  ? 'border-primary bg-primary/10 text-primary'
                  : 'border-slate-200 dark:border-slate-700 text-slate-600 dark:text-slate-400 hover:border-slate-300 dark:hover:border-slate-600'
              ]"
            >
              {{ role.label }}
            </button>
          </div>
        </div>

        <!-- Department (only for Teacher) -->
        <div v-if="form.role === 'TEACHER'">
          <label class="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-1.5">
            所属院系
          </label>
          <select
            v-model="form.department"
            class="w-full h-11 px-4 rounded-lg border border-slate-200 dark:border-slate-700 bg-white dark:bg-[#111418] text-slate-900 dark:text-white focus:outline-none focus:border-primary transition-all"
          >
            <option value="">选择院系</option>
            <option value="物理系">物理系</option>
            <option value="数学系">数学系</option>
            <option value="化学系">化学系</option>
            <option value="英语系">英语系</option>
            <option value="计算机系">计算机系</option>
            <option value="中文系">中文系</option>
          </select>
        </div>

        <!-- Status -->
        <div>
          <label class="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-1.5">
            状态
          </label>
          <div class="flex gap-3">
            <button
              v-for="status in statuses"
              :key="status.value"
              type="button"
              @click="form.status = status.value"
              :class="[
                'flex-1 h-11 rounded-lg border text-sm font-medium transition-all',
                form.status === status.value
                  ? form.status === 'Active'
                    ? 'border-green-500 bg-green-50 dark:bg-green-900/20 text-green-700 dark:text-green-400'
                    : 'border-red-500 bg-red-50 dark:bg-red-900/20 text-red-700 dark:text-red-400'
                  : 'border-slate-200 dark:border-slate-700 text-slate-600 dark:text-slate-400 hover:border-slate-300 dark:hover:border-slate-600'
              ]"
            >
              {{ status.label }}
            </button>
          </div>
        </div>
      </div>

      <!-- Footer -->
      <div class="px-6 py-4 border-t border-slate-200 dark:border-slate-700 flex justify-end gap-3">
        <button
          @click="$emit('close')"
          class="px-4 py-2 rounded-lg text-slate-600 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-800 transition-all"
        >
          取消
        </button>
        <button
          @click="handleSubmit"
          :disabled="!isValid || loading"
          class="px-4 py-2 rounded-lg bg-primary text-white hover:bg-blue-600 transition-all disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-2"
        >
          <span v-if="loading" class="material-symbols-outlined text-lg animate-spin">progress_activity</span>
          <span>保存</span>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const props = defineProps({
  user: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['close', 'save'])

const loading = ref(false)

const form = ref({
  id: props.user.id,
  name: props.user.name,
  email: props.user.email,
  role: props.user.role,
  department: props.user.department || '',
  status: props.user.status
})

const roles = [
  { value: 'ADMIN', label: '管理员' },
  { value: 'TEACHER', label: '教师' },
  { value: 'STUDENT', label: '学生' }
]

const statuses = [
  { value: 'Active', label: '活跃' },
  { value: 'Inactive', label: '停用' }
]

const isValid = computed(() => {
  return form.value.name.trim() && 
         form.value.email.trim() && 
         /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.value.email)
})

async function handleSubmit() {
  if (!isValid.value) return
  
  loading.value = true
  try {
    await emit('save', { ...form.value })
  } finally {
    loading.value = false
  }
}
</script>
