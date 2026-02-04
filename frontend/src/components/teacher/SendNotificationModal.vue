<template>
  <div class="fixed inset-0 bg-black/50 flex items-center justify-center z-50 overflow-y-auto py-4" @click="$emit('close')">
    <div class="bg-white dark:bg-[#1a222c] rounded-lg w-full max-w-lg mx-4 flex flex-col" @click.stop>
      <!-- Header -->
      <div class="px-6 py-4 border-b border-slate-200 dark:border-slate-700 flex-shrink-0">
        <div class="flex items-center gap-3">
          <div class="w-10 h-10 rounded-full bg-green-500/10 flex items-center justify-center">
            <span class="material-symbols-outlined text-green-500 text-xl">campaign</span>
          </div>
          <div>
            <h3 class="text-lg font-semibold text-slate-900 dark:text-white">发送通知</h3>
            <p class="text-sm text-slate-500 dark:text-slate-400">向所有学生广播消息</p>
          </div>
        </div>
      </div>

      <!-- Form -->
      <div class="p-6 space-y-4 overflow-y-auto flex-1">
        <!-- 收件人 -->
        <div>
          <label class="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-1.5">
            收件人
          </label>
          <div class="flex items-center gap-2 px-4 py-3 rounded-lg border border-slate-200 dark:border-slate-700 bg-slate-50 dark:bg-slate-800/50">
            <span class="material-symbols-outlined text-slate-400">group</span>
            <span class="text-sm text-slate-600 dark:text-slate-400">所有学生</span>
            <span class="ml-auto text-xs px-2 py-0.5 rounded-full bg-green-100 dark:bg-green-900/30 text-green-700 dark:text-green-400">
              广播
            </span>
          </div>
        </div>

        <!-- 消息内容 -->
        <div>
          <label class="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-1.5">
            消息内容 <span class="text-red-500">*</span>
          </label>
          <textarea
            v-model="messageContent"
            rows="5"
            placeholder="请输入要发送的通知内容..."
            class="w-full px-4 py-3 rounded-lg border border-slate-200 dark:border-slate-700 bg-white dark:bg-[#111418] text-slate-900 dark:text-white placeholder:text-slate-400 focus:outline-none focus:border-primary focus:ring-1 focus:ring-primary/20 transition-all resize-none"
            :disabled="loading"
          ></textarea>
          <div class="flex justify-between mt-1.5">
            <span class="text-xs text-slate-400">{{ messageContent.length }}/500 字符</span>
            <span v-if="messageContent.length > 500" class="text-xs text-red-500">超出字数限制</span>
          </div>
        </div>

        <!-- 提示信息 -->
        <div class="flex items-start gap-2 p-3 rounded-lg bg-amber-50 dark:bg-amber-900/20 border border-amber-200 dark:border-amber-800">
          <span class="material-symbols-outlined text-amber-500 text-sm mt-0.5">info</span>
          <p class="text-xs text-amber-700 dark:text-amber-400">
            通知将以系统消息形式发送给所有在线学生，请确保内容准确无误。
          </p>
        </div>
      </div>

      <!-- Footer -->
      <div class="px-6 py-4 border-t border-slate-200 dark:border-slate-700 flex justify-end gap-3 flex-shrink-0">
        <button
          @click="$emit('close')"
          class="px-4 py-2 rounded-lg text-slate-600 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-800 transition-all"
          :disabled="loading"
        >
          取消
        </button>
        <button
          @click="handleSend"
          :disabled="!isValid || loading"
          class="px-4 py-2 rounded-lg bg-green-500 text-white hover:bg-green-600 transition-all disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-2"
        >
          <span v-if="loading" class="material-symbols-outlined text-lg animate-spin">progress_activity</span>
          <span v-else class="material-symbols-outlined text-lg">send</span>
          <span>{{ loading ? '发送中...' : '发送通知' }}</span>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const emit = defineEmits(['close', 'send'])

const messageContent = ref('')
const loading = ref(false)

const isValid = computed(() => {
  return messageContent.value.trim().length > 0 && messageContent.value.length <= 500
})

async function handleSend() {
  if (!isValid.value) return

  loading.value = true
  try {
    // 模拟发送延迟
    await new Promise(resolve => setTimeout(resolve, 1500))
    
    // 触发发送事件
    emit('send', {
      content: messageContent.value.trim(),
      recipients: 'all_students',
      timestamp: new Date().toISOString()
    })
    
    // 清空并关闭
    messageContent.value = ''
    emit('close')
    
    // 显示成功提示（实际项目中可以用 toast）
    alert('通知发送成功！')
  } catch (error) {
    console.error('发送通知失败:', error)
    alert('发送失败，请重试')
  } finally {
    loading.value = false
  }
}
</script>