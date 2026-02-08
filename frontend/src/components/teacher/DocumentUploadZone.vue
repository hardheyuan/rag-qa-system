<template>
  <div class="bg-white dark:bg-card-dark border border-slate-200 dark:border-card-border rounded-xl p-6 shadow-sm">
    <div class="flex items-center justify-between mb-6">
      <div class="flex items-center gap-3">
        <div class="w-10 h-10 rounded-lg bg-primary/10 flex items-center justify-center">
          <span class="material-symbols-outlined text-primary text-2xl">cloud_upload</span>
        </div>
        <div>
          <h3 class="text-lg font-semibold text-slate-900 dark:text-white">上传文档</h3>
          <p class="text-sm text-slate-500 dark:text-slate-400">支持 PDF、DOCX、PPTX 格式，单个文件不超过 50MB</p>
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
        accept=".pdf,.docx,.pptx,application/pdf,application/vnd.openxmlformats-officedocument.wordprocessingml.document,application/vnd.openxmlformats-officedocument.presentationml.presentation"
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
          <span class="px-2 py-1 rounded bg-blue-100 dark:bg-blue-900/30 text-blue-600 dark:text-blue-400 text-xs font-medium">DOCX</span>
          <span class="px-2 py-1 rounded bg-orange-100 dark:bg-orange-900/30 text-orange-600 dark:text-orange-400 text-xs font-medium">PPTX</span>
        </div>
         <p class="text-xs text-slate-400 dark:text-slate-500 mt-2">
           单个文件最大 50MB · 扫描版 PDF 将自动使用 OCR 识别
         </p>
      </div>
    </div>

    <!-- Uploading Indicator -->
    <div v-if="isUploading" class="mt-6 p-4 bg-primary/5 dark:bg-primary/10 rounded-lg border border-primary/20">
      <div class="flex items-center gap-3">
        <div class="w-5 h-5 border-2 border-primary border-t-transparent rounded-full animate-spin"></div>
        <span class="text-sm font-medium text-slate-700 dark:text-slate-300">正在上传文件，请稍候...</span>
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
const isUploading = ref(false)

const MAX_FILE_SIZE = 50 * 1024 * 1024 // 50MB
const ALLOWED_TYPES = [
  'application/pdf',
  'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
  'application/vnd.openxmlformats-officedocument.presentationml.presentation'
]
const ALLOWED_EXTENSIONS = ['.pdf', '.docx', '.pptx']

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

async function processFiles(files) {
  for (const file of files) {
    // Validate file type
    const extension = '.' + file.name.split('.').pop().toLowerCase()
    const isValidType = ALLOWED_TYPES.includes(file.type) || ALLOWED_EXTENSIONS.includes(extension)

    if (!isValidType) {
      alert(`不支持的文件格式: ${file.name}\n仅支持 PDF、DOCX、PPTX 文件`)
      continue
    }

    // Validate file size
    if (file.size > MAX_FILE_SIZE) {
      alert(`文件过大: ${file.name}\n单个文件不能超过 50MB`)
      continue
    }

    // Upload file
    await uploadFile(file)
  }
}

async function uploadFile(file) {
  isUploading.value = true

  try {
    const result = await documentStore.uploadDocument(file)

    if (result.success) {
      emit('upload-success')
    } else {
      alert(`上传失败: ${result.message || '未知错误'}`)
    }
  } catch (error) {
    alert(`上传失败: ${error.message || '未知错误'}`)
  } finally {
    isUploading.value = false
  }
}

function formatFileSize(bytes) {
  if (bytes === 0) return '0 Bytes'
  const k = 1024
  const sizes = ['Bytes', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}
</script>
