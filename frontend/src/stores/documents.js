import { defineStore } from 'pinia'
import { ref } from 'vue'
import api from '@/api/interceptor'

export const useDocumentStore = defineStore('documents', () => {
  const documents = ref([])

  function normalizeFileName(name) {
    return (name || '').trim().toLowerCase()
  }

  // Transform backend document format to frontend format
  function transformDocument(backendDoc) {
    return {
      id: backendDoc.id,
      name: (backendDoc.filename || backendDoc.name || '').trim(),
      type: (backendDoc.fileType || backendDoc.type || '').toLowerCase(),
      size: backendDoc.fileSize || backendDoc.size || 0,
      createTime: backendDoc.createdAt || backendDoc.createTime || backendDoc.uploadedAt,
      status: backendDoc.status,
      url: backendDoc.filePath || backendDoc.url,
      description: backendDoc.description
    }
  }

  async function fetchDocuments() {
    try {
      // 使用配置了JWT拦截器的 api 实例
      const response = await api.get('/documents')
      const result = response.data
      
      if (result.code === 200 && result.data) {
        // Transform backend documents to frontend format
        documents.value = result.data.map(transformDocument)
      } else {
        documents.value = []
      }
    } catch (error) {
      console.error('加载文档失败:', error)
      documents.value = []
    }
  }

  async function uploadDocument(file) {
    const displayName = (file?.name || '').trim()
    if (!displayName) {
      return { success: false, message: '文件名不能为空' }
    }

    const normalizedName = normalizeFileName(displayName)
    const hasDuplicateName = documents.value.some(
      doc => normalizeFileName(doc.name) === normalizedName
    )
    if (hasDuplicateName) {
      return { success: false, message: `已存在同名文件: ${displayName}` }
    }

    // Extract file extension
    const extension = displayName.split('.').pop().toLowerCase()
    
    // Create a temporary document object with metadata
    const tempDoc = {
      id: 'temp-' + Date.now(),
      name: displayName,
      type: extension,
      size: file.size,
      createTime: new Date().toISOString(),
      status: 'UPLOADING',
      url: null
    }
    
    // Add to documents list immediately so user can see it
    documents.value.unshift(tempDoc)

    const formData = new FormData()
    formData.append('file', file)

    try {
      // 使用配置了JWT拦截器的 api 实例上传
      const response = await api.post('/documents/upload', formData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      })
      
      const result = response.data
      
      if (result.code === 200) {
        // Transform and update the document
        const serverDoc = transformDocument(result.data)
        const index = documents.value.findIndex(d => d.id === tempDoc.id)
        if (index !== -1) {
          documents.value[index] = serverDoc
        }
        return { success: true, document: serverDoc }
      } else {
        const index = documents.value.findIndex(d => d.id === tempDoc.id)
        if (index !== -1) {
          documents.value[index].status = 'FAILED'
        }
        return { success: false, message: result.message }
      }
    } catch (error) {
      const index = documents.value.findIndex(d => d.id === tempDoc.id)
      if (index !== -1) {
        documents.value[index].status = 'FAILED'
      }
      
      if (error.response?.status === 401) {
        return { success: false, message: '登录已过期，请重新登录' }
      }
      
      return { success: false, message: error.message }
    }
  }

  async function deleteDocument(docId) {
    try {
      // 使用配置了JWT拦截器的 api 实例
      const response = await api.delete(`/documents/${docId}`)
      const result = response.data
      
      if (result.code === 200) {
        const index = documents.value.findIndex(d => d.id === docId)
        if (index !== -1) {
          documents.value.splice(index, 1)
        }
        return { success: true }
      }
      return { success: false, message: result.message }
    } catch (error) {
      if (error.response?.status === 401) {
        return { success: false, message: '登录已过期，请重新登录' }
      }
      return { success: false, message: error.message }
    }
  }

  function getFileIcon(type) {
    const icons = { 
      pdf: 'picture_as_pdf', 
      docx: 'description', 
      doc: 'description', 
      pptx: 'slideshow', 
      ppt: 'slideshow', 
      txt: 'text_snippet' 
    }
    return icons[type?.toLowerCase()] || 'insert_drive_file'
  }

  function getStatusClass(status) {
    const classes = { 
      SUCCESS: 'success', 
      PROCESSING: 'warning', 
      UPLOADING: 'warning', 
      FAILED: 'error' 
    }
    return classes[status] || ''
  }

  function getStatusText(status) {
    const texts = { 
      SUCCESS: '处理完成', 
      PROCESSING: '处理中', 
      UPLOADING: '上传中', 
      FAILED: '处理失败' 
    }
    return texts[status] || '未知状态'
  }

  // 获取文档检索统计
  async function fetchDocumentStats() {
    try {
      // 使用配置了JWT拦截器的 api 实例
      const response = await api.get('/documents/stats')
      const result = response.data
      
      if (result.code === 200 && result.data) {
        return result.data.map(doc => ({
          id: doc.id,
          name: doc.name,
          retrievals: doc.retrievals || 0,
          status: doc.status,
          type: doc.type,
          size: doc.size,
          createTime: doc.createTime
        }))
      }
      return []
    } catch (error) {
      console.error('获取文档统计失败:', error)
      return []
    }
  }

  return {
    documents,
    fetchDocuments,
    uploadDocument,
    deleteDocument,
    getFileIcon,
    getStatusClass,
    getStatusText,
    fetchDocumentStats
  }
})
