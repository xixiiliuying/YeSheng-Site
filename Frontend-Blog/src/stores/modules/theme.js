import { defineStore } from 'pinia'
import { ref, computed, watch } from 'vue'

export const useThemeStore = defineStore(
  'theme',
  () => {
    const mode = ref('auto')
    const systemDark = ref(
      window.matchMedia('(prefers-color-scheme: dark)').matches
    )

    const isDark = computed(() => {
      if (mode.value === 'dark') return true
      if (mode.value === 'light') return false
      return systemDark.value
    })

    const applyTheme = () => {
      document.documentElement.classList.toggle('dark', isDark.value)
    }

    const mql = window.matchMedia('(prefers-color-scheme: dark)')
    mql.addEventListener('change', (e) => {
      systemDark.value = e.matches
      if (mode.value === 'auto') applyTheme()
    })

    const toggle = () => {
      const currentIsDark = isDark.value
      mode.value = currentIsDark ? 'light' : 'dark'
    }

    // 动态背景类型: 'image' | 'video'
    const bgType = ref('image')

    const cycleBg = () => {
      bgType.value = bgType.value === 'image' ? 'video' : 'image'
    }

    const bgLabel = computed(() => {
      return bgType.value === 'image' ? '🖼' : '🎬'
    })

    const hasBg = computed(() => true)

    watch(isDark, applyTheme)

    return { mode, isDark, toggle, applyTheme, bgType, cycleBg, bgLabel, hasBg }
  },
  {
    persist: {
      pick: ['mode', 'bgType']
    }
  }
)
