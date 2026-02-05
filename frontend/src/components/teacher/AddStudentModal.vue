<template>
  <div class="fixed inset-0 bg-black/50 flex items-center justify-center z-50 overflow-y-auto py-4 px-4" @click="$emit('close')">
    <div class="bg-white dark:bg-[#1a222c] rounded-lg w-full max-w-lg max-h-[85vh] flex flex-col" @click.stop>
      <!-- Header -->
      <div class="px-4 sm:px-6 py-3 sm:py-4 border-b border-slate-200 dark:border-slate-700 flex-shrink-0">
        <div class="flex items-center justify-between">
          <div>
            <h3 class="text-base sm:text-lg font-semibold text-slate-900 dark:text-white">添加学生</h3>
            <p class="text-xs sm:text-sm text-slate-500 dark:text-slate-400 mt-0.5">添加学生到您的班级</p>
          </div>
          <button
            @click="$emit('close')"
            class="p-1.5 rounded-lg text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-800 transition-all"
          >
            <span class="material-symbols-outlined text-lg sm:text-xl">close</span>
          </button>
        </div>
      </div>

      <!-- Tabs -->
      <div class="flex border-b border-slate-200 dark:border-slate-700 px-4 sm:px-6 flex-shrink-0">
        <button
          @click="activeTab = 'single'"
          :class="[
            'px-3 sm:px-4 py-2 sm:py-3 text-xs sm:text-sm font-medium border-b-2 transition-all -mb-px',
            activeTab === 'single'
              ? 'border-primary text-primary'
              : 'border-transparent text-slate-500 hover:text-slate-700 dark:hover:text-slate-300'
          ]"
        >
          单个添加
        </button>
        <button
          @click="activeTab = 'batch'"
          :class="[
            'px-3 sm:px-4 py-2 sm:py-3 text-xs sm:text-sm font-medium border-b-2 transition-all -mb-px',
            activeTab === 'batch'
              ? 'border-primary text-primary'
              : 'border-transparent text-slate-500 hover:text-slate-700 dark:hover:text-slate-300'
          ]"
        >
          批量添加
        </button>
      </div>

      <!-- Content - Scrollable -->
      <div class="flex-1 overflow-y-auto p-4 sm:p-6">
        <!-- Single Add Form -->
        <div v-if="activeTab === 'single'" class="space-y-4">
          <div>
            <label class="block text-xs sm:text-sm font-medium text-slate-700 dark:text-slate-300 mb-1.5 sm:mb-2">
              用户名或邮箱 <span class="text-red-500">*</span>
            </label>
            <input
              v-model="identifier"
              type="text"
              placeholder="请输入学生的用户名或邮箱"
              class="w-full h-10 sm:h-11 px-3 sm:px-4 rounded-lg border border-slate-200 dark:border-slate-700 bg-white dark:bg-[#111418] text-slate-900 dark:text-white placeholder:text-slate-400 focus:outline-none focus:border-primary transition-all text-sm"
              @keyup.enter="handleSingleAdd"
            />
          </div>
          <p class="text-xs text-slate-400 dark:text-slate-500">
            <span class="material-symbols-outlined text-xs align-middle mr-1">info</span>
            输入学生的用户名或注册邮箱，系统将自动查找并添加
          </p>
        </div>

        <!-- Batch Add Form -->
        <div v-if="activeTab === 'batch'" class="space-y-4">
          <div>
            <label class="block text-xs sm:text-sm font-medium text-slate-700 dark:text-slate-300 mb-1.5 sm:mb-2">
              上传文件 <span class="text-red-500">*</span>
            </label>
            <div
              class="border-2 border-dashed border-slate-200 dark:border-slate-700 rounded-lg p-6 sm:p-8 text-center hover:border-primary transition-all cursor-pointer"
              :class="{ 'border-primary bg-primary/5': isDragging }"
              @click="triggerFileInput"
              @dragover.prevent="isDragging = true"
              @dragleave.prevent="isDragging = false"
              @drop.prevent="handleFileDrop"
            >
              <input
                ref="fileInput"
                type="file"
                accept=".csv,.xlsx,.xls"
                class="hidden"
                @change="handleFileSelect"
              />
              <span class="material-symbols-outlined text-3xl sm:text-4xl text-slate-300 dark:text-slate-600">
                {{ selectedFile ? 'description' : 'upload_file' }}
              </span>
              <p class="text-slate-600 dark:text-slate-400 mt-2 text-sm">
                {{ selectedFile ? selectedFile.name : '点击或拖拽文件到此处' }}
              </p>
              <p class="text-xs text-slate-400 mt-1">支持 CSV、Excel 格式</p>
              <button
                v-if="selectedFile"
                @click.stop="selectedFile = null"
                class="mt-2 text-xs text-red-500 hover:text-red-600"
              >
                清除选择
              </button>
            </div>
          </div>

          <!-- File Format Help -->
          <div class="bg-slate-50 dark:bg-slate-800/50 rounded-lg p-3 sm:p-4">
            <p class="text-xs sm:text-sm font-medium text-slate-700 dark:text-slate-300 mb-2">文件格式说明：</p>
            <ul class="text-xs text-slate-500 space-y-1">
              <li>• 每行一个学生标识（用户名或邮箱）</li>
              <li>• CSV文件：直接列出标识，无需表头</li>
              <li>• Excel文件：第一列为学生标识</li>
            </ul>
          </div>

          <!-- Batch Result -->
          <div v-if="batchResult" class="bg-slate-50 dark:bg-slate-800/50 rounded-lg p-3 sm:p-4 space-y-2">
            <p class="text-sm font-medium text-slate-700 dark:text-slate-300">
              处理完成：共 {{ batchResult.totalProcessed }} 条
            </p>
            <div class="grid grid-cols-3 gap-2 text-center">
              <div class="bg-green-50 dark:bg-green-900/20 rounded-lg p-2">
                <p class="text-lg font-bold text-green-600">{{ batchResult.successCount }}</p>
                <p class="text-xs text-green-600">成功</p>
              </div>
              <div class="bg-yellow-50 dark:bg-yellow-900/20 rounded-lg p-2">
                <p class="text-lg font-bold text-yellow-600">{{ batchResult.skippedCount }}</p>
                <p class="text-xs text-yellow-600">跳过</p>
              </div>
              <div class="bg-red-50 dark:bg-red-900/20 rounded-lg p-2">
                <p class="text-lg font-bold text-red-600">{{ batchResult.failedCount }}</p>
                <p class="text-xs text-red-600">失败</p>
              </div>
            </div>
            <!-- Failed Details -->
            <div v-if="batchResult.failedList && batchResult.failedList.length > 0" class="mt-3 pt-3 border-t border-slate-200 dark:border-slate-700">
              <p class="text-xs font-medium text-red-600 mb-2">失败详情：</p>
              <div class="max-h-24 overflow-y-auto space-y-1">
                <p v-for="(item, index) in batchResult.failedList" :key="index" class="text-xs text-slate-500">
                  {{ item.identifier }}: {{ item.reason }}
                </p>
              </div>
            </div>
          </div>
        </div>

        <!-- Error Message -->
        <div v-if="errorMessage" class="mt-4 p-3 bg-red-50 dark:bg-red-900/20 rounded-lg flex items-start gap-2">
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
            {{ batchResult ? '关闭' : '取消' }}
          </button>
          <button
            v-if="activeTab === 'single'"
            @click="handleSingleAdd"
            :disabled="!identifier.trim() || loading"
            class="w-full sm:w-auto px-4 py-2 rounded-lg bg-primary text-white hover:bg-blue-600 transition-all disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2 text-sm"
          >
            <span v-if="loading" class="material-symbols-outlined text-lg animate-spin">progress_activity</span>
            {{ loading ? '添加中...' : '确认添加' }}
          </button>
          <button
            v-if="activeTab === 'batch' && !batchResult"
            @click="handleBatchAdd"
            :disabled="!selectedFile || loading"
            class="w-full sm:w-auto px-4 py-2 rounded-lg bg-primary text-white hover:bg-blue-600 transition-all disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2 text-sm"
          >
            <span v-if="loading" class="material-symbols-outlined text-lg animate-spin">progress_activity</span>
            {{ loading ? '导入中...' : '开始导入' }}
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

