import apiClient from './index'

export const aiProviderApi = {
  // 获取支持的提供商列表
  getSupportedProviders() {
    return apiClient.get('/admin/ai-providers/providers')
  },
  
  // 获取所有配置
  getAllConfigs() {
    return apiClient.get('/admin/ai-providers')
  },
  
  // 获取单个配置
  getConfigById(id) {
    return apiClient.get(`/admin/ai-providers/${id}`)
  },
  
  // 获取当前激活的配置
  getCurrentConfig() {
    return apiClient.get('/admin/ai-providers/current')
  },
  
  // 创建配置
  createConfig(data) {
    return apiClient.post('/admin/ai-providers', data)
  },
  
  // 更新配置
  updateConfig(id, data) {
    return apiClient.put(`/admin/ai-providers/${id}`, data)
  },
  
  // 删除配置
  deleteConfig(id) {
    return apiClient.delete(`/admin/ai-providers/${id}`)
  },
  
  // 激活配置
  activateConfig(id) {
    return apiClient.post(`/admin/ai-providers/${id}/activate`)
  }
}
