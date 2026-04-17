import * as echarts from 'echarts'

const MONTHS = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec']

const YEAR_COLORS = ['#ff7875', '#409eff', '#73d13d', '#9254de', '#faad14', '#13c2c6']

export function getCarbonEmissionsOption(summary) {
  return {
    title: { text: 'Carbon Emissions', left: 'center' },
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    legend: { data: ['Total Emissions', 'Avoided Emissions'], bottom: 10 },
    grid: { left: '3%', right: '4%', bottom: '15%', top: '15%', containLabel: true },
    xAxis: { type: 'category', data: ['Emissions (kg CO2e)'] },
    yAxis: { type: 'value' },
    series: [
      {
        name: 'Total Emissions',
        type: 'bar',
        data: [Number(Number(summary.totalEmissionsKg || 0).toFixed(2))],
        itemStyle: { color: '#ff7875' },
      },
      {
        name: 'Avoided Emissions',
        type: 'bar',
        data: [Number(Number(summary.estimatedAvoidedEmissionsKg || 0).toFixed(2))],
        itemStyle: { color: '#73d13d' },
      },
    ],
  }
}

export function getSupplierDistributionOption(summary) {
  return {
    title: { text: 'Supplier Distribution', left: 'center' },
    tooltip: { trigger: 'item' },
    legend: { orient: 'vertical', left: 'left', bottom: 10 },
    series: [
      {
        name: 'Suppliers',
        type: 'pie',
        radius: '60%',
        center: ['50%', '50%'],
        data: [
          { value: summary.certifiedSupplierCount || 0, name: 'Certified' },
          {
            value: (summary.registeredSupplierCount || 0) - (summary.certifiedSupplierCount || 0),
            name: 'Non-Certified',
          },
        ],
        emphasis: {
          itemStyle: { shadowBlur: 10, shadowOffsetX: 0, shadowColor: 'rgba(0, 0, 0, 0.5)' },
        },
      },
    ],
  }
}

export function getSupplyChainMetricsOption(summary) {
  return {
    title: { text: 'Supply Chain Metrics', left: 'center' },
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    legend: { data: ['Shipments', 'Enterprises'], bottom: 10 },
    grid: { left: '3%', right: '4%', bottom: '15%', top: '15%', containLabel: true },
    xAxis: { type: 'category', data: ['Count'] },
    yAxis: { type: 'value' },
    series: [
      {
        name: 'Shipments',
        type: 'bar',
        data: [summary.shipmentCount || 0],
        itemStyle: { color: '#409eff' },
      },
      {
        name: 'Enterprises',
        type: 'bar',
        data: [summary.enterprisesInSupplyChain || 0],
        itemStyle: { color: '#9254de' },
      },
    ],
  }
}

export function getCarbonEmissionsTrendOption(summary, availableYears) {
  const series = []
  const legendData = []

  legendData.push('All Years Emissions')
  series.push({
    name: 'All Years Emissions',
    type: 'line',
    stack: 'Total',
    areaStyle: {
      color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
        { offset: 0, color: 'rgba(0, 0, 0, 0.5)' },
        { offset: 1, color: 'rgba(0, 0, 0, 0.1)' },
      ]),
    },
    data: summary.monthlyEmissions
      ? summary.monthlyEmissions.map((val) => Number(Number(val).toFixed(2)))
      : Array(12).fill(0),
    lineStyle: { color: '#000000' },
    itemStyle: { color: '#000000' },
    tooltip: {
      formatter: (params) => `${params.marker}${params.seriesName}: ${Number(params.value).toFixed(2)} kg CO2e`,
    },
  })

  ;(availableYears || []).forEach((year, index) => {
    const yearData = summary.emissionsByYearMonth?.find((item) => item.year === year)
    if (yearData) {
      const color = YEAR_COLORS[index % YEAR_COLORS.length]
      legendData.push(`${year} Emissions`)
      series.push({
        name: `${year} Emissions`,
        type: 'line',
        stack: 'Total',
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: color + '80' },
            { offset: 1, color: color + '10' },
          ]),
        },
        data: yearData.monthlyEmissions
          ? yearData.monthlyEmissions.map((val) => Number(Number(val).toFixed(2)))
          : Array(12).fill(0),
        lineStyle: { color: color },
        itemStyle: { color: color },
        tooltip: {
          formatter: (params) => `${params.marker}${params.seriesName}: ${Number(params.value).toFixed(2)} kg CO2e`,
        },
      })
    }
  })

  return {
    title: { text: 'Carbon Emissions Trend', left: 'center' },
    tooltip: { trigger: 'axis' },
    legend: { data: legendData, bottom: 10 },
    grid: { left: '3%', right: '4%', bottom: '15%', top: '15%', containLabel: true },
    xAxis: { type: 'category', boundaryGap: false, data: MONTHS },
    yAxis: { type: 'value', name: 'kg CO2e' },
    series,
  }
}

