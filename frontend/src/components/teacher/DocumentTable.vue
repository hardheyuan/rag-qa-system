<template>
  <div class="bg-white dark:bg-card-dark border border-slate-200 dark:border-card-border rounded-xl p-0 overflow-hidden shadow-sm flex flex-col h-full">
    <!-- Header -->
    <div class="p-6 border-b border-slate-200 dark:border-slate-800 flex justify-between items-center">
      <h3 class="text-base font-semibold text-slate-900 dark:text-white">文档检索与覆盖率</h3>
      <button 
        @click="refreshStats"
        class="text-xs font-medium text-primary hover:text-primary/80 transition-colors flex items-center gap-1"
        :disabled="loading"
      >
        <span v-if="loading" class="material-symbols-outlined text-sm animate-spin">refresh</span>
        <span v-else class="material-symbols-outlined text-sm">refresh</span>
        {{ loading ? '刷新中...' : '刷新数据' }}
      </button>
    </div>

    <!-- Table -->
    <div class="flex-1 overflow-x-auto">
      <table class="w-full text-sm text-left">
        <thead class="text-xs text-slate-500 dark:text-slate-400 uppercase bg-slate-50 dark:bg-[#151b23]">
          <tr>
            <th class="px-6 py-3 font-medium" scope="col">文档名称</th>
            <th class="px-6 py-3 font-medium" scope="col">被检索次数</th>
            <th class="px-6 py-3 font-medium min-w-[160px]" scope="col">上下文覆盖率</th>
            <th class="px-6 py-3 font-medium" scope="col">状态</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-slate-200 dark:divide-slate-800">
          <tr 
            v-for="doc in documents" 
            :key="doc.id"
            class="bg-white dark:bg-card-dark hover:bg-slate-50 dark:hover:bg-slate-800/50 transition-colors"
          >
            <td class="px-6 py-4 font-medium text-slate-900 dark:text-white">
              <div class="flex items-center gap-3">
                <div class="p-1.5 rounded bg-red-100 dark:bg-red-900/30 text-red-600 dark:text-red-400">
                  <span class="material-symbols-outlined text-lg">{{ getFileIcon(doc.type) }}</span>
                </div>
                <div>
                  <div class="truncate max-w-[150px]" :title="doc.name">{{ doc.name }}</div>
                  <div class="text-[10px] text-slate-400 font-normal">{{ formatFileSize(doc.size) }}</div>
                </div>
              </div>
            </td>
            <td class="px-6 py-4 text-slate-600 dark:text-slate-300">{{ doc.retrievals }}</td>
            <td class="px-6 py-4 align-middle">
              <div class="w-full max-w-[140px]">
                <div class="flex justify-between text-[10px] mb-1 font-medium">
                  <span :class="getCoverageLabelClass(doc.coverage)">{{ getCoverageLabel(doc.coverage) }}</span>
                  <span class="text-slate-500">{{ doc.coverage }}%</span>
                </div>
                <div class="w-full bg-slate-100 dark:bg-slate-700 rounded-full h-1.5 overflow-hidden">
                  <div 
                    class="h-1.5 rounded-full transition-all duration-500"
                    :class="getCoverageBarClass(doc.coverage)"
                    :style="{ width: doc.coverage + '%' }"
                  ></div>
                </div>
              </div>
            </td>
            <td class="px-6 py-4">
              <span 
                class="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium"
                :class="getStatusClass(doc.status)"
              >
                {{ getStatusLabel(doc.status) }}
              </span>
            </td>
          </tr>
          <tr v-if="documents.length === 0 && !loading">
            <td colspan="4" class="px-6 py-12 text-center text-slate-400 dark:text-slate-500">
              暂无文档数据
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useDocumentStore } from '../../stores/documents'

const documentStore = useDocumentStore()
const documents = ref([])
const loading = ref(false)

// 加载文档统计
async function loadDocumentStats() {
  loading.value = true
  try {
    const stats = await documentStore.fetchDocumentStats()
    documents.value = stats
  } catch (error) {
    console.error('加载文档统计失败:', error)
  } finally {
    loading.value = false
  }
}

// 刷新数据
function refreshStats() {
  loadDocumentStats()
}

// 获取文件图标
function getFileIcon(type) {
  const icons = { 
    PDF: 'picture_as_pdf', 
    DOCX: 'description', 
    DOC: 'description', 
    PPTX: 'slideshow', 
    PPT: 'slideshow', 
    TXT: 'text_snippet' 
  }
  return icons[type?.toUpperCase()] || 'insert_drive_file'
}

// 格式化文件大小
function formatFileSize(bytes) {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

function getCoverageLabel(coverage) {
  if (coverage >= 80) return '高'
  if (coverage >= 50) return '中'
  return '低'
}

function getCoverageLabelClass(coverage) {
  if (coverage >= 80) return 'text-emerald-600 dark:text-emerald-400'
  if (coverage >= 50) return 'text-amber-600 dark:text-amber-400'
  return 'text-red-600 dark:text-red-400'
}

function getCoverageBarClass(coverage) {
  if (coverage >= 80) return 'bg-emerald-500'
  if (coverage >= 50) return 'bg-amber-500'
  return 'bg-red-500'
}

function getStatusLabel(status) {
  const labels = {
    'SUCCESS': '处理完成',
    'PROCESSING': '处理中',
    'UPLOADING': '上传中',
    'FAILED': '处理失败'
  }
  return labels[status] || status
}

function getStatusClass(status) {
  const classes = {
    'SUCCESS': 'bg-emerald-100 dark:bg-emerald-900/30 text-emerald-800 dark:text-emerald-400',
    'PROCESSING': 'bg-amber-100 dark:bg-amber-900/30 text-amber-800 dark:text-amber-400',
    'UPLOADING': 'bg-blue-100 dark:bg-blue-900/30 text-blue-800 dark:text-blue-400',
    'FAILED': 'bg-red-100 dark:bg-red-900/30 text-red-800 dark:text-red-400'
  }
  return classes[status] || classes['FAILED']
}

// 组件挂载时加载数据
onMounted(() => {
  loadDocumentStats()
})
</script>
