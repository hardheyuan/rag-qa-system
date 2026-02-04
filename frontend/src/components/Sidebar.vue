<template>
  <aside class="flex w-80 flex-col bg-background-light dark:bg-background-dark border-r border-gray-200 dark:border-gray-800 shrink-0 z-20 hidden md:flex">
    <!-- Header Logo Area -->
    <div class="flex items-center gap-3 px-6 py-5">
      <div class="flex items-center justify-center w-8 h-8 bg-primary rounded-lg text-white">
        <span class="material-symbols-outlined text-[20px]">school</span>
      </div>
      <span class="text-xl font-bold tracking-tight text-[#111418] dark:text-white">ScholarAI</span>
    </div>

    <!-- New Chat Button -->
    <div class="px-4 pb-2">
      <button @click="handleNewChat" class="flex w-full cursor-pointer items-center justify-center overflow-hidden rounded-lg h-12 px-5 bg-primary hover:bg-blue-600 transition text-white gap-2 text-base font-bold leading-normal tracking-[0.015em] shadow-sm hover:shadow-md">
        <span class="material-symbols-outlined">add</span>
        <span class="truncate">New Chat</span>
      </button>
    </div>

    <!-- Feedback Button - 我有话说 -->
    <div class="px-4 py-2">
      <button 
        @click="openFeedbackDialog" 
        class="flex w-full cursor-pointer items-center gap-3 px-4 py-3 rounded-lg border border-amber-200 dark:border-amber-900/30 bg-amber-50 dark:bg-amber-900/10 hover:bg-amber-100 dark:hover:bg-amber-900/20 transition group"
      >
        <div class="flex items-center justify-center w-8 h-8 rounded-lg bg-amber-100 dark:bg-amber-900/30 text-amber-600 dark:text-amber-400 shrink-0">
          <span class="material-symbols-outlined text-[20px]">feedback</span>
        </div>
        <div class="flex flex-col flex-1 min-w-0">
          <span class="text-sm font-semibold text-amber-800 dark:text-amber-300 truncate">我有话说</span>
          <span class="text-xs text-amber-600 dark:text-amber-400 truncate">反馈问题或建议</span>
        </div>
        <span class="material-symbols-outlined text-amber-400 group-hover:text-amber-600 dark:group-hover:text-amber-300 transition text-[18px]">arrow_forward_ios</span>
      </button>
    </div>

    <!-- Separator -->
    <div class="mx-4 my-2 border-t border-gray-200 dark:border-gray-700"></div>

    <!-- Chat History List -->
    <div class="flex-1 overflow-y-auto px-2 py-2">
      <!-- Today -->
      <template v-if="groupedConversations.today.length > 0">
        <h2 class="text-[#617289] dark:text-gray-400 text-xs font-bold uppercase tracking-wider px-4 pb-2 pt-4">Today</h2>
        <div v-for="chat in groupedConversations.today" 
             :key="chat.id"
             @click="selectChat(chat)"
             @contextmenu.prevent="showContextMenu($event, chat)"
             :class="{
               'bg-white dark:bg-[#1a222d] shadow-sm border border-gray-100 dark:border-gray-800': currentChatId === chat.id,
               'hover:bg-gray-100 dark:hover:bg-[#252f3e]': currentChatId !== chat.id
             }"
             class="flex items-center gap-3 p-3 mx-2 rounded-lg cursor-pointer group transition">
          <div class="text-[#111418] dark:text-white flex items-center justify-center rounded-lg bg-[#f0f2f4] dark:bg-gray-700 shrink-0 size-8">
            <span class="material-symbols-outlined text-[18px]">chat_bubble</span>
          </div>
          <p class="text-[#111418] dark:text-white text-sm font-medium leading-normal flex-1 truncate">{{ chat.title }}</p>
        </div>
      </template>

      <!-- Yesterday -->
      <template v-if="groupedConversations.yesterday.length > 0">
        <h2 class="text-[#617289] dark:text-gray-400 text-xs font-bold uppercase tracking-wider px-4 pb-2 pt-6">Yesterday</h2>
        <div v-for="chat in groupedConversations.yesterday" 
             :key="chat.id"
             @click="selectChat(chat)"
             @contextmenu.prevent="showContextMenu($event, chat)"
             :class="{
               'bg-white dark:bg-[#1a222d] shadow-sm border border-gray-100 dark:border-gray-800': currentChatId === chat.id,
               'hover:bg-gray-200/50 dark:hover:bg-white/5 opacity-70 hover:opacity-100': currentChatId !== chat.id
             }"
             class="flex items-center gap-3 p-3 mx-2 rounded-lg cursor-pointer transition">
          <div class="text-gray-500 dark:text-gray-400 flex items-center justify-center shrink-0 size-8">
            <span class="material-symbols-outlined text-[18px]">history_edu</span>
          </div>
          <p class="text-[#111418] dark:text-gray-300 text-sm font-normal leading-normal flex-1 truncate">{{ chat.title }}</p>
        </div>
      </template>

      <!-- Last 7 Days -->
      <template v-if="groupedConversations.last7Days.length > 0">
        <h2 class="text-[#617289] dark:text-gray-400 text-xs font-bold uppercase tracking-wider px-4 pb-2 pt-6">Last 7 Days</h2>
        <div v-for="chat in groupedConversations.last7Days" 
             :key="chat.id"
             @click="selectChat(chat)"
             @contextmenu.prevent="showContextMenu($event, chat)"
             :class="{
               'bg-white dark:bg-[#1a222d] shadow-sm border border-gray-100 dark:border-gray-800': currentChatId === chat.id,
               'hover:bg-gray-200/50 dark:hover:bg-white/5 opacity-60 hover:opacity-100': currentChatId !== chat.id
             }"
             class="flex items-center gap-3 p-3 mx-2 rounded-lg cursor-pointer transition">
          <div class="text-gray-500 dark:text-gray-400 flex items-center justify-center shrink-0 size-8">
            <span class="material-symbols-outlined text-[18px]">history_edu</span>
          </div>
          <p class="text-[#111418] dark:text-gray-300 text-sm font-normal leading-normal flex-1 truncate">{{ chat.title }}</p>
        </div>
      </template>

      <!-- Older -->
      <template v-if="groupedConversations.older.length > 0">
        <h2 class="text-[#617289] dark:text-gray-400 text-xs font-bold uppercase tracking-wider px-4 pb-2 pt-6">Older</h2>
        <div v-for="chat in groupedConversations.older" 
             :key="chat.id"
             @click="selectChat(chat)"
             @contextmenu.prevent="showContextMenu($event, chat)"
             :class="{
               'bg-white dark:bg-[#1a222d] shadow-sm border border-gray-100 dark:border-gray-800': currentChatId === chat.id,
               'hover:bg-gray-200/50 dark:hover:bg-white/5 opacity-50 hover:opacity-100': currentChatId !== chat.id
             }"
             class="flex items-center gap-3 p-3 mx-2 rounded-lg cursor-pointer transition">
          <div class="text-gray-500 dark:text-gray-400 flex items-center justify-center shrink-0 size-8">
            <span class="material-symbols-outlined text-[18px]">history_edu</span>
          </div>
          <p class="text-[#111418] dark:text-gray-300 text-sm font-normal leading-normal flex-1 truncate">{{ chat.title }}</p>
        </div>
      </template>

      <!-- Empty State -->
      <div v-if="conversations.length === 0" class="px-4 py-8 text-center">
        <p class="text-gray-400 dark:text-gray-500 text-sm">No conversations yet</p>
        <p class="text-gray-400 dark:text-gray-500 text-xs mt-1">Click "New Chat" to start</p>
      </div>
    </div>

    <!-- User Profile -->
    <div class="p-4 border-t border-gray-200 dark:border-gray-800 bg-background-light dark:bg-background-dark">
      <div class="flex items-center gap-3 p-2 rounded-xl hover:bg-white dark:hover:bg-white/5 cursor-pointer transition group">
        <div class="bg-center bg-no-repeat bg-cover rounded-full h-10 w-10 shrink-0 border border-gray-200 dark:border-gray-600"
             :style="`background-image: url('${user.avatar}')`"></div>
        <div class="flex flex-col flex-1 min-w-0">
          <p class="text-[#111418] dark:text-white text-sm font-bold leading-tight truncate">{{ user.name }}</p>
          <p class="text-[#617289] dark:text-gray-400 text-xs font-normal truncate">{{ user.major }}</p>
        </div>
        <span class="material-symbols-outlined text-gray-400 group-hover:text-primary cursor-pointer" @click="userStore.toggleDarkMode()">
          {{ userStore.isDarkMode ? 'light_mode' : 'dark_mode' }}
        </span>
        <span class="material-symbols-outlined text-gray-400 hover:text-red-500 cursor-pointer ml-1" @click="handleLogout" title="Logout">
          logout
        </span>
      </div>
    </div>

    <!-- Context Menu -->
    <div v-if="contextMenu.visible" 
         :style="{ left: contextMenu.x + 'px', top: contextMenu.y + 'px' }"
         class="fixed z-50 bg-white dark:bg-[#1a222c] rounded-lg shadow-lg border border-gray-200 dark:border-gray-700 py-1 min-w-[120px]">
      <button @click="handleDeleteChat" 
              class="w-full px-4 py-2 text-left text-sm text-red-600 dark:text-red-400 hover:bg-gray-100 dark:hover:bg-gray-700 transition flex items-center gap-2">
        <span class="material-symbols-outlined text-[18px]">delete</span>
        Delete
      </button>
    </div>

    <!-- Feedback Dialog Modal -->
    <div v-if="feedbackDialog.visible" 
         class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-sm"
         @click.self="closeFeedbackDialog">
      <div class="bg-white dark:bg-[#1a222c] rounded-xl shadow-2xl border border-gray-200 dark:border-gray-700 w-full max-w-md overflow-hidden">
        <!-- Header -->
        <div class="px-6 py-4 border-b border-gray-200 dark:border-gray-700 flex items-center justify-between">
          <div class="flex items-center gap-3">
            <div class="w-10 h-10 rounded-lg bg-amber-100 dark:bg-amber-900/30 flex items-center justify-center">
              <span class="material-symbols-outlined text-amber-600 dark:text-amber-400">feedback</span>
            </div>
            <div>
              <h3 class="text-lg font-bold text-[#111418] dark:text-white">我有话说</h3>
              <p class="text-xs text-gray-500 dark:text-gray-400">您的反馈对我们很重要</p>
            </div>
          </div>
          <button @click="closeFeedbackDialog" class="text-gray-400 hover:text-gray-600 dark:hover:text-gray-300 transition">
            <span class="material-symbols-outlined">close</span>
          </button>
        </div>

        <!-- Content -->
        <div class="p-6 space-y-4">
          <!-- Feedback Type -->
          <div>
            <label class="block text-sm font-medium text-[#111418] dark:text-white mb-2">反馈类型</label>
            <div class="grid grid-cols-3 gap-2">
              <button 
                @click="feedbackDialog.type = 'suggestion'"
                :class="{
                  'bg-primary text-white border-primary': feedbackDialog.type === 'suggestion',
                  'bg-gray-100 dark:bg-gray-700 text-gray-700 dark:text-gray-300 border-gray-200 dark:border-gray-600 hover:bg-gray-200 dark:hover:bg-gray-600': feedbackDialog.type !== 'suggestion'
                }"
                class="px-3 py-2 rounded-lg border text-sm font-medium transition">
                建议
              </button>
              <button 
                @click="feedbackDialog.type = 'issue'"
                :class="{
                  'bg-red-500 text-white border-red-500': feedbackDialog.type === 'issue',
                  'bg-gray-100 dark:bg-gray-700 text-gray-700 dark:text-gray-300 border-gray-200 dark:border-gray-600 hover:bg-gray-200 dark:hover:bg-gray-600': feedbackDialog.type !== 'issue'
                }"
                class="px-3 py-2 rounded-lg border text-sm font-medium transition">
                问题
              </button>
              <button 
                @click="feedbackDialog.type = 'other'"
                :class="{
                  'bg-amber-500 text-white border-amber-500': feedbackDialog.type === 'other',
                  'bg-gray-100 dark:bg-gray-700 text-gray-700 dark:text-gray-300 border-gray-200 dark:border-gray-600 hover:bg-gray-200 dark:hover:bg-gray-600': feedbackDialog.type !== 'other'
                }"
                class="px-3 py-2 rounded-lg border text-sm font-medium transition">
                其他
              </button>
            </div>
          </div>

          <!-- Feedback Content -->
          <div>
            <label class="block text-sm font-medium text-[#111418] dark:text-white mb-2">
              详细描述
              <span class="text-red-500">*</span>
            </label>
            <textarea 
              v-model="feedbackDialog.content"
              rows="4"
              placeholder="请详细描述您的建议或遇到的问题..."
              class="w-full px-4 py-3 rounded-lg border border-gray-200 dark:border-gray-600 bg-white dark:bg-[#1a222c] text-[#111418] dark:text-white placeholder:text-gray-400 focus:outline-none focus:ring-2 focus:ring-primary/50 focus:border-primary resize-none text-sm"
            ></textarea>
            <p class="text-xs text-gray-400 dark:text-gray-500 mt-1 text-right">{{ feedbackDialog.content.length }}/500</p>
          </div>
        </div>

        <!-- Footer -->
        <div class="px-6 py-4 border-t border-gray-200 dark:border-gray-700 flex items-center justify-end gap-3">
          <button 
            @click="closeFeedbackDialog"
            class="px-4 py-2 rounded-lg text-sm font-medium text-gray-600 dark:text-gray-400 hover:bg-gray-100 dark:hover:bg-gray-700 transition">
            取消
          </button>
          <button 
            @click="submitFeedback"
            :disabled="feedbackDialog.isSubmitting || !feedbackDialog.content.trim()"
            class="px-6 py-2 rounded-lg text-sm font-medium bg-primary text-white hover:bg-blue-600 disabled:opacity-50 disabled:cursor-not-allowed transition flex items-center gap-2">
            <span v-if="!feedbackDialog.isSubmitting">提交反馈</span>
            <span v-else>提交中...</span>
            <span v-if="feedbackDialog.isSubmitting" class="material-symbols-outlined text-[18px] animate-spin">progress_activity</span>
          </button>
        </div>
      </div>
    </div>
  </aside>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useChatStore } from '../stores/chat'