export function getEmissionsByDateOption(summary) {
  const sortedEmissions = [...(summary.emissionsByShipmentDate || [])].sort(
    (a, b) => new Date(a.date) - new Date(b.date),
  )
  const dates = sortedEmissions.map((r) => r.date)
  const seriesLine = sortedEmissions.map((r) => Number(Number(r.emissionsKg).toFixed(2)))

  if (dates.length === 0) {
    return {
      color: ['#528951'],
      title: {
        text: 'Emissions by shipment date',
        left: 0,
        textStyle: { fontSize: 14, color: '#3d5340' },
      },
      graphic: [
        {
          type: 'text',
          left: 'center',
          top: 'center',
          style: {
            text: '暂无按日数据。请在 Shipment Tracking 新建货运；\n有「发货日期」则按该日期汇总，否则按保存当天汇总。',
            fontSize: 13,
            fill: '#6b7d6b',
            lineHeight: 20,
          },
        },
      ],
      tooltip: { show: false },
      grid: { left: 12, right: 20, top: 52, bottom: 36, containLabel: true },
      xAxis: { type: 'category', data: [], show: false },
      yAxis: { type: 'value', show: false },
      series: [],
    }
  }

  return {
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
    series: [{ type: 'line', smooth: true, data: seriesLine, areaStyle: { opacity: 0.12 } }],
  }
}

export function getEmissionsByTransportModeOption(summary) {
  const pieDataRaw = (summary.emissionsByTransportMode || []).map((r) => ({
    name: r.transportMode,
    value: Number(Number(r.emissionsKg).toFixed(2)) || 0,
  }))
  const pieData = pieDataRaw.filter((d) => d.value > 0)

  if (pieData.length === 0) {
    return {
      color: ['#528951', '#7daf7c', '#a8c9a6', '#d4e5d3'],
      title: {
        text: 'Share by transport mode',
        left: 0,
        textStyle: { fontSize: 14, color: '#3d5340' },
      },
      graphic: [
        {
          type: 'text',
          left: 'center',
          top: 'center',
          style: {
            text: '暂无运输方式分布。保存带运输方式的货运后将显示各方式排放量占比。',
            fontSize: 13,
            fill: '#6b7d6b',
            lineHeight: 20,
          },
        },
      ],
      tooltip: { show: false },
      series: [{ type: 'pie', radius: ['36%', '62%'], data: [] }],
    }
  }

  return {
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
  }
}

export function getTransportModeFactorsOption(transportModes) {
  const transportModeData = (transportModes || []).map((mode) => ({
    name: mode.displayName || mode.mode || mode.emissionFactor || 'Unknown',
    value: mode.emissionFactorPerKmPerTon || 0,
  }))

  if (transportModeData.length === 0 || transportModeData.every((d) => d.value === 0)) {
    return {
      color: ['#528951', '#7daf7c', '#a8c9a6', '#d4e5d3'],
      title: {
        text: 'Transport Mode Emission Factors',
        left: 0,
        textStyle: { fontSize: 14, color: '#3d5340' },
      },
      graphic: [
        {
          type: 'text',
          left: 'center',
          top: 'center',
          style: {
            text: '暂无运输方式数据。系统将自动加载所有运输方式的碳排放因子对比。',
            fontSize: 13,
            fill: '#6b7d6b',
            lineHeight: 20,
          },
        },
      ],
      tooltip: { show: false },
      xAxis: { type: 'category', data: [], show: false },
      yAxis: { type: 'value', show: false },
      series: [],
    }
  }

  return {
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
      show: true,
    },
    grid: { left: 12, right: 20, top: 52, bottom: 36, containLabel: true },
    xAxis: {
      type: 'category',
      data: transportModeData.map((d) => d.name),
      show: true,
      axisLabel: { rotate: transportModeData.length > 4 ? 35 : 0 },
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
        data: transportModeData.map((d) => d.value),
        itemStyle: { borderRadius: [4, 4, 0, 0] },
      },
    ],
  }
}

export function getHistoryAnalysisOption(analysis) {
  return {
    title: { text: 'History Analysis Overview', left: 'center' },
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      formatter: '{b}: {c}',
    },
    grid: { left: '20%', right: '20%', bottom: '15%', top: '15%', containLabel: true },
    xAxis: { type: 'category', data: ['Total Shipments', 'Potential Savings %'] },
    yAxis: { type: 'value', axisLabel: { formatter: '{value}' } },
    series: [
      {
        name: 'History Analysis',
        type: 'bar',
        barWidth: '40%',
        data: [
          { value: Math.round(analysis.total_shipments || 0), itemStyle: { color: '#5470c6' } },
          {
            value: Math.round((analysis.potential_savings_percent || 0) * 100) / 100,
            itemStyle: { color: '#91cc75' },
          },
        ],
        emphasis: {
          itemStyle: { shadowBlur: 10, shadowOffsetX: 0, shadowColor: 'rgba(0, 0, 0, 0.5)' },
        },
        label: {
          show: true,
          position: 'top',
          formatter: (params) => (params.dataIndex === 0 ? params.value : `${params.value}%`),
        },
      },
    ],
  }
}
