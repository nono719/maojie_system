<script setup>
import { computed } from 'vue'

const props = defineProps({
  inhale: { type: Number, default: 4 },
  hold:   { type: Number, default: 7 },
  exhale: { type: Number, default: 8 },
  keep:   { type: Number, default: 0 }
})
const emit = defineEmits(['update:inhale', 'update:hold', 'update:exhale', 'update:keep'])

const cycle = computed(() => props.inhale + props.hold + props.exhale + props.keep)
const phases = computed(() => ([
  { key: 'inhale', label: '吸气', icon: '🌬️↑', color: '#10b7e0', value: props.inhale, min: 1 },
  { key: 'hold',   label: '屏息', icon: '⏸',   color: '#5ad2c1', value: props.hold,   min: 0 },
  { key: 'exhale', label: '呼气', icon: '💨↓', color: '#0066ff', value: props.exhale, min: 1 },
  { key: 'keep',   label: '保持', icon: '🧘',  color: '#7c83fd', value: props.keep,   min: 0 }
]))
</script>

<template>
  <div class="phase-editor">
    <div class="cycle-preview">
      <div class="cycle-bar">
        <div v-for="p in phases" :key="p.key"
          v-show="p.value > 0"
          class="cycle-seg"
          :style="{ flex: p.value, background: p.color }">
          <span class="seg-label">{{ p.label }} {{ p.value }}s</span>
        </div>
      </div>
      <div class="cycle-meta">单次循环：<strong>{{ cycle }}</strong> 秒</div>
    </div>

    <div class="phase-grid">
      <div v-for="p in phases" :key="p.key" class="phase-box" :style="{ borderTopColor: p.color }">
        <div class="phase-head">
          <span class="phase-icon">{{ p.icon }}</span>
          <span class="phase-name">{{ p.label }}</span>
        </div>
        <a-input-number
          :value="p.value"
          @update:value="v => emit(`update:${p.key}`, v ?? p.min)"
          :min="p.min" :max="60"
          addon-after="秒"
          style="width: 100%;"
        />
      </div>
    </div>
  </div>
</template>

<style scoped>
.phase-editor { display: flex; flex-direction: column; gap: 14px; }
.cycle-preview {
  background: #f5f7fa; border-radius: 10px; padding: 14px; text-align: center;
}
.cycle-bar {
  display: flex; height: 36px; border-radius: 8px; overflow: hidden;
  box-shadow: 0 2px 6px rgba(0,0,0,0.06);
}
.cycle-seg {
  color: #fff; display: flex; align-items: center; justify-content: center;
  font-size: 12px; font-weight: 600; min-width: 50px;
  transition: flex .3s ease;
}
.seg-label { white-space: nowrap; text-shadow: 0 1px 2px rgba(0,0,0,0.2); }
.cycle-meta { margin-top: 8px; font-size: 13px; color: #6b7280; }
.cycle-meta strong { color: #0066ff; font-size: 16px; margin: 0 2px; }

.phase-grid {
  display: grid; grid-template-columns: repeat(4, 1fr); gap: 10px;
}
.phase-box {
  background: #fff; border-radius: 10px;
  border: 1px solid #e5e7eb;
  border-top: 3px solid;
  padding: 12px;
}
.phase-head { display: flex; align-items: center; gap: 6px; margin-bottom: 8px; }
.phase-icon { font-size: 16px; }
.phase-name { font-weight: 600; font-size: 13px; color: var(--color-text); }
</style>
