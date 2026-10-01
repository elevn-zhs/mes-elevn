import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAppStore = defineStore('app', () => {
  const sidebarCollapsed = ref(false)

  function toggleSidebar() {
    sidebarCollapsed.value = !sidebarCollapsed.value
  }

  function closeSidebar() {
    sidebarCollapsed.value = true
  }

  function openSidebar() {
    sidebarCollapsed.value = false
  }

  return {
    sidebarCollapsed,
    toggleSidebar,
    closeSidebar,
    openSidebar
  }
})