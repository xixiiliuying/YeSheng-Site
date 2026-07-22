<script setup>
import { ref, computed, onMounted } from 'vue'
import { useThemeStore } from '@/stores'
import { getConfigByKey } from '@/api/systemConfig'

const themeStore = useThemeStore()
const bgImage = ref('/bgc.webp')
const bgVideo = ref('')
const loaded = ref(false)

onMounted(async () => {
  try {
    const [imgRes, videoRes] = await Promise.allSettled([
      getConfigByKey('blog_bg_image'),
      getConfigByKey('blog_bg_video'),
    ])
    if (imgRes.status === 'fulfilled' && imgRes.value?.data?.data?.configValue) {
      bgImage.value = imgRes.value.data.data.configValue
    }
    if (videoRes.status === 'fulfilled' && videoRes.value?.data?.data?.configValue) {
      bgVideo.value = videoRes.value.data.data.configValue
    }
  } catch { /* ignore */ }
  loaded.value = true
})

const showImage = computed(() => themeStore.bgType === 'image' && bgImage.value)
const showVideo = computed(() => themeStore.bgType === 'video' && bgVideo.value)
</script>

<template>
  <div v-if="loaded" class="bg-media-wrapper" aria-hidden="true">
    <img v-if="showImage" class="bg-image" :src="bgImage" />
    <video
      v-if="showVideo"
      class="bg-video"
      :src="bgVideo"
      autoplay muted loop playsinline
    />
    <div v-if="showImage || showVideo" class="bg-overlay" />
  </div>
</template>

<style scoped>
.bg-media-wrapper {
  position: fixed;
  inset: 0;
  z-index: -1;
  pointer-events: none;
  contain: layout style paint;
}
.bg-image {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.bg-video {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.bg-overlay {
  position: absolute;
  inset: 0;
  background: rgba(255, 255, 255, 0.45);
}
html.dark .bg-overlay {
  background: rgba(0, 0, 0, 0.45);
}
</style>
