<template>
  <div class="bg-white dark:bg-card-dark border border-slate-200 dark:border-card-border rounded-xl p-0 overflow-hidden shadow-sm flex flex-col">
    <!-- Header -->
    <div class="p-6 border-b border-slate-200 dark:border-slate-800 flex justify-between items-center">
      <div class="flex items-center gap-3">
        <div class="w-10 h-10 rounded-lg bg-primary/10 flex items-center justify-center">
          <span class="material-symbols-outlined text-primary text-2xl">folder_open</span>
        </div>
        <div>
          <h3 class="text-lg font-semibold text-slate-900 dark:text-white">文档列表</h3>
          <p class="text-sm text-slate-500 dark:text-slate-400">管理已上传的教学文档</p>
        </div>
      </div>
    </div>

    <!-- Document Table -->
    <div class="flex-1 overflow-x-auto">
      <table class="w-full text-sm text-left">
        <thead class="text-xs text-slate-500 dark:text-slate-400 uppercase bg-slate-50 dark:bg-[#151b23]">
          <tr>
            <th class="px-6 py-3 font-medium" scope="col">文档名称</th>
            <th class="px-6 py-3 font-medium" scope="col">类型</th>
            <th class="px-6 py-3 font-medium" scope="col">大小</th>
            <th class="px-6 py-3 font-medium" scope="col">上传时间</th>
            <th class="px-6 py-3 font-medium" scope="col">状态</th>
            <th class="px-6 py-3 font-medium text-right" scope="col">操作</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-slate-200 dark:divide-slate-800">
          <tr v-if="documents.length === 0">
            <td colspan="6" class="px-6 py-12 text-center">
              <div class="flex flex-col items-center gap-3">
                <div class="w-16 h-16 rounded-full bg-slate-100 dark:bg-slate-800 flex items-center justify-center">
                  <span class="material-symbols-outlined text-slate-400 text-3xl">folder_off</span>
                </div>
                <p class="text-slate-500 dark:text-slate-400 text-sm">暂无文档</p>
                <p class="text-slate-400 dark:text-slate-500 text-xs">请先上传文档</p>
              </div>
            </td>
          </tr>
          
          <tr 
            v-for="doc in documents" 
            :key="doc.id"
            class="bg-white dark:bg-card-dark hover:bg-slate-50 dark:hover:bg-slate-800/50 transition-colors"
          >
            <td class="px-6 py-4">
              <div class="flex items-center gap-3">
                <div class="w-10 h-10 rounded-lg flex items-center justify-center shrink-0" :class="getFileTypeClass(doc.type)">
                  <span class="material-symbols-outlined text-white">{{ getFileIcon(doc.type) }}</span>
                </div>
                <div class="min-w-0">
                  <p class="font-medium text-slate-900 dark:text-white truncate max-w-[200px]" :title="doc.name">{{ doc.name }}</p>
                  <p v-if="doc.description" class="text-xs text-slate-400 truncate max-w-[200px]">{{ doc.description }}</p>
                </div>
              </div>
            </td>
            <td class="px-6 py-4">
              <span class="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium" :class="getTypeBadgeClass(doc.type)">
                {{ getTypeLabel(doc.type) }}
              </span>
            </td>
            <td class="px-6 py-4 text-slate-600 dark:text-slate-300">
              {{ formatFileSize(doc.size) }}
            </td>
            <td class="px-6 py-4 text-slate-600 dark:text-slate-300">
              {{ formatDate(doc.createTime) }}
            </td>
            <td class="px-6 py-4">
              <div class="flex flex-col gap-1">
                <span class="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium" :class="getStatusClass(doc.status)">
                  {{ getStatusLabel(doc.status) }}
                </span>
                <!-- OCR 提示：仅在处理中状态显示 -->
                <span v-if="doc.status === 'PROCESSING'" class="text-[10px] text-slate-400 dark:text-slate-500">
                  正在解析文档...
                </span>
              </div>
            </td>
            <td class="px-6 py-4 text-right">
              <div class="flex items-center justify-end gap-2">
                <button 
                  @click="$emit('view', doc)"
                  class="p-2 rounded-lg text-slate-400 hover:text-primary hover:bg-primary/10 transition"
                  title="查看"
                >
                  <span class="material-symbols-outlined text-lg">visibility</span>
                </button>
                <button 
                  @click="$emit('delete', doc)"
                  class="p-2 rounded-lg text-slate-400 hover:text-red-500 hover:bg-red-50 dark:hover:bg-red-900/20 transition"
                  title="删除"
                >
                  <span class="material-symbols-outlined text-lg">delete</span>
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
defineProps({
  documents: {
    type: Array,
    default: () => []
  }
})

