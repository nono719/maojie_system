<script setup>
import { onMounted, ref } from 'vue'
import http from '@/api/index'

const info = ref({ chainId: null, blockNumber: null, breathTokenAddress: '', trainingRecordAddress: '', serviceAccount: '' })
onMounted(async () => { info.value = await http.get('/admin/chain-info') })
</script>

<template>
  <div>
    <h2 style="margin-bottom:16px;">区块链状态</h2>
    <a-descriptions bordered :column="1" size="middle">
      <a-descriptions-item label="ChainID">{{ info.chainId }}</a-descriptions-item>
      <a-descriptions-item label="当前区块号">{{ info.blockNumber }}</a-descriptions-item>
      <a-descriptions-item label="BreathToken 合约">
        <code>{{ info.breathTokenAddress }}</code>
      </a-descriptions-item>
      <a-descriptions-item label="TrainingRecord 合约">
        <code>{{ info.trainingRecordAddress }}</code>
      </a-descriptions-item>
      <a-descriptions-item label="后端服务账户">
        <code>{{ info.serviceAccount }}</code>
      </a-descriptions-item>
    </a-descriptions>
  </div>
</template>
