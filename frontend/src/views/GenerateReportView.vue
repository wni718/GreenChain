<script setup>
import { ref, computed, watch } from 'vue'
import { useAuth } from '../composables/useAuth'
import { formatErrorBody } from '../utils/apiError'
import jsPDF from 'jspdf'
import html2canvas from 'html2canvas'
import * as echarts from 'echarts'

const { apiAuthHeader, currentUser } = useAuth()

const isLoggedIn = computed(() => Boolean(currentUser.value?.username))

const loading = ref(false)
const message = ref('')
const messageKind = ref('err')

function setMsg(text, kind = 'err') {
  message.value = text
  messageKind.value = kind
}

async function generateChartsHtml(summary, suppliers, shipments, transportModes) {
  // Create a temporary container for charts
  const chartContainer = document.createElement('div')
  chartContainer.style.cssText = `
    position: fixed;
    top: 0;
    left: 0;
    width: 700px;
    height: 300px;
    z-index: 9999;
    background: white;
    opacity: 0.01;
  `
  document.body.appendChild(chartContainer)

  // Create chart elements
  const emissionsChartEl = document.createElement('div')
  emissionsChartEl.style.cssText = `width: 700px; height: 300px;`
  
  const supplierChartEl = document.createElement('div')
  supplierChartEl.style.cssText = `width: 700px; height: 300px;`
  
  const shipmentChartEl = document.createElement('div')
  shipmentChartEl.style.cssText = `width: 700px; height: 300px;`
  
  const trendChartEl = document.createElement('div')
  trendChartEl.style.cssText = `width: 700px; height: 300px;`
  
  const emissionsByDateChartEl = document.createElement('div')
  emissionsByDateChartEl.style.cssText = `width: 700px; height: 300px;`
  
  const emissionsByTransportModeChartEl = document.createElement('div')
  emissionsByTransportModeChartEl.style.cssText = `width: 700px; height: 300px;`

  const transportModeFactorChartEl = document.createElement('div')
  transportModeFactorChartEl.style.cssText = `width: 700px; height: 300px;`

  // Generate charts one by one
  const chartImages = []

  // Carbon Emissions Chart
  chartContainer.appendChild(emissionsChartEl)
  const emissionsChart = echarts.init(emissionsChartEl)
  emissionsChart.setOption({
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
        data: [Number(Number(summary.totalEmissionsKg || 0).toFixed(2))],
        itemStyle: {
          color: '#ff7875'
        }
      },
      {
        name: 'Avoided Emissions',
        type: 'bar',
        data: [Number(Number(summary.estimatedAvoidedEmissionsKg || 0).toFixed(2))],
        itemStyle: {
          color: '#73d13d'
        }
      }
    ]
  })
  emissionsChart.resize()
  await new Promise(resolve => setTimeout(resolve, 500))
  chartImages.push({
    title: 'Carbon Emissions',
    img: emissionsChart.getDataURL({ type: 'png', pixelRatio: 2 })
  })
  emissionsChart.dispose()
  chartContainer.removeChild(emissionsChartEl)

  // Supplier Distribution Chart
  chartContainer.appendChild(supplierChartEl)
  const supplierChart = echarts.init(supplierChartEl)
  supplierChart.setOption({
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
            value: summary.certifiedSupplierCount || 0,
            name: 'Certified'
          },
          {
            value: (summary.registeredSupplierCount || 0) - (summary.certifiedSupplierCount || 0),
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
  })
  supplierChart.resize()
  await new Promise(resolve => setTimeout(resolve, 500))
  chartImages.push({
    title: 'Supplier Distribution',
    img: supplierChart.getDataURL({ type: 'png', pixelRatio: 2 })
  })
  supplierChart.dispose()
  chartContainer.removeChild(supplierChartEl)

  // Shipment & Supply Chain Chart
  chartContainer.appendChild(shipmentChartEl)
  const shipmentChart = echarts.init(shipmentChartEl)
  shipmentChart.setOption({
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
        data: [summary.shipmentCount || 0],
        itemStyle: {
          color: '#409eff'
        }
      },
      {
        name: 'Enterprises',
        type: 'bar',
        data: [summary.enterprisesInSupplyChain || 0],
        itemStyle: {
          color: '#9254de'
        }
      }
    ]
  })
  shipmentChart.resize()
  await new Promise(resolve => setTimeout(resolve, 500))
  chartImages.push({
    title: 'Supply Chain Metrics',
    img: shipmentChart.getDataURL({ type: 'png', pixelRatio: 2 })
  })
  shipmentChart.dispose()
  chartContainer.removeChild(shipmentChartEl)

  // Carbon Emissions Trend Chart - All Years
  chartContainer.appendChild(trendChartEl)
  const trendChart = echarts.init(trendChartEl)
  // Use a different color for All Years chart
  const allYearsColor = '#000000'
  trendChart.setOption({
    title: {
      text: 'Carbon Emissions Trend (All Years)',
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
            { offset: 0, color: allYearsColor + '80' }, // 50% opacity
            { offset: 1, color: allYearsColor + '10' }  // 10% opacity
          ])
        },
        data: summary.monthlyEmissions ? summary.monthlyEmissions.map(val => Number(Number(val).toFixed(2))) : [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0],
        lineStyle: {
          color: allYearsColor
        },
        itemStyle: {
          color: allYearsColor
        }
      }
    ]
  })
  trendChart.resize()
  await new Promise(resolve => setTimeout(resolve, 500))
  chartImages.push({
    title: 'Carbon Emissions Trend (All Years)',
    img: trendChart.getDataURL({ type: 'png', pixelRatio: 2 })
  })
  trendChart.dispose()
  chartContainer.removeChild(trendChartEl)
  
  // Carbon Emissions Trend Chart - By Year
  if (summary.emissionsByYearMonth && summary.emissionsByYearMonth.length > 0) {
    // Color palette for different years
    const colors = ['#ff7875', '#409eff', '#73d13d', '#9254de', '#faad14', '#13c2c2']
    
    // Generate chart for each year
    summary.emissionsByYearMonth.forEach((yearData, index) => {
      const yearTrendChartEl = document.createElement('div')
      yearTrendChartEl.style.cssText = `width: 700px; height: 300px;`
      chartContainer.appendChild(yearTrendChartEl)
      
      const yearTrendChart = echarts.init(yearTrendChartEl)
      const color = colors[index % colors.length]
      
      yearTrendChart.setOption({
        title: {
          text: `Carbon Emissions Trend (${yearData.year})`,
          left: 'center'
        },
        tooltip: {
          trigger: 'axis'
        },
        legend: {
          data: [`${yearData.year} Emissions`],
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
            name: `${yearData.year} Emissions`,
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
            }
          }
        ]
      })
      
      yearTrendChart.resize()
      chartImages.push({
        title: `Carbon Emissions Trend (${yearData.year})`,
        img: yearTrendChart.getDataURL({ type: 'png', pixelRatio: 2 })
      })
      yearTrendChart.dispose()
      chartContainer.removeChild(yearTrendChartEl)
    })
  }

  // Emissions by Shipment Date Chart
  chartContainer.appendChild(emissionsByDateChartEl)
  const emissionsByDateChart = echarts.init(emissionsByDateChartEl)
  // Sort emissions by date in ascending order (oldest to newest)
  const sortedEmissions = [...(summary.emissionsByShipmentDate || [])].sort((a, b) => {
    return new Date(a.date) - new Date(b.date)
  })
  
  const dates = sortedEmissions.map((r) => r.date)
  const seriesLine = sortedEmissions.map((r) => Number(Number(r.emissionsKg).toFixed(2)))

  if (dates.length === 0) {
    emissionsByDateChart.setOption({
      color: ['#528951'],
      title: {
        text: 'Emissions by shipment date',
        left: 0,
        textStyle: { fontSize: 14, color: '#3d5340' },
      },
      graphic: [{
        type: 'text',
        left: 'center',
        top: 'center',
        style: {
          text: '暂无按日数据',
          fontSize: 13,
          fill: '#6b7d6b',
          lineHeight: 20,
        },
      }],
      tooltip: { show: false },
      grid: { left: 12, right: 20, top: 52, bottom: 36, containLabel: true },
      xAxis: { type: 'category', data: [], show: false },
      yAxis: { type: 'value', show: false },
      series: [],
    })
  } else {
    emissionsByDateChart.setOption({
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
    })
  }
  emissionsByDateChart.resize()
  await new Promise(resolve => setTimeout(resolve, 500))
  chartImages.push({
    title: 'Emissions by Shipment Date',
    img: emissionsByDateChart.getDataURL({ type: 'png', pixelRatio: 2 })
  })
  emissionsByDateChart.dispose()
  chartContainer.removeChild(emissionsByDateChartEl)

  // Emissions by Transport Mode Chart
  chartContainer.appendChild(emissionsByTransportModeChartEl)
  const emissionsByTransportModeChart = echarts.init(emissionsByTransportModeChartEl)
  const pieDataRaw = (summary.emissionsByTransportMode || []).map((r) => ({
    name: r.transportMode,
    value: Number(Number(r.emissionsKg).toFixed(2)) || 0,
  }))
  const pieData = pieDataRaw.filter((d) => d.value > 0)

  if (pieData.length === 0) {
    emissionsByTransportModeChart.setOption({
      color: ['#528951', '#7daf7c', '#a8c9a6', '#d4e5d3'],
      title: {
        text: 'Share by transport mode',
        left: 0,
        textStyle: { fontSize: 14, color: '#3d5340' },
      },
      graphic: [{
        type: 'text',
        left: 'center',
        top: 'center',
        style: {
          text: '暂无运输方式分布',
          fontSize: 13,
          fill: '#6b7d6b',
          lineHeight: 20,
        },
      }],
      tooltip: { show: false },
      series: [{ type: 'pie', radius: ['36%', '62%'], data: [] }],
    })
  } else {
    emissionsByTransportModeChart.setOption({
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
    })
  }
  emissionsByTransportModeChart.resize()
  await new Promise(resolve => setTimeout(resolve, 500))
  chartImages.push({
    title: 'Share by Transport Mode',
    img: emissionsByTransportModeChart.getDataURL({ type: 'png', pixelRatio: 2 })
  })
  emissionsByTransportModeChart.dispose()
  chartContainer.removeChild(emissionsByTransportModeChartEl)

  // Transport Mode Emission Factors Chart
  chartContainer.appendChild(transportModeFactorChartEl)
  const transportModeFactorChart = echarts.init(transportModeFactorChartEl)
  
  const transportModeData = (transportModes || []).map(mode => ({
    name: mode.displayName || mode.mode || mode.emissionFactor || 'Unknown',
    value: mode.emissionFactorPerKmPerTon || 0
  }))

  if (transportModeData.length === 0 || transportModeData.every(d => d.value === 0)) {
    transportModeFactorChart.setOption({
      color: ['#528951', '#7daf7c', '#a8c9a6', '#d4e5d3'],
      title: {
        text: 'Transport Mode Emission Factors',
        left: 0,
        textStyle: { fontSize: 14, color: '#3d5340' },
      },
      graphic: [{
        type: 'text',
        left: 'center',
        top: 'center',
        style: {
          text: '暂无运输方式排放因子数据',
          fontSize: 13,
          fill: '#6b7d6b',
          lineHeight: 20,
        },
      }],
      tooltip: { show: false },
      xAxis: { type: 'category', data: [], show: false },
      yAxis: { type: 'value', show: false },
      series: [],
    })
  } else {
    transportModeFactorChart.setOption({
      graphic: [],
      color: ['#528951', '#7daf7c', '#a8c9a6', '#d4e5d3'],
      title: {
        text: 'Transport Mode Emission Factors',
        left: 0,
        textStyle: { fontSize: 14, color: '#3d5340' },
      },
      tooltip: {
        trigger: 'axis',
        formatter: '{b}: {c} kg CO2e/km/ton',
        show: true
      },
      grid: { left: 12, right: 20, top: 52, bottom: 36, containLabel: true },
      xAxis: {
        type: 'category',
        data: transportModeData.map(d => d.name),
        show: true,
        axisLabel: { rotate: transportModeData.length > 4 ? 35 : 0 }
      },
      yAxis: {
        type: 'value',
        name: 'kg CO2e/km/ton',
        show: true,
        nameGap: 12,
        axisLabel: { margin: 20 },
      },
      series: [
        {
          type: 'bar',
          data: transportModeData.map(d => d.value),
          itemStyle: {
            borderRadius: [4, 4, 0, 0]
          }
        }
      ],
    })
  }
  transportModeFactorChart.resize()
  await new Promise(resolve => setTimeout(resolve, 500))
  chartImages.push({
    title: 'Transport Mode Emission Factors',
    img: transportModeFactorChart.getDataURL({ type: 'png', pixelRatio: 2 })
  })
  transportModeFactorChart.dispose()
  chartContainer.removeChild(transportModeFactorChartEl)

  // Remove temporary container
  document.body.removeChild(chartContainer)

  // Generate HTML with charts
  let chartsHtml = `
    <h2 style="color: #5f795f; margin-top: 40px; margin-bottom: 20px;">Charts</h2>
  `

  chartImages.forEach(chart => {
    chartsHtml += `
      <h3 style="color: #5f795f; margin-top: 30px; margin-bottom: 10px;">${chart.title}</h3>
      <div style="text-align: center; margin-bottom: 30px;">
        <img src="${chart.img}" style="max-width: 100%; height: auto;">
      </div>
    `
  })

  return chartsHtml
}

