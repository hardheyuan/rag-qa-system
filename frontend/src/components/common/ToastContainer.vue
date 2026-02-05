<template>
  <Teleport to="body">
    <div class="fixed bottom-6 right-6 z-50 flex flex-col gap-3">
      <TransitionGroup name="toast">
        <div
          v-for="toast in toasts"
          :key="toast.id"
          :class="[
            'px-4 py-3 rounded-lg shadow-lg flex items-center gap-3 min-w-[280px] max-w-[400px]',
            'transform transition-all duration-300',
            toastClasses[toast.type]
          ]"
        >
          <!-- Icon -->
          <span class="material-symbols-outlined text-lg flex-shrink-0">
            {{ toastIcons[toast.type] }}
          </span>
          
          <!-- Message -->
          <span class="text-sm font-medium flex-1">{{ toast.message }}</span>
          
          <!-- Close Button -->
          <button
            @click="removeToast(toast.id)"
            class="p-1 rounded hover:bg-white/20 transition-colors flex-shrink-0"
          >
            <span class="material-symbols-outlined text-sm">close</span>
          </button>
        </div>
      </TransitionGroup>
    </div>
  </Teleport>
</template>

<script setup>
import { useToast } from '@/composables/useToast'

const { toasts, removeToast } = useToast()

// Toast 样式映射
const toastClasses = {
  success: 'bg-green-500 text-white',
  error: 'bg-red-500 text-white',
  warning: 'bg-yellow-500 text-white',
  info: 'bg-blue-500 text-white'
}

// Toast 图标映射
const toastIcons = {
  success: 'check_circle',
  error: 'error',
  warning: 'warning',
  info: 'info'
}
</script>

<style scoped>
/* Toast 动画 */
.toast-enter-active,
.toast-leave-active {
  transition: all 0.3s ease;
}

.toast-enter-from {
  opacity: 0;
  transform: translateX(100%);
}

.toast-leave-to {
  opacity: 0;
  transform: translateX(100%);
}

.toast-move {
  transition: transform 0.3s ease;
}
</style>
