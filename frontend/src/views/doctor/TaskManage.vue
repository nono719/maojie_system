<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import { createTask, publishTask, myTasks } from '@/api/task'
import http from '@/api/index'
import BreathPhaseEditor from '@/components/BreathPhaseEditor.vue'

const tasks = ref([])
const patients = ref([])
const visible = ref(false)
const assignVisible = ref(false)
const assignTaskId = ref(null)
const assignTargets = ref([])

const form = reactive({
  taskName: '', description: '',
  inhaleSeconds: 4, holdSeconds: 7, exhaleSeconds: 8, keepSeconds: 0,
  duration: 600, dailyTimes: 3, rewardAmount: 10
})

const cycleSeconds = computed(() => form.inhaleSeconds + form.holdSeconds + form.exhaleSeconds + form.keepSeconds)
const cycleCount = computed(() => cycleSeconds.value > 0 ? Math.floor(form.duration / cycleSeconds.value) : 0)

const load = async () => {
  tasks.value = await myTasks()
  patients.value = await http.get('/doctor/patients')
}

const resetForm = () => {
  Object.assign(form, {
    taskName: '', description: '',
    inhaleSeconds: 4, holdSeconds: 7, exhaleSeconds: 8, keepSeconds: 0,
    duration: 600, dailyTimes: 3, rewardAmount: 10
  })
}

const onCreate = async () => {
  if (!form.taskName) { message.warning('请输入任务名'); return }
  await createTask(form)
  message.success('任务已创建（草稿状态）')
  visible.value = false
  resetForm()
  await load()
}

const onPublish = async (id) => {
  await publishTask(id)
  message.success('已发布')
  await load()
}

const openAssign = (taskId) => {
  assignTaskId.value = taskId
  assignTargets.value = []
  assignVisible.value = true
}

const submitAssign = async () => {
  if (!assignTargets.value.length) { message.warning('请选择至少一位患者'); return }
  for (const pid of assignTargets.value) {
    try { await http.post(`/tasks/${assignTaskId.value}/assign/${pid}`) } catch (_) {}
  }
  message.success('分配完成')
  assignVisible.value = false
}

const statusColor = { DRAFT: 'orange', PUBLISHED: 'green', ARCHIVED: 'default' }
const statusLabel = { DRAFT: '草稿', PUBLISHED: '已发布', ARCHIVED: '已归档' }

const columns = [
  { title: '任务名', dataIndex: 'taskName' },
  { title: '吸/屏/呼/保', key: 'phases', width: 140 },
  { title: '单次(秒)', dataIndex: 'duration', width: 90 },
  { title: '每日次数', dataIndex: 'dailyTimes', width: 90 },
  { title: '奖励(BREATH)', dataIndex: 'rewardAmount', width: 130 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '操作', key: 'action', width: 180 }
]

onMounted(load)
</script>

<template>
  <div class="page-container">
    <div class="page-title">
      任务管理
      <a-button type="primary" style="margin-left:auto;" @click="visible = true">➕ 新建任务</a-button>
    </div>

    <div class="section-card">
      <a-table :columns="columns" :data-source="tasks" row-key="id">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'phases'">
            <span style="color:#10b7e0;">{{ record.inhaleSeconds }}</span> /
            <span>{{ record.holdSeconds }}</span> /
            <span style="color:#0066ff;">{{ record.exhaleSeconds }}</span> /
            <span style="color:#7c83fd;">{{ record.keepSeconds }}</span>
          </template>
          <template v-else-if="column.key === 'status'">
            <a-tag :color="statusColor[record.status]">{{ statusLabel[record.status] }}</a-tag>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button v-if="record.status === 'DRAFT'" type="link" size="small" @click="onPublish(record.id)">
                发布
              </a-button>
              <a-button v-if="record.status === 'PUBLISHED'" type="link" size="small" @click="openAssign(record.id)">
                分配给患者
              </a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </div>

    <!-- 新建任务 -->
    <a-modal v-model:open="visible" :width="720" title="新建呼吸训练任务" ok-text="创建" cancel-text="取消" @ok="onCreate">
      <a-form layout="vertical">
        <a-row :gutter="16">
          <a-col :span="14">
            <a-form-item label="任务名称" required>
              <a-input v-model:value="form.taskName" placeholder="例如：腹式呼吸基础训练" size="large" />
            </a-form-item>
          </a-col>
          <a-col :span="10">
            <a-form-item label="奖励数量">
              <a-input-number v-model:value="form.rewardAmount" :min="0" :step="0.5"
                              addon-after="BREATH" style="width:100%" size="large" />
            </a-form-item>
          </a-col>
        </a-row>

        <a-form-item label="任务描述">
          <a-textarea v-model:value="form.description" :rows="2"
                      placeholder="给患者的指引，例如：适用于慢阻肺患者居家康复 - 478呼吸法变体" />
        </a-form-item>

        <a-divider style="margin: 12px 0;"><span style="color:#888;font-size:13px;">呼吸阶段配置</span></a-divider>

        <BreathPhaseEditor
          v-model:inhale="form.inhaleSeconds"
          v-model:hold="form.holdSeconds"
          v-model:exhale="form.exhaleSeconds"
          v-model:keep="form.keepSeconds"
        />

        <a-divider style="margin: 16px 0;"><span style="color:#888;font-size:13px;">训练强度</span></a-divider>

        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="单次训练时长">
              <a-input-number v-model:value="form.duration" :min="30" :step="30"
                              addon-after="秒" style="width:100%" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="每日完成次数">
              <a-input-number v-model:value="form.dailyTimes" :min="1" :max="20"
                              addon-after="次" style="width:100%" />
            </a-form-item>
          </a-col>
          <a-col :span="24" v-if="cycleCount > 0">
            <a-alert :message="`本次训练约可完成 ${cycleCount} 个完整呼吸循环（每循环 ${cycleSeconds}s）`"
                     type="info" show-icon style="margin-top:4px;" />
          </a-col>
        </a-row>
      </a-form>
    </a-modal>

    <!-- 分配给患者 -->
    <a-modal v-model:open="assignVisible" title="分配训练任务" ok-text="确认分配" cancel-text="取消" @ok="submitAssign">
      <a-form layout="vertical">
        <a-form-item label="选择患者（可多选）">
          <a-select v-model:value="assignTargets" mode="multiple" placeholder="选择要分配的患者">
            <a-select-option v-for="p in patients" :key="p.id" :value="p.id">
              {{ p.realName || p.username }}
              <span style="color:#999;font-size:12px;margin-left:8px;">{{ p.username }}</span>
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-alert v-if="!patients.length" type="warning" show-icon
                 message="您目前还没有绑定的患者。请提醒患者注册时选择您为主治医生。" />
      </a-form>
    </a-modal>
  </div>
</template>
