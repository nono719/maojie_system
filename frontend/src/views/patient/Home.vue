<script setup>
import { onMounted, ref } from 'vue'
import { patientHome } from '@/api/training'
import { assignedTasks as fetchAssigned } from '@/api/task'
import { useRouter } from 'vue-router'

const router = useRouter()
const stats = ref({ todayTrainingCount: 0, totalTrainingCount: 0 })
const tasks = ref([])

onMounted(async () => {
  try {
    stats.value = await patientHome()
    tasks.value = await fetchAssigned()
  } catch (_) {}
})
</script>

<template>
  <div>
    <h2 style="margin-bottom:16px;">我的训练</h2>
    <div class="card-grid" style="margin-bottom:24px;">
      <a-card><a-statistic title="今日训练" :value="stats.todayTrainingCount" suffix="次" /></a-card>
      <a-card><a-statistic title="累计训练" :value="stats.totalTrainingCount" suffix="次" /></a-card>
      <a-card><a-statistic title="代币余额" :value="0" suffix="BREATH" /></a-card>
      <a-card><a-statistic title="连续打卡" :value="0" suffix="天" /></a-card>
    </div>

    <a-card title="待完成任务">
      <a-empty v-if="!tasks.length" description="今天没有待完成任务" />
      <a-list v-else :data-source="tasks">
        <template #renderItem="{ item }">
          <a-list-item>
            <a-list-item-meta>
              <template #title>{{ item.taskName }}</template>
              <template #description>
                吸 {{ item.inhaleSeconds }}s · 屏 {{ item.holdSeconds }}s · 呼 {{ item.exhaleSeconds }}s · 保 {{ item.keepSeconds }}s
                | 单次 {{ item.duration }}s | 奖励 {{ item.rewardAmount }} BREATH
              </template>
            </a-list-item-meta>
            <a-button type="primary" @click="router.push(`/patient/training/${item.id}`)">开始训练</a-button>
          </a-list-item>
        </template>
      </a-list>
    </a-card>
  </div>
</template>
