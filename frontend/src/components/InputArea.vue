<template>
  <div class="w-full bg-white dark:bg-[#101822] p-4 md:p-6 border-t border-gray-100 dark:border-gray-800 z-10">
    <div class="max-w-[800px] mx-auto">
      <div class="relative flex items-end gap-2 bg-gray-50 dark:bg-[#1a222d] border border-gray-200 dark:border-gray-700 focus-within:ring-2 focus-within:ring-primary/30 focus-within:border-primary rounded-2xl p-2 transition shadow-sm">
        <!-- Attachment Button -->
        <button @click="triggerFileInput" class="flex items-center justify-center w-10 h-10 rounded-xl text-gray-500 hover:text-primary hover:bg-white dark:hover:bg-gray-800 transition mb-1 shrink-0" title="Upload document for context">
          <span class="material-symbols-outlined">add_circle</span>
        </button>
        <input ref="fileInput" type="file" multiple accept=".pdf,.docx,.pptx,.txt" class="hidden" @change="handleFileUpload">

        <!-- Text Input -->
        <textarea v-model="inputText" @keydown.enter.exact.prevent="send" @input="autoResize" ref="textareaRef" class="w-full bg-transparent border-0 text-[#111418] dark:text-white placeholder:text-gray-400 focus:ring-0 py-3 max-h-32 min-h-[56px] resize-none leading-relaxed" placeholder="Ask a follow-up question..." rows="1"></textarea>

        <!-- Send Button -->
        <button @click="send" :disabled="!inputText.trim() || isLoading" class="flex items-center justify-center w-10 h-10 rounded-xl bg-primary text-white hover:bg-blue-600 transition shadow-sm mb-1 shrink-0 disabled:opacity-50 disabled:cursor-not-allowed">
          <span class="material-symbols-outlined">arrow_upward</span>
        </button>
      </div>

      <div class="flex justify-center mt-3 gap-6 text-xs text-gray-400">
        <span>AI generated content can be inaccurate.</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, nextTick } from 'vue'
import { useChatStore } from '../stores/chat'

const emit = defineEmits(['send'])
const chatStore = useChatStore()
const inputText = ref('')
const textareaRef = ref(null)
const fileInput = ref(null)

const isLoading = computed(() => chatStore.isLoading)

function autoResize() {
  const el = textareaRef.value
  el.style.height = 'auto'
  el.style.height = Math.min(el.scrollHeight, 128) + 'px'
}

function send() {
  if (!inputText.value.trim() || isLoading.value) return
  emit('send', inputText.value.trim())
  inputText.value = ''
  nextTick(() => {
    autoResize()
  })
}

function triggerFileInput() {
  fileInput.value?.click()
}

function handleFileUpload(event) {
  const files = event.target.files
  console.log('Files selected:', files)
  // TODO: Implement file upload logic
}
</script>
