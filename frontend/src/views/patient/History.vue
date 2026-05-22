<script setup>
import { onMounted, ref } from 'vue'
import { myHistory } from '@/api/training'
import dayjs from 'dayjs'

const list = ref([])

const columns = [
  { title: '时间', dataIndex: 'createTime', customRender: ({ text }) => dayjs(text).format('YYYY-MM-DD HH:mm') },
  { title: '任务ID', dataIndex: 'taskId' },
  { title: '时长(秒)', dataIndex: 'duration' },
  { title: '呼吸次数', dataIndex: 'breathCount' },
  { title: '完成率', dataIndex: 'completionRate', customRender: ({ text }) => `${text}%` },
  { title: '评分', dataIndex: 'score' },
  { title: '上链状态', dataIndex: 'chainStatus' },
  { title: '链上交易', dataIndex: 'blockTxId',
    customRender: ({ text }) => text ? `${text.slice(0, 8)}...${text.slice(-6)}` : '-'
  }
]

onMounted(async () => { list.value = await myHistory() })
</script>

<template>
  <div>
    <h2 style="margin-bottom:16px;">训练历史</h2>
    <a-table :columns="columns" :data-source="list" row-key="id" />
  </div>
</template>
