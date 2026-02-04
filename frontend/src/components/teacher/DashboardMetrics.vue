<template>
  <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
    <!-- Pending Reviews Card -->
    <div class="bg-white dark:bg-card-dark border border-red-200 dark:border-red-900/30 rounded-xl p-5 shadow-sm flex flex-col justify-between h-full relative overflow-hidden group hover:border-red-300 dark:hover:border-red-800/50 transition-colors">
      <div class="absolute -right-6 -top-6 size-24 bg-red-500/5 rounded-full blur-2xl group-hover:bg-red-500/10 transition-colors"></div>
      <div class="flex items-start justify-between mb-4 relative z-10">
        <div class="p-2 bg-red-50 dark:bg-red-900/20 rounded-lg">
          <span class="material-symbols-outlined text-red-500">priority_high</span>
        </div>
        <span class="text-xs font-bold text-red-600 dark:text-red-400 bg-red-100 dark:bg-red-900/30 px-2 py-1 rounded-full animate-pulse">需要处理</span>
      </div>
      <div class="relative z-10">
        <p class="text-slate-500 dark:text-slate-400 text-sm font-medium">待审核反馈</p>
        <h3 class="text-3xl font-bold text-slate-900 dark:text-white mt-1">{{ metrics.pendingReviews }}</h3>
        <p class="text-xs text-red-500/80 mt-1 font-medium">{{ metrics.lowConfidence }} 个低置信度回答</p>
      </div>
    </div>

    <!-- Quick Actions Card -->
    <div class="bg-primary/5 dark:bg-primary/10 border border-primary/20 dark:border-primary/20 rounded-xl p-5 shadow-sm flex flex-col h-full">
      <div class="flex items-center gap-2 mb-4">
        <span class="material-symbols-outlined text-primary text-lg">bolt</span>
        <h3 class="text-sm font-bold text-slate-900 dark:text-white uppercase tracking-wider text-[10px]">快捷操作</h3>
      </div>
      <div class="grid grid-cols-2 gap-3 flex-1">
        <!-- 上传文档 -->
        <button 
          @click="goToDocuments"
          class="flex flex-col items-center justify-center gap-2 bg-white dark:bg-card-dark hover:bg-slate-50 dark:hover:bg-slate-800 border border-slate-200 dark:border-slate-700 p-4 rounded-lg transition-all group text-center shadow-sm hover:shadow-md hover:border-primary/30"
        >
          <div class="w-10 h-10 rounded-full bg-primary/10 flex items-center justify-center group-hover:bg-primary/20 transition-colors">
            <span class="material-symbols-outlined text-primary text-xl">upload_file</span>
          </div>
          <div>
            <span class="text-sm font-medium text-slate-700 dark:text-slate-200 block">上传文档</span>
            <span class="text-[10px] text-slate-400 dark:text-slate-500">管理教学资料</span>
          </div>
        </button>
        
        <!-- 发送通知 -->
        <button 
          @click="openNotificationModal"
          class="flex flex-col items-center justify-center gap-2 bg-white dark:bg-card-dark hover:bg-slate-50 dark:hover:bg-slate-800 border border-slate-200 dark:border-slate-700 p-4 rounded-lg transition-all group text-center shadow-sm hover:shadow-md hover:border-primary/30"
        >
          <div class="w-10 h-10 rounded-full bg-green-500/10 flex items-center justify-center group-hover:bg-green-500/20 transition-colors">
            <span class="material-symbols-outlined text-green-500 text-xl">campaign</span>
          </div>
          <div>
            <span class="text-sm font-medium text-slate-700 dark:text-slate-200 block">发送通知</span>
            <span class="text-[10px] text-slate-400 dark:text-slate-500">广播所有学生</span>
          </div>
        </button>
        
        <!-- 查看反馈 -->
        <button 
          @click="viewFeedback"
          class="flex flex-col items-center justify-center gap-2 bg-white dark:bg-card-dark hover:bg-slate-50 dark:hover:bg-slate-800 border border-slate-200 dark:border-slate-700 p-4 rounded-lg transition-all group text-center shadow-sm hover:shadow-md hover:border-primary/30"
        >
          <div class="w-10 h-10 rounded-full bg-amber-500/10 flex items-center justify-center group-hover:bg-amber-500/20 transition-colors">
            <span class="material-symbols-outlined text-amber-500 text-xl">forum</span>
          </div>
          <div>
            <span class="text-sm font-medium text-slate-700 dark:text-slate-200 block">查看反馈</span>
            <span class="text-[10px] text-slate-400 dark:text-slate-500">学生问题反馈</span>
          </div>
        </button>
        
        <!-- 数据分析 -->
        <button 
          @click="viewAnalytics"
          class="flex flex-col items-center justify-center gap-2 bg-white dark:bg-card-dark hover:bg-slate-50 dark:hover:bg-slate-800 border border-slate-200 dark:border-slate-700 p-4 rounded-lg transition-all group text-center shadow-sm hover:shadow-md hover:border-primary/30"
        >
          <div class="w-10 h-10 rounded-full bg-purple-500/10 flex items-center justify-center group-hover:bg-purple-500/20 transition-colors">
            <span class="material-symbols-outlined text-purple-500 text-xl">analytics</span>
          </div>
          <div>
            <span class="text-sm font-medium text-slate-700 dark:text-slate-200 block">数据分析</span>
            <span class="text-[10px] text-slate-400 dark:text-slate-500">学习数据统计</span>
          </div>
        </button>
      </div>
    </div>

    <!-- Total Questions Card -->
    <div class="bg-white dark:bg-card-dark border border-slate-200 dark:border-card-border rounded-xl p-6 shadow-sm flex flex-col justify-between h-full">
      <div class="flex items-start justify-between mb-4">
        <div class="p-2 bg-blue-50 dark:bg-blue-900/20 rounded-lg">
          <span class="material-symbols-outlined text-primary">forum</span>
        </div>
        <span class="flex items-center text-emerald-500 text-xs font-bold bg-emerald-50 dark:bg-emerald-900/20 px-2 py-1 rounded-full">
          <span class="material-symbols-outlined text-sm mr-1">trending_up</span>
          {{ metrics.questionsTrend }}
        </span>
      </div>
      <div>
        <p class="text-slate-500 dark:text-slate-400 text-sm font-medium">总提问数</p>
        <h3 class="text-3xl font-bold text-slate-900 dark:text-white mt-1">{{ formatNumber(metrics.totalQuestions) }}</h3>
      </div>
    </div>

    <!-- AI Relevance Card -->
    <div class="bg-white dark:bg-card-dark border border-slate-200 dark:border-card-border rounded-xl p-6 shadow-sm flex flex-col justify-between h-full">
      <div class="flex items-start justify-between mb-4">
        <div class="p-2 bg-amber-50 dark:bg-amber-900/20 rounded-lg">
          <span class="material-symbols-outlined text-amber-500">psychology</span>
        </div>
        <span class="flex items-center text-emerald-500 text-xs font-bold bg-emerald-50 dark:bg-emerald-900/20 px-2 py-1 rounded-full">
          <span class="material-symbols-outlined text-sm mr-1">arrow_upward</span>
          {{ metrics.relevanceTrend }}
        </span>
      </div>
      <div>
        <p class="text-slate-500 dark:text-slate-400 text-sm font-medium">AI回答准确率</p>
        <h3 class="text-3xl font-bold text-slate-900 dark:text-white mt-1">{{ metrics.aiRelevance }}%</h3>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()
const emit = defineEmits(['openNotification'])

// Mock data for metrics
const metrics = {
  pendingReviews: 12,
  lowConfidence: 5,
  totalQuestions: 1248,
  questionsTrend: '+12%',
  aiRelevance: 94,
  relevanceTrend: '+2%'
}

function formatNumber(num) {
  return num.toLocaleString()
}

// 跳转到文档管理页面
function goToDocuments() {
  router.push('/teacher/documents')
}

// 打开发送通知弹窗
function openNotificationModal() {
  emit('openNotification')
}

// 查看反馈（功能即将推出）
function viewFeedback() {
  alert('查看反馈功能即将推出！')
}

// 数据分析（功能即将推出）
function viewAnalytics() {
  alert('数据分析功能即将推出！')
}
</script>
