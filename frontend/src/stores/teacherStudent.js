import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '@/api/interceptor'
import { getErrorMessage } from '@/utils/errorHandler'

/**
 * 教师学生管理 Store
 * 
 * 管理教师的学生列表、学生详情、问答历史和文档访问记录
 * 支持添加/移除学生、批量导入和数据导出功能
 */
export const useTeacherStudentStore = defineStore('teacherStudent', () => {
  // ========== State ==========
  
  // 学生列表
  const students = ref([])
  
  // 当前选中的学生详情
  const currentStudent = ref(null)
  
  // 分页信息
  const pagination = ref({
    page: 0,
    size: 20,
    totalElements: 0,
    totalPages: 0
  })
  
  // 加载状态
  const loading = ref(false)
  
  // 搜索关键词
  const searchQuery = ref('')
  
  // 问答历史
  const qaHistory = ref([])
  const qaHistoryPagination = ref({
    page: 0,
    size: 20,
    totalElements: 0,
    totalPages: 0
  })
  
  // 文档访问记录
  const documentAccess = ref([])
  const documentAccessPagination = ref({
    page: 0,
    size: 20,
    totalElements: 0,
    totalPages: 0
  })
  
  // 错误信息
  const error = ref(null)
  
  // ========== Getters ==========
  
  const hasStudents = computed(() => students.value.length > 0)
  const totalStudents = computed(() => pagination.value.totalElements)
  const isFirstPage = computed(() => pagination.value.page === 0)
  const isLastPage = computed(() => pagination.value.page >= pagination.value.totalPages - 1)
  
  // Additional stats computed from students list
  const activeToday = computed(() => {
    if (!students.value.length) return 0
    const today = new Date()
    today.setHours(0, 0, 0, 0)
    return students.value.filter(s => {
      if (!s.lastActivity) return false
      const lastActivity = new Date(s.lastActivity)
      return lastActivity >= today
    }).length
  })
  
  const totalQuestions = computed(() => {
    if (!students.value.length) return 0
    return students.value.reduce((sum, s) => sum + (s.totalQuestions || 0), 0)
  })
  
  const newThisWeek = computed(() => {
    if (!students.value.length) return 0
    const weekAgo = new Date()
    weekAgo.setDate(weekAgo.getDate() - 7)
    weekAgo.setHours(0, 0, 0, 0)
    return students.value.filter(s => {
      if (!s.enrolledAt) return false
      const enrolledAt = new Date(s.enrolledAt)
      return enrolledAt >= weekAgo
    }).length
  })
  
  // ========== Actions ==========
  
  /**
   * 获取学生列表
   * @param {number} page 页码（从0开始）
   * @param {string} search 搜索关键词
   */
  async function fetchStudents(page = 0, search = '') {
    loading.value = true
    error.value = null
    searchQuery.value = search
    
    try {
      const response = await api.get('/teacher/students', {
        params: { 
          page, 
          size: pagination.value.size, 
          search 
        }
      })
      
      if (response.data.code === 200) {
        students.value = response.data.data.content || []
        pagination.value = {
          page: response.data.data.number,
          size: response.data.data.size,
          totalElements: response.data.data.totalElements,
          totalPages: response.data.data.totalPages
        }
      } else {
        error.value = response.data.message || '获取学生列表失败'
        students.value = []
      }
    } catch (err) {
      console.error('获取学生列表失败:', err)
      error.value = getErrorMessage(err)
      students.value = []
    } finally {
      loading.value = false
    }
  }
  
  /**
   * 获取学生详情
   * @param {string} studentId 学生ID
   */
  async function fetchStudentDetail(studentId) {
    loading.value = true
    error.value = null
    
    try {
      const response = await api.get(`/teacher/students/${studentId}`)
      
      if (response.data.code === 200) {
        currentStudent.value = response.data.data
        return { success: true, data: response.data.data }
      } else {
        error.value = response.data.message || '获取学生详情失败'
        return { success: false, message: error.value }
      }
    } catch (err) {
      console.error('获取学生详情失败:', err)
      error.value = getErrorMessage(err)
      return { success: false, message: error.value }
    } finally {
      loading.value = false
    }
  }
  
  /**
   * 添加学生到班级
   * @param {string} identifier 学生用户名或邮箱
   */
  async function addStudent(identifier) {
    loading.value = true
    error.value = null
    
    try {
      const response = await api.post('/teacher/students', { identifier })
      
      if (response.data.code === 200) {
        // 刷新学生列表
        await fetchStudents(pagination.value.page, searchQuery.value)
        return { success: true, message: '学生添加成功' }
      } else {
        error.value = response.data.message || '添加学生失败'
        return { success: false, message: error.value }
      }
    } catch (err) {
      console.error('添加学生失败:', err)
      error.value = getErrorMessage(err)
      return { success: false, message: error.value }
    } finally {
      loading.value = false
    }
  }
  
  /**
   * 批量添加学生
   * @param {File} file CSV或Excel文件
   */
  async function batchAddStudents(file) {
    loading.value = true
    error.value = null
    
    const formData = new FormData()
    formData.append('file', file)
    
    try {
      const response = await api.post('/teacher/students/batch', formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
      })
      
      if (response.data.code === 200) {
        // 刷新学生列表
        await fetchStudents(pagination.value.page, searchQuery.value)
        return { success: true, data: response.data.data }
      } else {
        error.value = response.data.message || '批量添加学生失败'
        return { success: false, message: error.value }
      }
    } catch (err) {
      console.error('批量添加学生失败:', err)
      error.value = getErrorMessage(err)
      return { success: false, message: error.value }
    } finally {
      loading.value = false
    }
  }
  
  /**
   * 从班级移除学生
   * @param {string} studentId 学生ID
   */
  async function removeStudent(studentId) {
    loading.value = true
    error.value = null
    
    try {
      const response = await api.delete(`/teacher/students/${studentId}`)
      
      if (response.data.code === 200) {
        // 刷新学生列表
        await fetchStudents(pagination.value.page, searchQuery.value)
        return { success: true, message: '学生移除成功' }
      } else {
        error.value = response.data.message || '移除学生失败'
        return { success: false, message: error.value }
      }
    } catch (err) {
      console.error('移除学生失败:', err)
      error.value = getErrorMessage(err)
      return { success: false, message: error.value }
    } finally {
      loading.value = false
    }
  }
  
  /**
   * 获取学生问答历史
   * @param {string} studentId 学生ID
   * @param {Object} options 查询选项
   * @param {number} options.page 页码
   * @param {string} options.startDate 开始日期
   * @param {string} options.endDate 结束日期
   */
  async function fetchStudentQaHistory(studentId, options = {}) {
    loading.value = true
    error.value = null
    
    const { page = 0, startDate, endDate } = options
    
    try {
      const params = { 
        page, 
        size: qaHistoryPagination.value.size 
      }
      
      if (startDate) params.startDate = startDate
      if (endDate) params.endDate = endDate
      
      const response = await api.get(`/teacher/students/${studentId}/qa-history`, { params })
      
      if (response.data.code === 200) {
        qaHistory.value = response.data.data.content || []
        qaHistoryPagination.value = {
          page: response.data.data.number,
          size: response.data.data.size,
          totalElements: response.data.data.totalElements,
          totalPages: response.data.data.totalPages
        }
        return { success: true, data: qaHistory.value }
      } else {
        error.value = response.data.message || '获取问答历史失败'
        return { success: false, message: error.value }
      }
    } catch (err) {
      console.error('获取问答历史失败:', err)
      error.value = getErrorMessage(err)
      return { success: false, message: error.value }
    } finally {
      loading.value = false
    }
  }
  
  /**
   * 获取学生文档访问记录
   * @param {string} studentId 学生ID
   * @param {number} page 页码
   */
  async function fetchStudentDocumentAccess(studentId, page = 0) {
    loading.value = true
    error.value = null
    
    try {
      const response = await api.get(`/teacher/students/${studentId}/document-access`, {
        params: { 
          page, 
          size: documentAccessPagination.value.size 
        }
      })
      
      if (response.data.code === 200) {
        documentAccess.value = response.data.data.content || []
        documentAccessPagination.value = {
          page: response.data.data.number,
          size: response.data.data.size,
          totalElements: response.data.data.totalElements,
          totalPages: response.data.data.totalPages
        }
        return { success: true, data: documentAccess.value }
      } else {
        error.value = response.data.message || '获取文档访问记录失败'
        return { success: false, message: error.value }
      }
    } catch (err) {
      console.error('获取文档访问记录失败:', err)
      error.value = getErrorMessage(err)
      return { success: false, message: error.value }
    } finally {
      loading.value = false
    }
  }
  
  /**
   * 导出学生数据报表
   * @param {string} startDate 开始日期
   * @param {string} endDate 结束日期
   * @param {string} format 导出格式 (csv/excel)
   */
  async function exportReport(startDate, endDate, format = 'csv') {
    loading.value = true
    error.value = null
    
    try {
      const params = { format }
      if (startDate) params.startDate = startDate
      if (endDate) params.endDate = endDate
      
      const response = await api.post('/teacher/students/export', null, {
        params,
        responseType: 'blob'
      })
      
      // 创建下载链接
      const blob = new Blob([response.data], {
        type: format === 'excel' 
          ? 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' 
          : 'text/csv'
      })
      
      const url = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      
      // 生成文件名
      const timestamp = new Date().toISOString().slice(0, 10).replace(/-/g, '')
      const extension = format === 'excel' ? 'xlsx' : 'csv'
      link.download = `student_report_${timestamp}.${extension}`
      
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      window.URL.revokeObjectURL(url)
      
      return { success: true, message: '报表导出成功' }
    } catch (err) {
      console.error('导出报表失败:', err)
      error.value = getErrorMessage(err)
      return { success: false, message: error.value }
    } finally {
      loading.value = false
    }
  }
  
  /**
   * 设置分页大小
   * @param {number} size 每页数量
   */
  function setPageSize(size) {
    pagination.value.size = size
  }
  
  /**
   * 清除当前学生详情
   */
  function clearCurrentStudent() {
    currentStudent.value = null
    qaHistory.value = []
    documentAccess.value = []
  }
  
  /**
   * 清除错误信息
   */
  function clearError() {
    error.value = null
  }
  
  /**
   * 重置所有状态
   */
  function resetState() {
    students.value = []
    currentStudent.value = null
    pagination.value = {
      page: 0,
      size: 20,
      totalElements: 0,
      totalPages: 0
    }
    loading.value = false
    searchQuery.value = ''
    qaHistory.value = []
    qaHistoryPagination.value = {
      page: 0,
      size: 20,
      totalElements: 0,
      totalPages: 0
    }
    documentAccess.value = []
    documentAccessPagination.value = {
      page: 0,
      size: 20,
      totalElements: 0,
      totalPages: 0
    }
    error.value = null
  }
  
  // ========== Return ==========
  return {
    // State
    students,
    currentStudent,
    pagination,
    loading,
    searchQuery,
    qaHistory,
    qaHistoryPagination,
    documentAccess,
    documentAccessPagination,
    error,
    
    // Getters
    hasStudents,
    totalStudents,
    isFirstPage,
    isLastPage,
    activeToday,
    totalQuestions,
    newThisWeek,
    
    // Actions
    fetchStudents,
    fetchStudentDetail,
    addStudent,
    batchAddStudents,
    removeStudent,
    fetchStudentQaHistory,
    fetchStudentDocumentAccess,
    exportReport,
    setPageSize,
    clearCurrentStudent,
    clearError,
    resetState
  }
})
