import { describe, expect, it, vi } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import DashboardMetrics from './DashboardMetrics.vue'
import FeedbackList from './FeedbackList.vue'
import DocumentTable from './DocumentTable.vue'

vi.mock('vue-router', () => ({
  useRouter: () => ({
    push: vi.fn()
  })
}))

vi.mock('@/api/feedback', () => ({
  feedbackApi: {
    getTeacherPendingFeedback: vi.fn().mockResolvedValue({ content: [] }),
    getTeacherPendingCount: vi.fn().mockResolvedValue(0),
    getTeacherFeedbackDetail: vi.fn(),
    replyFeedback: vi.fn()
  },
  default: {
    getTeacherPendingFeedback: vi.fn().mockResolvedValue({ content: [] }),
    getTeacherPendingCount: vi.fn().mockResolvedValue(0),
    getTeacherFeedbackDetail: vi.fn(),
    replyFeedback: vi.fn()
  }
}))

vi.mock('../../stores/documents', () => ({
  useDocumentStore: () => ({
    fetchDocumentStats: vi.fn().mockResolvedValue([])
  })
}))

describe('Teacher dashboard cleanup', () => {
  it('does not show low-confidence indicator in metrics', () => {
    const wrapper = mount(DashboardMetrics)

    expect(wrapper.text()).not.toContain('低置信度回答')
  })

  it('does not show deprecated feedback actions and badges', () => {
    const wrapper = mount(FeedbackList)
    const text = wrapper.text()

    expect(text).not.toContain('低置信度')
    expect(text).not.toContain('优化回答')
    expect(text).not.toContain('忽略')
    expect(text).not.toContain('查看来源')
    expect(text).not.toContain('学生标记')
  })

  it('does not show context coverage column in document table', async () => {
    const wrapper = mount(DocumentTable)
    await flushPromises()

    expect(wrapper.text()).not.toContain('上下文覆盖率')
  })
})
