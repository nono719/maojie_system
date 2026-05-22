<script setup>
import { onMounted, ref } from 'vue'
import { message } from 'ant-design-vue'
import http from '@/api/index'
import { verifyOnChain } from '@/api/training'
import dayjs from 'dayjs'

const list = ref([])
const detail = ref(null)
const drawerOpen = ref(false)
const loadingDetail = ref(false)

const load = async () => { list.value = await http.get('/admin/training-records') }

const openDetail = async (row) => {
  drawerOpen.value = true
  detail.value = null
  loadingDetail.value = true
  try {
    detail.value = await http.get(`/admin/training-records/${row.id}/detail`)
  } finally {
    loadingDetail.value = false
  }
}

const verify = async (id) => {
  const r = await verifyOnChain(id)
  message[r.verified ? 'success' : 'error'](r.verified ? '链上哈希一致' : '哈希校验失败 — 数据已被篡改')
}

const copy = (text) => {
  if (!text) return
  navigator.clipboard?.writeText(text)
  message.success('已复制')
}

const columns = [
  { title: '记录ID', dataIndex: 'id', width: 80 },
  { title: '用户', key: 'user', width: 130 },
  { title: '任务', dataIndex: 'taskName' },
  { title: '完成率', dataIndex: 'completionRate', width: 90,
    customRender: ({ text }) => text != null ? `${text}%` : '-' },
  { title: '评分', dataIndex: 'score', width: 70 },
  { title: '上链', dataIndex: 'chainStatus', key: 'chainStatus', width: 80 },
  { title: '哈希', dataIndex: 'dataHash', key: 'hash', width: 140 },
  { title: '时间', dataIndex: 'createTime', width: 140,
    customRender: ({ text }) => dayjs(text).format('YYYY-MM-DD HH:mm') },
  { title: '操作', key: 'action', width: 160, fixed: 'right' }
]

onMounted(load)
</script>

<template>
  <div class="page-container">
    <div class="page-title">训练记录审计</div>

    <div class="section-card">
      <a-table :columns="columns" :data-source="list" row-key="id" :pagination="{ pageSize: 20 }" :scroll="{ x: 1200 }">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'user'">
            <div>
              <div style="font-weight:600;">{{ record.userRealName || record.username }}</div>
              <div style="color:#999;font-size:12px;">@{{ record.username }}</div>
            </div>
          </template>
          <template v-else-if="column.key === 'chainStatus'">
            <a-tag :color="record.chainStatus === 'SUCCESS' ? 'green' : record.chainStatus === 'FAILED' ? 'red' : 'orange'">
              {{ record.chainStatus }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'hash'">
            <code v-if="record.dataHash" style="font-size:12px;">{{ record.dataHash.slice(0,10) }}…{{ record.dataHash.slice(-6) }}</code>
            <span v-else style="color:#bbb;">—</span>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="openDetail(record)">详情</a-button>
              <a-button type="link" size="small" @click="verify(record.id)" :disabled="!record.dataHash">链上校验</a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </div>

    <a-drawer v-model:open="drawerOpen" :width="720" title="训练记录详情">
      <a-spin :spinning="loadingDetail">
        <template v-if="detail">
          <div class="section-card-title">用户 & 任务</div>
          <a-descriptions :column="2" bordered size="small" style="margin-bottom: 16px;">
            <a-descriptions-item label="用户ID">{{ detail.record.userId }}</a-descriptions-item>
            <a-descriptions-item label="用户名">{{ detail.user?.username }}</a-descriptions-item>
            <a-descriptions-item label="真实姓名">{{ detail.user?.realName || '-' }}</a-descriptions-item>
            <a-descriptions-item label="手机号">{{ detail.user?.phone || '-' }}</a-descriptions-item>
            <a-descriptions-item label="任务ID">{{ detail.record.taskId }}</a-descriptions-item>
            <a-descriptions-item label="任务名">{{ detail.task?.taskName }}</a-descriptions-item>
            <a-descriptions-item :span="2" label="任务描述">{{ detail.task?.description || '-' }}</a-descriptions-item>
            <a-descriptions-item label="呼吸阶段">
              吸 {{ detail.task?.inhaleSeconds }}s / 屏 {{ detail.task?.holdSeconds }}s / 呼 {{ detail.task?.exhaleSeconds }}s / 保 {{ detail.task?.keepSeconds }}s
            </a-descriptions-item>
            <a-descriptions-item label="任务奖励">{{ detail.task?.rewardAmount }} BREATH</a-descriptions-item>
          </a-descriptions>

          <div class="section-card-title">训练数据</div>
          <a-descriptions :column="2" bordered size="small" style="margin-bottom: 16px;">
            <a-descriptions-item label="实际时长">{{ detail.record.duration }} 秒</a-descriptions-item>
            <a-descriptions-item label="呼吸次数">{{ detail.record.breathCount ?? '-' }}</a-descriptions-item>
            <a-descriptions-item label="完成率">{{ detail.record.completionRate }}%</a-descriptions-item>
            <a-descriptions-item label="评分">{{ detail.record.score }}</a-descriptions-item>
            <a-descriptions-item label="平均心率">{{ detail.record.heartRate ?? '-' }}</a-descriptions-item>
            <a-descriptions-item label="完成时间">
              {{ dayjs(detail.record.createTime).format('YYYY-MM-DD HH:mm:ss') }}
            </a-descriptions-item>
          </a-descriptions>

          <div class="section-card-title">
            链上信息
            <a-tag v-if="detail.chainVerified === true" color="green" style="margin-left:8px;">✅ 链上一致</a-tag>
            <a-tag v-else-if="detail.chainVerified === false" color="red" style="margin-left:8px;">❌ 哈希不匹配</a-tag>
            <a-tag v-else color="default" style="margin-left:8px;">链不可用</a-tag>
          </div>
          <a-descriptions :column="1" bordered size="small">
            <a-descriptions-item label="上链状态">
              <a-tag :color="detail.record.chainStatus === 'SUCCESS' ? 'green' : detail.record.chainStatus === 'FAILED' ? 'red' : 'orange'">
                {{ detail.record.chainStatus }}
              </a-tag>
            </a-descriptions-item>
            <a-descriptions-item label="数据哈希 (Keccak-256)">
              <div style="display:flex;align-items:center;gap:8px;">
                <code style="font-size:12px;word-break:break-all;flex:1;">{{ detail.record.dataHash || '—' }}</code>
                <a-button v-if="detail.record.dataHash" size="small" @click="copy(detail.record.dataHash)">复制</a-button>
              </div>
            </a-descriptions-item>
            <a-descriptions-item label="区块链交易哈希">
              <div style="display:flex;align-items:center;gap:8px;">
                <code style="font-size:12px;word-break:break-all;flex:1;">{{ detail.record.blockTxId || '—' }}</code>
                <a-button v-if="detail.record.blockTxId" size="small" @click="copy(detail.record.blockTxId)">复制</a-button>
              </div>
            </a-descriptions-item>
          </a-descriptions>
        </template>
      </a-spin>
    </a-drawer>
  </div>
</template>
