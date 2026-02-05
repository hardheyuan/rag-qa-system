<template>
  <div class="flex h-screen w-full bg-background-light dark:bg-background-dark overflow-hidden font-display text-slate-900 dark:text-white">
    <!-- Sidebar -->
    <AdminSidebar />
    
    <!-- Main Content -->
    <main class="flex-1 flex flex-col h-full overflow-hidden relative">
      <!-- Header -->
      <header class="flex-shrink-0 px-8 py-6 border-b border-slate-200 dark:border-slate-800 bg-white/50 dark:bg-[#111418]/50 backdrop-blur-sm sticky top-0 z-10">
        <div class="flex flex-col md:flex-row md:items-center justify-between gap-4">
          <div>
            <h2 class="text-2xl font-bold text-slate-900 dark:text-white tracking-tight">文档管理</h2>
            <p class="text-slate-500 dark:text-slate-400 text-sm mt-1">
              共 {{ allDocuments.length }} 个文档
              <span v-if="processingCount > 0" class="text-amber-500 ml-2">({{ processingCount }} 个处理中)</span>
            </p>
          </div>
          <div class="flex items-center gap-3">
            <button 
              @click="refreshDocuments"
              :disabled="isRefreshing"
              class="flex items-center gap-2 px-4 py-2.5 rounded-lg border border-slate-200 dark:border-slate-700 text-slate-600 dark:text-slate-300 hover:bg-slate-50 dark:hover:bg-slate-800 transition disabled:opacity-50"
            >
              <span class="material-symbols-outlined text-lg" :class="{ 'animate-spin': isRefreshing }">refresh</span>
              <span class="text-sm font-medium">刷新</span>
            </button>
          </div>
        </div>
      </header>

      <!-- Content -->
      <div class="flex-1 overflow-y-auto p-8 scrollbar-custom">
        <div class="max-w-7xl mx-auto flex flex-col gap-6">
          <!-- Document Table -->
          <div class="bg-white dark:bg-[#1a222c] rounded-xl border border-slate-200 dark:border-slate-700 overflow-hidden">
            <div class="overflow-x-auto">
              <table class="w-full">
                <thead class="bg-slate-50 dark:bg-slate-800/50 border-b border-slate-200 dark:border-slate-700">
                  <tr>
                    <th class="px-6 py-3 text-left text-xs font-semibold text-slate-600 dark:text-slate-400 uppercase tracking-wider">文档名称</th>
                    <th class="px-6 py-3 text-left text-xs font-semibold text-slate-600 dark:text-slate-400 uppercase tracking-wider">上传者</th>
                    <th class="px-6 py-3 text-left text-xs font-semibold text-slate-600 dark:text-slate-400 uppercase tracking-wider">类型</th>
                    <th class="px-6 py-3 text-left text-xs font-semibold text-slate-600 dark:text-slate-400 uppercase tracking-wider">状态</th>
                    <th class="px-6 py-3 text-left text-xs font-semibold text-slate-600 dark:text-slate-400 uppercase tracking-wider">上传时间</th>
                    <th class="px-6 py-3 text-right text-xs font-semibold text-slate-600 dark:text-slate-400 uppercase tracking-wider">操作</th>
                  </tr>
                </thead>
                <tbody class="divide-y divide-slate-200 dark:divide-slate-700">
                  <tr v-if="isLoading" class="hover:bg-slate-50 dark:hover:bg-slate-800/50">
                    <td colspan="6" class="px-6 py-8 text-center text-slate-500 dark:text-slate-400">
                      <span class="material-symbols-outlined animate-spin text-2xl">progress_activity</span>
                      <p class="mt-2">加载中...</p>
                    </td>
                  </tr>
                  <tr v-else-if="allDocuments.length === 0" class="hover:bg-slate-50 dark:hover:bg-slate-800/50">
                    <td colspan="6" class="px-6 py-8 text-center text-slate-500 dark:text-slate-400">
                      <span class="material-symbols-outlined text-4xl">folder_open</span>
                      <p class="mt-2">暂无文档</p>
                    </td>
                  </tr>
                  <tr v-else v-for="doc in allDocuments" :key="doc.id" class="hover:bg-slate-50 dark:hover:bg-slate-800/50 transition">
                    <td class="px-6 py-4">
                      <div class="flex items-center gap-3">
                        <div class="w-10 h-10 rounded-lg bg-blue-100 dark:bg-blue-900/30 flex items-center justify-center">
                          <span class="material-symbols-outlined text-blue-600 dark:text-blue-400">{{ getFileIcon(doc.fileType) }}</span>
                        </div>
                        <div>
                          <p class="text-sm font-medium text-slate-900 dark:text-white">{{ doc.filename }}</p>
                          <p class="text-xs text-slate-500 dark:text-slate-400">{{ formatFileSize(doc.fileSize) }}</p>
                        </div>
                      </div>
                    </td>
                    <td class="px-6 py-4">
                      <p class="text-sm text-slate-600 dark:text-slate-300">{{ doc.uploaderName || '未知用户' }}</p>
                    </td>
                    <td class="px-6 py-4">
                      <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-slate-100 dark:bg-slate-700 text-slate-700 dark:text-slate-300">
                        {{ doc.fileType }}
                      </span>
                    </td>
                    <td class="px-6 py-4">
                      <span :class="getStatusClass(doc.status)">
                        {{ getStatusText(doc.status) }}
                      </span>
                    </td>
                    <td class="px-6 py-4 text-sm text-slate-500 dark:text-slate-400">
                      {{ formatDate(doc.uploadedAt) }}
                    </td>
                    <td class="px-6 py-4 text-right">
                      <button 
                        @click="confirmDelete(doc)"
                        class="inline-flex items-center gap-1 px-3 py-1.5 rounded-lg text-xs font-medium text-red-600 dark:text-red-400 hover:bg-red-50 dark:hover:bg-red-900/20 transition"
                      >
                        <span class="material-symbols-outlined text-[16px]">delete</span>
                        删除
                      </button>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>
    </main>

    <!-- Delete Confirmation Modal -->
    <div v-if="deleteModal.visible" 
         class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-sm"
         @click.self="closeDeleteModal">
      <div class="bg-white dark:bg-[#1a222c] rounded-xl shadow-2xl border border-gray-200 dark:border-gray-700 w-full max-w-md overflow-hidden">
        <div class="p-6">
          <div class="flex items-center gap-4 mb-4">
            <div class="w-12 h-12 rounded-full bg-red-100 dark:bg-red-900/30 flex items-center justify-center">
              <span class="material-symbols-outlined text-red-600 dark:text-red-400 text-2xl">delete_forever</span>
            </div>
            <div>
              <h3 class="text-lg font-bold text-slate-900 dark:text-white">确认删除</h3>
              <p class="text-sm text-slate-500 dark:text-slate-400">此操作不可撤销</p>
            </div>
          </div>
          <p class="text-slate-600 dark:text-slate-300 mb-6">
            确定要删除文档 <span class="font-semibold text-slate-900 dark:text-white">"{{ deleteModal.document?.filename }}"</span> 吗？
          </p>
          <div class="flex items-center justify-end gap-3">
            <button 
              @click="closeDeleteModal"
              class="px-4 py-2 rounded-lg text-sm font-medium text-slate-600 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-700 transition"
            >
              取消
            </button>
            <button 
              @click="executeDelete"
              :disabled="deleteModal.isDeleting"
              class="px-6 py-2 rounded-lg text-sm font-medium bg-red-500 text-white hover:bg-red-600 disabled:opacity-50 disabled:cursor-not-allowed transition flex items-center gap-2"
            >
              <span v-if="!deleteModal.isDeleting">删除</span>
              <span v-else>删除中...</span>
              <span v-if="deleteModal.isDeleting" class="material-symbols-outlined text-[18px] animate-spin">progress_activity</span>
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import AdminSidebar from '../../components/admin/AdminSidebar.vue'
import api from '../../api/interceptor'

