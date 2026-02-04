import axios from 'axios'

/**
 * Axios 实例配置
 * 
 * 创建带默认配置的 axios 实例
 * 基础URL指向后端API服务器
 */
const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

export default api
