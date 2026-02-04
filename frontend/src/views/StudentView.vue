<script setup>
import { onMounted } from 'vue'
import { useUserStore } from '../stores/user'
import { useChatStore } from '../stores/chat'
import Sidebar from '../components/Sidebar.vue'
import ChatView from '../components/ChatView.vue'

const userStore = useUserStore()
const chatStore = useChatStore()

onMounted(() => {
  userStore.initDarkMode()
  userStore.initAuth()
  // 确保对话数据已加载
  chatStore.loadFromStorage()
})
</script>

<template>
  <div class="flex h-screen w-full bg-background-light dark:bg-background-dark">
    <Sidebar />
    <main class="flex-1 flex flex-col h-full overflow-hidden">
      <ChatView />
    </main>
  </div>
</template>
