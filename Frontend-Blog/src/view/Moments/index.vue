<script setup>
import { ref, onMounted, onUnmounted, inject } from 'vue'
import { getRandomMoment, getMomentsList, submitMoment } from '@/api/moments'
import { useVisitorStore } from '@/stores'
import SidebarCard from '@/components/SidebarCard.vue'

const visitorStore = useVisitorStore()
const { articleTitle, articleMeta } = inject('setHero')

// Canvas
const canvasRef = ref(null)
let stars = [], particles = [], trailParticles = []
let animId = null, ctx = null, w = 0, h = 0
let mouse = { x: -100, y: -100, tx: -100, ty: -100 }
// Canvas 点击目标（每帧更新）
const orbitTags = []

// 卡片状态
const currentMoment = ref(null)
const showCard = ref(false)
const allMoments = ref([])
const showList = ref(false)
const fanIndex = ref(0) // 弧形卡片当前居中索引
const showPublish = ref(false)
const publishForm = ref({ content: '', emoji: '💬', nickname: '' })

/* ====== Canvas 引擎 ====== */
const initCanvas = () => {
  const cvs = canvasRef.value
  if (!cvs) return
  const rect = cvs.getBoundingClientRect()
  w = rect.width; h = rect.height
  if (w === 0 || h === 0) return
  ctx = cvs.getContext('2d')
  const dpr = Math.min(window.devicePixelRatio || 1, 2)
  cvs.width = w * dpr; cvs.height = h * dpr
  ctx.setTransform(dpr, 0, 0, dpr, 0, 0)

  stars = []
  const colors = [[255,255,255],[200,210,255],[255,240,200],[180,220,255],[255,220,180],[220,200,255]]
  for (let i = 0; i < 60; i++) {
    const [r,g,b] = colors[Math.floor(Math.random()*colors.length)]
    stars.push({
      x: Math.random()*w, y: Math.random()*h, r: 0.3+Math.random()*2,
      twinkle: Math.random()*Math.PI*2, speed: 0.01+Math.random()*0.03,
      opacity: 0.3+Math.random()*0.7, color: [r,g,b], halo: Math.random()>0.6,
    })
  }
}

