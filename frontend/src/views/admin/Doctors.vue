<script setup>
import { onMounted, ref } from 'vue'
import { message } from 'ant-design-vue'
import http from '@/api/index'

const list = ref([])
const load = async () => { list.value = await http.get('/admin/doctors') }

const certify = async (row) => {
  await http.put(`/admin/doctors/${row.id}/certify`)
  message.success('已通过认证')
  await load()
}

const columns = [
  { title: 'ID', dataIndex: 'id', width: 60 },
  { title: '用户名', dataIndex: 'username' },
  { title: '真实姓名', dataIndex: 'realName' },
  { title: '执业证号', dataIndex: 'licenseNo' },
  { title: '医院', dataIndex: 'hospital' },
  { title: '科室', dataIndex: 'department' },
  { title: '职称', dataIndex: 'title' },
  { title: '认证', key: 'certified' },
  { title: '操作', key: 'action' }
]

onMounted(load)
</script>

<template>
  <div>
    <h2 style="margin-bottom:16px;">医生资质审核</h2>
    <a-table :columns="columns" :data-source="list" row-key="id">
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'certified'">
          <a-tag :color="record.certified === 1 ? 'green' : 'orange'">
            {{ record.certified === 1 ? '已认证' : '未认证' }}
          </a-tag>
        </template>
        <template v-else-if="column.key === 'action'">
          <a-button v-if="record.certified !== 1" type="link" @click="certify(record)">通过认证</a-button>
          <span v-else style="color:#999;">已认证</span>
        </template>
      </template>
    </a-table>
  </div>
</template>
