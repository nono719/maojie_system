<script setup>
import { computed, onBeforeUnmount, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { assignedTasks } from '@/api/task'
import { completeTraining } from '@/api/training'

const route = useRoute()
const router = useRouter()
const taskId = Number(route.params.taskId)

const task = ref(null)
const status = ref('idle') // idle | running | done
const phaseIndex = ref(0)
const phaseRemaining = ref(0)
const elapsed = ref(0)
const breathCount = ref(0)
const result = ref(null)

let timer = null

const phases = computed(() => {
  if (!task.value) return []
  return [
    { name: '吸气', seconds: task.value.inhaleSeconds, scale: 1.4, color: '#10b7e0' },
    { name: '屏息', seconds: task.value.holdSeconds,   scale: 1.4, color: '#5ad2c1' },
    { name: '呼气', seconds: task.value.exhaleSeconds, scale: 0.8, color: '#0066ff' },
    { name: '保持', seconds: task.value.keepSeconds,   scale: 0.8, color: '#7c83fd' }
  ].filter(p => p.seconds > 0)
})

const currentPhase = computed(() => phases.value[phaseIndex.value] || { name: '准备', scale: 1, color: '#999' })

const circleStyle = computed(() => ({
  transform: `scale(${currentPhase.value.scale})`,
  background: `radial-gradient(circle, ${currentPhase.value.color} 0%, rgba(255,255,255,0.2) 70%)`,
  transition: `transform ${currentPhase.value.seconds || 1}s ease-in-out`
}))

const remainingTotal = computed(() => {
  if (!task.value) return 0
  return Math.max(0, task.value.duration - elapsed.value)
})

const completionRate = computed(() => {
  if (!task.value) return 0
  return Math.min(100, Math.round(elapsed.value / task.value.duration * 100))
})

const loadTask = async () => {
  const all = await assignedTasks()
  task.value = all.find(t => t.id === taskId)
  if (!task.value) {
    message.error('任务不存在或未分配')
    router.back()
  } else {
    phaseRemaining.value = phases.value[0]?.seconds || 0
  }
}

const start = () => {
  if (status.value === 'running' || !task.value) return
  status.value = 'running'
  phaseIndex.value = 0
  phaseRemaining.value = phases.value[0].seconds
  elapsed.value = 0
  breathCount.value = 0

  timer = setInterval(() => {
    elapsed.value++
    phaseRemaining.value--
    if (phaseRemaining.value <= 0) {
      phaseIndex.value = (phaseIndex.value + 1) % phases.value.length
      phaseRemaining.value = phases.value[phaseIndex.value].seconds
      if (phaseIndex.value === 0) breathCount.value++
    }
    if (elapsed.value >= task.value.duration) finish(true)
  }, 1000)
}

const stop = () => finish(false)

const finish = async (completed) => {
  if (timer) { clearInterval(timer); timer = null }
  status.value = 'done'

  const dto = {
    duration: elapsed.value,
    breathCount: breathCount.value,
    completionRate: completionRate.value,
    score: Math.min(100, completionRate.value),
    heartRate: null
  }
  try {
    result.value = await completeTraining(taskId, dto)
    message.success(completed ? '训练完成，奖励已发放' : '训练已结束')
  } catch (_) {
    message.error('提交训练记录失败')
  }
}

onBeforeUnmount(() => { if (timer) clearInterval(timer) })
loadTask()
</script>

<template>
  <div v-if="task" class="training">
    <h2 style="margin-bottom:8px;">{{ task.taskName }}</h2>
    <p style="color:#888;margin-bottom:24px;">{{ task.description }}</p>

    <div class="stage">
      <div class="circle" :style="circleStyle">
        <div class="circle-inner">
          <div class="phase-name">{{ currentPhase.name }}</div>
          <div class="phase-time">{{ phaseRemaining }}s</div>
        </div>
      </div>
    </div>

    <div class="metrics">
      <div><span>剩余时间</span><strong>{{ remainingTotal }}s</strong></div>
      <div><span>已呼吸</span><strong>{{ breathCount }}</strong></div>
      <div><span>完成度</span><strong>{{ completionRate }}%</strong></div>
    </div>

    <div class="actions">
      <a-button v-if="status === 'idle'"    type="primary" size="large" @click="start">开始训练</a-button>
      <a-button v-if="status === 'running'" danger size="large" @click="stop">提前结束</a-button>
      <a-button v-if="status === 'done'"    type="primary" size="large" @click="router.push('/patient/home')">返回首页</a-button>
    </div>

    <a-modal :open="!!result" :footer="null" title="训练完成" :width="560" @cancel="result = null">
      <div v-if="result" class="result-modal">
        <div class="reward-banner" v-if="result.rewardAmount > 0">
          <div class="reward-amount">+{{ result.rewardAmount }} <span class="unit">BREATH</span></div>
          <div class="reward-label">本次奖励到账 🎉</div>
        </div>
        <div class="reward-banner skipped" v-else>
          <div class="reward-amount">未发奖</div>
          <div class="reward-label">{{ result.rewardReasons?.[0] || '完成率不足，请继续努力' }}</div>
        </div>

        <div class="result-block">
          <div class="block-title">奖励明细</div>
          <div v-if="result.rewardBase != null" class="reward-row">
            <span class="key">任务基础奖励</span>
            <span class="val">{{ result.rewardBase }} BREATH</span>
          </div>
          <div v-if="result.streakDays != null" class="reward-row">
            <span class="key">连续打卡</span>
            <span class="val">{{ result.streakDays }} 天</span>
          </div>
          <ul class="reasons" v-if="result.rewardReasons?.length">
            <li v-for="(r, i) in result.rewardReasons" :key="i">{{ r }}</li>
          </ul>
        </div>

        <div class="result-block">
          <div class="block-title">联盟链存证信息</div>
          <div class="reward-row">
            <span class="key">联盟链记录ID</span>
            <span class="val">#{{ result.recordId }}</span>
          </div>
          <div class="reward-row">
            <span class="key">联盟链状态</span>
            <a-tag :color="result.chainStatus === 'SUCCESS' ? 'green' : 'orange'">{{ result.chainStatus }}</a-tag>
          </div>
          <div class="reward-row">
            <span class="key">链类型</span>
            <span class="val">FISCO BCOS 联盟链</span>
          </div>
          <div class="reward-row">
            <span class="key">联盟链存证哈希</span>
            <code class="hash">{{ result.dataHash || '—' }}</code>
          </div>
          <div class="reward-row" v-if="result.blockTxId">
            <span class="key">联盟链交易哈希</span>
            <code class="hash">{{ result.blockTxId }}</code>
          </div>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<style scoped>
.training {
  max-width: 720px;
  margin: 0 auto;
  text-align: center;
}
.stage {
  height: 320px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 24px 0;
}
.circle {
  width: 200px;
  height: 200px;
  border-radius: 50%;
  box-shadow: 0 0 60px rgba(16, 183, 224, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
}
.circle-inner {
  color: #fff;
  text-align: center;
  text-shadow: 0 2px 8px rgba(0,0,0,0.2);
}
.phase-name { font-size: 24px; font-weight: 600; }
.phase-time { font-size: 38px; margin-top: 8px; }

.metrics {
  display: flex;
  justify-content: space-around;
  margin: 24px 0;
}
.metrics div { display: flex; flex-direction: column; align-items: center; }
.metrics span { color: #999; font-size: 13px; }
.metrics strong { font-size: 22px; margin-top: 4px; color: #0066ff; }

.actions { margin-top: 12px; }
.result p { margin: 6px 0; word-break: break-all; }

/* Result Modal */
.result-modal { text-align: left; }
.reward-banner {
  background: linear-gradient(135deg, #fa8c16 0%, #d46b08 100%);
  color: #fff;
  border-radius: 12px;
  padding: 24px;
  text-align: center;
  margin-bottom: 16px;
  box-shadow: 0 6px 18px rgba(250, 140, 22, 0.3);
}
.reward-banner.skipped {
  background: linear-gradient(135deg, #bfbfbf, #8c8c8c);
  box-shadow: 0 6px 18px rgba(140, 140, 140, 0.3);
}
.reward-amount { font-size: 36px; font-weight: 700; line-height: 1; }
.reward-amount .unit { font-size: 16px; opacity: 0.9; margin-left: 4px; }
.reward-label { font-size: 13px; opacity: 0.9; margin-top: 6px; }

.result-block {
  background: #fafbfc;
  border-radius: 10px;
  padding: 14px 18px;
  margin-bottom: 12px;
}
.block-title {
  font-size: 13px; font-weight: 600;
  color: var(--color-text-light);
  margin-bottom: 10px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}
.reward-row {
  display: flex; justify-content: space-between; align-items: flex-start;
  padding: 6px 0; font-size: 14px;
  gap: 12px;
}
.reward-row .key { color: var(--color-text-light); }
.reward-row .val { font-weight: 600; color: var(--color-text); }
.hash {
  font-family: ui-monospace, monospace;
  font-size: 12px;
  color: var(--brand-primary);
  flex: 1;
  text-align: right;
  word-break: break-all;
  white-space: normal;
}
.reasons {
  margin-top: 8px;
  padding: 8px 12px;
  background: #fff;
  border-radius: 6px;
  border-left: 3px solid var(--brand-primary);
  font-size: 12px;
  color: var(--color-text-light);
  line-height: 1.7;
  list-style: none;
}
.reasons li::before { content: '✓ '; color: var(--brand-primary); margin-right: 4px; }
</style>
