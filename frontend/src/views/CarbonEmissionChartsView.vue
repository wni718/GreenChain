<script setup>
import { ref, computed, watch, onMounted, onBeforeUnmount, nextTick } from 'vue'
import * as echarts from 'echarts'
import { useAuth } from '../composables/useAuth'
import { formatErrorBody } from '../utils/apiError'

const { apiAuthHeader, currentUser } = useAuth()

const isLoggedIn = computed(() => Boolean(currentUser.value?.username))

const loading = ref(false)
const message = ref('')
const summary = ref(null)

const lineEl = ref(null)
const pieEl = ref(null)
let lineChart = null
let pieChart = null

async function apiFetch(path, options = {}) {
  const headers = {
    'Content-Type': 'application/json',
    ...apiAuthHeader(),
    ...options.headers,
  }
  return fetch(path, { ...options, headers })
}

function disposeCharts() {
  lineChart?.dispose()
  pieChart?.dispose()
  lineChart = null
  pieChart = null
}

const emptyLineGraphic = {
  type: 'text',
  left: 'center',
  top: 'center',
  style: {
    text:
      '暂无按日数据。请在 Shipment Tracking 新建货运；\n有「发货日期」则按该日期汇总，否则按保存当天汇总。',
    fontSize: 13,
    fill: '#6b7d6b',
    lineHeight: 20,
  },
}

const emptyPieGraphic = {
  type: 'text',
  left: 'center',
  top: 'center',
  style: {
    text: '暂无运输方式分布。保存带运输方式的货运后将显示各方式排放量占比。',
    fontSize: 13,
    fill: '#6b7d6b',
    lineHeight: 20,
  },
}

function applyChartOptions() {
  const s = summary.value
  if (!s || !lineEl.value || !pieEl.value) return

  const dates = (s.emissionsByShipmentDate || []).map((r) => r.date)
  const seriesLine = (s.emissionsByShipmentDate || []).map((r) => r.emissionsKg)

  if (!lineChart) lineChart = echarts.init(lineEl.value)

  if (dates.length === 0) {
    lineChart.setOption(
      {
        color: ['#528951'],
        title: {
          text: 'Emissions by shipment date',
          left: 0,
          textStyle: { fontSize: 14, color: '#3d5340' },
        },
        graphic: [emptyLineGraphic],
        tooltip: { show: false },
        grid: { left: 12, right: 20, top: 52, bottom: 36, containLabel: true },
        xAxis: { type: 'category', data: [], show: false },
        yAxis: { type: 'value', show: false },
        series: [],
      },
      { notMerge: true },
    )
  } else {
    lineChart.setOption(
      {
        graphic: [],
        color: ['#528951'],
        title: {
          text: 'Emissions by shipment date',
          left: 0,
          textStyle: { fontSize: 14, color: '#3d5340' },
        },
        tooltip: { trigger: 'axis', show: true },
        grid: { left: 12, right: 20, top: 52, bottom: 36, containLabel: true },
        xAxis: {
          type: 'category',
          data: dates,
          show: true,
          axisLabel: { rotate: dates.length > 8 ? 35 : 0 },
        },
        yAxis: {
          type: 'value',
          name: 'kg CO2e',
          show: true,
          nameGap: 12,
          axisLabel: { margin: 8 },
        },
        series: [
          { type: 'line', smooth: true, data: seriesLine, areaStyle: { opacity: 0.12 } },
        ],
      },
      { notMerge: true },
    )
  }

  const pieDataRaw = (s.emissionsByTransportMode || []).map((r) => ({
    name: r.transportMode,
    value: Number(r.emissionsKg) || 0,
  }))
  const pieData = pieDataRaw.filter((d) => d.value > 0)

  if (!pieChart) pieChart = echarts.init(pieEl.value)

  if (pieData.length === 0) {
    pieChart.setOption(
      {
        color: ['#528951', '#7daf7c', '#a8c9a6', '#d4e5d3'],
        title: {
          text: 'Share by transport mode',
          left: 0,
          textStyle: { fontSize: 14, color: '#3d5340' },
        },
        graphic: [emptyPieGraphic],
        tooltip: { show: false },
        series: [{ type: 'pie', radius: ['36%', '62%'], data: [] }],
      },
      { notMerge: true },
    )
  } else {
    pieChart.setOption(
      {
        graphic: [],
        color: ['#528951', '#7daf7c', '#a8c9a6', '#d4e5d3'],
        title: {
          text: 'Share by transport mode',
          left: 0,
          textStyle: { fontSize: 14, color: '#3d5340' },
        },
        tooltip: { trigger: 'item', formatter: '{b}: {c} kg ({d}%)', show: true },
        series: [
          {
            type: 'pie',
            radius: ['36%', '62%'],
            avoidLabelOverlap: true,
            label: { color: '#2d4a2c' },
            data: pieData,
          },
        ],
      },
      { notMerge: true },
    )
  }

  lineChart.resize()
  pieChart.resize()
}

