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

          <div>
            <label class="block text-sm font-medium text-[#111418] dark:text-white mb-2">发送给教师</label>
            <select
              v-model="feedbackDialog.teacherId"
              :disabled="feedbackDialog.loadingTeachers || feedbackDialog.teachers.length === 0"
              class="w-full h-11 px-3 rounded-lg border border-gray-200 dark:border-gray-600 bg-white dark:bg-[#1a222c] text-[#111418] dark:text-white focus:outline-none focus:ring-2 focus:ring-primary/50 focus:border-primary text-sm disabled:opacity-60"
            >
              <option value="" disabled>
                {{ feedbackDialog.loadingTeachers ? '正在加载教师列表...' : '请选择教师' }}
              </option>
              <option v-for="teacher in feedbackDialog.teachers" :key="teacher.id" :value="teacher.id">
                {{ teacher.username }}
              </option>
            </select>
            <p v-if="!feedbackDialog.loadingTeachers && feedbackDialog.teachers.length === 0" class="mt-1 text-xs text-amber-600 dark:text-amber-400">
              您暂未绑定教师，暂时无法提交反馈。
            </p>
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

          <div>
            <div class="flex items-center justify-between mb-2">
              <label class="block text-sm font-medium text-[#111418] dark:text-white">我的反馈记录</label>
              <button
                @click="loadFeedbackHistory"
                :disabled="feedbackDialog.loadingHistory"
                class="text-xs text-primary hover:text-blue-600 disabled:opacity-50"
              >
                刷新
              </button>
            </div>
            <div v-if="feedbackDialog.loadingHistory" class="text-xs text-gray-400 dark:text-gray-500">加载中...</div>
            <div v-else-if="feedbackDialog.history.length === 0" class="text-xs text-gray-400 dark:text-gray-500">
              暂无反馈记录
            </div>
            <div v-else class="space-y-2 max-h-40 overflow-y-auto pr-1">
              <div
                v-for="item in feedbackDialog.history"
                :key="item.id"
                class="rounded-lg border border-gray-200 dark:border-gray-700 p-2.5 bg-gray-50 dark:bg-gray-800/50 cursor-pointer hover:bg-gray-100 dark:hover:bg-gray-800 transition"
                @click="openFeedbackHistoryDetail(item)"
              >
                <div class="flex items-center justify-between gap-2 mb-1">
                  <span class="text-xs text-gray-500 dark:text-gray-400">{{ item.teacherName }} · {{ getFeedbackTypeLabel(item.type) }}</span>
                  <span class="text-[10px] px-1.5 py-0.5 rounded" :class="getStatusClass(item.status)">
                    {{ getStatusLabel(item.status) }}
                  </span>
                </div>
                <p class="text-xs text-[#111418] dark:text-white leading-snug">{{ item.content }}</p>
                <p v-if="item.replyContent" class="text-xs text-green-700 dark:text-green-300 mt-1">
                  教师回复：{{ item.replyContent }}
                </p>
                <p class="text-[10px] text-gray-400 dark:text-gray-500 mt-1 flex items-center justify-between">
                  <span>{{ formatFeedbackTime(item.createdAt) }}</span>
                  <span class="text-primary">点击查看详情</span>
                </p>
              </div>
            </div>
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
            :disabled="feedbackDialog.isSubmitting || !feedbackDialog.content.trim() || !feedbackDialog.teacherId"
            class="px-6 py-2 rounded-lg text-sm font-medium bg-primary text-white hover:bg-blue-600 disabled:opacity-50 disabled:cursor-not-allowed transition flex items-center gap-2">
            <span v-if="!feedbackDialog.isSubmitting">提交反馈</span>
            <span v-else>提交中...</span>
            <span v-if="feedbackDialog.isSubmitting" class="material-symbols-outlined text-[18px] animate-spin">progress_activity</span>
          </button>
        </div>
      </div>
    </div>

    <div
      v-if="feedbackDialog.showHistoryDetail && feedbackDialog.selectedHistory"
      class="fixed inset-0 z-[70] flex items-center justify-center p-4 bg-black/60"
      @click.self="closeFeedbackHistoryDetail"
    >
      <div class="bg-white dark:bg-[#1a222c] rounded-xl shadow-2xl border border-gray-200 dark:border-gray-700 w-full max-w-lg overflow-hidden">
        <div class="px-6 py-4 border-b border-gray-200 dark:border-gray-700 flex items-center justify-between">
          <h3 class="text-lg font-bold text-[#111418] dark:text-white">反馈详情</h3>
          <button @click="closeFeedbackHistoryDetail" class="text-gray-400 hover:text-gray-600 dark:hover:text-gray-300 transition">
            <span class="material-symbols-outlined">close</span>
          </button>
        </div>

        <div class="p-6 space-y-3">
          <p class="text-sm text-gray-700 dark:text-gray-300">教师：{{ feedbackDialog.selectedHistory.teacherName }}</p>
          <p class="text-sm text-gray-700 dark:text-gray-300">类型：{{ getFeedbackTypeLabel(feedbackDialog.selectedHistory.type) }}</p>
          <p class="text-sm text-gray-700 dark:text-gray-300">状态：{{ getStatusLabel(feedbackDialog.selectedHistory.status) }}</p>
          <p class="text-sm text-gray-700 dark:text-gray-300">提交时间：{{ formatFeedbackDateTime(feedbackDialog.selectedHistory.createdAt) }}</p>
          <div class="rounded-lg border border-gray-200 dark:border-gray-700 bg-gray-50 dark:bg-gray-800/60 p-3">
            <p class="text-sm text-[#111418] dark:text-white whitespace-pre-wrap">{{ feedbackDialog.selectedHistory.content }}</p>
          </div>
          <div v-if="feedbackDialog.selectedHistory.replyContent" class="rounded-lg border border-green-200 dark:border-green-900/30 bg-green-50 dark:bg-green-900/10 p-3">
            <p class="text-xs text-green-700 dark:text-green-300 mb-1">教师回复</p>
            <p class="text-sm text-green-900 dark:text-green-100 whitespace-pre-wrap">{{ feedbackDialog.selectedHistory.replyContent }}</p>
          </div>
        </div>

        <div class="px-6 py-4 border-t border-gray-200 dark:border-gray-700 flex justify-between items-center">
          <button
            @click="deleteFeedbackHistoryItem"
            :disabled="feedbackDialog.deletingHistory"
            class="px-4 py-2 rounded-lg text-sm font-medium bg-red-500 text-white hover:bg-red-600 disabled:opacity-50 transition"
          >
            {{ feedbackDialog.deletingHistory ? '删除中...' : '删除该反馈' }}
          </button>
          <button
            @click="closeFeedbackHistoryDetail"
            class="px-4 py-2 rounded-lg text-sm font-medium text-gray-600 dark:text-gray-400 hover:bg-gray-100 dark:hover:bg-gray-700 transition"
          >
            关闭
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
import { useToast } from '@/composables/useToast'
import { feedbackApi } from '@/api/feedback'
import { getErrorMessage } from '@/utils/errorHandler'