const draw = (time) => {
  if (!ctx) return
  ctx.clearRect(0, 0, w, h)
  mouse.x += (mouse.tx - mouse.x) * 0.08
  mouse.y += (mouse.ty - mouse.y) * 0.08

  // 鼠标尾迹
  if (mouse.x > 0 && mouse.y > 0) {
    for (let i=0;i<2;i++) {
      trailParticles.push({ x:mouse.x+(Math.random()-0.5)*10, y:mouse.y+(Math.random()-0.5)*10, r:0.6+Math.random()*1.5, life:1, color:'180,210,255' })
    }
    if (trailParticles.length>60) trailParticles.splice(0,2)
  }
  for (const tp of trailParticles) { tp.life-=0.015; if(tp.life<=0)continue; ctx.beginPath(); ctx.arc(tp.x,tp.y,tp.r,0,Math.PI*2); ctx.fillStyle=`rgba(${tp.color},${tp.life*0.6})`; ctx.fill() }
  trailParticles = trailParticles.filter(t=>t.life>0)

  // ====== 中央光球 + 环绕标签（Canvas 绘制） ======
  const cx = w/2, cy = h/2 - 20
  const t = time * 0.001

  // 外发光晕
  const outerGlow = ctx.createRadialGradient(cx, cy, 15, cx, cy, 120)
  outerGlow.addColorStop(0, 'rgba(180,160,240,0.2)')
  outerGlow.addColorStop(0.4, 'rgba(120,140,220,0.06)')
  outerGlow.addColorStop(1, 'rgba(0,0,0,0)')
  ctx.fillStyle = outerGlow; ctx.beginPath(); ctx.arc(cx, cy, 120, 0, Math.PI*2); ctx.fill()

  // 日冕环 — 多个椭圆环，不同角度倾斜
  for (let ring = 0; ring < 4; ring++) {
    const tilt = ring * 0.4 + t * 0.3
    const rx = 70 + ring * 25 + Math.sin(t * 1.2 + ring) * 8
    const ry = 22 + ring * 3 + Math.cos(t * 0.9 + ring) * 5
    const alpha = 0.15 - ring * 0.03
    ctx.save()
    ctx.translate(cx, cy)
    ctx.rotate(tilt)
    ctx.strokeStyle = `rgba(180,200,240,${alpha})`
    ctx.lineWidth = 0.6 + ring * 0.2
    ctx.beginPath(); ctx.ellipse(0, 0, rx, ry, 0, 0, Math.PI*2); ctx.stroke()
    ctx.restore()
  }

  // 光环粒子 — 沿轨道分布
  for (let ring = 0; ring < 3; ring++) {
    const r = 90 + ring * 38
    const count = 40 + ring * 20
    const speed = 0.4 - ring * 0.1
    for (let i = 0; i < count; i++) {
      const angle = (i / count) * Math.PI * 2 + t * speed
      const px = cx + Math.cos(angle) * r
      const py = cy + Math.sin(angle) * r * 0.5 // 椭圆压扁
      const alpha = 0.12 + 0.08 * Math.sin(t * 2 + i * 0.3)
      ctx.beginPath()
      ctx.arc(px, py, 0.6 + ring * 0.2, 0, Math.PI*2)
      ctx.fillStyle = `rgba(200,210,255,${alpha})`
      ctx.fill()
    }
  }

  // 光球核心
  const coreGrad = ctx.createRadialGradient(cx, cy, 0, cx, cy, 24)
  coreGrad.addColorStop(0, 'rgba(255,255,255,0.9)')
  coreGrad.addColorStop(0.15, 'rgba(230,220,255,0.5)')
  coreGrad.addColorStop(0.4, 'rgba(160,160,220,0.2)')
  coreGrad.addColorStop(1, 'rgba(0,0,0,0)')
  ctx.fillStyle = coreGrad; ctx.beginPath(); ctx.arc(cx, cy, 24, 0, Math.PI*2); ctx.fill()

  // ====== Canvas 绘制环绕标签 ======
  const tagLabels = [
    { emoji: '💬', text: '碎碎念', tag: '碎碎念' },
    { emoji: '💡', text: '灵感', tag: '灵感' },
    { emoji: '🎯', text: '心得', tag: '心得' },
  ]
  orbitTags.length = 0 // 清空旧数据
  for (let i = 0; i < 3; i++) {
    const angle = t * 0.35 + i * Math.PI * 2 / 3
    const orbitR = 130
    const tx = cx + Math.cos(angle) * orbitR
    const ty = cy + Math.sin(angle) * orbitR * 0.5
    orbitTags.push({ x: tx, y: ty, ...tagLabels[i] })

    // 连接线
    ctx.beginPath()
    ctx.moveTo(cx, cy)
    ctx.lineTo(tx, ty)
    ctx.strokeStyle = 'rgba(180,200,240,0.06)'
    ctx.lineWidth = 1
    ctx.stroke()

    // 标签背景光晕
    const tagGlow = ctx.createRadialGradient(tx, ty, 0, tx, ty, 28)
    tagGlow.addColorStop(0, 'rgba(180,200,240,0.15)')
    tagGlow.addColorStop(1, 'rgba(0,0,0,0)')
    ctx.fillStyle = tagGlow; ctx.beginPath(); ctx.arc(tx, ty, 28, 0, Math.PI*2); ctx.fill()

    // 半透明圆形容器
    ctx.fillStyle = 'rgba(255,255,255,0.1)'
    ctx.strokeStyle = 'rgba(255,255,255,0.18)'
    ctx.lineWidth = 1
    ctx.beginPath(); ctx.arc(tx, ty, 26, 0, Math.PI*2); ctx.fill(); ctx.stroke()

    // emoji
    ctx.font = '18px sans-serif'
    ctx.textAlign = 'center'
    ctx.textBaseline = 'middle'
    ctx.fillText(tagLabels[i].emoji, tx, ty - 4)

    // 文字
    ctx.font = '10px "PingFang SC", "Microsoft YaHei", sans-serif'
    ctx.fillStyle = 'rgba(255,255,255,0.75)'
    ctx.fillText(tagLabels[i].text, tx, ty + 16)
  }

  // 星星
  for (const s of stars) {
    s.twinkle += s.speed
    const alpha = s.opacity*(0.45+0.55*Math.sin(s.twinkle))
    const [r,g,b]=s.color
    const glow = ctx.createRadialGradient(s.x,s.y,0,s.x,s.y,s.r*3)
    glow.addColorStop(0,`rgba(${r},${g},${b},${alpha*0.4})`); glow.addColorStop(1,'rgba(0,0,0,0)')
    ctx.fillStyle=glow; ctx.fillRect(s.x-s.r*3,s.y-s.r*3,s.r*6,s.r*6)
    ctx.beginPath(); ctx.arc(s.x,s.y,s.r,0,Math.PI*2); ctx.fillStyle=`rgba(${r},${g},${b},${alpha})`; ctx.fill()
  }

  // 粒子
  for (const p of particles){ p.x+=p.vx;p.y+=p.vy;p.vy+=0.03;p.life-=0.018;if(p.life<=0)continue;ctx.beginPath();ctx.arc(p.x,p.y,p.r,0,Math.PI*2);ctx.fillStyle=`rgba(${p.color},${p.life})`;ctx.fill() }
  particles=particles.filter(p=>p.life>0)

  animId = requestAnimationFrame(draw)
}