function onResize() {
  lineChart?.resize()
  pieChart?.resize()
}

async function loadSummary() {
  if (!isLoggedIn.value) return
  loading.value = true
  message.value = ''
  try {
    const res = await apiFetch('/api/dashboard/summary')
    const text = await res.text()
    if (!res.ok) {
      message.value = formatErrorBody(res.status, text)
      summary.value = null
      disposeCharts()
      return
    }
    summary.value = text ? JSON.parse(text) : null
    await nextTick()
    await nextTick()
    applyChartOptions()
  } catch {
    message.value = 'Cannot reach the server. Check backend and Vite proxy for /api.'
    summary.value = null
    disposeCharts()
  } finally {
    loading.value = false
  }
}

watch(
  isLoggedIn,
  async (loggedIn) => {
    if (loggedIn) {
      await nextTick()
      await loadSummary()
    } else {
      summary.value = null
      message.value = ''
      disposeCharts()
    }
  },
  { immediate: true },
)

onMounted(() => {
  window.addEventListener('resize', onResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', onResize)
  disposeCharts()
})
</script>

<template>
  <div class="page">
    <template v-if="!isLoggedIn">
      <header class="page__head">
        <h1 class="page__title">Carbon Emission Charts</h1>
      </header>
      <p class="guest-notice" role="status">You need to log in to view carbon charts.</p>
    </template>

    <template v-else>
      <header class="page__head">
        <h1 class="page__title">Carbon Emission Charts</h1>
      </header>

      <p v-if="message" class="page__alert" role="status">{{ message }}</p>

      <div class="toolbar">
        <button type="button" class="btn btn--ghost" :disabled="loading" @click="loadSummary">
          {{ loading ? 'Loading…' : 'Refresh' }}
        </button>
      </div>

      <div class="charts">
        <div ref="lineEl" class="chart" aria-label="Line chart of emissions by date" />
        <div ref="pieEl" class="chart" aria-label="Pie chart of emissions by mode" />
      </div>
    </template>
  </div>
</template>

<style scoped>
.page {
  flex: 0 0 auto;
  width: 100%;
  padding: 1.25rem 1.5rem;
  background: #fff;
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.page__title {
  margin: 0 0 0.35rem;
  font-size: 2rem;
  font-weight: 900;
  color: #5f795f;
}

.guest-notice {
  margin: 0;
  font-size: 1.05rem;
  color: #4a5c4a;
}

.page__alert {
  margin: 0;
  padding: 0.65rem 0.9rem;
  border-radius: 6px;
  background: #fef2f2;
  color: #991b1b;
  font-size: 0.9rem;
}

.toolbar {
  display: flex;
  gap: 0.5rem;
}

.btn {
  padding: 0.45rem 1rem;
  border-radius: 6px;
  font-size: 0.9rem;
  font-weight: 650;
  cursor: pointer;
  font-family: inherit;
  border: 1px solid #b5c4b5;
  background: #fff;
  color: #3d5340;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.charts {
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

.chart {
  min-height: 300px;
  border: 1px solid #c5d6c5;
  border-radius: 10px;
  background: #fafcf9;
}
</style>
