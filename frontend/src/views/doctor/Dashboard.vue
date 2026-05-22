<script setup>
import { onMounted, ref } from 'vue'
import http from '@/api/index'
import { doctorDashboard } from '@/api/training'
import StatCard from '@/components/StatCard.vue'
import TrendChart from '@/components/TrendChart.vue'

const stats = ref({ patientCount: 0, todayTrainings: 0, activeTaskCount: 0, totalRewards: 0 })
const trend = ref({ labels: [], records: [], completion: [] })

onMounted(async () => {
  try { stats.value = await doctorDashboard() } catch (_) {}
  try { trend.value = await http.get('/doctor/trend?days=7') } catch (_) {}
})
</script>

<template>
  <div class="page-container">
    <div class="page-title">数据概览</div>

    <div class="stat-grid">
      <StatCard label="🧑‍⚕️ 我的患者"   :value="stats.patientCount" accent="linear-gradient(135deg, #10b7e0, #0066ff)" />
      <StatCard label="🔥 今日训练"     :value="stats.todayTrainings" accent="linear-gradient(135deg, #52c41a, #389e0d)" />
      <StatCard label="📝 活跃任务"     :value="stats.activeTaskCount || 0" accent="linear-gradient(135deg, #faad14, #d48806)" />
      <StatCard label="🪙 累计发奖"   :value="stats.totalRewards || 0" suffix=" BREATH" accent="linear-gradient(135deg, #fa8c16, #d46b08)" />
    </div>

    <div class="section-card">
      <div class="section-card-title">患者最近 7 天训练情况</div>
      <TrendChart
        :labels="trend.labels"
        :series="[
          { name: '训练次数', data: trend.records, color: '#0066ff' },
          { name: '平均完成率(%)', data: trend.completion, color: '#52c41a' }
        ]"
        height="300px"
      />
    </div>
  </div>
</template>
