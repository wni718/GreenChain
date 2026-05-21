import { ref, computed, watch } from 'vue'

const isDarkMode = ref(false)

export function useDarkMode() {
  const toggleDarkMode = () => {
    isDarkMode.value = !isDarkMode.value
  }

  const darkModeClass = computed(() => isDarkMode.value ? 'dark-mode' : '')

  watch(isDarkMode, (newVal) => {
    if (newVal) {
      document.documentElement.classList.add('dark-mode')
    } else {
      document.documentElement.classList.remove('dark-mode')
    }
  }, { immediate: true })

  return {
    isDarkMode,
    toggleDarkMode,
    darkModeClass
  }
}