/* ====== 交互 ====== */
const handleCanvasClick = async (e) => {
  if (showCard.value) return
  const rect = canvasRef.value.getBoundingClientRect()
  const cx = e.clientX - rect.left, cy = e.clientY - rect.top

  // 先检查是否点击了环绕标签
  const tagHit = orbitTags.find(t => Math.hypot(t.x - cx, t.y - cy) < 28)
  if (tagHit) {
    burstParticles(cx, cy)
    try {
      const res = await getRandomMoment()
      currentMoment.value = res.data.data
      if (currentMoment.value && tagHit.tag) currentMoment.value.tag = tagHit.tag
      showCard.value = true
    } catch {}
    return
  }

  // 再检查星星
  const hit = stars.find(s => Math.hypot(s.x-cx, s.y-cy) < 20)
  if (!hit) return
  burstParticles(hit.x, hit.y)
  try {
    const res = await getRandomMoment()
    currentMoment.value = res.data.data || { content:'还没有人留下小瞬间，来做第一个吧 ✨', emoji:'🌟', color:'#FFF8DC', nickname:'系统' }
    showCard.value = true
  } catch {}
}

const burstParticles = (x, y) => {
  for (let i=0;i<15;i++){ const a=Math.PI*2*i/15+Math.random()*0.3, sp=1+Math.random()*2.5; particles.push({x,y,vx:Math.cos(a)*sp,vy:Math.sin(a)*sp-1,r:1+Math.random()*2,life:1,color:'255,220,150'}) }
}

const drawAgain = async () => {
  try {
    const res = await getRandomMoment()
    currentMoment.value = res.data.data || { content:'还没有人留下小瞬间，来做第一个吧 ✨', emoji:'🌟', color:'#FFF8DC', nickname:'系统' }
  } catch {}
}

const closeCard = () => { showCard.value = false; currentMoment.value = null }
const loadAll = async () => {
  try {
    const r = await getMomentsList()
    allMoments.value = r.data.data ?? []
    fanIndex.value = 0
    showList.value = true
  } catch (e) {
    console.error('加载小瞬间失败', e)
  }
}
const fanPrev = () => { if (fanIndex.value > 0) fanIndex.value-- }
const fanNext = () => { if (fanIndex.value < allMoments.value.length - 1) fanIndex.value++ }
const fanGo = (i) => { fanIndex.value = i }
// 弧形偏移计算：返回每张卡片的 transform 样式
const fanStyle = (i) => {
  const offset = i - fanIndex.value
  const absOff = Math.abs(offset)
  if (absOff > 2) return { display: 'none' }
  const rotateY = offset * 20
  const translateX = offset * 130
  const translateZ = -absOff * 100
  const scale = 1 - absOff * 0.15
  const zIndex = 10 - absOff
  const opacity = absOff > 1 ? 0 : 1 - absOff * 0.25
  return {
    transform: `translate(-50%, -50%) perspective(800px) rotateY(${rotateY}deg) translateX(${translateX}px) translateZ(${translateZ}px) scale(${scale})`,
    zIndex,
    opacity,
    cursor: absOff === 0 ? 'default' : 'pointer',
  }
}

