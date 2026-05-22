<script setup>
import { onMounted, onBeforeUnmount, ref, watch } from 'vue'
import * as echarts from 'echarts/core'
import { LineChart, BarChart } from 'echarts/charts'
import {
  TitleComponent, TooltipComponent, GridComponent, LegendComponent
} from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'

echarts.use([LineChart, BarChart, TitleComponent, TooltipComponent, GridComponent, LegendComponent, CanvasRenderer])

const props = defineProps({
  labels: { type: Array, default: () => [] },
  series: { type: Array, default: () => [] }, // [{ name, data, color }]
  type: { type: String, default: 'line' },
  height: { type: String, default: '280px' },
  smooth: { type: Boolean, default: true }
})

const el = ref(null)
let inst = null

const render = () => {
  if (!el.value) return
  if (!inst) inst = echarts.init(el.value)
  inst.setOption({
    grid: { top: 30, right: 20, bottom: 30, left: 40, containLabel: true },
    tooltip: { trigger: 'axis', backgroundColor: 'rgba(0,0,0,0.8)', borderColor: 'transparent', textStyle: { color: '#fff' } },
    legend: { top: 0, textStyle: { color: '#6b7280' } },
    xAxis: {
      type: 'category', data: props.labels, boundaryGap: props.type === 'bar',
      axisLine: { lineStyle: { color: '#e5e7eb' } },
      axisLabel: { color: '#6b7280' }
    },
    yAxis: {
      type: 'value',
      axisLine: { show: false },
      splitLine: { lineStyle: { color: '#f0f2f5' } },
      axisLabel: { color: '#6b7280' }
    },
    series: props.series.map(s => ({
      name: s.name,
      type: props.type,
      data: s.data,
      smooth: props.smooth,
      itemStyle: { color: s.color || '#0066ff' },
      lineStyle: { width: 3 },
      areaStyle: props.type === 'line' ? {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: (s.color || '#0066ff') + '60' },
          { offset: 1, color: (s.color || '#0066ff') + '00' }
        ])
      } : undefined,
      symbol: 'circle', symbolSize: 6
    }))
  }, true)
}

const onResize = () => inst?.resize()

onMounted(() => { render(); window.addEventListener('resize', onResize) })
onBeforeUnmount(() => { window.removeEventListener('resize', onResize); inst?.dispose() })
watch(() => [props.labels, props.series], render, { deep: true })
</script>

<template>
  <div ref="el" :style="{ width: '100%', height }" />
</template>