const chatStore = useChatStore()
const userStore = useUserStore()
const router = useRouter()
const user = userStore.user
const toast = useToast()

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
  teacherId: '',
  teachers: [],
  history: [],
  selectedHistory: null,
  showHistoryDetail: false,
  deletingHistory: false,
  content: '',
  isSubmitting: false,
  loadingTeachers: false,
  loadingHistory: false
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
async function openFeedbackDialog() {
  feedbackDialog.value = {
    visible: true,
    type: 'suggestion',
    teacherId: '',
    teachers: [],
    history: [],
    selectedHistory: null,
    showHistoryDetail: false,
    deletingHistory: false,
    content: '',
    isSubmitting: false,
    loadingTeachers: false,
    loadingHistory: false
  }

  await loadFeedbackDialogData()
}

function closeFeedbackDialog() {
  feedbackDialog.value.visible = false
  feedbackDialog.value.content = ''
  feedbackDialog.value.isSubmitting = false
  feedbackDialog.value.showHistoryDetail = false
  feedbackDialog.value.selectedHistory = null
  feedbackDialog.value.deletingHistory = false
}

async function loadFeedbackDialogData() {
  feedbackDialog.value.loadingTeachers = true
  feedbackDialog.value.loadingHistory = true

  try {
    const [teachers, historyPage] = await Promise.all([
      feedbackApi.getTeacherOptions(),
      feedbackApi.getMyFeedback({ page: 0, size: 5 })
    ])

    feedbackDialog.value.teachers = teachers || []
    feedbackDialog.value.history = historyPage?.content || []

    if (feedbackDialog.value.teachers.length > 0) {
      feedbackDialog.value.teacherId = feedbackDialog.value.teachers[0].id
    }
  } catch (error) {
    toast.error(getErrorMessage(error))
  } finally {
    feedbackDialog.value.loadingTeachers = false
    feedbackDialog.value.loadingHistory = false
  }
}

