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

    <a-modal :open="!!result" :footer="null" title="训练完成" @cancel="result = null">
      <a-result status="success" title="本次训练已记录" sub-title="数据已写入区块链并发放奖励">
        <template #extra>
          <div class="result">
            <p>训练记录 ID：<strong>{{ result.recordId }}</strong></p>
            <p>数据哈希：<code>{{ result.dataHash }}</code></p>
            <p>上链状态：<a-tag :color="result.chainStatus === 'SUCCESS' ? 'green' : 'orange'">{{ result.chainStatus }}</a-tag></p>
            <p v-if="result.blockTxId">区块链交易：<code>{{ result.blockTxId }}</code></p>
            <p v-if="result.rewardAmount">奖励：<a-tag color="gold">{{ result.rewardAmount }} BREATH</a-tag></p>
          </div>
        </template>
      </a-result>
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
</style>
