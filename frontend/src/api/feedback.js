import api from './interceptor'

function unwrap(response) {
  const payload = response?.data || {}
  if (payload.code === 200) {
    return payload.data
  }
  throw new Error(payload.message || '请求失败')
}

export const feedbackApi = {
  getTeacherOptions() {
    return api.get('/student/feedback/teachers').then(unwrap)
  },

  submitFeedback(data) {
    return api.post('/student/feedback', data).then(unwrap)
  },

  getMyFeedback(params = { page: 0, size: 10 }) {
    return api.get('/student/feedback/mine', { params }).then(unwrap)
  },

  deleteMyFeedback(id) {
    return api.delete(`/student/feedback/${id}`).then(unwrap)
  },

  getTeacherPendingFeedback(params = { page: 0, size: 10 }) {
    return api.get('/teacher/feedback/pending', { params }).then(unwrap)
  },

  getTeacherPendingCount() {
    return api.get('/teacher/feedback/pending/count').then(unwrap)
  },

  getTeacherFeedbackDetail(id) {
    return api.get(`/teacher/feedback/${id}`).then(unwrap)
  },

  replyFeedback(id, replyContent) {
    return api.post(`/teacher/feedback/${id}/reply`, { replyContent }).then(unwrap)
  }
}

export default feedbackApi
