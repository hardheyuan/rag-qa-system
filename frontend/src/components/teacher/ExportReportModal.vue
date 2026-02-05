<template>
  <div class="fixed inset-0 bg-black/50 flex items-center justify-center z-50 overflow-y-auto py-4 px-4" @click="$emit('close')">
    <div class="bg-white dark:bg-[#1a222c] rounded-lg w-full max-w-md max-h-[85vh] flex flex-col" @click.stop>
      <!-- Header -->
      <div class="px-4 sm:px-6 py-3 sm:py-4 border-b border-slate-200 dark:border-slate-700 flex-shrink-0">
        <div class="flex items-center justify-between">
          <div>
            <h3 class="text-base sm:text-lg font-semibold text-slate-900 dark:text-white">导出学生报表</h3>
            <p class="text-xs sm:text-sm text-slate-500 dark:text-slate-400 mt-0.5">导出班级学生数据到文件</p>
          </div>
          <button
            @click="$emit('close')"
            class="p-1.5 rounded-lg text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-800 transition-all"
          >
            <span class="material-symbols-outlined text-lg sm:text-xl">close</span>
          </button>
        </div>
      </div>

      <!-- Content - Scrollable -->
      <div class="flex-1 overflow-y-auto p-4 sm:p-6 space-y-4">
        <!-- Date Range -->
        <div>
          <label class="block text-xs sm:text-sm font-medium text-slate-700 dark:text-slate-300 mb-1.5 sm:mb-2">
            活动数据日期范围
            <span class="text-slate-400 font-normal">（可选）</span>
          </label>
          <div class="grid grid-cols-2 gap-2 sm:gap-3">
            <div>
              <label class="block text-[10px] sm:text-xs text-slate-500 mb-1">开始日期</label>
              <input
                v-model="startDate"
                type="date"
                class="w-full h-9 sm:h-10 px-2 sm:px-3 rounded-lg border border-slate-200 dark:border-slate-700 bg-white dark:bg-[#111418] text-slate-900 dark:text-white focus:outline-none focus:border-primary transition-all text-xs sm:text-sm"
              />
            </div>
            <div>
              <label class="block text-[10px] sm:text-xs text-slate-500 mb-1">结束日期</label>
              <input
                v-model="endDate"
                type="date"
                class="w-full h-9 sm:h-10 px-2 sm:px-3 rounded-lg border border-slate-200 dark:border-slate-700 bg-white dark:bg-[#111418] text-slate-900 dark:text-white focus:outline-none focus:border-primary transition-all text-xs sm:text-sm"
              />
            </div>
          </div>
          <p class="text-[10px] sm:text-xs text-slate-400 mt-1.5">
            <span class="material-symbols-outlined text-[10px] sm:text-xs align-middle mr-0.5">info</span>
            不选择日期则导出全部数据
          </p>
        </div>

        <!-- Format Selection -->
        <div>
          <label class="block text-xs sm:text-sm font-medium text-slate-700 dark:text-slate-300 mb-1.5 sm:mb-2">
            导出格式
          </label>
          <div class="grid grid-cols-2 gap-2 sm:gap-3">
            <button
              type="button"
              @click="format = 'csv'"
              :class="[
                'h-12 sm:h-14 rounded-lg border text-sm font-medium transition-all flex flex-col items-center justify-center gap-1',
                format === 'csv'
                  ? 'border-primary bg-primary/10 text-primary'
                  : 'border-slate-200 dark:border-slate-700 text-slate-600 dark:text-slate-400 hover:border-slate-300 dark:hover:border-slate-600'
              ]"
            >
              <span class="material-symbols-outlined text-lg sm:text-xl">description</span>
              <span class="text-xs sm:text-sm">CSV</span>
            </button>
            <button
              type="button"
              @click="format = 'excel'"
              :class="[
                'h-12 sm:h-14 rounded-lg border text-sm font-medium transition-all flex flex-col items-center justify-center gap-1',
                format === 'excel'
                  ? 'border-green-500 bg-green-50 dark:bg-green-900/20 text-green-600'
                  : 'border-slate-200 dark:border-slate-700 text-slate-600 dark:text-slate-400 hover:border-slate-300 dark:hover:border-slate-600'
              ]"
            >
              <span class="material-symbols-outlined text-lg sm:text-xl">table_chart</span>
              <span class="text-xs sm:text-sm">Excel</span>
            </button>
          </div>
        </div>

        <!-- Export Content Info -->
        <div class="bg-slate-50 dark:bg-slate-800/50 rounded-lg p-3 sm:p-4">
          <p class="text-xs sm:text-sm font-medium text-slate-700 dark:text-slate-300 mb-2 flex items-center gap-1.5">
            <span class="material-symbols-outlined text-base sm:text-lg text-slate-400">list_alt</span>
            导出内容包括：
          </p>
          <ul class="text-[10px] sm:text-xs text-slate-500 space-y-1 ml-6">
            <li class="flex items-center gap-1.5">
              <span class="w-1 h-1 rounded-full bg-slate-400"></span>
              学生基本信息（用户名、真实姓名、邮箱）
            </li>
            <li class="flex items-center gap-1.5">
              <span class="w-1 h-1 rounded-full bg-slate-400"></span>
              总提问数
            </li>
            <li class="flex items-center gap-1.5">
              <span class="w-1 h-1 rounded-full bg-slate-400"></span>
              总文档访问数
            </li>
            <li class="flex items-center gap-1.5">
              <span class="w-1 h-1 rounded-full bg-slate-400"></span>
              最后活动时间
            </li>
          </ul>
        </div>

        <!-- Error Message -->
        <div v-if="errorMessage" class="p-3 bg-red-50 dark:bg-red-900/20 rounded-lg flex items-start gap-2">
          <span class="material-symbols-outlined text-red-500 text-lg flex-shrink-0">error</span>
          <p class="text-sm text-red-600 dark:text-red-400">{{ errorMessage }}</p>
        </div>
      </div>

      <!-- Footer -->
      <div class="px-4 sm:px-6 py-3 sm:py-4 border-t border-slate-200 dark:border-slate-700 flex-shrink-0">
        <div class="flex flex-col-reverse sm:flex-row justify-end gap-2 sm:gap-3">
          <button
            @click="$emit('close')"
            class="w-full sm:w-auto px-4 py-2 rounded-lg text-slate-600 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-800 transition-all text-sm"
          >
            取消
          </button>
          <button
            @click="handleExport"
            :disabled="loading"
            class="w-full sm:w-auto px-4 py-2 rounded-lg bg-green-500 text-white hover:bg-green-600 transition-all disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2 text-sm"
          >
            <span v-if="loading" class="material-symbols-outlined text-lg animate-spin">progress_activity</span>
            <span v-else class="material-symbols-outlined text-lg">download</span>
            {{ loading ? '导出中...' : '导出报表' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useTeacherStudentStore } from '../../stores/teacherStudent'

const emit = defineEmits(['close', 'success'])
const teacherStudentStore = useTeacherStudentStore()

const startDate = ref('')
const endDate = ref('')
const format = ref('csv')
const loading = ref(false)
const errorMessage = ref('')

// Handle export
async function handleExport() {
  loading.value = true
  errorMessage.value = ''
  
  // Format dates for API
  const formattedStartDate = startDate.value ? `${startDate.value}T00:00:00` : null
  const formattedEndDate = endDate.value ? `${endDate.value}T23:59:59` : null
  
  const result = await teacherStudentStore.exportReport(
    formattedStartDate,
    formattedEndDate,
    format.value
  )
  
  loading.value = false
  
  if (result.success) {
    emit('success')
    emit('close')
  } else {
    errorMessage.value = result.message || '导出失败'
  }
}
</script>
