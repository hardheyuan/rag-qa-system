<template>
  <div class="bg-white dark:bg-card-dark border border-slate-200 dark:border-card-border rounded-xl p-6 shadow-sm flex flex-col h-full">
    <!-- Header -->
    <div class="flex items-center justify-between mb-4">
      <h3 class="text-lg font-semibold text-slate-900 dark:text-white">Topic Demand</h3>
      <span class="text-xs text-slate-500 bg-slate-100 dark:bg-slate-800 px-2 py-1 rounded">High Frequency</span>
    </div>

    <!-- Word Cloud -->
    <div class="flex-1 flex flex-wrap content-center justify-center gap-x-4 gap-y-2 text-center p-2 relative overflow-hidden min-h-[200px]">
      <div class="absolute top-0 left-0 w-full h-full bg-gradient-to-br from-primary/5 to-transparent rounded-full blur-3xl -z-10"></div>
      
      <span 
        v-for="(topic, index) in topics" 
        :key="index"
        class="word-cloud-item transition-all duration-300 cursor-default hover:scale-110"
        :class="topic.class"
        :title="`${topic.name} - ${topic.count} questions`"
      >
        {{ topic.name }}
      </span>
    </div>
  </div>
</template>

<script setup>
const topics = [
  { name: 'Thermodynamics', count: 156, class: 'text-primary text-2xl font-bold' },
  { name: 'Lab Reports', count: 89, class: 'text-slate-400 dark:text-slate-500 text-sm font-light' },
  { name: "Newton's Laws", count: 134, class: 'text-slate-600 dark:text-slate-300 text-lg font-medium' },
  { name: 'Kinematics', count: 142, class: 'text-primary/70 text-xl font-semibold' },
  { name: 'Chapter 4', count: 112, class: 'text-slate-500 dark:text-slate-400 text-base' },
  { name: 'Homework 3', count: 76, class: 'text-slate-400 dark:text-slate-500 text-xs' },
  { name: 'Formulas', count: 98, class: 'text-purple-500 text-lg font-medium' },
  { name: 'Energy', count: 145, class: 'text-slate-600 dark:text-slate-300 text-xl font-bold' },
  { name: 'Vector Analysis', count: 67, class: 'text-slate-400 dark:text-slate-500 text-sm' },
  { name: 'Gravity', count: 103, class: 'text-primary/60 text-base font-medium' }
]
</script>

<style scoped>
.word-cloud-item:hover {
  color: #1269e2;
}
</style>
