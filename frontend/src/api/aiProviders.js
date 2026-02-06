import api from './interceptor'

export const aiProviderApi = {
  // 获取支持的提供商列表
  getSupportedProviders() {
    return api.get('/admin/ai-providers/providers')
  },
  
  // 获取所有配置
  getAllConfigs() {
    return api.get('/admin/ai-providers')
  },
  
  // 获取单个配置
  getConfigById(id) {
    return api.get(`/admin/ai-providers/${id}`)
  },
  
  // 获取当前激活的配置
  getCurrentConfig() {
    return api.get('/admin/ai-providers/current')
  },
  
  // 创建配置
  createConfig(data) {
    return api.post('/admin/ai-providers', data)
  },
  
  // 更新配置
  updateConfig(id, data) {
    return api.put(`/admin/ai-providers/${id}`, data)
  },
  
  // 删除配置
  deleteConfig(id) {
    return api.delete(`/admin/ai-providers/${id}`)
  },
  
  // 激活配置
  activateConfig(id) {
    return api.post(`/admin/ai-providers/${id}/activate`)
  }
}
