<script setup>
defineProps({
  label: String,
  value: [Number, String],
  suffix: { type: String, default: '' },
  icon: { type: String, default: '' },
  accent: { type: String, default: 'linear-gradient(135deg, #10b7e0 0%, #0066ff 100%)' }
})
</script>

<template>
  <div class="stat-card">
    <div class="accent-bar" :style="{ background: accent }" />
    <div class="label">
      <span v-if="icon" class="icon">{{ icon }}</span>
      {{ label }}
    </div>
    <div class="value" :style="{ background: accent, '-webkit-background-clip': 'text', 'background-clip': 'text' }">
      {{ value }}<span v-if="suffix" class="suffix">{{ suffix }}</span>
    </div>
  </div>
</template>

<style scoped>
.icon { font-size: 18px; }
</style>