const allDocuments = ref([])
const isLoading = ref(false)
const isRefreshing = ref(false)

const deleteModal = ref({
  visible: false,
  document: null,
  isDeleting: false
})

const processingCount = computed(() => {
  return allDocuments.value.filter(doc => 
    doc.status === 'PROCESSING' || doc.status === 'UPLOADING'
  ).length
})

onMounted(async () => {
  await refreshDocuments()
})

async function refreshDocuments() {
  isRefreshing.value = true
  isLoading.value = true
  try {
    // 管理员获取所有文档的API
    const response = await api.get('/admin/documents')
    if (response.data.code === 200) {
      allDocuments.value = response.data.data
    }
  } catch (error) {
    console.error('获取文档列表失败:', error)
  } finally {
    isLoading.value = false
    isRefreshing.value = false
  }
}

function getFileIcon(fileType) {
  const icons = {
    'PDF': 'picture_as_pdf',
    'DOCX': 'description',
    'PPTX': 'slideshow',
    'TXT': 'article'
  }
  return icons[fileType] || 'insert_drive_file'
}

function getStatusClass(status) {
  const classes = {
    'SUCCESS': 'inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-green-100 dark:bg-green-900/30 text-green-700 dark:text-green-400',
    'PROCESSING': 'inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-blue-100 dark:bg-blue-900/30 text-blue-700 dark:text-blue-400',
    'UPLOADING': 'inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-amber-100 dark:bg-amber-900/30 text-amber-700 dark:text-amber-400',
    'FAILED': 'inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-red-100 dark:bg-red-900/30 text-red-700 dark:text-red-400'
  }
  return classes[status] || classes['PROCESSING']
}

function getStatusText(status) {
  const texts = {
    'SUCCESS': '已完成',
    'PROCESSING': '处理中',
    'UPLOADING': '上传中',
    'FAILED': '失败'
  }
  return texts[status] || status
}

function formatFileSize(bytes) {
  if (!bytes) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i]
}

function formatDate(dateString) {
  if (!dateString) return '-'
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN', { 
    year: 'numeric', 
    month: '2-digit', 
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

function confirmDelete(document) {
  deleteModal.value = {
    visible: true,
    document: document,
    isDeleting: false
  }
}

function closeDeleteModal() {
  deleteModal.value.visible = false
  deleteModal.value.document = null
  deleteModal.value.isDeleting = false
}

async function executeDelete() {
  if (!deleteModal.value.document) return
  
  deleteModal.value.isDeleting = true
  try {
    const response = await api.delete(`/documents/${deleteModal.value.document.id}`)
    if (response.data.code === 200) {
      closeDeleteModal()
      await refreshDocuments()
    } else {
      alert('删除失败: ' + response.data.message)
    }
  } catch (error) {
    console.error('删除文档失败:', error)
    alert('删除失败: ' + error.message)
  } finally {
    deleteModal.value.isDeleting = false
  }
}
</script>

<style scoped>
.scrollbar-custom::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}
.scrollbar-custom::-webkit-scrollbar-track {
  background: transparent; 
}
.scrollbar-custom::-webkit-scrollbar-thumb {
  background: #cbd5e1; 
  border-radius: 4px;
}
.dark .scrollbar-custom::-webkit-scrollbar-thumb {
  background: #2d3748; 
}
.scrollbar-custom::-webkit-scrollbar-thumb:hover {
  background: #94a3b8; 
}
.dark .scrollbar-custom::-webkit-scrollbar-thumb:hover {
  background: #4a5568; 
}
</style>
