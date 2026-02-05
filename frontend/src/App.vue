<script setup>
import { onMounted } from 'vue'
import { useUserStore } from './stores/user'
import ToastContainer from './components/common/ToastContainer.vue'

const userStore = useUserStore()

onMounted(() => {
  userStore.initDarkMode()
  userStore.initAuth()
})
</script>

<template>
  <router-view />
  <!-- 全局 Toast 通知容器 -->
  <ToastContainer />
</template>

<style>
/* Global styles are in style.css */
</style>
