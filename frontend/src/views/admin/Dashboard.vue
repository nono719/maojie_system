<script setup>
import { onMounted, ref } from 'vue'
import http from '@/api/index'
import StatCard from '@/components/StatCard.vue'
import TrendChart from '@/components/TrendChart.vue'

const stats = ref({ userCount: 0, doctorCount: 0, patientCount: 0, taskCount: 0, recordCount: 0, totalRewards: 0, chainBlock: 0 })
const trend = ref({ labels: [], records: [], rewards: [] })

onMounted(async () => {
  try { stats.value = await http.get('/admin/overview') } catch (_) {}
  try { trend.value = await http.get('/admin/trend?days=7') } catch (_) {}
})
</script>

<template>
  <div class="page-container">
    <div class="page-title">系统数据概览</div>

    <div class="stat-grid">
      <StatCard label="👥 用户总数"   :value="stats.userCount" accent="linear-gradient(135deg, #ff7875, #ff4d4f)" />
      <StatCard label="🩺 医生数"     :value="stats.doctorCount" accent="linear-gradient(135deg, #10b7e0, #0066ff)" />
      <StatCard label="🤝 患者数"     :value="stats.patientCount" accent="linear-gradient(135deg, #52c41a, #389e0d)" />
      <StatCard label="📝 训练任务"   :value="stats.taskCount" accent="linear-gradient(135deg, #faad14, #d48806)" />
      <StatCard label="📋 训练记录"   :value="stats.recordCount" accent="linear-gradient(135deg, #722ed1, #531dab)" />
      <StatCard label="🪙 累计发奖" :value="stats.totalRewards" suffix=" BREATH" accent="linear-gradient(135deg, #fa8c16, #d46b08)" />
      <StatCard label="⛓ 链上区块"  :value="stats.chainBlock" accent="linear-gradient(135deg, #13c2c2, #08979c)" />
    </div>

    <div class="section-card">
      <div class="section-card-title">最近 7 天训练 & 奖励趋势</div>
      <TrendChart
        :labels="trend.labels"
        :series="[
          { name: '训练次数', data: trend.records, color: '#0066ff' },
          { name: '发奖数(BREATH)', data: trend.rewards, color: '#faad14' }
        ]"
        height="320px"
      />
    </div>
  </div>
</template>
