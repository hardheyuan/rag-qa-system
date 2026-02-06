<template>
  <div class="min-h-screen bg-gray-50">
    <!-- 顶部导航栏 -->
    <nav class="bg-white shadow-sm border-b border-gray-200">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex justify-between h-16">
          <div class="flex items-center">
            <button @click="$router.back()" class="mr-4 text-gray-500 hover:text-gray-700">
              <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18"/>
              </svg>
            </button>
            <h1 class="text-xl font-semibold text-gray-900">AI模型配置</h1>
          </div>
          <button 
            @click="showModal = true"
            class="flex items-center gap-2 bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-lg transition-colors">
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"/>
            </svg>
            新增配置
          </button>
        </div>
      </div>
    </nav>
    
    <!-- 主内容区 -->
    <main class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <!-- 加载状态 -->
      <div v-if="loading" class="flex justify-center py-12">
        <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
      </div>
      
      <!-- 空状态 -->
      <div v-else-if="configs.length === 0" class="text-center py-12">
        <div class="bg-gray-100 rounded-full w-24 h-24 flex items-center justify-center mx-auto mb-4">
          <svg class="w-12 h-12 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9.663 17h4.673M12 3v1m6.364 1.636l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 117.072 0l-.548.547A3.374 3.374 0 0014 18.469V19a2 2 0 11-4 0v-.531c0-.895-.356-1.754-.988-2.386l-.548-.547z"/>
          </svg>
        </div>
        <h3 class="text-lg font-medium text-gray-900 mb-2">暂无AI配置</h3>
        <p class="text-gray-500 mb-4">点击"新增配置"按钮添加您的第一个AI提供商配置</p>
      </div>
      
      <!-- 配置列表 -->
      <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        <div v-for="config in configs" :key="config.id" 
             class="bg-white rounded-lg shadow-md p-6 border-2 transition-all duration-200"
             :class="config.isActive ? 'border-green-500 ring-2 ring-green-200' : 'border-gray-200 hover:border-gray-300'">
          <!-- 头部 -->
          <div class="flex justify-between items-start mb-4">
            <div class="flex items-center gap-3">
              <div class="w-12 h-12 rounded-lg flex items-center justify-center text-white font-bold text-lg"
                   :class="getProviderColor(config.providerCode)">
                {{ getProviderInitials(config.providerCode) }}
              </div>
              <div>
                <h3 class="font-semibold text-lg text-gray-900">{{ config.providerName }}</h3>
                <p class="text-sm text-gray-500">{{ config.providerCode }}</p>
              </div>
            </div>
            <div v-if="config.isActive" class="bg-green-100 text-green-800 px-3 py-1 rounded-full text-sm font-medium">
              当前使用中
            </div>
          </div>
          
          <!-- 配置信息 -->
          <div class="space-y-2 text-sm text-gray-600 mb-4">
            <div class="flex justify-between">
              <span class="text-gray-500">模型:</span>
              <span class="font-medium">{{ config.chatModel }}</span>
            </div>
            <div class="flex justify-between">
              <span class="text-gray-500">API密钥:</span>
              <span class="font-mono">{{ config.apiKeyMasked }}</span>
            </div>
            <div class="flex justify-between">
              <span class="text-gray-500">温度:</span>
              <span>{{ config.temperature }}</span>
            </div>
            <div class="flex justify-between">
              <span class="text-gray-500">最大Token:</span>
              <span>{{ config.maxTokens }}</span>
            </div>
          </div>
          
          <!-- 操作按钮 -->
          <div class="flex gap-2 pt-4 border-t border-gray-100">
            <button 
              v-if="!config.isActive"
              @click="activateConfig(config.id)"
              class="flex-1 bg-green-600 hover:bg-green-700 text-white px-4 py-2 rounded-lg text-sm font-medium transition-colors">
              激活
            </button>
            <button 
              @click="editConfig(config)"
              class="flex-1 bg-gray-100 hover:bg-gray-200 text-gray-700 px-4 py-2 rounded-lg text-sm font-medium transition-colors">
              编辑
            </button>
            <button 
              v-if="!config.isActive"
              @click="deleteConfig(config.id)"
              class="bg-red-100 hover:bg-red-200 text-red-700 px-4 py-2 rounded-lg text-sm font-medium transition-colors">
              删除
            </button>
          </div>
        </div>
      </div>
    </main>
    
    <!-- 新增/编辑弹窗 -->
    <div v-if="showModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
      <div class="bg-white rounded-xl shadow-2xl w-full max-w-lg max-h-[90vh] overflow-y-auto">
        <div class="flex justify-between items-center p-6 border-b border-gray-200">
          <h2 class="text-xl font-semibold text-gray-900">{{ isEditing ? '编辑AI配置' : '新增AI配置' }}</h2>
          <button @click="closeModal" class="text-gray-400 hover:text-gray-600 transition-colors">
            <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
            </svg>
          </button>
        </div>
        
        <form @submit.prevent="handleSubmit" class="p-6 space-y-4">
          <!-- 提供商选择 -->
          <div v-if="!isEditing">
            <label class="block text-sm font-medium text-gray-700 mb-1">选择提供商 *</label>
            <select v-model="form.providerCode" @change="onProviderChange" required
                    class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent">
              <option value="">请选择提供商</option>
              <option v-for="provider in supportedProviders" :key="provider.code" :value="provider.code">
                {{ provider.name }}
              </option>
            </select>
          </div>
          
          <!-- Base URL -->
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">API基础URL *</label>
            <input v-model="form.baseUrl" type="url" required
                   placeholder="https://api.example.com/v1"
                   class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent">
          </div>
          
          <!-- API Key -->
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">API密钥 *</label>
            <div class="relative">
              <input v-model="form.apiKey" :type="showApiKey ? 'text' : 'password'" required
                     placeholder="sk-xxxxxxxxxxxxxxxx"
                     class="w-full px-4 py-2 pr-10 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent">
              <button type="button" @click="showApiKey = !showApiKey"
                      class="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600">
                <span v-if="showApiKey">🙈</span>
                <span v-else>👁️</span>
              </button>
            </div>
          </div>
          
          <!-- 模型选择 -->
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">模型 *</label>
            <select v-model="form.chatModel" required
                    class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent">
              <option value="">请选择模型</option>
              <option v-for="model in availableModels" :key="model" :value="model">{{ model }}</option>
            </select>
          </div>
          
          <!-- 温度 -->
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">温度 ({{ form.temperature }})</label>
            <input v-model.number="form.temperature" type="range" min="0" max="2" step="0.1"
                   class="w-full">
            <div class="flex justify-between text-xs text-gray-500 mt-1">
              <span>保守 (0)</span>
              <span>平衡 (1)</span>
              <span>创造性 (2)</span>
            </div>
          </div>
          
          <!-- 最大Token -->
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">最大Token数 *</label>
            <input v-model.number="form.maxTokens" type="number" min="1" max="8192" required
                   class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent">
          </div>
          
          <!-- 错误提示 -->
          <div v-if="error" class="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg text-sm">
            {{ error }}
          </div>
          
          <!-- 按钮 -->
          <div class="flex gap-3 pt-4">
            <button type="button" @click="closeModal"
                    class="flex-1 px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors">
              取消
            </button>
            <button type="submit" :disabled="modalLoading"
                    class="flex-1 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors">
              {{ modalLoading ? '保存中...' : '保存' }}
            </button>
          </div>
        </form>
      </div>
    </div>
    
    <!-- 成功提示 -->
    <div v-if="showSuccess" class="fixed top-4 right-4 bg-green-600 text-white px-6 py-3 rounded-lg shadow-lg z-50">
      {{ successMessage }}
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useAiProviderStore } from '@/stores/aiProviders'