import { useUserStore } from '../stores/user'

const chatStore = useChatStore()
const userStore = useUserStore()
const router = useRouter()
const user = userStore.user

const conversations = computed(() => chatStore.conversations)
const currentChatId = computed(() => chatStore.currentChatId)
const groupedConversations = computed(() => chatStore.groupedConversations)

const contextMenu = ref({
  visible: false,
  x: 0,
  y: 0,
  chat: null
})

// Feedback dialog state
const feedbackDialog = ref({
  visible: false,
  type: 'suggestion', // suggestion, issue, other
  content: '',
  isSubmitting: false
})

function handleNewChat() {
  chatStore.createNewConversation()
}

function selectChat(chat) {
  chatStore.loadConversation(chat.id)
}

function showContextMenu(event, chat) {
  contextMenu.value = {
    visible: true,
    x: event.clientX,
    y: event.clientY,
    chat: chat
  }
}

function handleDeleteChat() {
  if (contextMenu.value.chat) {
    chatStore.deleteConversation(contextMenu.value.chat.id)
    contextMenu.value.visible = false
    contextMenu.value.chat = null
  }
}

function hideContextMenu() {
  contextMenu.value.visible = false
  contextMenu.value.chat = null
}

function handleLogout() {
  userStore.logout()
  router.push('/login')
}