const handlePublish = async () => {
  if (!publishForm.value.content.trim()) { ElMessage.warning('先写点内容再提交吧'); return }
  try {
    await submitMoment(
      { content: publishForm.value.content, emoji: publishForm.value.emoji, nickname: publishForm.value.nickname || visitorStore.nickname || '匿名' },
      visitorStore.visitorToken,
      visitorStore.fingerprint
    )
    publishForm.value = { content: '', emoji: '💬', nickname: '' }
    showPublish.value = false
    ElMessage.success('已提交，等待审核 ✨')
  } catch (e) {
    ElMessage.error(e?.msg || '提交失败，请稍后重试')
  }
}

const mouseMove = (e) => {
  if (!canvasRef.value) return
  const r = canvasRef.value.getBoundingClientRect()
  mouse.tx = e.clientX - r.left; mouse.ty = e.clientY - r.top
}
const mouseLeave = () => { mouse.tx=-100; mouse.ty=-100 }

const onKey = (e) => { if(e.key==='Escape'){ showPublish.value=false; showList.value=false; showCard.value&&closeCard() } }

let resizeObs = null
onMounted(async () => {
  articleTitle.value = '星空拾光'; articleMeta.value = '点击星星或环绕标签，拾取散落在时光里的小瞬间'
  initCanvas(); draw(0)
  resizeObs = new ResizeObserver(() => { initCanvas() }); resizeObs.observe(canvasRef.value)
  window.addEventListener('keydown', onKey)
})
onUnmounted(() => { cancelAnimationFrame(animId); resizeObs?.disconnect(); window.removeEventListener('keydown',onKey) })
</script>

<template>
  <div class="moments-wrapper">
    <div class="moments-layout">
      <!-- 左侧：星空交互区 -->
      <div class="moments-main">
        <div class="starfield-card">
          <canvas
            ref="canvasRef"
            class="starfield-canvas"
            @click="handleCanvasClick"
            @mousemove="mouseMove"
            @mouseleave="mouseLeave"
          />
          <!-- 中央提示 -->
          <div class="starfield-hint">点击星星或环绕标签，发现一瞬间 ✨</div>
        </div>

        <!-- 底部浮动按钮 -->
        <div class="floating-actions">
          <button class="float-btn" @click="loadAll">
            <!-- <span class="float-icon">📋</span> -->
            <span>浏览全部</span>
          </button>
          <button class="float-btn primary" @click="showPublish = true">
            <!-- <span class="float-icon">✨</span> -->
            <span>留下瞬间</span>
          </button>
        </div>
      </div>

      <!-- 右侧：个人信息 -->
      <aside class="moments-sidebar">
        <SidebarCard />
      </aside>
    </div>

    <!-- 卡片弹窗 —— -->
    <transition name="card-pop">
      <div v-if="showCard" class="card-overlay" @click.self="closeCard">
        <div class="moment-card-wrap">
          <div class="moment-card" :style="{ background: currentMoment?.color || '#FFF8DC' }">
          <div class="card-inner">
            <span class="card-emoji">{{ currentMoment?.emoji || '💫' }}</span>
            <p class="card-content">{{ currentMoment?.content }}</p>
            <span class="card-author">— {{ currentMoment?.nickname || 'YeSheng' }}</span>
          </div>
          <div class="card-actions">
            <button class="card-btn" @click="drawAgain">🎲 再抽一张</button>
            <!-- <button class="card-btn outline" @click="closeCard(); loadAll()">📋 浏览全部</button> -->
          </div>
          <button class="card-close" @click="closeCard"><svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg></button>
        </div>
        </div>
      </div>
    </transition>

    <!-- 全部浏览 - 弧形卡片 -->
    <transition name="fade">
      <div v-if="showList" class="fan-overlay" @click.self="showList=false">
        <button class="fan-close" @click="showList=false">
          <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="2"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
        </button>

        <h2 class="fan-title">全部小瞬间 <span class="fan-count">{{ allMoments.length }} 条</span></h2>

        <div class="fan-stage">
          <!-- 左右切换 -->
          <button class="fan-nav fan-nav-left" :class="{ disabled: fanIndex === 0 }" @click="fanPrev">
            <svg viewBox="0 0 24 24" width="28" height="28" fill="none" stroke="currentColor" stroke-width="2"><polyline points="15 18 9 12 15 6"/></svg>
          </button>

          <div class="fan-track">
            <div
              v-for="(m, idx) in allMoments" :key="m.id"
              class="fan-card"
              :style="{ ...fanStyle(idx), background: m.color || '#FFF8DC' }"
              @click="fanGo(idx)"
            >
              <span class="fan-card-emoji">{{ m.emoji || '💫' }}</span>
              <p>{{ m.content }}</p>
              <span class="fan-card-author">— {{ m.nickname || 'YeSheng' }}</span>
            </div>
          </div>

          <button class="fan-nav fan-nav-right" :class="{ disabled: fanIndex >= allMoments.length - 1 }" @click="fanNext">
            <svg viewBox="0 0 24 24" width="28" height="28" fill="none" stroke="currentColor" stroke-width="2"><polyline points="9 18 15 12 9 6"/></svg>
          </button>
        </div>

        <!-- 底部圆点 -->
        <div class="fan-dots">
          <button
            v-for="(m, idx) in allMoments" :key="'d'+m.id"
            class="fan-dot"
            :class="{ active: idx === fanIndex }"
            @click="fanGo(idx)"
          />
        </div>
      </div>
    </transition>

    <!-- 发布 -->
    <transition name="fade"><div v-if="showPublish" class="list-overlay" @click.self="showPublish=false"><div class="publish-panel"><h3>✨ 留下你的小瞬间</h3><p class="publish-sub">一句话、一个念头、一种心情</p><textarea v-model="publishForm.content" placeholder="写下此刻的感受..." maxlength="500" rows="4" class="publish-textarea"/><div class="publish-row"><input v-model="publishForm.nickname" placeholder="你的昵称" class="publish-input"/><select v-model="publishForm.emoji" class="publish-emoji"><option>💬</option><option>💡</option><option>🌈</option><option>🌙</option><option>🔥</option><option>🎵</option><option>💪</option><option>🫧</option><option>🍃</option><option>💭</option><option>❤️</option><option>🎯</option></select></div><div class="publish-actions"><button class="card-btn" @click="showPublish=false">取消</button><button class="card-btn primary-fill" @click="handlePublish">提交</button></div></div></div></transition>
  </div>
