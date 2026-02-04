import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '@/api/interceptor'
import { useUserStore } from './user'

export const useChatStore = defineStore('chat', () => {
  // 当前活跃的对话
  const messages = ref([])
  const isLoading = ref(false)
  const currentChatId = ref(null)
  
  // 所有对话记录
  const conversations = ref([])
  
  const MAX_CONVERSATIONS = 20
  const STORAGE_KEY = 'rag_conversations'
  const CURRENT_CHAT_KEY = 'rag_current_chat_id'

  const getChatMessages = computed(() => messages.value)
  
  // 按时间分组的对话题列表
  const groupedConversations = computed(() => {
    const groups = {
      today: [],
      yesterday: [],
      last7Days: [],
      older: []
    }
    
    const now = new Date()
    const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
    const yesterday = new Date(today)
    yesterday.setDate(yesterday.getDate() - 1)
    const last7Days = new Date(today)
    last7Days.setDate(last7Days.getDate() - 7)
    
    conversations.value.forEach(conv => {
      const updatedAt = new Date(conv.updatedAt)
      
      if (updatedAt >= today) {
        groups.today.push(conv)
      } else if (updatedAt >= yesterday) {
        groups.yesterday.push(conv)
      } else if (updatedAt >= last7Days) {
        groups.last7Days.push(conv)
      } else {
        groups.older.push(conv)
      }
    })
    
    // 按时间倒序排序
    Object.keys(groups).forEach(key => {
      groups[key].sort((a, b) => new Date(b.updatedAt) - new Date(a.updatedAt))
    })
    
    return groups
  })

  function generateId() {
    return Date.now().toString(36) + Math.random().toString(36).substr(2)
  }

  function generateTitle(messages) {
    const firstUserMessage = messages.find(m => m.type === 'user')
    if (firstUserMessage) {
      const content = firstUserMessage.content
      return content.length > 20 ? content.substring(0, 20) + '...' : content
    }
    return 'New Chat'
  }

  function createNewConversation() {
    // 如果当前有活跃对话，先保存
    if (currentChatId.value && messages.value.length > 0) {
      saveCurrentConversation()
    }
    
    // 创建新对话
    const newChat = {
      id: generateId(),
      title: 'New Chat',
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
      messages: []
    }
    
    conversations.value.unshift(newChat)
    currentChatId.value = newChat.id
    messages.value = []
    
    // 限制对话数量
    if (conversations.value.length > MAX_CONVERSATIONS) {
      conversations.value = conversations.value.slice(0, MAX_CONVERSATIONS)
    }
    
    saveToStorage()
    return newChat.id
  }

  function saveCurrentConversation() {
    if (!currentChatId.value) return
    
    const index = conversations.value.findIndex(c => c.id === currentChatId.value)
    if (index !== -1) {
      conversations.value[index].messages = [...messages.value]
      conversations.value[index].updatedAt = new Date().toISOString()
      
      // 如果有消息，自动生成标题
      if (messages.value.length > 0 && conversations.value[index].title === 'New Chat') {
        conversations.value[index].title = generateTitle(messages.value)
      }
      
      saveToStorage()
    }
  }

  function loadConversation(chatId) {
    // 先保存当前对话
    if (currentChatId.value && messages.value.length > 0) {
      saveCurrentConversation()
    }
    
    const conversation = conversations.value.find(c => c.id === chatId)
    if (conversation) {
      currentChatId.value = conversation.id
      messages.value = [...conversation.messages]
      localStorage.setItem(CURRENT_CHAT_KEY, chatId)
      return true
    }
    return false
  }

  function deleteConversation(chatId) {
    const index = conversations.value.findIndex(c => c.id === chatId)
    if (index !== -1) {
      conversations.value.splice(index, 1)
      
      // 如果删除的是当前活跃对话
      if (currentChatId.value === chatId) {
        if (conversations.value.length > 0) {
          // 加载最新的对话
          loadConversation(conversations.value[0].id)
        } else {
          // 没有对话了，创建新对话
          createNewConversation()
        }
      }
      
      saveToStorage()
      return true
    }
    return false
  }

  function addMessage(type, content, citations = []) {
    const message = {
      id: Date.now(),
      type,
      content,
      citations,
      timestamp: new Date().toISOString()
    }
    messages.value.push(message)
    
    // 更新当前对话
    if (currentChatId.value) {
      const index = conversations.value.findIndex(c => c.id === currentChatId.value)
      if (index !== -1) {
        conversations.value[index].messages.push(message)
        conversations.value[index].updatedAt = new Date().toISOString()
        
        // 第一条用户消息时自动生成标题
        if (type === 'user' && conversations.value[index].title === 'New Chat') {
          conversations.value[index].title = generateTitle(conversations.value[index].messages)
        }
      }
    }
    
    saveToStorage()
  }

  async function sendMessage(question) {
    if (!question.trim()) return

    // 如果没有活跃对话，创建一个新的
    if (!currentChatId.value) {
      createNewConversation()
    }

    addMessage('user', question)
    isLoading.value = true

    try {
      // 使用配置了JWT拦截器的 api 实例
      const response = await api.post('/qa/ask', {
        question
      })

      const result = response.data
      isLoading.value = false

      if (result.code === 200) {
        addMessage('assistant', result.data.answer, result.data.citations || [])
      } else {
        addMessage('assistant', `抱歉，我遇到了一些问题：${result.message}`)
      }
    } catch (error) {
      isLoading.value = false
      console.error('发送消息失败:', error)
      
      // 如果是401错误，会被拦截器处理
      if (error.response?.status === 401) {
        addMessage('assistant', '您的登录已过期，请重新登录')
      } else {
        addMessage('assistant', `抱歉，我遇到了一些问题：${error.message}`)
      }
    }
  }

  function clearMessages() {
    // 保存当前对话后再清空
    if (currentChatId.value && messages.value.length > 0) {
      saveCurrentConversation()
    }
    
    messages.value = []
    currentChatId.value = null
    localStorage.removeItem(CURRENT_CHAT_KEY)
    
    // 创建新对话
    createNewConversation()
  }

  function saveToStorage() {
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(conversations.value))
      if (currentChatId.value) {
        localStorage.setItem(CURRENT_CHAT_KEY, currentChatId.value)
      }
    } catch (e) {
      console.error('保存失败', e)
    }
  }

  function loadFromStorage() {
    try {
      // 加载所有对话
      const saved = localStorage.getItem(STORAGE_KEY)
      if (saved) {
        conversations.value = JSON.parse(saved)
      }
      
      // 加载当前对话ID
      const savedChatId = localStorage.getItem(CURRENT_CHAT_KEY)
      if (savedChatId) {
        const conversation = conversations.value.find(c => c.id === savedChatId)
        if (conversation) {
          currentChatId.value = savedChatId
          messages.value = [...conversation.messages]
        } else {
          // 找不到保存的对话ID，创建新对话
          createNewConversation()
        }
      } else if (conversations.value.length > 0) {
        // 没有保存的ID，加载最新的
        loadConversation(conversations.value[0].id)
      } else {
        // 没有对话，创建新对话
        createNewConversation()
      }
    } catch (e) {
      console.error('加载失败', e)
      conversations.value = []
      createNewConversation()
    }
  }

  return {
    messages,
    isLoading,
    currentChatId,
    conversations,
    getChatMessages,
    groupedConversations,
    createNewConversation,
    loadConversation,
    deleteConversation,
    sendMessage,
    clearMessages,
    loadFromStorage,
    saveCurrentConversation
  }
})
