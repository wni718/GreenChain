<script setup>
import { ref, computed, watch, onMounted, nextTick } from 'vue'
import { useAuth } from '../composables/useAuth'
import { formatErrorBody } from '../utils/apiError'
import * as echarts from 'echarts'

const { apiAuthHeader, currentUser } = useAuth()

const isLoggedIn = computed(() => Boolean(currentUser.value?.username))

const loading = ref(false)
const message = ref('')
const messageKind = ref('err')

const summary = ref(null)
const emissionsChart = ref(null)
const supplierChart = ref(null)
const shipmentChart = ref(null)
const trendChart = ref(null)

function setMsg(text, kind = 'err') {
  message.value = text
  messageKind.value = kind
}

async function apiFetch(path, options = {}) {
  const headers = {
    'Content-Type': 'application/json',
    ...apiAuthHeader(),
    ...options.headers,
  }
  return fetch(path, { ...options, headers })
}

function initCharts() {
  if (!summary.value) return
  
  // Carbon Emissions Chart
  if (emissionsChart.value) {
    const chart = echarts.init(emissionsChart.value)
    const option = {
      title: {
        text: 'Carbon Emissions',
        left: 'center'
      },
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'shadow'
        }
      },
      legend: {
        data: ['Total Emissions', 'Avoided Emissions'],
        bottom: 10
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '15%',
        top: '15%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        data: ['Emissions (kg CO2e)']
      },
      yAxis: {
        type: 'value'
      },
      series: [
        {
          name: 'Total Emissions',
          type: 'bar',
          data: [Number(summary.value.totalEmissionsKg).toFixed(2)],
          itemStyle: {
            color: '#ff7875'
          }
        },
        {
          name: 'Avoided Emissions',
          type: 'bar',
          data: [Number(summary.value.estimatedAvoidedEmissionsKg).toFixed(2)],
          itemStyle: {
            color: '#73d13d'
          }
        }
      ]
    }
    chart.setOption(option)
  }
  
  // Supplier Distribution Chart
  if (supplierChart.value) {
    const chart = echarts.init(supplierChart.value)
    const option = {
      title: {
        text: 'Supplier Distribution',
        left: 'center'
      },
      tooltip: {
        trigger: 'item'
      },
      legend: {
        orient: 'vertical',
        left: 'left',
        bottom: 10
      },
      series: [
        {
          name: 'Suppliers',
          type: 'pie',
          radius: '60%',
          center: ['50%', '50%'],
          data: [
            {
              value: summary.value.certifiedSupplierCount,
              name: 'Certified'
            },
            {
              value: summary.value.registeredSupplierCount - summary.value.certifiedSupplierCount,
              name: 'Non-Certified'
            }
          ],
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.5)'
            }
          }
        }
      ]
    }
    chart.setOption(option)
  }
  
  // Shipment & Supply Chain Chart
  if (shipmentChart.value) {
    const chart = echarts.init(shipmentChart.value)
    const option = {
      title: {
        text: 'Supply Chain Metrics',
        left: 'center'
      },
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'shadow'
        }
      },
      legend: {
        data: ['Shipments', 'Enterprises'],
        bottom: 10
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '15%',
        top: '15%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        data: ['Count']
      },
      yAxis: {
        type: 'value'
      },
      series: [
        {
          name: 'Shipments',
          type: 'bar',
          data: [summary.value.shipmentCount],
          itemStyle: {
            color: '#409eff'
          }
        },
        {
          name: 'Enterprises',
          type: 'bar',
          data: [summary.value.enterprisesInSupplyChain],
          itemStyle: {
            color: '#9254de'
          }
        }
      ]
    }
    chart.setOption(option)
  }
  
  // Carbon Emissions Trend Chart
  if (trendChart.value) {
    const chart = echarts.init(trendChart.value)
    const option = {
      title: {
        text: 'Carbon Emissions Trend',
        left: 'center'
      },
      tooltip: {
        trigger: 'axis'
      },
      legend: {
        data: ['Monthly Emissions'],
        bottom: 10
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '15%',
        top: '15%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        boundaryGap: false,
        data: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec']
      },
      yAxis: {
        type: 'value',
        name: 'kg CO2e'
      },
      series: [
        {
          name: 'Monthly Emissions',
          type: 'line',
          stack: 'Total',
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(255, 120, 117, 0.5)' },
              { offset: 1, color: 'rgba(255, 120, 117, 0.1)' }
            ])
          },
          data: [2800, 3200, 2900, 3500, 4200, 3800, 4500, 4100, 3900, 3600, 3300, 3000],
          lineStyle: {
            color: '#ff7875'
          },
          itemStyle: {
            color: '#ff7875'
          }
        }
      ]
    }
    chart.setOption(option)
  }
}

