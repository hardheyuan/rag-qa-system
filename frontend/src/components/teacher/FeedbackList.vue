<template>
  <div class="bg-white dark:bg-card-dark border border-slate-200 dark:border-card-border rounded-xl shadow-sm flex flex-col overflow-hidden h-full">
    <!-- Header -->
    <div class="p-4 border-b border-slate-200 dark:border-slate-800 flex justify-between items-center bg-slate-50/50 dark:bg-[#151b23]">
      <h3 class="text-sm font-bold text-slate-900 dark:text-white flex items-center gap-2">
        <span class="material-symbols-outlined text-amber-500 text-lg">rate_review</span>
        待审核反馈
      </h3>
    </div>

    <!-- Feedback Items -->
    <div class="flex-1 overflow-y-auto p-0">
      <div 
        v-for="(item, index) in feedbackItems" 
        :key="index"
        class="p-4 border-b border-slate-100 dark:border-slate-800 hover:bg-slate-50 dark:hover:bg-slate-800/50 transition-colors cursor-pointer group"
        :class="{ 'border-b-0': index === feedbackItems.length - 1 }"
      >
        <!-- Badge and Time -->
        <div class="flex justify-between items-start mb-1.5">
          <span 
            class="text-[10px] font-bold px-1.5 py-0.5 rounded border"
            :class="getBadgeClasses(item.type)"
          >
            {{ item.badge }}
          </span>
          <span class="text-[10px] text-slate-400">{{ item.time }}</span>
        </div>

        <!-- Question -->
        <p class="text-sm font-medium text-slate-800 dark:text-slate-200 mb-1 leading-snug">"{{ item.question }}"</p>

        <!-- Actions -->
        <div 
          v-if="item.actions && item.actions.length > 0"
          class="flex items-center gap-3 mt-2.5 opacity-60 group-hover:opacity-100 transition-opacity"
        >
          <button 
            v-for="(action, actionIndex) in item.actions" 
            :key="actionIndex"
            @click.stop="handleAction(action, item)"
            :class="[
              'text-[10px] px-2 py-1 rounded font-medium transition-colors',
              action.type === 'primary' 
                ? 'text-white bg-primary hover:bg-primary/90' 
                : 'text-slate-500 hover:text-slate-300'
            ]"
          >
            {{ action.label }}
          </button>
        </div>
      </div>
    </div>

    <!-- Footer -->
    <div class="p-3 border-t border-slate-200 dark:border-slate-800 text-center bg-slate-50/50 dark:bg-[#151b23]">
      <button class="text-xs font-medium text-primary hover:text-primary/80 transition-colors">
        查看全部待办事项 →
      </button>
    </div>
  </div>
</template>

<script setup>
const feedbackItems = [
  {
    type: 'low-confidence',
    badge: '低置信度 (42%)',
    time: '10分钟前',
    question: '薛定谔方程的推导能再简单解释一下吗？',
    actions: [
      { label: '优化回答', type: 'primary' },
      { label: '忽略', type: 'secondary' }
    ]
  },
  {
    type: 'negative',
    badge: '负面反馈',
    time: '2小时前',
    question: '关于热力学的回答似乎与第四章有矛盾。',
    actions: [
      { label: '查看来源', type: 'secondary' }
    ]
  },
  {
    type: 'flagged',
    badge: '学生标记',
    time: '5小时前',
    question: '实验报告是周一还是周二交？',
    actions: []
  }
]

function getBadgeClasses(type) {
  const classes = {
    'low-confidence': 'text-red-500 bg-red-50 dark:bg-red-900/20 border-red-100 dark:border-red-900/30',
    'negative': 'text-amber-500 bg-amber-50 dark:bg-amber-900/20 border-amber-100 dark:border-amber-900/30',
    'flagged': 'text-slate-500 bg-slate-100 dark:bg-slate-800 border-slate-200 dark:border-slate-700'
  }
  return classes[type] || classes['flagged']
}

function handleAction(action, item) {
  console.log('Action:', action.label, 'on item:', item.question)
  alert(`${action.label} 功能即将推出！`)
}
</script>
