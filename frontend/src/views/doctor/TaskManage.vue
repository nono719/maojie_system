<script setup>
import { onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import { createTask, publishTask, myTasks } from '@/api/task'

const tasks = ref([])
const visible = ref(false)
const form = reactive({
  taskName: '', description: '',
  inhaleSeconds: 4, holdSeconds: 7, exhaleSeconds: 8, keepSeconds: 0,
  duration: 600, dailyTimes: 3, rewardAmount: 10
})

const load = async () => { tasks.value = await myTasks() }

const onCreate = async () => {
  await createTask(form)
  message.success('任务已创建（草稿）')
  visible.value = false
  await load()
}

const onPublish = async (id) => {
  await publishTask(id)
  message.success('已发布')
  await load()
}

const columns = [
  { title: '任务名', dataIndex: 'taskName' },
  { title: '吸/屏/呼/保', customRender: ({ record }) =>
    `${record.inhaleSeconds}/${record.holdSeconds}/${record.exhaleSeconds}/${record.keepSeconds}` },
  { title: '单次(秒)', dataIndex: 'duration' },
  { title: '每日次数', dataIndex: 'dailyTimes' },
  { title: '奖励', dataIndex: 'rewardAmount' },
  { title: '状态', dataIndex: 'status' },
  { title: '操作', key: 'action' }
]

onMounted(load)
</script>

<template>
  <div>
    <div style="display:flex;justify-content:space-between;margin-bottom:16px;">
      <h2>任务管理</h2>
      <a-button type="primary" @click="visible = true">新建任务</a-button>
    </div>
    <a-table :columns="columns" :data-source="tasks" row-key="id">
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'action'">
          <a-button v-if="record.status === 'DRAFT'" type="link" @click="onPublish(record.id)">发布</a-button>
          <a-tag v-else color="green">已发布</a-tag>
        </template>
      </template>
    </a-table>

    <a-modal v-model:open="visible" title="新建训练任务" @ok="onCreate" ok-text="创建" cancel-text="取消">
      <a-form layout="vertical">
        <a-form-item label="任务名"><a-input v-model:value="form.taskName" /></a-form-item>
        <a-form-item label="描述"><a-textarea v-model:value="form.description" :rows="2" /></a-form-item>
        <a-row :gutter="12">
          <a-col :span="6"><a-form-item label="吸气(秒)"><a-input-number v-model:value="form.inhaleSeconds" :min="1" style="width:100%" /></a-form-item></a-col>
          <a-col :span="6"><a-form-item label="屏息(秒)"><a-input-number v-model:value="form.holdSeconds" :min="0" style="width:100%" /></a-form-item></a-col>
          <a-col :span="6"><a-form-item label="呼气(秒)"><a-input-number v-model:value="form.exhaleSeconds" :min="1" style="width:100%" /></a-form-item></a-col>
          <a-col :span="6"><a-form-item label="保持(秒)"><a-input-number v-model:value="form.keepSeconds" :min="0" style="width:100%" /></a-form-item></a-col>
        </a-row>
        <a-row :gutter="12">
          <a-col :span="8"><a-form-item label="单次(秒)"><a-input-number v-model:value="form.duration" :min="30" style="width:100%" /></a-form-item></a-col>
          <a-col :span="8"><a-form-item label="每日次数"><a-input-number v-model:value="form.dailyTimes" :min="1" style="width:100%" /></a-form-item></a-col>
          <a-col :span="8"><a-form-item label="奖励数量"><a-input-number v-model:value="form.rewardAmount" :min="0" :step="0.1" style="width:100%" /></a-form-item></a-col>
        </a-row>
      </a-form>
    </a-modal>
  </div>
</template>