async function loadSummary() {
  if (!isLoggedIn.value) return
  loading.value = true
  message.value = ''
  try {
    const res = await apiFetch('/api/dashboard/summary')
    const text = await res.text()
    if (!res.ok) {
      setMsg(formatErrorBody(res.status, text))
      summary.value = null
      return
    }
    summary.value = text ? JSON.parse(text) : null
    // Initialize charts after data is loaded
    await nextTick()
    initCharts()
  } catch {
    setMsg(
      'Cannot reach the server. Start the backend and run the frontend with npm run dev (proxy /api).',
    )
    summary.value = null
  } finally {
    loading.value = false
  }
}

watch(
  isLoggedIn,
  (loggedIn) => {
    if (loggedIn) loadSummary()
    else {
      summary.value = null
      message.value = ''
    }
  },
  { immediate: true },
)

onMounted(() => {
  // Reinitialize charts on window resize
  window.addEventListener('resize', () => {
    if (emissionsChart.value) {
      echarts.init(emissionsChart.value).resize()
    }
    if (supplierChart.value) {
      echarts.init(supplierChart.value).resize()
    }
    if (shipmentChart.value) {
      echarts.init(shipmentChart.value).resize()
    }
    if (trendChart.value) {
      echarts.init(trendChart.value).resize()
    }
  })
})
</script>

<template>
  <div class="page">
    <template v-if="!isLoggedIn">
      <header class="page__head">
        <h1 class="page__title">Core indicators</h1>
      </header>
      <p class="guest-notice" role="status">You need to log in to view core sustainability indicators.</p>
    </template>

    <template v-else>
      <header class="page__head">
        <h1 class="page__title">Core indicators</h1>
      </header>

      <p
        v-if="message"
        class="page__alert"
        :class="{ 'page__alert--ok': messageKind === 'ok' }"
        role="status"
      >
        {{ message }}
      </p>

      <div class="toolbar">
        <button type="button" class="btn btn--ghost" :disabled="loading" @click="loadSummary">
          {{ loading ? 'Loading…' : 'Refresh' }}
        </button>
      </div>

      <div v-if="summary" class="content-container">
        <!-- KPI Cards -->
        <div class="kpi-container">
          <!-- First row: 3 items -->
          <div class="kpi-row" role="list">
            <article class="kpi-card" role="listitem">
              <h2 class="kpi-card__label">Total carbon emissions</h2>
              <p class="kpi-card__value">{{ Number(summary.totalEmissionsKg).toFixed(2) }} <span class="unit">{{ summary.emissionsUnit }}</span></p>
            </article>
            <article class="kpi-card" role="listitem">
              <h2 class="kpi-card__label">Estimated avoided emissions</h2>
              <p class="kpi-card__value">{{ Number(summary.estimatedAvoidedEmissionsKg).toFixed(2) }} <span class="unit">kg CO2e</span></p>
              <p class="kpi-card__hint">vs assuming every shipment used the highest emission factor among configured modes</p>
            </article>
            <article class="kpi-card" role="listitem">
              <h2 class="kpi-card__label">Reduction vs worst-mode baseline</h2>
              <p class="kpi-card__value">{{ Number(summary.reductionPercentVsHighestFactorMode).toFixed(2) }}<span class="unit">%</span></p>
            </article>
          </div>
          
          <!-- Second row: 4 items -->
          <div class="kpi-row kpi-row--four" role="list">
            <article class="kpi-card" role="listitem">
              <h2 class="kpi-card__label">Shipments recorded</h2>
              <p class="kpi-card__value">{{ summary.shipmentCount }}</p>
            </article>
            <article class="kpi-card" role="listitem">
              <h2 class="kpi-card__label">Registered suppliers</h2>
              <p class="kpi-card__value">{{ summary.registeredSupplierCount }}</p>
            </article>
            <article class="kpi-card" role="listitem">
              <h2 class="kpi-card__label">Certified suppliers</h2>
              <p class="kpi-card__value">{{ summary.certifiedSupplierCount }}</p>
            </article>
            <article class="kpi-card" role="listitem">
              <h2 class="kpi-card__label">Non-certified suppliers</h2>
              <p class="kpi-card__value">{{ summary.registeredSupplierCount - summary.certifiedSupplierCount }}</p>
            </article>
          </div>
          
          <!-- Third row: 1 item -->
          <div class="kpi-row kpi-row--one" role="list">
            <article class="kpi-card kpi-card--full" role="listitem">
              <h2 class="kpi-card__label">Enterprises in active supply chain</h2>
              <p class="kpi-card__value">{{ summary.enterprisesInSupplyChain }}</p>
              <p class="kpi-card__hint">Distinct suppliers linked to at least one shipment</p>
            </article>
          </div>
        </div>
        
        <!-- Charts Section -->
        <div class="charts-container">
          <h2 class="section-title">Sustainability Insights</h2>
          <div class="charts-grid">
            <div class="chart-card">
              <div ref="emissionsChart" class="chart-container"></div>
            </div>
            <div class="chart-card">
              <div ref="supplierChart" class="chart-container"></div>
            </div>
            <div class="chart-card chart-card--wide">
              <div ref="shipmentChart" class="chart-container"></div>
            </div>
            <div class="chart-card chart-card--wide">
              <div ref="trendChart" class="chart-container"></div>
            </div>
          </div>
        </div>
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

