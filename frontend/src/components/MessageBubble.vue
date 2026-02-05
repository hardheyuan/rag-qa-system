<template>
  <div :class="[
    'flex animate-fade-in',
    isUser ? 'justify-end' : 'gap-4 md:gap-6'
  ]">
    <!-- AI Avatar -->
    <div v-if="!isUser" class="flex shrink-0 flex-col items-center">
      <div class="flex h-10 w-10 items-center justify-center rounded-full bg-primary text-white shadow-md shadow-blue-500/20">
        <span class="material-symbols-outlined text-[24px]">smart_toy</span>
      </div>
    </div>

    <!-- Message Content -->
    <div :class="['flex flex-col gap-1', isUser ? 'items-end max-w-[85%] md:max-w-[70%]' : 'flex-1 min-w-0']">
      <!-- Message Bubble -->
      <div :class="[
        isUser
          ? 'bg-primary/10 dark:bg-primary/20 text-[#111418] dark:text-white px-6 py-4 rounded-2xl rounded-tr-sm'
          : ''
      ]">
        <div class="text-[#111418] dark:text-gray-100 text-base leading-relaxed tracking-wide">
          <p v-html="formattedContent" class="mb-4"></p>
          <p v-if="isStreaming" class="streaming-text"></p>
        </div>
      </div>

      <!-- Citations (only for AI) -->
      <div v-if="!isUser && citations.length > 0" class="flex flex-wrap gap-3 mt-1">
        <div v-for="(citation, index) in citations" :key="index"
             class="flex items-center gap-3 bg-white dark:bg-[#1a222d] border border-gray-200 dark:border-gray-700 rounded-lg p-3 pr-4 cursor-pointer hover:border-primary/50 hover:shadow-sm transition group">
          <div :class="[
            'h-10 w-10 rounded-lg flex items-center justify-center shrink-0',
            getSourceColor(citation.sourceType)
          ]">
            <span class="material-symbols-outlined">{{ getSourceIcon(citation.sourceType) }}</span>
          </div>
          <div class="flex flex-col">
            <span class="text-xs font-bold text-gray-500 dark:text-gray-400 uppercase tracking-wider">{{ citation.sourceType || 'Source' }}</span>
            <span class="text-sm font-medium text-[#111418] dark:text-gray-200 group-hover:text-primary transition truncate max-w-[200px]">
              {{ citation.documentTitle || 'Unknown Document' }}
            </span>
            <span class="text-[10px] text-gray-400">{{ citation.page ? `Page ${citation.page}` : 'Document' }}</span>
          </div>
        </div>
      </div>

      <!-- Actions (only for AI) -->
      <div v-if="!isUser" class="flex gap-2 mt-1">
        <button class="flex items-center gap-1.5 text-xs font-medium text-gray-500 hover:text-gray-700 dark:text-gray-400 dark:hover:text-gray-200 px-3 py-1.5 rounded-full hover:bg-gray-100 dark:hover:bg-white/5 transition">
          <span class="material-symbols-outlined text-[16px]">thumb_up</span>
          Helpful
        </button>
        <button class="flex items-center gap-1.5 text-xs font-medium text-gray-500 hover:text-gray-700 dark:text-gray-400 dark:hover:text-gray-200 px-3 py-1.5 rounded-full hover:bg-gray-100 dark:hover:bg-white/5 transition">
          <span class="material-symbols-outlined text-[16px]">content_copy</span>
          Copy
        </button>
        <button class="flex items-center gap-1.5 text-xs font-medium text-gray-500 hover:text-gray-700 dark:text-gray-400 dark:hover:text-gray-200 px-3 py-1.5 rounded-full hover:bg-gray-100 dark:hover:bg-white/5 transition ml-auto">
          <span class="material-symbols-outlined text-[16px]">refresh</span>
          Regenerate
        </button>
      </div>

      <!-- Timestamp -->
      <span v-if="isUser" class="text-xs text-gray-400 pr-1">{{ formattedTime }}</span>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  type: { type: String, required: true },
  content: { type: String, default: '' },
  citations: { type: Array, default: () => [] },
  timestamp: { type: String, default: '' },
  isStreaming: { type: Boolean, default: false }
})

const isUser = computed(() => props.type === 'user')

const formattedContent = computed(() => {
  return props.content.replace(/\n/g, '<br>')
})

const formattedTime = computed(() => {
  if (!props.timestamp) return ''
  return new Date(props.timestamp).toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' })
})

function getSourceIcon(type) {
  const icons = { PDF: 'picture_as_pdf', Lecture: 'description', Textbook: 'menu_book' }
  return icons[type?.toUpperCase()] || 'article'
}

function getSourceColor(type) {
  const colors = {
    PDF: 'bg-red-50 dark:bg-red-900/20 text-red-500',
    Lecture: 'bg-blue-50 dark:bg-blue-900/20 text-blue-500',
    Textbook: 'bg-green-50 dark:bg-green-900/20 text-green-500'
  }
  return colors[type?.toUpperCase()] || 'bg-gray-50 dark:bg-gray-700 text-gray-500'
}
</script>
