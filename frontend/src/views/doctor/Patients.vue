<script setup>
import { onMounted, ref } from 'vue'
import { message } from 'ant-design-vue'
import { myPatients, verifyOnChain } from '@/api/training'
import http from '@/api/index'
import dayjs from 'dayjs'

const patients = ref([])
const drawerOpen = ref(false)
const detail = ref(null)
const loadingDetail = ref(false)

const openDetail = async (row) => {
  drawerOpen.value = true
  detail.value = null
  loadingDetail.value = true
  try {
    detail.value = await http.get(`/doctor/patients/${row.id}/detail`)
  } finally {
    loadingDetail.value = false
  }
}

const verifyRecord = async (id) => {
  const r = await verifyOnChain(id)
  message[r.verified ? 'success' : 'error'](r.verified ? '链上哈希一致' : '哈希校验失败')
}

const columns = [
  { title: 'ID', dataIndex: 'id', width: 60 },
  { title: '用户名', dataIndex: 'username' },
  { title: '真实姓名', dataIndex: 'realName' },
  { title: '手机号', dataIndex: 'phone' },
  { title: '钱包地址', dataIndex: 'walletAddress',
    customRender: ({ text }) => text ? `${text.slice(0,8)}...${text.slice(-6)}` : '-' },
  { title: '操作', key: 'action', width: 120 }
]

const recordColumns = [
  { title: '时间', dataIndex: 'createTime',
    customRender: ({ text }) => dayjs(text).format('MM-DD HH:mm') },
  { title: '完成率', dataIndex: 'completionRate', customRender: ({ text }) => `${text}%` },
  { title: '评分', dataIndex: 'score' },
  { title: '上链', dataIndex: 'chainStatus',
    customRender: ({ text }) => text === 'SUCCESS' ? '✅' : text === 'FAILED' ? '❌' : '⏳' },
  { title: '操作', key: 'verify', width: 80 }
]

onMounted(async () => { patients.value = await myPatients() })
</script>

<template>
  <div class="page-container">
    <div class="page-title">我的患者</div>

    <div class="section-card">
      <a-table :columns="columns" :data-source="patients" row-key="id">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'action'">
            <a-button type="link" size="small" @click="openDetail(record)">查看详情</a-button>
          </template>
        </template>
      </a-table>
      <a-empty v-if="!patients.length" description="还没有绑定的患者 — 请提醒患者注册时选择您为主治医生" />
    </div>

    <a-drawer v-model:open="drawerOpen" :width="640" title="患者详情">
      <a-spin :spinning="loadingDetail">
        <template v-if="detail">
          <a-descriptions :column="2" bordered size="small" style="margin-bottom: 16px;">
            <a-descriptions-item label="用户名">{{ detail.user.username }}</a-descriptions-item>
            <a-descriptions-item label="真实姓名">{{ detail.user.realName || '-' }}</a-descriptions-item>
            <a-descriptions-item label="手机号">{{ detail.user.phone || '-' }}</a-descriptions-item>
            <a-descriptions-item label="邮箱">{{ detail.user.email || '-' }}</a-descriptions-item>
            <a-descriptions-item label="钱包地址" :span="2">
              <code v-if="detail.user.walletAddress">{{ detail.user.walletAddress }}</code>
              <span v-else style="color:#999;">未绑定</span>
            </a-descriptions-item>
            <a-descriptions-item label="最近活跃" :span="2">
              {{ detail.lastActive ? dayjs(detail.lastActive).format('YYYY-MM-DD HH:mm') : '从未训练' }}
            </a-descriptions-item>
          </a-descriptions>

          <div class="stat-grid" style="margin-bottom: 16px;">
            <a-card size="small">
              <a-statistic title="累计训练" :value="detail.totalTrainings" suffix="次" />
            </a-card>
            <a-card size="small">
              <a-statistic title="平均完成率" :value="detail.avgCompletion" suffix="%" />
            </a-card>
            <a-card size="small">
              <a-statistic title="累计奖励" :value="detail.totalReward" suffix=" BREATH" />
            </a-card>
          </div>

          <div class="section-card-title" style="margin: 16px 0 8px;">最近 10 次训练</div>
          <a-table :columns="recordColumns" :data-source="detail.recentRecords" row-key="id"
                   size="small" :pagination="false">
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'verify'">
                <a-button type="link" size="small" @click="verifyRecord(record.id)" :disabled="!record.dataHash">
                  验证
                </a-button>
              </template>
            </template>
          </a-table>
        </template>
      </a-spin>
    </a-drawer>
  </div>
</template>
