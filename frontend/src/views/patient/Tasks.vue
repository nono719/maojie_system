<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { assignedTasks } from '@/api/task'

const router = useRouter()
const tasks = ref([])

onMounted(async () => { tasks.value = await assignedTasks() })
</script>

<template>
  <div>
    <h2 style="margin-bottom:16px;">我的训练任务</h2>
    <a-list :data-source="tasks">
      <template #renderItem="{ item }">
        <a-list-item>
          <a-list-item-meta>
            <template #title>{{ item.taskName }}</template>
            <template #description>{{ item.description }}</template>
          </a-list-item-meta>
          <a-button type="primary" @click="router.push(`/patient/training/${item.id}`)">开始训练</a-button>
        </a-list-item>
      </template>
    </a-list>
  </div>
</template>
