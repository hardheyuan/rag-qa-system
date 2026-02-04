import api from './interceptor'

/**
 * 认证相关 API 服务
 * 
 * 封装登录、注册、刷新Token等认证接口
 */
export const authApi = {
  /**
   * 用户登录
   * @param {string} username 用户名
   * @param {string} password 密码
   * @returns {Promise} 登录响应
   */
  login: (username, password) => {
    return api.post('/auth/login', { username, password })
  },
  
  /**
   * 用户注册
   * @param {Object} data 注册数据
   * @returns {Promise} 注册响应
   */
  register: (data) => {
    return api.post('/auth/register', data)
  },
  
  /**
   * 刷新 Access Token
   * @param {string} refreshToken Refresh Token
   * @returns {Promise} 刷新响应
   */
  refreshToken: (refreshToken) => {
    return api.post('/auth/refresh', { refreshToken })
  },
  
  /**
   * 用户登出
   * @param {string} token 可选的Access Token
   * @returns {Promise} 登出响应
   */
  logout: (token) => {
    const config = token
      ? { headers: { Authorization: `Bearer ${token}` } }
      : undefined
    return api.post('/auth/logout', null, config)
  },
  
  /**
   * 获取当前登录用户信息
   * @returns {Promise} 用户信息
   */
  getCurrentUser: () => {
    return api.get('/auth/me')
  },
  
  /**
   * 验证Token有效性
   * @returns {Promise} 验证结果
   */
  validateToken: () => {
    return api.get('/auth/validate')
  }
}

/**
 * 文档相关 API 服务
 */
export const documentApi = {
  /**
   * 获取文档列表
   * @returns {Promise} 文档列表
   */
  getDocuments: () => {
    return api.get('/documents')
  },
  
  /**
   * 上传文档
   * @param {FormData} formData 包含文件的表单数据
   * @returns {Promise} 上传响应
   */
  uploadDocument: (formData) => {
    return api.post('/documents', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },
  
  /**
   * 删除文档
   * @param {string} documentId 文档ID
   * @returns {Promise} 删除响应
   */
  deleteDocument: (documentId) => {
    return api.delete(`/documents/${documentId}`)
  }
}

/**
 * 问答相关 API 服务
 */
export const qaApi = {
  /**
   * 发送问题
   * @param {Object} data 问题数据
   * @returns {Promise} 回答响应
   */
  askQuestion: (data) => {
    return api.post('/qa/ask', data)
  },
  
  /**
   * 流式问答
   * @param {Object} data 问题数据
   * @returns {Promise} SSE流
   */
  askQuestionStream: (data) => {
    return api.post('/qa/ask/stream', data, {
      responseType: 'stream'
    })
  }
}

/**
 * 历史记录相关 API 服务
 */
export const historyApi = {
  /**
   * 获取问答历史
   * @returns {Promise} 历史记录列表
   */
  getHistory: () => {
    return api.get('/history')
  }
}

/**
 * 统计相关 API 服务
 */
export const statisticsApi = {
  /**
   * 获取统计数据
   * @returns {Promise} 统计数据
   */
  getStatistics: () => {
    return api.get('/statistics')
  }
}

/**
 * 系统相关 API 服务
 */
export const systemApi = {
  /**
   * 获取系统健康状态
   * @returns {Promise} 健康状态
   */
  getHealth: () => {
    return api.get('/actuator/health')
  }
}