async function loadFeedbackHistory() {
  feedbackDialog.value.loadingHistory = true
  try {
    const historyPage = await feedbackApi.getMyFeedback({ page: 0, size: 5 })
    feedbackDialog.value.history = historyPage?.content || []
    const selectedId = feedbackDialog.value.selectedHistory?.id
    if (selectedId && !feedbackDialog.value.history.some(item => item.id === selectedId)) {
      closeFeedbackHistoryDetail()
    }
  } catch (error) {
    toast.error(getErrorMessage(error))
  } finally {
    feedbackDialog.value.loadingHistory = false
  }
}

function openFeedbackHistoryDetail(item) {
  feedbackDialog.value.selectedHistory = item
  feedbackDialog.value.showHistoryDetail = true
}

function closeFeedbackHistoryDetail() {
  feedbackDialog.value.showHistoryDetail = false
  feedbackDialog.value.selectedHistory = null
  feedbackDialog.value.deletingHistory = false
}

async function deleteFeedbackHistoryItem() {
  const current = feedbackDialog.value.selectedHistory
  if (!current?.id) {
    return
  }

  const confirmed = window.confirm('确认删除这条反馈记录吗？删除后不可恢复。')
  if (!confirmed) {
    return
  }

  feedbackDialog.value.deletingHistory = true
  try {
    await feedbackApi.deleteMyFeedback(current.id)
    feedbackDialog.value.history = feedbackDialog.value.history.filter(item => item.id !== current.id)
    toast.success('反馈已删除')
    closeFeedbackHistoryDetail()
  } catch (error) {
    toast.error(getErrorMessage(error))
    feedbackDialog.value.deletingHistory = false
  }
}

async function submitFeedback() {
  if (!feedbackDialog.value.content.trim()) {
    toast.warning('请输入反馈内容')
    return
  }

  if (!feedbackDialog.value.teacherId) {
    toast.warning('请先选择教师')
    return
  }

  feedbackDialog.value.isSubmitting = true

  try {
    await feedbackApi.submitFeedback({
      teacherId: feedbackDialog.value.teacherId,
      type: feedbackDialog.value.type,
      content: feedbackDialog.value.content.trim()
    })

    feedbackDialog.value.isSubmitting = false
    feedbackDialog.value.visible = false
    feedbackDialog.value.content = ''
    toast.success('反馈已提交，教师会尽快处理')
  } catch (error) {
    feedbackDialog.value.isSubmitting = false
    toast.error(getErrorMessage(error))
  }
}

function getFeedbackTypeLabel(type) {
  const map = {
    suggestion: '建议',
    issue: '问题',
    other: '其他',
    SUGGESTION: '建议',
    ISSUE: '问题',
    OTHER: '其他'
  }
  return map[type] || '其他'
}

function getStatusLabel(status) {
  return status === 'REPLIED' ? '已回复' : '待处理'
}

function getStatusClass(status) {
  if (status === 'REPLIED') {
    return 'bg-green-100 text-green-700 dark:bg-green-900/30 dark:text-green-300'
  }
  return 'bg-amber-100 text-amber-700 dark:bg-amber-900/30 dark:text-amber-300'
}

function formatFeedbackTime(value) {
  if (!value) {
    return '-'
  }
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) {
    return value
  }
  return date.toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

function formatFeedbackDateTime(value) {
  if (!value) {
    return '-'
  }
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) {
    return value
  }
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
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
