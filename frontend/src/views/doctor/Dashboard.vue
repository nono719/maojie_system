<script setup>
import { onMounted, ref } from 'vue'
import { doctorDashboard } from '@/api/training'

const stats = ref({ patientCount: 0, todayTrainings: 0, activeTaskCount: 0, totalRewards: 0 })

onMounted(async () => {
  try {
    stats.value = await doctorDashboard()
  } catch (_) {}
})
</script>

<template>
  <div>
    <h2 style="margin-bottom:24px;">数据概览</h2>
    <div class="card-grid">
      <a-card><a-statistic title="患者总数" :value="stats.patientCount" /></a-card>
      <a-card><a-statistic title="今日训练次数" :value="stats.todayTrainings" /></a-card>
      <a-card><a-statistic title="活跃任务数" :value="stats.activeTaskCount" /></a-card>
      <a-card><a-statistic title="累计发放奖励" :value="stats.totalRewards" suffix="BREATH" /></a-card>
    </div>

    <a-card style="margin-top:24px;" title="说明">
      <p>这里后续接 ECharts 折线图、患者活跃度热力图（论文图 6-3）。</p>
    </a-card>
  </div>
</template>
