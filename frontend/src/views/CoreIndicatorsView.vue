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
const historyAnalysis = ref(null)
const emissionsChart = ref(null)
const supplierChart = ref(null)
const shipmentChart = ref(null)
const trendChart = ref(null)
const historyChart = ref(null)

// Get available years from data
const availableYears = computed(() => {
  if (!summary.value?.emissionsByYearMonth) return []
  return summary.value.emissionsByYearMonth.map(item => item.year).sort()
})

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
    
    // Color palette for different years
    const colors = ['#ff7875', '#409eff', '#73d13d', '#9254de', '#faad14', '#13c2c2']
    
    // Generate series based on selected years
    const series = []
    const legendData = []
    
    // Always show total emissions (All Years)
    legendData.push('All Years Emissions')
    series.push({
      name: 'All Years Emissions',
      type: 'line',
      stack: 'Total',
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(0, 0, 0, 0.5)' },
          { offset: 1, color: 'rgba(0, 0, 0, 0.1)' }
        ])
      },
      data: summary.value.monthlyEmissions ? summary.value.monthlyEmissions.map(val => Number(Number(val).toFixed(2))) : [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0],
      lineStyle: {
        color: '#000000'
      },
      itemStyle: {
        color: '#000000'
      },
      tooltip: {
        formatter: function(params) {
          return params.marker + params.seriesName + ': ' + Number(params.value).toFixed(2) + ' kg CO2e'
        }
      }
    })
    
    // Show all years by default
    const yearsToShow = availableYears.value
    
    yearsToShow.forEach((year, index) => {
      const yearData = summary.value.emissionsByYearMonth?.find(item => item.year === year)
      if (yearData) {
        const color = colors[index % colors.length]
        legendData.push(`${year} Emissions`)
        series.push({
          name: `${year} Emissions`,
          type: 'line',
          stack: 'Total',
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: color + '80' }, // 50% opacity
              { offset: 1, color: color + '10' }  // 10% opacity
            ])
          },
          data: yearData.monthlyEmissions ? yearData.monthlyEmissions.map(val => Number(Number(val).toFixed(2))) : [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0],
          lineStyle: {
            color: color
          },
          itemStyle: {
            color: color
          },
          tooltip: {
            formatter: function(params) {
              return params.marker + params.seriesName + ': ' + Number(params.value).toFixed(2) + ' kg CO2e'
            }
          }
        })
      }
    })
    
    const option = {
      title: {
        text: 'Carbon Emissions Trend',
        left: 'center'
      },
      tooltip: {
        trigger: 'axis'
      },
      legend: {
        data: legendData,
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
      series: series
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

async function loadHistoryAnalysis() {
  if (!isLoggedIn.value) return
  try {
    const res = await apiFetch('/api/recommend/history-analysis')
    const text = await res.text()
    if (!res.ok) {
      console.error('Failed to load history analysis:', formatErrorBody(res.status, text))
      historyAnalysis.value = null
      return
    }
    const data = text ? JSON.parse(text) : null
    historyAnalysis.value = data
    // Wait for next tick and a bit more for DOM to be ready
    await nextTick()
    setTimeout(() => {
      initHistoryChart()
    }, 100)
  } catch (err) {
    console.error('Failed to load history analysis:', err)
    historyAnalysis.value = null
  }
}

function initHistoryChart() {
  if (!historyAnalysis.value || !historyAnalysis.value.total_shipments || historyAnalysis.value.total_shipments === 0) {
    return
  }
  
  if (!historyChart.value) {
    return
  }
  
  const existingChart = echarts.getInstanceByDom(historyChart.value)
  if (existingChart) {
    existingChart.dispose()
  }
  
  const chart = echarts.init(historyChart.value)
  const analysis = historyAnalysis.value
  
  const option = {
    title: {
      text: 'History Analysis Overview',
      left: 'center'
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      },
      formatter: '{b}: {c}'
    },
    grid: {
      left: '20%',
      right: '20%',
      bottom: '15%',
      top: '15%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: ['Total Shipments', 'Potential Savings %']
    },
    yAxis: {
      type: 'value',
      axisLabel: {
        formatter: '{value}'
      }
    },
    series: [
      {
        name: 'History Analysis',
        type: 'bar',
        barWidth: '40%',
        data: [
          {
            value: Math.round(analysis.total_shipments || 0),
            itemStyle: {
              color: '#5470c6'
            }
          },
          {
            value: Math.round((analysis.potential_savings_percent || 0) * 100) / 100,
            itemStyle: {
              color: '#91cc75'
            }
          }
        ],
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        },
        label: {
          show: true,
          position: 'top',
          formatter: function(params) {
            if (params.dataIndex === 0) {
              return params.value
            } else {
              return params.value + '%'
            }
          }
        }
      }
    ]
  }
  chart.setOption(option)
}