async function apiFetch(path, options = {}) {
  const headers = {
    'Content-Type': 'application/json',
    ...apiAuthHeader(),
    ...options.headers,
  }
  return fetch(path, { ...options, headers })
}

function csvEscape(value) {
  if (value == null) return ''
  const s = String(value)
  if (/[",\n\r]/.test(s)) return `"${s.replace(/"/g, '""')}"`
  return s
}

function downloadBlob(filename, mime, body) {
  const blob = new Blob([body], { type: mime })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = filename
  a.click()
  URL.revokeObjectURL(url)
}

async function exportCsv() {
  if (!isLoggedIn.value) return
  loading.value = true
  message.value = ''
  try {
    const [sumRes, supRes, shipRes] = await Promise.all([
      apiFetch('/api/dashboard/summary'),
      apiFetch('/api/suppliers'),
      apiFetch('/api/shipments'),
    ])

    const sumText = await sumRes.text()
    if (!sumRes.ok) {
      setMsg(formatErrorBody(sumRes.status, sumText))
      return
    }
    const supText = await supRes.text()
    if (!supRes.ok) {
      setMsg(formatErrorBody(supRes.status, supText))
      return
    }
    const shipText = await shipRes.text()
    if (!shipRes.ok) {
      setMsg(formatErrorBody(shipRes.status, shipText))
      return
    }

    const summary = sumText ? JSON.parse(sumText) : {}
    const suppliers = supText ? JSON.parse(supText) : []
    const shipments = shipText ? JSON.parse(shipText) : []

    const lines = []

    lines.push('# GreenChain sustainability report (CSV)')
    lines.push(`# Generated,${csvEscape(new Date().toISOString())}`)
    lines.push('')

    lines.push('## KPI summary')
    lines.push(
      [
        'totalEmissionsKg',
        'estimatedAvoidedEmissionsKg',
        'reductionPercentVsHighestFactorMode',
        'shipmentCount',
        'registeredSupplierCount',
        'certifiedSupplierCount',
        'enterprisesInSupplyChain',
      ].join(','),
    )
    lines.push(
      [
        csvEscape(summary.totalEmissionsKg),
        csvEscape(summary.estimatedAvoidedEmissionsKg),
        csvEscape(summary.reductionPercentVsHighestFactorMode),
        csvEscape(summary.shipmentCount),
        csvEscape(summary.registeredSupplierCount),
        csvEscape(summary.certifiedSupplierCount),
        csvEscape(summary.enterprisesInSupplyChain),
      ].join(','),
    )
    lines.push('')

    lines.push('## Emissions by date')
    lines.push('date,emissionsKg')
    for (const row of summary.emissionsByShipmentDate || []) {
      lines.push([csvEscape(row.date), csvEscape(row.emissionsKg)].join(','))
    }
    lines.push('')

    lines.push('## Emissions by transport mode')
    lines.push('transportMode,emissionsKg')
    for (const row of summary.emissionsByTransportMode || []) {
      lines.push([csvEscape(row.transportMode), csvEscape(row.emissionsKg)].join(','))
    }
    lines.push('')

    lines.push('## Suppliers')
    lines.push('id,name,country,certified,emissionFactorPerUnit,contactEmail')
    for (const s of suppliers) {
      lines.push(
        [
          csvEscape(s.id),
          csvEscape(s.name),
          csvEscape(s.country),
          csvEscape(s.hasEnvironmentalCertification ? 'yes' : 'no'),
          csvEscape(s.emissionFactorPerUnit),
          csvEscape(s.contactEmail),
        ].join(','),
      )
    }
    lines.push('')

    lines.push('## Shipments')
    lines.push(
      'id,supplierId,supplierName,transportMode,distanceKm,cargoWeightTons,origin,destination,shipmentDate,calculatedCarbonEmission',
    )
    for (const sh of shipments) {
      const sid = sh.supplier?.id
      const sname = sh.supplier?.name
      const mode = sh.transportMode?.displayName || sh.transportMode?.mode || ''
      lines.push(
        [
          csvEscape(sh.id),
          csvEscape(sid),
          csvEscape(sname),
          csvEscape(mode),
          csvEscape(sh.distanceKm),
          csvEscape(sh.cargoWeightTons),
          csvEscape(sh.origin),
          csvEscape(sh.destination),
          csvEscape(sh.shipmentDate),
          csvEscape(sh.calculatedCarbonEmission),
        ].join(','),
      )
    }

    const csv = lines.join('\r\n')
    const stamp = new Date().toISOString().slice(0, 19).replace(/[:T]/g, '-')
    downloadBlob(`greenchain-report-${stamp}.csv`, 'text/csv;charset=utf-8', '\uFEFF' + csv)
    setMsg('Report downloaded as CSV.', 'ok')
  } catch {
    setMsg('Export failed. Check network and backend.')
  } finally {
    loading.value = false
  }
}

async function exportPdf() {
  if (!isLoggedIn.value) return
  loading.value = true
  message.value = ''
  try {
    const [sumRes, supRes, shipRes, modeRes] = await Promise.all([
      apiFetch('/api/dashboard/summary'),
      apiFetch('/api/suppliers'),
      apiFetch('/api/shipments'),
      apiFetch('/api/transport-modes'),
    ])

    const sumText = await sumRes.text()
    if (!sumRes.ok) {
      setMsg(formatErrorBody(sumRes.status, sumText))
      return
    }
    const supText = await supRes.text()
    if (!supRes.ok) {
      setMsg(formatErrorBody(supRes.status, supText))
      return
    }
    const shipText = await shipRes.text()
    if (!shipRes.ok) {
      setMsg(formatErrorBody(shipRes.status, shipText))
      return
    }
    const modeText = await modeRes.text()
    const transportModes = modeRes.ok && modeText ? JSON.parse(modeText) : []

    const summary = sumText ? JSON.parse(sumText) : {}
    const suppliers = supText ? JSON.parse(supText) : []
    const shipments = shipText ? JSON.parse(shipText) : []

    // Create PDF report container
    const reportContainer = document.createElement('div')
    reportContainer.style.cssText = `
      width: 800px;
      padding: 40px;
      background: white;
      font-family: Arial, sans-serif;
      color: #333;
    `

    // Add report header
    reportContainer.innerHTML = `
      <div style="text-align: center; margin-bottom: 40px;">
        <h1 style="color: #5f795f; margin-bottom: 10px;">GreenChain Sustainability Report</h1>
        <p style="color: #666;">Generated on ${new Date().toISOString()}</p>
      </div>
      
      <h2 style="color: #5f795f; margin-top: 30px; margin-bottom: 20px;">KPI Summary</h2>
      <table style="width: 100%; border-collapse: collapse; margin-bottom: 30px;">
        <tr style="background: #f8fbf8;">
          <th style="border: 1px solid #c5d6c5; padding: 8px; text-align: left;">Metric</th>
          <th style="border: 1px solid #c5d6c5; padding: 8px; text-align: right;">Value</th>
        </tr>
        <tr>
          <td style="border: 1px solid #c5d6c5; padding: 8px;">Total Emissions (kg CO2e)</td>
          <td style="border: 1px solid #c5d6c5; padding: 8px; text-align: right;">${Number(summary.totalEmissionsKg || 0).toFixed(2)}</td>
        </tr>
        <tr>
          <td style="border: 1px solid #c5d6c5; padding: 8px;">Estimated Avoided Emissions (kg CO2e)</td>
          <td style="border: 1px solid #c5d6c5; padding: 8px; text-align: right;">${Number(summary.estimatedAvoidedEmissionsKg || 0).toFixed(2)}</td>
        </tr>
        <tr>
          <td style="border: 1px solid #c5d6c5; padding: 8px;">Reduction vs Highest Factor Mode (%)</td>
          <td style="border: 1px solid #c5d6c5; padding: 8px; text-align: right;">${Number(summary.reductionPercentVsHighestFactorMode || 0).toFixed(2)}%</td>
        </tr>
        <tr>
          <td style="border: 1px solid #c5d6c5; padding: 8px;">Shipment Count</td>
          <td style="border: 1px solid #c5d6c5; padding: 8px; text-align: right;">${summary.shipmentCount || 0}</td>
        </tr>
        <tr>
          <td style="border: 1px solid #c5d6c5; padding: 8px;">Registered Suppliers</td>
          <td style="border: 1px solid #c5d6c5; padding: 8px; text-align: right;">${summary.registeredSupplierCount || 0}</td>
        </tr>
        <tr>
          <td style="border: 1px solid #c5d6c5; padding: 8px;">Certified Suppliers</td>
          <td style="border: 1px solid #c5d6c5; padding: 8px; text-align: right;">${summary.certifiedSupplierCount || 0}</td>
        </tr>
        <tr>
          <td style="border: 1px solid #c5d6c5; padding: 8px;">Enterprises in Supply Chain</td>
          <td style="border: 1px solid #c5d6c5; padding: 8px; text-align: right;">${summary.enterprisesInSupplyChain || 0}</td>
        </tr>
      </table>
      
      <h2 style="color: #5f795f; margin-top: 30px; margin-bottom: 20px;">Emissions by Transport Mode</h2>
      <table style="width: 100%; border-collapse: collapse; margin-bottom: 30px;">
        <tr style="background: #f8fbf8;">
          <th style="border: 1px solid #c5d6c5; padding: 8px; text-align: left;">Transport Mode</th>
          <th style="border: 1px solid #c5d6c5; padding: 8px; text-align: right;">Emissions (kg CO2e)</th>
        </tr>
        ${(summary.emissionsByTransportMode || []).map(row => `
          <tr>
            <td style="border: 1px solid #c5d6c5; padding: 8px;">${row.transportMode}</td>
            <td style="border: 1px solid #c5d6c5; padding: 8px; text-align: right;">${Number(row.emissionsKg).toFixed(2)}</td>
          </tr>
        `).join('')}
      </table>
      
      <h2 style="color: #5f795f; margin-top: 30px; margin-bottom: 20px;">Top Suppliers</h2>
      <table style="width: 100%; border-collapse: collapse; margin-bottom: 30px;">
        <tr style="background: #f8fbf8;">
          <th style="border: 1px solid #c5d6c5; padding: 8px; text-align: left;">Supplier Name</th>
          <th style="border: 1px solid #c5d6c5; padding: 8px; text-align: left;">Country</th>
          <th style="border: 1px solid #c5d6c5; padding: 8px; text-align: center;">Certified</th>
        </tr>
        ${suppliers.slice(0, 10).map(s => `
          <tr>
            <td style="border: 1px solid #c5d6c5; padding: 8px;">${s.name}</td>
            <td style="border: 1px solid #c5d6c5; padding: 8px;">${s.country}</td>
            <td style="border: 1px solid #c5d6c5; padding: 8px; text-align: center;">${s.hasEnvironmentalCertification ? 'Yes' : 'No'}</td>
          </tr>
        `).join('')}
      </table>
      
      <h2 style="color: #5f795f; margin-top: 30px; margin-bottom: 20px;">Recent Shipments</h2>
      <table style="width: 100%; border-collapse: collapse; margin-bottom: 30px;">
        <tr style="background: #f8fbf8;">
          <th style="border: 1px solid #c5d6c5; padding: 8px; text-align: left;">Supplier</th>
          <th style="border: 1px solid #c5d6c5; padding: 8px; text-align: left;">Transport Mode</th>
          <th style="border: 1px solid #c5d6c5; padding: 8px; text-align: right;">Distance (km)</th>
          <th style="border: 1px solid #c5d6c5; padding: 8px; text-align: right;">Emissions (kg CO2e)</th>
        </tr>
        ${shipments.slice(0, 10).map(sh => {
          const sname = sh.supplier?.name || 'Unknown'
          const mode = sh.transportMode?.displayName || sh.transportMode?.mode || 'Unknown'
          return `
            <tr>
              <td style="border: 1px solid #c5d6c5; padding: 8px;">${sname}</td>
              <td style="border: 1px solid #c5d6c5; padding: 8px;">${mode}</td>
              <td style="border: 1px solid #c5d6c5; padding: 8px; text-align: right;">${Number(sh.distanceKm || 0).toFixed(2)}</td>
              <td style="border: 1px solid #c5d6c5; padding: 8px; text-align: right;">${Number(sh.calculatedCarbonEmission || 0).toFixed(2)}</td>
            </tr>
          `
        }).join('')}
      </table>
    `

    // Create and add charts to the report
    const chartsHtml = await generateChartsHtml(summary, suppliers, shipments, transportModes)
    reportContainer.innerHTML += chartsHtml

    // Append container to body
    document.body.appendChild(reportContainer)

    // Convert to canvas
    const canvas = await html2canvas(reportContainer, {
      scale: 2,
      useCORS: true,
      logging: false
    })

    // Remove temporary container
    document.body.removeChild(reportContainer)

    // Create PDF
    const pdf = new jsPDF('p', 'mm', 'a4')
    const imgData = canvas.toDataURL('image/png')
    const imgWidth = 210
    const imgHeight = canvas.height * imgWidth / canvas.width
    
    let heightLeft = imgHeight
    let position = 0

    // Add first page
    pdf.addImage(imgData, 'PNG', 0, position, imgWidth, imgHeight)
    heightLeft -= 297

    // Add additional pages if needed
    while (heightLeft > 0) {
      position = heightLeft - imgHeight
      pdf.addPage()
      pdf.addImage(imgData, 'PNG', 0, position, imgWidth, imgHeight)
      heightLeft -= 297
    }

    // Download PDF
    const stamp = new Date().toISOString().slice(0, 19).replace(/[:T]/g, '-')
    pdf.save(`greenchain-report-${stamp}.pdf`)
    setMsg('Report downloaded as PDF.', 'ok')
  } catch (error) {
    console.error('PDF export error:', error)
    setMsg('PDF export failed. Check network and backend.')
  } finally {
    loading.value = false
  }
}

watch(isLoggedIn, (loggedIn) => {
  if (!loggedIn) message.value = ''
})
</script>

<template>
  <div class="page">
    <template v-if="!isLoggedIn">
      <header class="page__head">
        <h1 class="page__title">Generate Report</h1>
      </header>
      <p class="guest-notice" role="status">You need to log in to export a report.</p>
    </template>

    <template v-else>
      <header class="page__head">
        <h1 class="page__title">Generate Report</h1>
      </header>

      <p
        v-if="message"
        class="page__alert"
        :class="{ 'page__alert--ok': messageKind === 'ok' }"
        role="status"
      >
        {{ message }}
      </p>

      <div class="panel">
        <div style="display: flex; gap: 1rem; flex-wrap: wrap;">
          <button type="button" class="btn btn--primary" :disabled="loading" @click="exportCsv">
            {{ loading ? 'Preparing…' : 'Download CSV report' }}
          </button>
          <button type="button" class="btn btn--primary" :disabled="loading" @click="exportPdf">
            {{ loading ? 'Preparing…' : 'Download PDF report' }}
          </button>
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

.page__alert--ok {
  background: #ecfdf3;
  color: #166434;
}

.panel {
  max-width: 32rem;
  padding: 1.25rem;
  border-radius: 10px;
  border: 1px solid #c5d6c5;
  background: #f8fbf8;
}

.btn {
  padding: 0.55rem 1.15rem;
  border-radius: 6px;
  font-size: 0.95rem;
  font-weight: 650;
  cursor: pointer;
  font-family: inherit;
  border: 1px solid transparent;
  align-self: flex-start;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn--primary {
  background: #528951;
  color: #fff;
  border-color: #3f6d3e;
}

.btn--primary:hover:not(:disabled) {
  background: #457844;
}

</style>
