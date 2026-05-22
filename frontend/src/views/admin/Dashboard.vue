<script setup>
import { onMounted, ref } from 'vue'
import http from '@/api/index'

const stats = ref({ userCount: 0, doctorCount: 0, patientCount: 0, taskCount: 0, recordCount: 0, totalRewards: 0, chainBlock: 0 })

onMounted(async () => {
  try { stats.value = await http.get('/admin/overview') } catch (_) {}
})
</script>

<template>
  <div>
    <h2 style="margin-bottom:24px;">系统数据概览</h2>
    <div class="card-grid">
      <a-card><a-statistic title="用户总数"     :value="stats.userCount" /></a-card>
      <a-card><a-statistic title="医生数"       :value="stats.doctorCount" /></a-card>
      <a-card><a-statistic title="患者数"       :value="stats.patientCount" /></a-card>
      <a-card><a-statistic title="训练任务数"   :value="stats.taskCount" /></a-card>
      <a-card><a-statistic title="累计训练记录" :value="stats.recordCount" /></a-card>
      <a-card><a-statistic title="累计发奖" :value="stats.totalRewards" suffix="BREATH" /></a-card>
      <a-card><a-statistic title="链上区块号"   :value="stats.chainBlock" /></a-card>
    </div>
  </div>
</template>