</template>

<style scoped>
/* ====== 布局 ====== */
.moments-wrapper { width: 100%; }
.moments-layout { display: flex; gap: 24px; align-items: flex-start; }
.moments-main { flex: 1; min-width: 0; }
.moments-sidebar { width: 260px; flex-shrink: 0; }
@media (max-width: 960px) { .moments-layout { flex-direction: column-reverse; } .moments-sidebar { width: 100%; } }

/* ====== 星空卡片 ====== */
.starfield-card {
  position: relative; height: 520px; border-radius: 20px; overflow: hidden;
  background: radial-gradient(ellipse at 50% 30%, #1a1a38 0%, #0c0c1e 70%);
  border: 1px solid rgba(180,200,255,0.08);
  box-shadow: 0 4px 32px rgba(0,0,0,0.15), inset 0 1px 0 rgba(255,255,255,0.03);
  cursor: pointer;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}
/* 噪点纹理 */
.starfield-card::after {
  content: ''; position: absolute; inset: 0; pointer-events: none; z-index: 3;
  background-image: url("data:image/svg+xml,%3Csvg viewBox='0 0 256 256' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='n'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.9' numOctaves='4' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23n)' opacity='0.03'/%3E%3C/svg%3E");
  background-size: 200px 200px;
}
.starfield-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 40px rgba(0,0,0,0.25), inset 0 1px 0 rgba(255,255,255,0.05);
}
.starfield-canvas { position: absolute; inset: 0; width: 100%; height: 100%; }
.starfield-hint {
  position: absolute; bottom: 60px; left: 50%; transform: translateX(-50%);
  color: rgba(255,255,255,0.5); font-size: 13px; pointer-events: none; z-index: 2;
}

