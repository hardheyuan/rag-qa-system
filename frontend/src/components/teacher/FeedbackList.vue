<template>
  <div class="bg-white dark:bg-card-dark border border-slate-200 dark:border-card-border rounded-xl shadow-sm flex flex-col overflow-hidden h-full">
    <div class="p-4 border-b border-slate-200 dark:border-slate-800 flex justify-between items-center bg-slate-50/50 dark:bg-[#151b23]">
      <h3 class="text-sm font-bold text-slate-900 dark:text-white flex items-center gap-2">
        <span class="material-symbols-outlined text-amber-500 text-lg">rate_review</span>
        待审核反馈
      </h3>
      <button
        @click="loadPendingFeedback"
        :disabled="loading"
        class="text-xs text-primary hover:text-primary/80 disabled:opacity-50"
      >
        刷新
      </button>
    </div>

    <div v-if="loading" class="p-4 space-y-3">
      <div v-for="i in 3" :key="i" class="animate-pulse p-3 rounded-lg border border-slate-100 dark:border-slate-800">
        <div class="h-3 bg-slate-200 dark:bg-slate-700 rounded w-1/3 mb-2"></div>
        <div class="h-4 bg-slate-200 dark:bg-slate-700 rounded w-full mb-1"></div>
        <div class="h-3 bg-slate-200 dark:bg-slate-700 rounded w-2/3"></div>
      </div>
    </div>

    <div v-else-if="error" class="p-4 text-xs text-red-500 dark:text-red-400">
      {{ error }}
    </div>

    <div v-else-if="feedbackItems.length === 0" class="p-6 text-center text-sm text-slate-500 dark:text-slate-400">
      暂无待审核反馈
    </div>

    <div v-else class="flex-1 overflow-y-auto p-0">
      <div
        v-for="(item, index) in feedbackItems"
        :key="item.id"
        class="p-4 border-b border-slate-100 dark:border-slate-800 hover:bg-slate-50 dark:hover:bg-slate-800/50 transition-colors cursor-pointer"
        :class="{ 'border-b-0': index === feedbackItems.length - 1 }"
        @click="openFeedbackDetail(item.id)"
      >
        <div class="flex justify-between items-start mb-1.5 gap-2">
          <span class="text-[10px] font-bold px-1.5 py-0.5 rounded border" :class="getBadgeClasses(item.type)">
            {{ getTypeLabel(item.type) }}
          </span>
          <span class="text-[10px] text-slate-400">{{ formatRelativeTime(item.createdAt) }}</span>
        </div>
        <p class="text-xs text-slate-500 dark:text-slate-400 mb-1">学生：{{ item.studentName }}</p>
        <p class="text-sm font-medium text-slate-800 dark:text-slate-200 leading-snug line-clamp-2">{{ item.content }}</p>
      </div>
    </div>

    <div class="p-3 border-t border-slate-200 dark:border-slate-800 text-center bg-slate-50/50 dark:bg-[#151b23]">
      <button @click="loadPendingFeedback" class="text-xs font-medium text-primary hover:text-primary/80 transition-colors">
        查看最新待办事项 →
      </button>
    </div>
  </div>

  <div
    v-if="showDetailModal && selectedFeedback"
    class="fixed inset-0 z-50 bg-black/45 flex items-center justify-center p-4"
    @click.self="closeDetailModal"
  >
    <div class="w-full max-w-xl bg-white dark:bg-[#1a222c] rounded-xl border border-slate-200 dark:border-slate-700 shadow-2xl overflow-hidden">
      <div class="px-5 py-4 border-b border-slate-200 dark:border-slate-700 flex items-center justify-between">
        <h3 class="text-base font-semibold text-slate-900 dark:text-white">反馈详情</h3>
        <button @click="closeDetailModal" class="text-slate-400 hover:text-slate-600 dark:hover:text-slate-200">
          <span class="material-symbols-outlined">close</span>
        </button>
      </div>

      <div class="p-5 space-y-4">
        <div class="text-sm text-slate-600 dark:text-slate-300">
          <p><span class="font-medium">学生：</span>{{ selectedFeedback.studentName }}（{{ selectedFeedback.studentEmail || '未填写邮箱' }}）</p>
          <p><span class="font-medium">类型：</span>{{ getTypeLabel(selectedFeedback.type) }}</p>
          <p><span class="font-medium">时间：</span>{{ formatDateTime(selectedFeedback.createdAt) }}</p>
        </div>

        <div class="rounded-lg bg-slate-50 dark:bg-slate-800/60 border border-slate-200 dark:border-slate-700 p-3">
          <p class="text-sm text-slate-900 dark:text-slate-100 whitespace-pre-wrap">{{ selectedFeedback.content }}</p>
        </div>

        <div>
          <label class="block text-sm font-medium text-slate-900 dark:text-white mb-2">回复学生</label>
          <textarea
            v-model="replyContent"
            rows="4"
            class="w-full rounded-lg border border-slate-200 dark:border-slate-600 bg-white dark:bg-[#111827] px-3 py-2 text-sm text-slate-900 dark:text-white focus:outline-none focus:ring-2 focus:ring-primary/40"
            placeholder="请输入回复内容..."
          ></textarea>
        </div>

        <p v-if="selectedFeedback.replyContent" class="text-xs text-green-700 dark:text-green-300">
          当前回复：{{ selectedFeedback.replyContent }}
        </p>
      </div>

      <div class="px-5 py-4 border-t border-slate-200 dark:border-slate-700 flex justify-end gap-3">
        <button
          @click="closeDetailModal"
          class="px-4 py-2 text-sm rounded-lg text-slate-600 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-slate-700"
        >
          取消
        </button>
        <button
          @click="submitReply"
          :disabled="submittingReply || !replyContent.trim()"
          class="px-4 py-2 text-sm rounded-lg bg-primary text-white hover:bg-blue-600 disabled:opacity-50"
        >
          {{ submittingReply ? '发送中...' : '发送回复' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, onUnmounted, ref } from 'vue'
import { feedbackApi } from '@/api/feedback'
import { useToast } from '@/composables/useToast'
import { getErrorMessage } from '@/utils/errorHandler'

const emit = defineEmits(['pending-count-change'])
const toast = useToast()

const feedbackItems = ref([])
const loading = ref(false)
const error = ref('')

const showDetailModal = ref(false)
const selectedFeedback = ref(null)
const replyContent = ref('')
const submittingReply = ref(false)
let refreshTimer = null

async function loadPendingFeedback() {
  loading.value = true
  error.value = ''

  try {
    const [feedbackPage, pendingCount] = await Promise.all([
      feedbackApi.getTeacherPendingFeedback({ page: 0, size: 6 }),
      feedbackApi.getTeacherPendingCount()
    ])

    feedbackItems.value = feedbackPage?.content || []
    emit('pending-count-change', Number(pendingCount || 0))
  } catch (err) {
    error.value = getErrorMessage(err)
  } finally {
    loading.value = false
  }
}

async function openFeedbackDetail(id) {
  try {
    const detail = await feedbackApi.getTeacherFeedbackDetail(id)
    selectedFeedback.value = detail
    replyContent.value = detail?.replyContent || ''
    showDetailModal.value = true
  } catch (err) {
    toast.error(getErrorMessage(err))
  }
}

function closeDetailModal() {
  showDetailModal.value = false
  selectedFeedback.value = null
  replyContent.value = ''
  submittingReply.value = false
}

async function submitReply() {
  if (!selectedFeedback.value || !replyContent.value.trim()) {
    return
  }

  submittingReply.value = true
  try {
    const updated = await feedbackApi.replyFeedback(selectedFeedback.value.id, replyContent.value.trim())
    selectedFeedback.value = updated
    toast.success('已回复学生反馈')
    closeDetailModal()
    await loadPendingFeedback()
  } catch (err) {
    const message = getErrorMessage(err)
    toast.error(message)
    if (message.includes('不存在')) {
      closeDetailModal()
      await loadPendingFeedback()
    }
  } finally {
    submittingReply.value = false
  }
}

function getBadgeClasses(type) {
  const classes = {
    ISSUE: 'text-red-600 bg-red-50 dark:bg-red-900/20 border-red-100 dark:border-red-900/30',
    SUGGESTION: 'text-blue-600 bg-blue-50 dark:bg-blue-900/20 border-blue-100 dark:border-blue-900/30',
    OTHER: 'text-amber-600 bg-amber-50 dark:bg-amber-900/20 border-amber-100 dark:border-amber-900/30'
  }
  return classes[type] || classes.OTHER
}

function getTypeLabel(type) {
  const labels = {
    ISSUE: '问题反馈',
    SUGGESTION: '建议反馈',
    OTHER: '其他反馈'
  }
  return labels[type] || '其他反馈'
}

function formatDateTime(value) {
  if (!value) return '-'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

function formatRelativeTime(value) {
  if (!value) return '-'
  const time = new Date(value).getTime()
  if (Number.isNaN(time)) return value

  const diffMinutes = Math.floor((Date.now() - time) / 60000)
  if (diffMinutes < 1) return '刚刚'
  if (diffMinutes < 60) return `${diffMinutes}分钟前`
  if (diffMinutes < 1440) return `${Math.floor(diffMinutes / 60)}小时前`
  return `${Math.floor(diffMinutes / 1440)}天前`
}

onMounted(() => {
  if (import.meta.env.MODE === 'test') {
    return
  }
  loadPendingFeedback()
  refreshTimer = window.setInterval(() => {
    loadPendingFeedback()
  }, 15000)
})

onUnmounted(() => {
  if (refreshTimer) {
    window.clearInterval(refreshTimer)
    refreshTimer = null
  }
})
</script>
