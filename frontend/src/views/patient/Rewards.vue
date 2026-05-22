<script setup>
import { onMounted, ref } from 'vue'
import { myRewards } from '@/api/training'
import dayjs from 'dayjs'

const list = ref([])

const columns = [
  { title: '时间', dataIndex: 'createTime', customRender: ({ text }) => dayjs(text).format('YYYY-MM-DD HH:mm') },
  { title: '任务ID', dataIndex: 'taskId' },
  { title: '奖励数量', dataIndex: 'amount', customRender: ({ text }) => `${text} BREATH` },
  { title: '状态', dataIndex: 'status' },
  { title: '链上交易', dataIndex: 'txHash', customRender: ({ text }) => text || '-' }
]

onMounted(async () => { list.value = await myRewards() })
</script>

<template>
  <div>
    <h2 style="margin-bottom:16px;">奖励记录</h2>
    <a-table :columns="columns" :data-source="list" row-key="id" />
  </div>
</template>