const activeTab = ref('single')
const identifier = ref('')
const selectedFile = ref(null)
const fileInput = ref(null)
const loading = ref(false)
const errorMessage = ref('')
const batchResult = ref(null)
const isDragging = ref(false)

// Trigger file input click
function triggerFileInput() {
  fileInput.value?.click()
}

// Handle file selection
function handleFileSelect(event) {
  const file = event.target.files?.[0]
  if (file) {
    selectedFile.value = file
    errorMessage.value = ''
    batchResult.value = null
  }
}

// Handle file drop
function handleFileDrop(event) {
  isDragging.value = false
  const file = event.dataTransfer.files?.[0]
  if (file) {
    const validTypes = ['.csv', '.xlsx', '.xls']
    const fileExt = '.' + file.name.split('.').pop().toLowerCase()
    if (validTypes.includes(fileExt)) {
      selectedFile.value = file
      errorMessage.value = ''
      batchResult.value = null
    } else {
      errorMessage.value = '请上传 CSV 或 Excel 格式的文件'
    }
  }
}

// Handle single add
async function handleSingleAdd() {
  if (!identifier.value.trim()) return
  
  loading.value = true
  errorMessage.value = ''
  
  const result = await teacherStudentStore.addStudent(identifier.value.trim())
  
  loading.value = false
  
  if (result.success) {
    emit('success', '学生添加成功')
    emit('close')
  } else {
    errorMessage.value = result.message || '添加学生失败'
  }
}

// Handle batch add
async function handleBatchAdd() {
  if (!selectedFile.value) return
  
  loading.value = true
  errorMessage.value = ''
  
  const result = await teacherStudentStore.batchAddStudents(selectedFile.value)
  
  loading.value = false
  
  if (result.success) {
    batchResult.value = result.data
    emit('success', `批量添加完成：成功 ${result.data.successCount} 条`)
  } else {
    errorMessage.value = result.message || '批量添加失败'
  }
}
</script>