.page__head {
  margin: 0;
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
  line-height: 1.5;
}

.page__alert {
  margin: 0;
  padding: 0.65rem 0.9rem;
  border-radius: 6px;
  background: #fef2f2;
  color: #991b1b;
  font-size: 0.9rem;
}

.page__alert--ok {
  background: #ecfdf3;
  color: #166434;
}

.toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.btn {
  padding: 0.45rem 1rem;
  border-radius: 6px;
  font-size: 0.9rem;
  font-weight: 650;
  cursor: pointer;
  font-family: inherit;
  border: 1px solid transparent;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn--ghost {
  background: #fff;
  color: #3d5340;
  border-color: #b5c4b5;
}

.btn--ghost:hover:not(:disabled) {
  background: #f4faf4;
}

.kpi-container {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  margin-top: 1rem;
  margin-bottom: 2rem;
}

.kpi-row {
  display: flex;
  gap: 1rem;
  width: 100%;
}

.kpi-row .kpi-card {
  flex: 1;
  margin: 0;
  padding: 1rem 1.1rem;
  border-radius: 10px;
  border: 1px solid #c5d6c5;
  background: linear-gradient(160deg, #f8fbf8 0%, #eef4ee 100%);
}

.kpi-card--full {
  width: 100%;
}

.kpi-card__label {
  margin: 0 0 0.5rem;
  font-size: 0.8rem;
  font-weight: 750;
  text-transform: uppercase;
  letter-spacing: 0.04em;
  color: #5a6b5a;
}

.kpi-card__value {
  margin: 0;
  font-size: 1.65rem;
  font-weight: 900;
  color: #2d4a2c;
}

.kpi-card__hint {
  margin: 0.5rem 0 0;
  font-size: 0.8rem;
  color: #6b7d6b;
  line-height: 1.4;
}

.unit {
  font-size: 0.55em;
  font-weight: 700;
  color: #5f795f;
}

.content-container {
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

.charts-container {
  margin-top: 1rem;
}

.section-title {
  margin: 0 0 1rem;
  font-size: 1.5rem;
  font-weight: 800;
  color: #5f795f;
}

.charts-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
  gap: 1.5rem;
}

.chart-card {
  padding: 1.5rem;
  border-radius: 10px;
  border: 1px solid #c5d6c5;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.chart-card--wide {
  grid-column: 1 / -1;
}

.chart-container {
  width: 100%;
  height: 300px;
}

@media (max-width: 768px) {
  .charts-grid {
    grid-template-columns: 1fr;
  }
  
  .chart-card--wide {
    grid-column: 1;
  }
  
  .chart-container {
    height: 250px;
  }
}
</style>