watch(
  isLoggedIn,
  (loggedIn) => {
    if (loggedIn) {
      loadSummary()
      loadHistoryAnalysis()
    }
    else {
      summary.value = null
      historyAnalysis.value = null
      message.value = ''
    }
  },
  { immediate: true },
)

onMounted(() => {
  // Reinitialize charts on window resize
  window.addEventListener('resize', () => {
    const chartInstances = [
      { ref: emissionsChart.value, name: 'emissions' },
      { ref: supplierChart.value, name: 'supplier' },
      { ref: shipmentChart.value, name: 'shipment' },
      { ref: trendChart.value, name: 'trend' },
      { ref: historyChart.value, name: 'history' }
    ]
    
    chartInstances.forEach(({ ref, name }) => {
      if (ref) {
        const instance = echarts.getInstanceByDom(ref)
        if (instance) {
          instance.resize()
        }
      }
    })
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
          
          <!-- History Analysis Section -->
          <div v-if="historyAnalysis" class="history-analysis-section" :class="{ 'history-analysis-section--empty': !historyAnalysis.total_shipments }">
            <h3 class="subsection-title">History Analysis & Smart Recommendations</h3>
            <div class="history-kpi-row">
              <div class="kpi-card-mini">
                <h4 class="kpi-card-mini__label">Total Shipments</h4>
                <p class="kpi-card-mini__value">{{ historyAnalysis.total_shipments }}</p>
              </div>
              <div class="kpi-card-mini">
                <h4 class="kpi-card-mini__label">Total Carbon Emission</h4>
                <p class="kpi-card-mini__value">{{ Number(historyAnalysis.total_carbon_emission).toFixed(2) }} <span class="unit-sm">kg CO2e</span></p>
              </div>
              <div class="kpi-card-mini">
                <h4 class="kpi-card-mini__label">Average Emission/Shipment</h4>
                <p class="kpi-card-mini__value">{{ Number(historyAnalysis.average_emission_per_shipment).toFixed(2) }} <span class="unit-sm">kg CO2e</span></p>
              </div>
              <div class="kpi-card-mini">
                <h4 class="kpi-card-mini__label">Most Used Mode</h4>
                <p class="kpi-card-mini__value">{{ historyAnalysis.most_used_transport_mode }}</p>
              </div>
              <div class="kpi-card-mini">
                <h4 class="kpi-card-mini__label">Lowest Carbon Mode</h4>
                <p class="kpi-card-mini__value">{{ historyAnalysis.lowest_carbon_transport_mode }}</p>
              </div>
            </div>
            
            <div class="history-savings">
              <div class="savings-card">
                <h4 class="savings-card__title">Potential Savings</h4>
                <p class="savings-card__percent">{{ Number(historyAnalysis.potential_savings_percent).toFixed(1) }}%</p>
                <p class="savings-card__amount">({{ Number(historyAnalysis.potential_savings_amount).toFixed(2) }} kg CO2e)</p>
              </div>
              <div class="recommendation-card">
                <h4 class="recommendation-card__title">Recommendation</h4>
                <p class="recommendation-card__text">{{ historyAnalysis.recommendation }}</p>
              </div>
            </div>
            
            <div v-if="historyAnalysis.total_shipments > 0" class="history-chart-container">
              <div ref="historyChart" class="chart-container-sm"></div>
            </div>
          </div>
          
          <div class="charts-grid">
            <div class="chart-card">
              <div ref="emissionsChart" class="chart-container"></div>
            </div>
            <div class="chart-card">
              <div ref="supplierChart" class="chart-container"></div>
            </div>
            <div class="chart-card">
              <div ref="shipmentChart" class="chart-container"></div>
            </div>
            <div class="chart-card">
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
  grid-template-columns: repeat(2, 1fr);
  gap: 1.5rem;
  max-width: 1200px;
  margin: 0 auto;
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

@media (max-width: 992px) {
  .charts-grid {
    grid-template-columns: 1fr;
  }
}

/* Year Selection Styles */
.year-selection {
  margin-bottom: 1.5rem;
  padding: 1rem;
  border-radius: 10px;
  border: 1px solid #c5d6c5;
  background: #f8fbf8;
}

.year-selection__title {
  margin: 0 0 0.75rem;
  font-size: 1rem;
  font-weight: 700;
  color: #5f795f;
}

.year-selection__controls {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 0.75rem;
}

.btn--sm {
  padding: 0.25rem 0.75rem;
  font-size: 0.8rem;
}

.year-selection__tags {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.year-tag {
  padding: 0.35rem 0.75rem;
  border-radius: 20px;
  font-size: 0.85rem;
  font-weight: 600;
  cursor: pointer;
  background: #fff;
  border: 1px solid #b5c4b5;
  color: #3d5340;
  transition: all 0.2s ease;
}

.year-tag:hover {
  background: #f4faf4;
  border-color: #5f795f;
}

.year-tag--selected {
  background: #5f795f;
  color: #fff;
  border-color: #5f795f;
}

.year-tag--selected:hover {
  background: #4a5c4a;
  border-color: #4a5c4a;
}

/* History Analysis Styles */
.history-analysis-section {
  background: linear-gradient(160deg, #f0f7f0 0%, #e4ede4 100%);
  border: 1px solid #b5d4b5;
  border-radius: 12px;
  padding: 1.5rem;
  margin-bottom: 1.5rem;
}

.history-analysis-section--empty {
  padding: 1.5rem;
}

.subsection-title {
  margin: 0 0 1rem;
  font-size: 1.2rem;
  font-weight: 700;
  color: #3d5340;
}

.history-kpi-row {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.kpi-card-mini {
  background: #fff;
  border: 1px solid #c5d6c5;
  border-radius: 8px;
  padding: 1rem;
  text-align: center;
}

.kpi-card-mini__label {
  margin: 0 0 0.5rem;
  font-size: 0.75rem;
  font-weight: 700;
  text-transform: uppercase;
  color: #5a6b5a;
}

.kpi-card-mini__value {
  margin: 0;
  font-size: 1.2rem;
  font-weight: 900;
  color: #2d4a2c;
}

.unit-sm {
  font-size: 0.5em;
  font-weight: 700;
  color: #5f795f;
}

.history-savings {
  display: grid;
  grid-template-columns: 1fr 2fr;
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.savings-card {
  background: linear-gradient(135deg, #73d13d 0%, #52c41a 100%);
  border-radius: 8px;
  padding: 1.25rem;
  text-align: center;
  color: #fff;
}

.savings-card__title {
  margin: 0 0 0.5rem;
  font-size: 0.85rem;
  font-weight: 600;
  color: #fff;
}

.savings-card__percent {
  margin: 0 0 0.25rem;
  font-size: 2rem;
  font-weight: 900;
  color: #fff;
}

.savings-card__amount {
  margin: 0;
  font-size: 0.85rem;
  color: #fff;
}

.recommendation-card {
  background: #fff;
  border: 1px solid #c5d6c5;
  border-radius: 8px;
  padding: 1.25rem;
}

.recommendation-card__title {
  margin: 0 0 0.5rem;
  font-size: 0.85rem;
  font-weight: 700;
  color: #3d5340;
}

.recommendation-card__text {
  margin: 0;
  font-size: 0.9rem;
  color: #5a6b5a;
  line-height: 1.5;
}

.history-chart-container {
  margin-top: 1rem;
}

.chart-container-sm {
  width: 100%;
  height: 250px;
}
</style>
