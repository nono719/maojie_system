<script setup>
import { onMounted, ref } from 'vue'
import { message, Modal } from 'ant-design-vue'
import http from '@/api/index'

const list = ref([])
const load = async () => { list.value = await http.get('/admin/doctors') }

const certify = async (row) => {
  await http.put(`/admin/doctors/${row.id}/certify`)
  message.success('已通过认证，账户已启用')
  await load()
}

const reject = (row) => {
  Modal.confirm({
    title: '驳回医生申请？',
    content: `确认驳回「${row.realName || row.username}」的医生认证申请？账户将被停用。`,
    okText: '确认驳回', okType: 'danger', cancelText: '取消',
    onOk: async () => {
      await http.put(`/admin/doctors/${row.id}/reject`)
      message.success('已驳回')
      await load()
    }
  })
}

const certifyTag = (c) => {
  if (c === 1) return { color: 'green', text: '已认证' }
  if (c === -1) return { color: 'red', text: '已驳回' }
  return { color: 'orange', text: '待审核' }
}

const columns = [
  { title: 'ID', dataIndex: 'id', width: 60 },
  { title: '用户名', dataIndex: 'username' },
  { title: '真实姓名', dataIndex: 'realName' },
  { title: '执业证号', dataIndex: 'licenseNo' },
  { title: '医院', dataIndex: 'hospital' },
  { title: '科室', dataIndex: 'department' },
  { title: '职称', dataIndex: 'title' },
  { title: '认证状态', key: 'certified', width: 100 },
  { title: '操作', key: 'action', width: 180 }
]

onMounted(load)
</script>

<template>
  <div class="page-container">
    <div class="page-title">医生资质审核</div>
    <div class="section-card">
      <a-table :columns="columns" :data-source="list" row-key="id">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'certified'">
            <a-tag :color="certifyTag(record.certified).color">
              {{ certifyTag(record.certified).text }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space v-if="record.certified === 0 || record.certified === -1">
              <a-button v-if="record.certified !== 1" type="link" size="small" @click="certify(record)">
                通过认证
              </a-button>
              <a-button v-if="record.certified === 0" type="link" size="small" danger @click="reject(record)">
                驳回
              </a-button>
            </a-space>
            <span v-else style="color:#999;">已认证</span>
          </template>
        </template>
      </a-table>
    </div>
  </div>
</template>
