<template>
  <div class="flex h-screen w-full bg-background-light dark:bg-background-dark overflow-hidden font-display text-slate-900 dark:text-white">
    <!-- Sidebar -->
    <TeacherSidebar />
    
    <!-- Main Content -->
    <main class="flex-1 flex flex-col h-full overflow-hidden relative">
      <TeacherHeader />
      
      <div class="flex-1 overflow-y-auto p-8 scrollbar-custom">
        <div class="max-w-7xl mx-auto flex flex-col gap-6">
          
          <!-- Metrics Grid -->
          <DashboardMetrics :pending-reviews="pendingFeedbackCount" @open-notification="handleOpenNotification" />
          
          <!-- Charts & Feedback Grid -->
          <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
            <TrendsChart class="lg:col-span-2" />
            <FeedbackList id="teacher-feedback-panel" class="lg:col-span-1" @pending-count-change="handlePendingFeedbackCountChange" />
          </div>
          
          <!-- Bottom Grid -->
          <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
            <TopicCloud class="lg:col-span-1" />
            <DocumentTable class="lg:col-span-2" />
          </div>

          <!-- Footer -->
          <div class="mt-8 pt-4 border-t border-slate-200 dark:border-slate-800 flex justify-between items-center text-xs text-slate-400 dark:text-slate-500">
            <p>© 2023 EduAI 教育科技有限公司 版权所有</p>
            <div class="flex gap-4">
              <a class="hover:text-primary" href="#">隐私政策</a>
              <a class="hover:text-primary" href="#">技术支持</a>
            </div>
          </div>
        </div>
      </div>
    </main>

    <!-- Send Notification Modal -->
    <SendNotificationModal
      v-if="showNotificationModal"
      @close="showNotificationModal = false"
      @send="handleSendNotification"
    />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import TeacherSidebar from '../components/teacher/TeacherSidebar.vue'
import TeacherHeader from '../components/teacher/TeacherHeader.vue'
import DashboardMetrics from '../components/teacher/DashboardMetrics.vue'
import TrendsChart from '../components/teacher/TrendsChart.vue'
import FeedbackList from '../components/teacher/FeedbackList.vue'
import TopicCloud from '../components/teacher/TopicCloud.vue'
import DocumentTable from '../components/teacher/DocumentTable.vue'
import SendNotificationModal from '../components/teacher/SendNotificationModal.vue'

// 控制发送通知弹窗显示
const showNotificationModal = ref(false)
const pendingFeedbackCount = ref(0)

function handleOpenNotification() {
  showNotificationModal.value = true
}

function handlePendingFeedbackCountChange(count) {
  pendingFeedbackCount.value = count
}

function handleSendNotification(notificationData) {
  console.log('发送通知:', notificationData)
  // 这里可以调用后端API发送通知
  // 例如：await notificationApi.sendBroadcast(notificationData)
}
</script>

<style scoped>
.scrollbar-custom::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}
.scrollbar-custom::-webkit-scrollbar-track {
  background: transparent; 
}
.scrollbar-custom::-webkit-scrollbar-thumb {
  background: #cbd5e1; 
  border-radius: 4px;
}
.dark .scrollbar-custom::-webkit-scrollbar-thumb {
  background: #2d3748; 
}
.scrollbar-custom::-webkit-scrollbar-thumb:hover {
  background: #94a3b8; 
}
.dark .scrollbar-custom::-webkit-scrollbar-thumb:hover {
  background: #4a5568; 
}
</style>
