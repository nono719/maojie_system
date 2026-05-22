<script setup>
import { onMounted, ref } from 'vue'
import { patientHome } from '@/api/training'
import { assignedTasks as fetchAssigned } from '@/api/task'
import { useRouter } from 'vue-router'
import StatCard from '@/components/StatCard.vue'
import TrendChart from '@/components/TrendChart.vue'
import http from '@/api/index'

const router = useRouter()
const stats = ref({ todayTrainingCount: 0, totalTrainingCount: 0, tokenBalance: 0, streakDays: 0 })
const tasks = ref([])
const trend = ref({ labels: [], records: [], completion: [] })

onMounted(async () => {
  try { stats.value = { ...stats.value, ...(await patientHome()) } } catch (_) {}
  try { tasks.value = await fetchAssigned() } catch (_) {}
  try { trend.value = await http.get('/patient/trend?days=7') } catch (_) {}
})
</script>

<template>
  <div class="page-container">
    <div class="page-title">今日训练</div>

    <div class="stat-grid">
      <StatCard label="🔥 今日训练" :value="stats.todayTrainingCount" suffix=" 次" accent="linear-gradient(135deg, #ff7a45, #fa541c)" />
      <StatCard label="📊 累计训练" :value="stats.totalTrainingCount" suffix=" 次" accent="linear-gradient(135deg, #10b7e0, #0066ff)" />
      <StatCard label="🪙 代币余额" :value="stats.tokenBalance" suffix=" BREATH" accent="linear-gradient(135deg, #faad14, #d48806)" />
      <StatCard label="⭐ 连续打卡" :value="stats.streakDays" suffix=" 天" accent="linear-gradient(135deg, #722ed1, #531dab)" />
    </div>

    <div class="section-card">
      <div class="section-card-title">最近 7 天训练趋势</div>
      <TrendChart
        :labels="trend.labels"
        :series="[
          { name: '训练次数', data: trend.records, color: '#0066ff' },
          { name: '完成率(%)', data: trend.completion, color: '#52c41a' }
        ]"
        height="260px"
      />
    </div>

    <div class="section-card">
      <div class="section-card-title">今日待完成训练</div>
      <a-empty v-if="!tasks.length" description="今天没有待完成任务" />
      <div v-else class="task-list">
        <div v-for="item in tasks" :key="item.id" class="task-card">
          <div class="task-info">
            <div class="task-name">{{ item.taskName }}</div>
            <div class="task-meta">
              <a-tag color="blue">吸 {{ item.inhaleSeconds }}s</a-tag>
              <a-tag>屏 {{ item.holdSeconds }}s</a-tag>
              <a-tag color="green">呼 {{ item.exhaleSeconds }}s</a-tag>
              <a-tag v-if="item.keepSeconds > 0">保 {{ item.keepSeconds }}s</a-tag>
              <span class="dot">·</span>
              <span>单次 {{ item.duration }}s</span>
              <span class="dot">·</span>
              <span class="reward">奖励 {{ item.rewardAmount }} BREATH</span>
            </div>
          </div>
          <a-button type="primary" size="large" @click="router.push(`/patient/training/${item.id}`)">
            开始训练 →
          </a-button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.task-list { display: flex; flex-direction: column; gap: 12px; }
.task-card {
  background: linear-gradient(135deg, #f0f7ff 0%, #ffffff 100%);
  border: 1px solid rgba(0, 102, 255, 0.1);
  border-radius: 12px;
  padding: 16px 20px;
  display: flex; justify-content: space-between; align-items: center;
  transition: transform .2s, box-shadow .2s;
}
.task-card:hover { transform: translateY(-2px); box-shadow: 0 6px 16px rgba(0, 102, 255, 0.1); }
.task-name { font-size: 16px; font-weight: 600; margin-bottom: 8px; color: var(--color-text); }
.task-meta { font-size: 13px; color: var(--color-text-light); display: flex; align-items: center; gap: 6px; flex-wrap: wrap; }
.dot { color: #d1d5db; margin: 0 2px; }
.reward { color: #fa8c16; font-weight: 600; }
</style>
