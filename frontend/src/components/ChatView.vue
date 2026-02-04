<template>
  <div class="flex flex-col h-full bg-white dark:bg-[#101822] relative overflow-hidden">
    <!-- Mobile Header -->
    <div class="flex md:hidden items-center justify-between p-4 border-b border-gray-100 dark:border-gray-800">
      <span class="material-symbols-outlined text-gray-500 cursor-pointer">menu</span>
      <span class="font-bold text-lg">ScholarAI</span>
      <div class="w-8 h-8 rounded-full bg-gray-200 bg-cover" :style="`background-image: url('${user.avatar}')`"></div>
    </div>

    <!-- Chat Scroll Container -->
    <div class="flex-1 overflow-y-auto p-4 md:p-8 lg:p-12 scroll-smooth" ref="scrollContainer">
      <div class="max-w-[800px] mx-auto flex flex-col gap-8 pb-4">
        <!-- Welcome Heading -->
        <div v-if="messages.length === 0" class="flex flex-col gap-2 pb-6 border-b border-gray-100 dark:border-gray-800/50 mb-2">
          <h1 class="text-[#111418] dark:text-white text-3xl md:text-4xl font-bold leading-tight tracking-tight">Hello, {{ user.name.split(' ')[0] }}</h1>
          <p class="text-[#617289] dark:text-gray-400 text-base font-normal">Continuing your study session on <span class="font-medium text-primary">European History</span>.</p>
        </div>

        <!-- Welcome Message (Empty State) -->
        <div v-if="messages.length === 0" class="welcome-message">
          <div class="welcome-icon">
            <span class="material-symbols-outlined text-6xl text-primary">school</span>
          </div>
          <h3 class="text-xl font-bold text-[#111418] dark:text-white mt-4">Start your study session</h3>
          <p class="text-[#617289] dark:text-gray-400 mt-2 text-center max-w-md">Ask questions about your uploaded documents and study materials.</p>

          <div class="example-questions mt-8 flex flex-col gap-3 max-w-md">
            <div class="example-item p-4 bg-gray-50 dark:bg-[#1a222d] rounded-xl cursor-pointer hover:bg-gray-100 dark:hover:bg-[#252f3e] transition border border-gray-200 dark:border-gray-700" @click="askExample('What are the main causes of the French Revolution?')">
              <div class="flex items-center gap-3">
                <span class="material-symbols-outlined text-primary">quiz</span>
                <div>
                  <h4 class="text-sm font-medium text-[#111418] dark:text-white">Document Q&A</h4>
                  <p class="text-xs text-gray-500 dark:text-gray-400 mt-1">"What are the main causes of the French Revolution?"</p>
                </div>
              </div>
            </div>
            <div class="example-item p-4 bg-gray-50 dark:bg-[#1a222d] rounded-xl cursor-pointer hover:bg-gray-100 dark:hover:bg-[#252f3e] transition border border-gray-200 dark:border-gray-700" @click="askExample('Explain the concept of derivatives in calculus')">
              <div class="flex items-center gap-3">
                <span class="material-symbols-outlined text-primary">menu_book</span>
                <div>
                  <h4 class="text-sm font-medium text-[#111418] dark:text-white">Study Help</h4>
                  <p class="text-xs text-gray-500 dark:text-gray-400 mt-1">"Explain the concept of derivatives in calculus"</p>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Messages -->
        <MessageBubble v-for="msg in messages" :key="msg.id" :type="msg.type" :content="msg.content" :citations="msg.citations" :timestamp="msg.timestamp" />

        <!-- Loading State -->
        <div v-if="isLoading" class="flex gap-4 md:gap-6">
          <div class="flex shrink-0 flex-col items-center">
            <div class="flex h-10 w-10 items-center justify-center rounded-full bg-primary text-white shadow-md shadow-blue-500/20">
              <span class="material-symbols-outlined text-[24px] animate-pulse">smart_toy</span>
            </div>
          </div>
          <div class="flex flex-col gap-4 flex-1 min-w-0">
            <div class="text-[#111418] dark:text-gray-100 text-base leading-relaxed tracking-wide">
              <div class="flex gap-2">
                <span class="w-2 h-2 bg-gray-400 rounded-full animate-bounce"></span>
                <span class="w-2 h-2 bg-gray-400 rounded-full animate-bounce" style="animation-delay: 0.1s"></span>
                <span class="w-2 h-2 bg-gray-400 rounded-full animate-bounce" style="animation-delay: 0.2s"></span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Input Area -->
    <InputArea @send="handleSend" />
  </div>
</template>

<script setup>
import { ref, computed, watch, nextTick, onMounted } from 'vue'
import { useChatStore } from '../stores/chat'
import { useUserStore } from '../stores/user'
import MessageBubble from './MessageBubble.vue'
import InputArea from './InputArea.vue'

const chatStore = useChatStore()
const userStore = useUserStore()
const user = userStore.user
const scrollContainer = ref(null)

const messages = computed(() => chatStore.messages)
const isLoading = computed(() => chatStore.isLoading)

function handleSend(question) {
  chatStore.sendMessage(question)
  nextTick(() => scrollToBottom())
}

function askExample(question) {
  chatStore.sendMessage(question)
  nextTick(() => scrollToBottom())
}

function scrollToBottom() {
  if (scrollContainer.value) {
    scrollContainer.value.scrollTop = scrollContainer.value.scrollHeight
  }
}

watch(messages.value, () => {
  nextTick(() => scrollToBottom())
}, { deep: true })

onMounted(() => {
  chatStore.loadFromStorage()
  scrollToBottom()
})
</script>

<style scoped>
.welcome-message {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 2rem 0;
}

.welcome-icon {
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, #428bf0 0%, #6ba3f5 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 10px 40px rgba(66, 139, 240, 0.3);
}

.example-item {
  transition: all 0.2s ease;
}

.example-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}
</style>
