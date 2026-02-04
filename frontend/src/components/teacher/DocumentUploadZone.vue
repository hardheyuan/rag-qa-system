<template>
  <div class="bg-white dark:bg-card-dark border border-slate-200 dark:border-card-border rounded-xl p-6 shadow-sm">
    <div class="flex items-center justify-between mb-6">
      <div class="flex items-center gap-3">
        <div class="w-10 h-10 rounded-lg bg-primary/10 flex items-center justify-center">
          <span class="material-symbols-outlined text-primary text-2xl">cloud_upload</span>
        </div>
        <div>
          <h3 class="text-lg font-semibold text-slate-900 dark:text-white">上传文档</h3>
          <p class="text-sm text-slate-500 dark:text-slate-400">支持 PDF、Word、PPT 格式，单个文件不超过 50MB</p>
        </div>
      </div>
    </div>

    <!-- Upload Zone -->
    <div
      ref="dropZone"
      @dragover.prevent="handleDragOver"
      @dragleave.prevent="handleDragLeave"
      @drop.prevent="handleDrop"
      @click="triggerFileInput"
      :class="[
        'relative border-2 border-dashed rounded-xl p-8 text-center cursor-pointer transition-all duration-300',
        isDragging 
          ? 'border-primary bg-primary/5 dark:bg-primary/10' 
          : 'border-slate-300 dark:border-slate-600 hover:border-primary/50 hover:bg-slate-50 dark:hover:bg-slate-800/50'
      ]"
    >
      <input
        ref="fileInput"
        type="file"
        multiple
        accept=".pdf,.doc,.docx,.ppt,.pptx,application/pdf,application/msword,application/vnd.openxmlformats-officedocument.wordprocessingml.document,application/vnd.ms-powerpoint,application/vnd.openxmlformats-officedocument.presentationml.presentation"
        class="hidden"
        @change="handleFileSelect"
      />
      
      <div class="flex flex-col items-center gap-3">
        <div class="w-16 h-16 rounded-full bg-slate-100 dark:bg-slate-700 flex items-center justify-center">
          <span class="material-symbols-outlined text-slate-400 dark:text-slate-500 text-3xl">upload_file</span>
        </div>
        <div>
          <p class="text-base font-medium text-slate-700 dark:text-slate-300">
            拖拽文件到此处上传
          </p>
          <p class="text-sm text-slate-500 dark:text-slate-400 mt-1">
            或点击选择文件
          </p>
        </div>
        <div class="flex items-center gap-2 mt-2">
          <span class="px-2 py-1 rounded bg-red-100 dark:bg-red-900/30 text-red-600 dark:text-red-400 text-xs font-medium">PDF</span>
          <span class="px-2 py-1 rounded bg-blue-100 dark:bg-blue-900/30 text-blue-600 dark:text-blue-400 text-xs font-medium">Word</span>
          <span class="px-2 py-1 rounded bg-orange-100 dark:bg-orange-900/30 text-orange-600 dark:text-orange-400 text-xs font-medium">PPT</span>
        </div>
         <p class="text-xs text-slate-400 dark:text-slate-500 mt-2">
           单个文件最大 50MB · 扫描版 PDF 将自动使用 OCR 识别
         </p>
      </div>
    </div>

    <!-- Upload Progress List -->
    <div v-if="uploadTasks.length > 0" class="mt-6 space-y-3">
      <div class="flex items-center justify-between">
        <h4 class="text-sm font-semibold text-slate-700 dark:text-slate-300">上传进度</h4>
        <button 
          @click="clearCompleted"
          class="text-xs text-slate-400 hover:text-primary transition"
        >
          清除已完成
        </button>
      </div>
      
      <div v-for="task in uploadTasks" :key="task.id" class="bg-slate-50 dark:bg-slate-800/50 rounded-lg p-3">
        <div class="flex items-center gap-3 mb-2">
          <div class="w-8 h-8 rounded flex items-center justify-center shrink-0" :class="getFileTypeClass(task.file.name)">
            <span class="material-symbols-outlined text-white text-sm">{{ getFileIcon(task.file.name) }}</span>
          </div>
          <div class="flex-1 min-w-0">
            <p class="text-sm font-medium text-slate-700 dark:text-slate-300 truncate">{{ task.file.name }}</p>
            <p class="text-xs text-slate-400">{{ formatFileSize(task.file.size) }}</p>
          </div>
          <div class="flex items-center gap-2">
            <span v-if="task.status === 'pending'" class="text-xs text-slate-400">等待中</span>
            <span v-if="task.status === 'uploading'" class="text-xs text-primary">{{ task.progress }}%</span>
            <span v-if="task.status === 'success'" class="material-symbols-outlined text-emerald-500 text-lg">check_circle</span>
            <span v-if="task.status === 'error'" class="material-symbols-outlined text-red-500 text-lg">error</span>
            <button 
              v-if="task.status === 'error'" 
              @click="retryUpload(task)"
              class="text-xs text-primary hover:text-primary/80 transition"
            >
              重试
            </button>
          </div>
        </div>
        
        <!-- Progress Bar -->
        <div v-if="task.status === 'uploading' || task.status === 'pending'" class="w-full bg-slate-200 dark:bg-slate-700 rounded-full h-1.5 overflow-hidden">
          <div 
            class="bg-primary h-1.5 rounded-full transition-all duration-300"
            :style="{ width: task.progress + '%' }"
          ></div>
        </div>
        
        <!-- Error Message -->
        <p v-if="task.status === 'error' && task.error" class="text-xs text-red-500 mt-2">
          {{ task.error }}
        </p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useDocumentStore } from '../../stores/documents'

