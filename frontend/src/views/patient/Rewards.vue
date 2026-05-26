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
  { title: '联盟链交易哈希', dataIndex: 'txHash', width: 420 }
]

onMounted(async () => { list.value = await myRewards() })
</script>

<template>
  <div>
    <h2 style="margin-bottom:16px;">奖励记录</h2>
    <a-alert
      type="info"
      show-icon
      style="margin-bottom:16px;"
      message="奖励发放基于 FISCO BCOS 联盟链"
      description="下表中的联盟链交易哈希对应代币奖励交易，可用于审计奖励是否已经写入联盟链。"
    />
    <a-table :columns="columns" :data-source="list" row-key="id" :scroll="{ x: 1000 }">
      <template #bodyCell="{ column, text }">
        <template v-if="column.dataIndex === 'txHash'">
          <code v-if="text" style="font-size:12px;word-break:break-all;white-space:normal;">{{ text }}</code>
          <span v-else>-</span>
        </template>
      </template>
    </a-table>
  </div>
</template>