/* ====== 浮动按钮 ====== */
.floating-actions {
  display: flex; gap: 12px; justify-content: center; margin-top: 20px;
}
.float-btn {
  display: inline-flex; align-items: center; gap: 6px;
  padding: 12px 28px; border-radius: 50px;
  border: 1px solid var(--blog-border, #e4e7ed);
  background: var(--blog-card, #fff);
  color: var(--blog-text2, #606266); cursor: pointer;
  font-size: 14px; font-family: inherit;
  transition: all 0.25s;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04);
}
.float-btn:hover { transform: translateY(-2px); box-shadow: 0 4px 16px rgba(0,0,0,0.08); border-color: #ccc; }
.float-btn.primary { background: #1a1a2e; color: #fff; border-color: #1a1a2e; }
.float-btn.primary:hover { background: #2d2d45; box-shadow: 0 6px 20px rgba(26,26,46,0.3); }
.float-icon { font-size: 16px; }

/* ====== 卡片弹窗 / 按钮 ====== */
.card-overlay { position: fixed; inset: 0; z-index: 100; background: rgba(0,0,0,0.5); backdrop-filter: blur(16px); -webkit-backdrop-filter: blur(16px); display: flex; align-items: center; justify-content: center; }
.moment-card-wrap {
  position: relative; padding: 2px; border-radius: 26px;
  background: linear-gradient(135deg, rgba(255,255,255,0.3), rgba(180,200,255,0.15), rgba(220,180,255,0.2), rgba(255,255,255,0.1));
  background-size: 300% 300%;
  animation: card-enter 0.5s cubic-bezier(0.34,1.56,0.64,1), border-shift 4s ease-in-out infinite;
  box-shadow: 0 0 40px rgba(180,200,255,0.15), 0 20px 60px rgba(0,0,0,0.3);
}
@keyframes border-shift {
  0%,100% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
}
.moment-card { position: relative; width: 400px; max-width: 90vw; border-radius: 24px; box-shadow: 0 0 0 1px rgba(255,255,255,0.05) inset; }
.card-inner { padding: 44px 36px 28px; text-align: center; }
.card-emoji { font-size: 56px; display: block; margin-bottom: 18px; filter: drop-shadow(0 2px 8px rgba(0,0,0,0.08)); }
.card-content { font-size: 20px; line-height: 1.8; color: #2c2c2c; margin: 0 0 14px; font-family: 'Noto Serif SC', var(--blog-serif), serif; letter-spacing: 0.5px; }
.card-author { font-size: 13px; color: #999; letter-spacing: 1px; }
.card-close { position: absolute; top: 14px; right: 14px; width: 32px; height: 32px; border-radius: 50%; border: none; background: rgba(0,0,0,0.06); color: #888; cursor: pointer; display: flex; align-items: center; justify-content: center; transition: all 0.2s; }
.card-close:hover { background: rgba(0,0,0,0.12); color: #444; transform: rotate(90deg); }
.card-actions { display: flex; gap: 10px; justify-content: center; padding: 0 36px 32px; }
.card-btn { display: inline-flex; align-items: center; gap: 4px; padding: 10px 22px; border: 1.5px solid rgba(0,0,0,0.1); border-radius: 50px; background: rgba(255,255,255,0.6); color: #555; cursor: pointer; font-size: 14px; font-family: inherit; transition: all 0.25s; backdrop-filter: blur(4px); }
.card-btn:hover { background: rgba(255,255,255,0.9); border-color: rgba(0,0,0,0.2); color: #333; transform: translateY(-1px); box-shadow: 0 4px 12px rgba(0,0,0,0.08); }
.card-btn.outline { background: transparent; backdrop-filter: none; }
.card-btn.primary-fill { background: #2c2c2c; color: #fff; border-color: #2c2c2c; }
.card-btn.primary-fill:hover { background: #444; border-color: #444; box-shadow: 0 4px 16px rgba(0,0,0,0.2); }

/* ====== 弧形卡片浏览器 ====== */
.fan-overlay {
  position: fixed; inset: 0; z-index: 100;
  background: rgba(0,0,0,0.55); backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  gap: 24px;
}
.fan-close {
  position: absolute; top: 28px; right: 28px;
  width: 40px; height: 40px; border-radius: 50%;
  border: 1.5px solid rgba(255,255,255,0.2); background: rgba(255,255,255,0.08);
  color: #fff; cursor: pointer; display: flex; align-items: center; justify-content: center;
  transition: all 0.25s;
}
.fan-close:hover { background: rgba(255,255,255,0.2); border-color: rgba(255,255,255,0.4); }

.fan-title { color: #fff; font-size: 22px; font-weight: 600; margin: 0; text-align: center; }
.fan-count { font-size: 14px; font-weight: 400; opacity: 0.6; margin-left: 6px; }

/* 舞台 */
.fan-stage {
  display: flex; align-items: center; gap: 16px;
  perspective: 1000px;
}
.fan-track {
  position: relative;
  width: 340px; height: 320px;
  transform-style: preserve-3d;
}

/* 卡片 */
.fan-card {
  position: absolute;
  left: 50%; top: 50%;
  width: 260px; padding: 28px 22px; border-radius: 20px; text-align: center;
  transition: all 0.45s cubic-bezier(0.34,1.56,0.64,1);
  box-shadow: 0 4px 20px rgba(0,0,0,0.12);
  user-select: none;
}
.fan-card-emoji { font-size: 38px; display: block; margin-bottom: 10px; }
.fan-card p { font-size: 15px; color: #444; margin: 0 0 8px; line-height: 1.6; }
.fan-card-author { font-size: 12px; color: #aaa; }

/* 导航按钮 */
.fan-nav {
  flex-shrink: 0; width: 44px; height: 44px; border-radius: 50%;
  border: 1.5px solid rgba(255,255,255,0.2); background: rgba(255,255,255,0.08);
  color: #fff; cursor: pointer; display: flex; align-items: center; justify-content: center;
  transition: all 0.25s;
}
.fan-nav:hover { background: rgba(255,255,255,0.18); border-color: rgba(255,255,255,0.4); }
.fan-nav.disabled { opacity: 0.25; cursor: default; pointer-events: none; }

/* 底部圆点 */
.fan-dots { display: flex; gap: 8px; }
.fan-dot {
  width: 8px; height: 8px; border-radius: 50%; border: none;
  background: rgba(255,255,255,0.3); cursor: pointer; padding: 0;
  transition: all 0.3s;
}
.fan-dot.active { background: #fff; transform: scale(1.4); box-shadow: 0 0 8px rgba(255,255,255,0.4); }
.list-overlay { position: fixed; inset: 0; z-index: 100; background: rgba(0,0,0,0.5); backdrop-filter: blur(10px); display: flex; align-items: center; justify-content: center; }
.publish-panel { background: #fff; border-radius: 20px; padding: 28px 28px 20px; width: 440px; max-width: 92vw; text-align: center; box-shadow: 0 20px 60px rgba(0,0,0,0.3); }
.publish-panel h3 { margin: 0 0 6px; font-size: 20px; }
.publish-sub { font-size: 13px; color: #aaa; margin: 0 0 16px; }
.publish-textarea { width: 100%; padding: 14px; border: 1.5px solid #e8e8e8; border-radius: 12px; font-size: 15px; resize: vertical; font-family: inherit; box-sizing: border-box; outline: none; }
.publish-textarea:focus { border-color: #bbb; }
.publish-row { display: flex; gap: 10px; margin-top: 10px; }
.publish-input { flex: 1; padding: 10px 14px; border: 1.5px solid #e8e8e8; border-radius: 10px; font-size: 14px; outline: none; }
.publish-input:focus { border-color: #bbb; }
.publish-emoji { padding: 8px; border: 1.5px solid #e8e8e8; border-radius: 10px; font-size: 20px; cursor: pointer; background: #fff; }
.publish-actions { display: flex; gap: 10px; justify-content: center; margin-top: 18px; }

/* ====== 动画 ====== */
@keyframes card-enter { from { opacity:0; transform:scale(0.85) translateY(20px); } to { opacity:1; transform:scale(1) translateY(0); } }
.card-pop-enter-active { transition: all 0.4s cubic-bezier(0.34,1.56,0.64,1); }
.card-pop-leave-active { transition: all 0.2s ease; }
.card-pop-enter-from { opacity: 0; }
.fade-enter-active, .fade-leave-active { transition: opacity 0.25s; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
</style>