defineEmits(['view', 'delete', 'refresh'])

function getFileIcon(type) {
  const icons = {
    pdf: 'picture_as_pdf',
    doc: 'description',
    docx: 'description',
    ppt: 'slideshow',
    pptx: 'slideshow'
  }
  return icons[type?.toLowerCase()] || 'insert_drive_file'
}

function getFileTypeClass(type) {
  const classes = {
    pdf: 'bg-red-500',
    doc: 'bg-blue-500',
    docx: 'bg-blue-500',
    ppt: 'bg-orange-500',
    pptx: 'bg-orange-500'
  }
  return classes[type?.toLowerCase()] || 'bg-slate-500'
}

function getTypeBadgeClass(type) {
  const classes = {
    pdf: 'bg-red-100 dark:bg-red-900/30 text-red-600 dark:text-red-400',
    doc: 'bg-blue-100 dark:bg-blue-900/30 text-blue-600 dark:text-blue-400',
    docx: 'bg-blue-100 dark:bg-blue-900/30 text-blue-600 dark:text-blue-400',
    ppt: 'bg-orange-100 dark:bg-orange-900/30 text-orange-600 dark:text-orange-400',
    pptx: 'bg-orange-100 dark:bg-orange-900/30 text-orange-600 dark:text-orange-400'
  }
  return classes[type?.toLowerCase()] || 'bg-slate-100 dark:bg-slate-800 text-slate-600 dark:text-slate-400'
}

function getTypeLabel(type) {
  const labels = {
    pdf: 'PDF',
    doc: 'Word',
    docx: 'Word',
    ppt: 'PPT',
    pptx: 'PPT'
  }
  return labels[type?.toLowerCase()] || type?.toUpperCase() || '未知'
}

function getStatusClass(status) {
  const classes = {
    SUCCESS: 'bg-emerald-100 dark:bg-emerald-900/30 text-emerald-600 dark:text-emerald-400',
    PROCESSING: 'bg-amber-100 dark:bg-amber-900/30 text-amber-600 dark:text-amber-400',
    UPLOADING: 'bg-amber-100 dark:bg-amber-900/30 text-amber-600 dark:text-amber-400',
    FAILED: 'bg-red-100 dark:bg-red-900/30 text-red-600 dark:text-red-400'
  }
  return classes[status] || 'bg-slate-100 dark:bg-slate-800 text-slate-600 dark:text-slate-400'
}

function getStatusLabel(status) {
  const labels = {
    SUCCESS: '已就绪',
    PROCESSING: '处理中',
    UPLOADING: '上传中',
    FAILED: '处理失败'
  }
  return labels[status] || '未知'
}

function formatFileSize(bytes) {
  if (!bytes || bytes === 0) return '0 Bytes'
  const k = 1024
  const sizes = ['Bytes', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

function formatDate(dateString) {
  if (!dateString) return '-'
  const date = new Date(dateString)
  const now = new Date()
  const diff = now - date
  
  // Less than 1 hour
  if (diff < 3600000) {
    const minutes = Math.floor(diff / 60000)
    return minutes < 1 ? '刚刚' : `${minutes}分钟前`
  }
  
  // Less than 24 hours
  if (diff < 86400000) {
    const hours = Math.floor(diff / 3600000)
    return `${hours}小时前`
  }
  
  // Less than 7 days
  if (diff < 604800000) {
    const days = Math.floor(diff / 86400000)
    return `${days}天前`
  }
  
  // Format as date
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  })
}
</script>
