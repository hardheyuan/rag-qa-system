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
          <div v-html="renderedContent" class="chat-markdown"></div>
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
      <div v-if="!isUser && !isStreaming && hasContent" class="flex gap-2 mt-1">
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
import MarkdownIt from 'markdown-it'

const props = defineProps({
  type: { type: String, required: true },
  content: { type: String, default: '' },
  citations: { type: Array, default: () => [] },
  timestamp: { type: String, default: '' },
  isStreaming: { type: Boolean, default: false }
})

const markdown = new MarkdownIt({
  html: false,
  linkify: true,
  breaks: true,
  typographer: false
})

const isUser = computed(() => props.type === 'user')
const hasContent = computed(() => Boolean((props.content || '').trim()))

const renderedContent = computed(() => {
  return markdown.render(props.content || '')
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

<style scoped>
.chat-markdown:deep(p) {
  margin: 0 0 0.75rem;
}

.chat-markdown:deep(p:last-child) {
  margin-bottom: 0;
}

.chat-markdown:deep(ul),
.chat-markdown:deep(ol) {
  margin: 0.5rem 0 0.75rem;
  padding-left: 1.25rem;
}

.chat-markdown:deep(code) {
  background: rgba(148, 163, 184, 0.18);
  padding: 0.1rem 0.35rem;
  border-radius: 0.35rem;
  font-size: 0.92em;
}

.chat-markdown:deep(pre) {
  background: rgba(15, 23, 42, 0.06);
  padding: 0.75rem;
  border-radius: 0.65rem;
  overflow-x: auto;
  margin: 0.5rem 0 0.75rem;
}

.dark .chat-markdown:deep(pre) {
  background: rgba(15, 23, 42, 0.5);
}

.chat-markdown:deep(pre code) {
  background: transparent;
  padding: 0;
}

.chat-markdown:deep(blockquote) {
  border-left: 3px solid rgba(66, 139, 240, 0.45);
  padding-left: 0.75rem;
  margin: 0.5rem 0 0.75rem;
  color: rgba(71, 85, 105, 1);
}

.dark .chat-markdown:deep(blockquote) {
  color: rgba(148, 163, 184, 1);
}

.chat-markdown:deep(a) {
  color: rgba(37, 99, 235, 1);
  text-decoration: underline;
  text-underline-offset: 2px;
}
</style>
