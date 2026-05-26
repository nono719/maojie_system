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
  { title: '联盟链状态', dataIndex: 'chainStatus' },
  { title: '联盟链交易哈希', dataIndex: 'blockTxId', width: 420 }
]

onMounted(async () => { list.value = await myHistory() })
</script>

<template>
  <div>
    <h2 style="margin-bottom:16px;">训练历史</h2>
    <a-alert
      type="info"
      show-icon
      style="margin-bottom:16px;"
      message="当前训练记录已写入 FISCO BCOS 联盟链"
      description="下表中的联盟链交易哈希对应训练记录存证交易，可用于管理员端进一步校验链上真实哈希。"
    />
    <a-table :columns="columns" :data-source="list" row-key="id" :scroll="{ x: 1200 }">
      <template #bodyCell="{ column, text }">
        <template v-if="column.dataIndex === 'blockTxId'">
          <code v-if="text" style="font-size:12px;word-break:break-all;white-space:normal;">{{ text }}</code>
          <span v-else>-</span>
        </template>
      </template>
    </a-table>
  </div>
</template>
