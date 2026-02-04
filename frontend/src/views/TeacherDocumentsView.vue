<template>
  <div class="flex h-screen w-full bg-background-light dark:bg-background-dark overflow-hidden font-display text-slate-900 dark:text-white">
    <!-- Sidebar -->
    <TeacherSidebar />
    
    <!-- Main Content -->
    <main class="flex-1 flex flex-col h-full overflow-hidden relative">
      <!-- Header -->
      <header class="flex-shrink-0 px-8 py-6 border-b border-slate-200 dark:border-slate-800 bg-white/50 dark:bg-[#111418]/50 backdrop-blur-sm sticky top-0 z-10">
        <div class="flex flex-col md:flex-row md:items-center justify-between gap-4">
          <div>
            <h2 class="text-2xl font-bold text-slate-900 dark:text-white tracking-tight">文档管理</h2>
            <p class="text-slate-500 dark:text-slate-400 text-sm mt-1">
              共 {{ documentStore.documents.length }} 个文档
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
          <!-- Upload Zone -->
          <DocumentUploadZone @upload-success="refreshDocuments" />
          
          <!-- Document List -->
          <DocumentList 
            :documents="documentStore.documents"
            @refresh="refreshDocuments"
            @view="viewDocument"
            @delete="confirmDelete"
          />

          <!-- Footer -->
          <div class="mt-8 pt-4 border-t border-slate-200 dark:border-slate-800 flex justify-between items-center text-xs text-slate-400 dark:text-slate-500">
            <p>© 2023 EduAI 教育科技有限公司 版权所有</p>
            <div class="flex gap-4">
              <a class="hover:text-primary" href="#">隐私政策</a>
              <a class="hover:text-primary" href="#">技术支持</a>
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
            确定要删除文档 <span class="font-semibold text-slate-900 dark:text-white">"{{ deleteModal.document?.name }}"</span> 吗？
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
import TeacherSidebar from '../components/teacher/TeacherSidebar.vue'
import DocumentUploadZone from '../components/teacher/DocumentUploadZone.vue'
import DocumentList from '../components/teacher/DocumentList.vue'
import { useDocumentStore } from '../stores/documents'

const documentStore = useDocumentStore()
const isRefreshing = ref(false)

const deleteModal = ref({
  visible: false,
  document: null,
  isDeleting: false
})

const processingCount = computed(() => {
  return documentStore.documents.filter(doc => 
    doc.status === 'PROCESSING' || doc.status === 'UPLOADING'
  ).length
})

onMounted(async () => {
  await refreshDocuments()
})

async function refreshDocuments() {
  isRefreshing.value = true
  await documentStore.fetchDocuments()
  isRefreshing.value = false
}

function viewDocument(document) {
  if (document.url) {
    window.open(document.url, '_blank')
  } else {
    alert('文档链接不可用')
  }
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
  const result = await documentStore.deleteDocument(deleteModal.value.document.id)
  deleteModal.value.isDeleting = false
  
  if (result.success) {
    closeDeleteModal()
    await refreshDocuments()
  } else {
    alert('删除失败: ' + result.message)
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
