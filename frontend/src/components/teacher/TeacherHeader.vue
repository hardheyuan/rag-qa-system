<template>
  <header class="flex-shrink-0 px-8 py-6 border-b border-slate-200 dark:border-slate-800 bg-white/50 dark:bg-[#111418]/50 backdrop-blur-sm sticky top-0 z-10">
    <div class="flex flex-col md:flex-row md:items-center justify-between gap-4">
      <!-- Title Section -->
      <div>
        <h2 class="text-2xl font-bold text-slate-900 dark:text-white tracking-tight">数据分析概览</h2>
        <p class="text-slate-500 dark:text-slate-400 text-sm mt-1">2023年秋季学期 • 物理101</p>
      </div>

      <!-- Actions Section -->
      <div class="flex items-center gap-3">
        <!-- Date Selector -->
        <div class="relative">
          <select 
            v-model="selectedPeriod"
            class="appearance-none bg-white dark:bg-card-dark border border-slate-200 dark:border-card-border text-slate-700 dark:text-slate-300 text-sm rounded-lg pl-4 pr-10 py-2.5 focus:outline-none focus:ring-2 focus:ring-primary/50 cursor-pointer"
          >
            <option v-for="period in periods" :key="period.value" :value="period.value">
              {{ period.label }}
            </option>
          </select>
          <span class="material-symbols-outlined absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 pointer-events-none text-lg">calendar_today</span>
        </div>

        <!-- Export Button -->
        <button 
          @click="exportReport"
          class="flex items-center gap-2 bg-primary hover:bg-primary/90 text-white px-4 py-2.5 rounded-lg text-sm font-semibold transition-all shadow-lg shadow-primary/20"
        >
          <span class="material-symbols-outlined text-lg">download</span>
          导出报告
        </button>
      </div>
    </div>
  </header>
</template>

<script setup>
import { ref } from 'vue'

const selectedPeriod = ref('30days')

const periods = [
  { value: '30days', label: '最近30天' },
  { value: 'semester', label: '本学期' },
  { value: 'week', label: '最近7天' },
]

function exportReport() {
  console.log('Exporting report for period:', selectedPeriod.value)
  // In the future, this would trigger a report download
  alert('报告导出功能即将推出！')
}
</script>