const emit = defineEmits(['upload-success'])
const documentStore = useDocumentStore()

const dropZone = ref(null)
const fileInput = ref(null)
const isDragging = ref(false)
const uploadTasks = ref([])

const MAX_FILE_SIZE = 50 * 1024 * 1024 // 50MB
const ALLOWED_TYPES = [
  'application/pdf',
  'application/msword',
  'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
  'application/vnd.ms-powerpoint',
  'application/vnd.openxmlformats-officedocument.presentationml.presentation'
]
const ALLOWED_EXTENSIONS = ['.pdf', '.doc', '.docx', '.ppt', '.pptx']

function triggerFileInput() {
  fileInput.value?.click()
}

function handleDragOver() {
  isDragging.value = true
}

function handleDragLeave() {
  isDragging.value = false
}

function handleDrop(e) {
  isDragging.value = false
  const files = Array.from(e.dataTransfer.files)
  processFiles(files)
}

function handleFileSelect(e) {
  const files = Array.from(e.target.files)
  processFiles(files)
  e.target.value = '' // Reset input
}

function processFiles(files) {
  files.forEach(file => {
    // Validate file type
    const extension = '.' + file.name.split('.').pop().toLowerCase()
    const isValidType = ALLOWED_TYPES.includes(file.type) || ALLOWED_EXTENSIONS.includes(extension)
    
    if (!isValidType) {
      alert(`不支持的文件格式: ${file.name}\n仅支持 PDF、Word、PPT 文件`)
      return
    }
    
    // Validate file size
    if (file.size > MAX_FILE_SIZE) {
      alert(`文件过大: ${file.name}\n单个文件不能超过 50MB`)
      return
    }
    
    // Create upload task
    const task = {
      id: Date.now() + Math.random(),
      file: file,
      status: 'pending',
      progress: 0,
      error: null
    }
    
    uploadTasks.value.push(task)
    uploadFile(task)
  })
}

async function uploadFile(task) {
  task.status = 'uploading'
  task.progress = 0
  
  try {
    const result = await documentStore.uploadDocument(
      task.file,
      (progress) => {
        task.progress = progress
      }
    )
    
    if (result.success) {
      task.status = 'success'
      task.progress = 100
      emit('upload-success')
    } else {
      task.status = 'error'
      task.error = result.message || '上传失败'
    }
  } catch (error) {
    task.status = 'error'
    task.error = error.message || '上传失败'
  }
}

function retryUpload(task) {
  task.status = 'pending'
  task.progress = 0
  task.error = null
  uploadFile(task)
}

function clearCompleted() {
  uploadTasks.value = uploadTasks.value.filter(task => 
    task.status !== 'success' && task.status !== 'error'
  )
}

function getFileIcon(filename) {
  const ext = filename.split('.').pop().toLowerCase()
  const icons = {
    pdf: 'picture_as_pdf',
    doc: 'description',
    docx: 'description',
    ppt: 'slideshow',
    pptx: 'slideshow'
  }
  return icons[ext] || 'insert_drive_file'
}

function getFileTypeClass(filename) {
  const ext = filename.split('.').pop().toLowerCase()
  const classes = {
    pdf: 'bg-red-500',
    doc: 'bg-blue-500',
    docx: 'bg-blue-500',
    ppt: 'bg-orange-500',
    pptx: 'bg-orange-500'
  }
  return classes[ext] || 'bg-slate-500'
}

function formatFileSize(bytes) {
  if (bytes === 0) return '0 Bytes'
  const k = 1024
  const sizes = ['Bytes', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}
</script>