const store = useAiProviderStore()

// 状态
const loading = ref(false)
const showModal = ref(false)
const isEditing = ref(false)
const editingId = ref(null)
const modalLoading = ref(false)
const error = ref(null)
const showApiKey = ref(false)
const showSuccess = ref(false)
const successMessage = ref('')

// 表单
const form = ref({
  providerCode: '',
  baseUrl: '',
  apiKey: '',
  chatModel: '',
  temperature: 0.7,
  maxTokens: 4096
})

// 计算属性
const configs = computed(() => store.configs)
const supportedProviders = computed(() => store.supportedProviders)

const availableModels = computed(() => {
  if (!form.value.providerCode) return []
  const provider = supportedProviders.value.find(p => p.code === form.value.providerCode)
  return provider?.supportedModels || []
})

// 初始化
onMounted(async () => {
  loading.value = true
  try {
    await Promise.all([
      store.fetchSupportedProviders(),
      store.fetchAllConfigs()
    ])
  } catch (err) {
    error.value = err.message
  } finally {
    loading.value = false
  }
})

// 方法
const getProviderColor = (code) => {
  const colors = {
    'SILICONFLOW': 'bg-blue-600',
    'MODELSCOPE': 'bg-purple-600',
    'NVIDIA': 'bg-green-600'
  }
  return colors[code] || 'bg-gray-600'
}

const getProviderInitials = (code) => {
  const names = {
    'SILICONFLOW': 'SF',
    'MODELSCOPE': 'MS',
    'NVIDIA': 'NV'
  }
  return names[code] || 'AI'
}

const onProviderChange = () => {
  const provider = supportedProviders.value.find(p => p.code === form.value.providerCode)
  if (provider) {
    form.value.baseUrl = provider.defaultBaseUrl
    form.value.chatModel = provider.supportedModels[0] || ''
  }
}

const editConfig = (config) => {
  editingId.value = config.id
  isEditing.value = true
  form.value = {
    providerCode: config.providerCode,
    baseUrl: config.baseUrl,
    apiKey: '', // 编辑时不回填密钥
    chatModel: config.chatModel,
    temperature: config.temperature,
    maxTokens: config.maxTokens
  }
  showModal.value = true
}

const closeModal = () => {
  showModal.value = false
  isEditing.value = false
  editingId.value = null
  error.value = null
  form.value = {
    providerCode: '',
    baseUrl: '',
    apiKey: '',
    chatModel: '',
    temperature: 0.7,
    maxTokens: 4096
  }
}

const handleSubmit = async () => {
  modalLoading.value = true
  error.value = null
  
  try {
    if (isEditing.value) {
      await store.updateConfig(editingId.value, form.value)
      showSuccessMsg('配置已更新')
    } else {
      await store.createConfig(form.value)
      showSuccessMsg('配置已创建')
    }
    closeModal()
  } catch (err) {
    error.value = err.response?.data?.message || err.message
  } finally {
    modalLoading.value = false
  }
}

const activateConfig = async (id) => {
  if (!confirm('切换AI提供商后需要重启应用才能生效，是否继续？')) return
  
  try {
    const result = await store.activateConfig(id)
    showSuccessMsg(result.message || '配置已激活')
  } catch (err) {
    error.value = err.message
  }
}

const deleteConfig = async (id) => {
  if (!confirm('确定要删除此配置吗？')) return
  
  try {
    await store.deleteConfig(id)
    showSuccessMsg('配置已删除')
  } catch (err) {
    error.value = err.message
  }
}

const showSuccessMsg = (msg) => {
  successMessage.value = msg
  showSuccess.value = true
  setTimeout(() => {
    showSuccess.value = false
  }, 3000)
}
</script>
