<script setup>
import { ref, computed, watch, onMounted, onBeforeUnmount, nextTick } from 'vue'
import * as echarts from 'echarts'
import { useAuth } from '../composables/useAuth'
import { useApiCache } from '../composables/useApiCache'
import { formatErrorBody } from '../utils/apiError'
import {
  getEmissionsByDateOption,
  getEmissionsByTransportModeOption,
  getTransportModeFactorsOption,
} from '../utils/chartConfig'
import SkeletonLoader from '../components/SkeletonLoader.vue'

const { apiAuthHeader, currentUser } = useAuth()
const { get: getCache, set: setCache } = useApiCache()

const isLoggedIn = computed(() => Boolean(currentUser.value?.username))

const loading = ref(false)
const message = ref('')
const summary = ref(null)
const transportModes = ref([])

const lineEl = ref(null)
const pieEl = ref(null)
const transportModeEl = ref(null)
let lineChart = null
let pieChart = null
let transportModeChart = null

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
  transportModeChart?.dispose()
  lineChart = null
  pieChart = null
  transportModeChart = null
}

function applyChartOptions() {
  const s = summary.value
  if (!s || !lineEl.value || !pieEl.value || !transportModeEl.value) return

  if (!lineChart) lineChart = echarts.init(lineEl.value)
  lineChart.setOption(getEmissionsByDateOption(s), { notMerge: true })

  if (!pieChart) pieChart = echarts.init(pieEl.value)
  pieChart.setOption(getEmissionsByTransportModeOption(s), { notMerge: true })

  if (!transportModeChart) transportModeChart = echarts.init(transportModeEl.value)
  transportModeChart.setOption(getTransportModeFactorsOption(transportModes.value), { notMerge: true })

  lineChart.resize()
  pieChart.resize()
  transportModeChart.resize()
}

function onResize() {
  lineChart?.resize()
  pieChart?.resize()
  transportModeChart?.resize()
}

async function loadSummary(useCache = true) {
  if (!isLoggedIn.value) return
  loading.value = true
  message.value = ''

  // Try cache first for summary
  if (useCache) {
    const cachedSummary = getCache('/api/dashboard/summary')
    const cachedTransportModes = getCache('/api/transport-modes')
    if (cachedSummary && cachedTransportModes) {
      summary.value = cachedSummary.data
      transportModes.value = cachedTransportModes.data
      loading.value = false
      await nextTick()
      await nextTick()
      await new Promise(resolve => setTimeout(resolve, 50))
      applyChartOptions()
      return
    }
  }

  try {
    // Fetch dashboard summary and transport modes in parallel
    const [summaryRes, transportModesRes] = await Promise.all([
      apiFetch('/api/dashboard/summary'),
      apiFetch('/api/transport-modes')
    ])

    // Process summary response
    const summaryText = await summaryRes.text()
    if (!summaryRes.ok) {
      message.value = formatErrorBody(summaryRes.status, summaryText)
      summary.value = null
      disposeCharts()
      loading.value = false
      return
    }
    const summaryData = summaryText ? JSON.parse(summaryText) : null
    summary.value = summaryData
    setCache('/api/dashboard/summary', summaryData)

    // Process transport modes response
    const transportModesText = await transportModesRes.text()
    let transportModesData = []
    if (transportModesRes.ok) {
      try {
        transportModesData = transportModesText ? JSON.parse(transportModesText) : []
      } catch {
        transportModesData = []
      }
    }
    transportModes.value = transportModesData
    setCache('/api/transport-modes', transportModesData)

    // Wait for loading to be false so chart containers are rendered
    loading.value = false
    // Give DOM time to update and containers to become visible
    await nextTick()
    await nextTick()
    // Small delay to ensure containers are fully rendered
    await new Promise(resolve => setTimeout(resolve, 50))
    applyChartOptions()
  } catch {
    message.value = 'Cannot reach the server. Check backend and Vite proxy for /api.'
    summary.value = null
    transportModes.value = []
    disposeCharts()
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

      <!-- Loading skeleton -->
      <template v-if="loading">
        <SkeletonLoader type="chart" />
        <div class="charts-row">
          <SkeletonLoader type="chart" />
          <SkeletonLoader type="chart" />
        </div>
      </template>

      <!-- Charts -->
      <template v-else>
        <div class="charts">
          <div ref="lineEl" class="chart" aria-label="Line chart of emissions by date" />
          <div class="charts-row">
            <div ref="pieEl" class="chart" aria-label="Pie chart of emissions by mode" />
            <div ref="transportModeEl" class="chart" aria-label="Bar chart of transport mode emission factors" />
          </div>
        </div>
      </template>
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

.charts-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1.25rem;
}

@media (max-width: 768px) {
  .charts-row {
    grid-template-columns: 1fr;
  }
}

.chart {
  min-height: 300px;
  border: 1px solid #c5d6c5;
  border-radius: 10px;
  background: #fafcf9;
}
</style>
