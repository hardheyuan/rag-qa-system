import { defineStore } from 'pinia'
import { aiProviderApi } from '@/api/aiProviders'

export const useAiProviderStore = defineStore('aiProviders', {
  state: () => ({
    configs: [],
    supportedProviders: [],
    currentConfig: null,
    loading: false,
    error: null
  }),
  
  getters: {
    activeConfig: (state) => state.configs.find(c => c.isActive),
    configsByProvider: (state) => {
      return state.configs.reduce((acc, config) => {
        if (!acc[config.providerCode]) {
          acc[config.providerCode] = []
        }
        acc[config.providerCode].push(config)
        return acc
      }, {})
    }
  },
  
  actions: {
    async fetchSupportedProviders() {
      try {
        const response = await aiProviderApi.getSupportedProviders()
        this.supportedProviders = response.data
        return response.data
      } catch (error) {
        this.error = error.message
        throw error
      }
    },
    
    async fetchAllConfigs() {
      this.loading = true
      try {
        const response = await aiProviderApi.getAllConfigs()
        this.configs = response.data
        return response.data
      } catch (error) {
        this.error = error.message
        throw error
      } finally {
        this.loading = false
      }
    },
    
    async fetchCurrentConfig() {
      try {
        const response = await aiProviderApi.getCurrentConfig()
        this.currentConfig = response.data
        return response.data
      } catch (error) {
        this.error = error.message
        throw error
      }
    },
    
    async createConfig(configData) {
      try {
        const response = await aiProviderApi.createConfig(configData)
        this.configs.push(response.data)
        return response.data
      } catch (error) {
        this.error = error.message
        throw error
      }
    },
    
    async updateConfig(id, configData) {
      try {
        const response = await aiProviderApi.updateConfig(id, configData)
        const index = this.configs.findIndex(c => c.id === id)
        if (index !== -1) {
          this.configs[index] = response.data
        }
        return response.data
      } catch (error) {
        this.error = error.message
        throw error
      }
    },
    
    async deleteConfig(id) {
      try {
        await aiProviderApi.deleteConfig(id)
        this.configs = this.configs.filter(c => c.id !== id)
      } catch (error) {
        this.error = error.message
        throw error
      }
    },
    
    async activateConfig(id) {
      try {
        const response = await aiProviderApi.activateConfig(id)
        // 更新本地状态
        this.configs.forEach(config => {
          config.isActive = (config.id === id)
        })
        return response.data
      } catch (error) {
        this.error = error.message
        throw error
      }
    },
    
    getProviderInfo(providerCode) {
      return this.supportedProviders.find(p => p.code === providerCode)
    },
    
    clearError() {
      this.error = null
    }
  }
})