// Feedback functions
function openFeedbackDialog() {
  feedbackDialog.value = {
    visible: true,
    type: 'suggestion',
    content: '',
    isSubmitting: false
  }
}

function closeFeedbackDialog() {
  feedbackDialog.value.visible = false
  feedbackDialog.value.content = ''
  feedbackDialog.value.isSubmitting = false
}

async function submitFeedback() {
  if (!feedbackDialog.value.content.trim()) {
    alert('请输入反馈内容')
    return
  }

  feedbackDialog.value.isSubmitting = true

  // Simulate API call
  await new Promise(resolve => setTimeout(resolve, 1000))

  // In a real app, this would send data to the backend
  console.log('Feedback submitted:', {
    type: feedbackDialog.value.type,
    content: feedbackDialog.value.content,
    user: userStore.user.name,
    timestamp: new Date().toISOString()
  })

  feedbackDialog.value.isSubmitting = false
  feedbackDialog.value.visible = false
  feedbackDialog.value.content = ''

  alert('反馈已提交，感谢您的建议！')
}

// 点击其他地方隐藏右键菜单
onMounted(() => {
  document.addEventListener('click', hideContextMenu)
  chatStore.loadFromStorage()
})

onUnmounted(() => {
  document.removeEventListener('click', hideContextMenu)
})
</script>
