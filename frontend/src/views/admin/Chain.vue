<script setup>
import { onMounted, ref } from 'vue'
import http from '@/api/index'

const info = ref({ chainId: null, blockNumber: null, breathTokenAddress: '', trainingRecordAddress: '', serviceAccount: '' })
onMounted(async () => { info.value = await http.get('/admin/chain-info') })
</script>

<template>
  <div class="page-container">
    <div style="display:flex;align-items:center;gap:12px;margin-bottom:16px;">
      <h2 style="margin:0;">联盟链状态</h2>
      <a-tag color="blue">FISCO BCOS 联盟链</a-tag>
      <a-tag color="cyan">可信存证网络</a-tag>
    </div>
    <a-alert
      type="info"
      show-icon
      style="margin-bottom:16px;"
      message="当前系统使用 FISCO BCOS 联盟链"
      description="训练记录仅把 Keccak-256 数据哈希写入联盟链，原始训练数据保存在链下数据库；前端看到的“链上真实哈希”来自联盟链合约实时查询。"
    />
    <a-descriptions bordered :column="1" size="middle">
      <a-descriptions-item label="链类型">联盟链（FISCO BCOS）</a-descriptions-item>
      <a-descriptions-item label="ChainID">{{ info.chainId }}</a-descriptions-item>
      <a-descriptions-item label="当前联盟链区块号">{{ info.blockNumber }}</a-descriptions-item>
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
