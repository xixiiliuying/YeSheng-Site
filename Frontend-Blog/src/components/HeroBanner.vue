<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import { useBlogStore } from '@/stores'
import { getConfigByKey } from '@/api/systemConfig'

const route = useRoute()
const blogStore = useBlogStore()

const props = defineProps({
  /** 文章封面 (文章详情页传入) */
  coverImage: { type: String, default: '' },
  /** 文章标题 */
  title: { type: String, default: '' },
  /** 文章 meta 信息 */
  meta: { type: String, default: '' }
})

const isHome = computed(() => route.name === 'home')

// 从 system_config 读取 Hero 背景图，key = blog_hero_image
const heroImage = ref('')
onMounted(async () => {
  try {
    const res = await getConfigByKey('blog_hero_image')
    if (res?.data?.data?.configValue) {
      heroImage.value = res.data.data.configValue
    }
  } catch { /* ignore */ }
})

const bgImage = computed(() => {
  if (props.coverImage) return props.coverImage
  if (heroImage.value) return heroImage.value
  return '/bgc.webp' // 默认兜底
})

/* ---- 打字机效果 ---- */
const fullDesc = '每天学一点技术，每天长一点脑子。每一步都算数！！'
const typedDesc = ref('')
const cursorShow = ref(true)
let typeTimer = null
let cursorTimer = null

const startTyping = () => {
  typedDesc.value = ''
  let i = 0
  typeTimer = setInterval(() => {
    typedDesc.value += fullDesc[i]
    i++
    if (i >= fullDesc.length) {
      clearInterval(typeTimer)
      typeTimer = null
    }
  }, 100) // 每 100ms 打一个字
}

onMounted(() => {
  if (isHome.value) {
    startTyping()
    // 光标闪烁
    cursorTimer = setInterval(() => {
      cursorShow.value = !cursorShow.value
    }, 530)
  }
})

onUnmounted(() => {
  if (typeTimer) clearInterval(typeTimer)
  if (cursorTimer) clearInterval(cursorTimer)
})
</script>

<template>
  <div class="hero-banner" :style="{ backgroundImage: `url(${bgImage})` }">
    <div class="hero-overlay" />
    <div class="hero-fade" />
    <div class="hero-content">
      <!-- 主页: 显示个人信息 -->
      <template v-if="isHome">
        <h1 class="hero-title">
          {{ blogStore.personalInfo.nickname }}
        </h1>
        <p class="hero-desc">
          {{ typedDesc }}<span class="typing-cursor" :class="{ blink: cursorShow }">|</span>
        </p>
      </template>
      <!-- 自定义标题 (文章/分类/标签等) -->
      <template v-else-if="title">
        <h1 class="hero-article-title">{{ title }}</h1>
        <p v-if="meta" class="hero-article-meta" v-html="meta" />
      </template>
      <!-- 其他页面: 显示路由 meta 标题 -->
      <template v-else>
        <h1 class="hero-page-title">{{ $route.meta.title || '' }}</h1>
      </template>
    </div>
  </div>
</template>

<style scoped>
.hero-banner {
  position: relative;
  width: 100%;
  height: 40vh;
  min-height: 280px;
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}
.hero-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.35);
  z-index: 1;
}
.hero-fade {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 80px;
  background: linear-gradient(to bottom, transparent, rgba(255, 255, 255, 0.95));
  z-index: 2;
  pointer-events: none;
}
html.dark .hero-fade {
  background: linear-gradient(to bottom, transparent, rgba(24, 24, 24, 0.95));
}
.hero-content {
  position: relative;
  z-index: 3;
  text-align: center;
  color: #fff;
  padding: 0 24px;
  max-width: 600px;
}
.hero-title {
  font-family: var(--blog-serif);
  font-size: 34px;
  font-weight: 800;
  margin: 0 0 10px;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.4);
  letter-spacing: 1px;
}
.hero-desc {
  font-size: 16px;
  margin: 0;
  opacity: 0.9;
  text-shadow: 0 1px 4px rgba(0, 0, 0, 0.3);
  line-height: 1.6;
}
.typing-cursor {
  font-weight: 100;
  opacity: 1;
  transition: opacity 0.1s;
}
.typing-cursor.blink {
  opacity: 0;
}
.hero-article-title {
  font-family: var(--blog-serif);
  font-size: 30px;
  font-weight: 800;
  margin: 0 0 10px;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.4);
  line-height: 1.35;
}
.hero-article-meta {
  font-size: 14px;
  margin: 0;
  opacity: 0.85;
  text-shadow: 0 1px 4px rgba(0, 0, 0, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-wrap: wrap;
  gap: 4px 5px;
}
.hero-article-meta :deep(.meta-item) {
  display: inline-flex;
  align-items: center;
  gap: 3px;
}
.hero-article-meta :deep(.meta-dot) {
  opacity: 0.5;
  margin: 0 1px;
}
.hero-article-meta :deep(.iconfont) {
  font-size: 13px;
}
.hero-page-title {
  font-family: var(--blog-serif);
  font-size: 32px;
  font-weight: 800;
  margin: 0;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.4);
}

@media (max-width: 768px) {
  .hero-banner {
    height: 30vh;
    min-height: 200px;
  }
  .hero-title {
    font-size: 24px;
  }
  .hero-desc {
    font-size: 14px;
  }
  .hero-article-title {
    font-size: 22px;
  }
  .hero-page-title {
    font-size: 24px;
  }
  .hero-fade {
    height: 50px;
  }
}
</style>
