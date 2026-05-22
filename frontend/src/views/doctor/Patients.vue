<script setup>
import { onMounted, ref } from 'vue'
import { myPatients } from '@/api/training'
import { verifyOnChain } from '@/api/training'
import { message } from 'ant-design-vue'

const patients = ref([])

const columns = [
  { title: 'ID', dataIndex: 'id', width: 60 },
  { title: '用户名', dataIndex: 'username' },
  { title: '真实姓名', dataIndex: 'realName' },
  { title: '手机号', dataIndex: 'phone' },
  { title: '钱包地址', dataIndex: 'walletAddress' }
]

const verify = async (id) => {
  const r = await verifyOnChain(id)
  message[r.verified ? 'success' : 'error'](r.verified ? '记录未被篡改' : '哈希校验失败')
}

onMounted(async () => { patients.value = await myPatients() })
</script>

<template>
  <div>
    <h2 style="margin-bottom:16px;">我的患者</h2>
    <a-table :columns="columns" :data-source="patients" row-key="id" />
  </div>
</template>
