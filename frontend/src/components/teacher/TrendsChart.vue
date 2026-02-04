<template>
  <div class="bg-white dark:bg-card-dark border border-slate-200 dark:border-card-border rounded-xl p-6 shadow-sm">
    <div class="flex items-center justify-between mb-6">
      <h3 class="text-lg font-semibold text-slate-900 dark:text-white">提问趋势</h3>
      <button class="text-slate-400 hover:text-primary transition-colors">
        <span class="material-symbols-outlined">more_horiz</span>
      </button>
    </div>

    <div class="w-full h-64 relative">
      <!-- Y-Axis Labels -->
      <div class="absolute inset-0 flex flex-col justify-between text-xs text-slate-400 pointer-events-none">
        <div class="w-full border-b border-dashed border-slate-200 dark:border-slate-700/50 h-0 flex items-center">
          <span class="-ml-8">100</span>
        </div>
        <div class="w-full border-b border-dashed border-slate-200 dark:border-slate-700/50 h-0 flex items-center">
          <span class="-ml-8">75</span>
        </div>
        <div class="w-full border-b border-dashed border-slate-200 dark:border-slate-700/50 h-0 flex items-center">
          <span class="-ml-8">50</span>
        </div>
        <div class="w-full border-b border-dashed border-slate-200 dark:border-slate-700/50 h-0 flex items-center">
          <span class="-ml-8">25</span>
        </div>
        <div class="w-full border-b border-slate-300 dark:border-slate-600 h-0 flex items-center">
          <span class="-ml-8">0</span>
        </div>
      </div>

      <!-- Chart SVG -->
      <svg class="absolute inset-0 w-full h-full overflow-visible pl-8" preserveAspectRatio="none" viewBox="0 0 100 100">
        <defs>
          <linearGradient id="chartGradient" x1="0" x2="0" y1="0" y2="1">
            <stop offset="0%" stop-color="#1269e2" stop-opacity="0.2"></stop>
            <stop offset="100%" stop-color="#1269e2" stop-opacity="0"></stop>
          </linearGradient>
        </defs>
        <!-- Area -->
        <path 
          d="M0,80 C10,75 10,60 20,65 C30,70 30,50 40,45 C50,40 50,30 60,35 C70,40 70,20 80,15 C90,10 90,25 100,20 V100 H0 Z" 
          fill="url(#chartGradient)"
        ></path>
        <!-- Line -->
        <path 
          d="M0,80 C10,75 10,60 20,65 C30,70 30,50 40,45 C50,40 50,30 60,35 C70,40 70,20 80,15 C90,10 90,25 100,20" 
          fill="none" 
          stroke="#1269e2" 
          stroke-linecap="round" 
          stroke-linejoin="round" 
          stroke-width="2" 
          vector-effect="non-scaling-stroke"
        ></path>
        <!-- Data Points -->
        <circle cx="20" cy="65" fill="#1269e2" r="1.5" stroke="white" stroke-width="0.5"></circle>
        <circle cx="40" cy="45" fill="#1269e2" r="1.5" stroke="white" stroke-width="0.5"></circle>
        <circle cx="60" cy="35" fill="#1269e2" r="1.5" stroke="white" stroke-width="0.5"></circle>
        <circle cx="80" cy="15" fill="#1269e2" r="1.5" stroke="white" stroke-width="0.5"></circle>
      </svg>
    </div>

    <!-- X-Axis Labels -->
    <div class="flex justify-between text-xs text-slate-400 mt-2 px-2 pl-8">
      <span>第1周</span>
      <span>第2周</span>
      <span>第3周</span>
      <span>第4周</span>
    </div>
  </div>
</template>

<script setup>
// Mock chart data - in the future this could be made dynamic
const chartData = {
  labels: ['第1周', '第2周', '第3周', '第4周'],
  values: [20, 35, 55, 80] // Inverted for SVG coordinates (100 - value)
}
</script>
