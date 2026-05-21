import * as echarts from 'echarts'

const MONTHS_EN = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec']
const MONTHS_ZH = ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月']

const YEAR_COLORS = ['#ff7875', '#409eff', '#73d13d', '#9254de', '#faad14', '#13c2c6']

const i18nTexts = {
  en: {
    'carbon-emissions': 'Carbon Emissions',
    'total-emissions': 'Total Emissions',
    'avoided-emissions': 'Avoided Emissions',
    'supplier-distribution': 'Supplier Distribution',
    'certified': 'Certified',
    'non-certified': 'Non-Certified',
    'supply-chain-metrics': 'Supply Chain Metrics',
    'shipments': 'Shipments',
    'enterprises': 'Enterprises',
    'carbon-emissions-trend': 'Carbon Emissions Trend',
    'all-years-emissions': 'All Years Emissions',
    'emissions-by-shipment-date': 'Emissions by Shipment Date',
    'share-by-transport-mode': 'Share by Transport Mode',
    'transport-mode-emission-factors': 'Transport Mode Emission Factors',
    'history-analysis-overview': 'History Analysis Overview',
    'total-shipments': 'Total Shipments',
    'potential-savings': 'Potential Savings',
    'kg-co2e': 'kg CO2e',
    'no-date-data': 'No date-based data. Create shipments with shipment dates in Shipment Tracking.',
    'no-transport-data': 'No transport mode distribution. Save shipments with transport modes to see emission share.',
    'no-transport-factors-data': 'No transport mode data. System will auto-load all transport modes carbon emission factors.',
    'count': 'Count',
    'unknown': 'Unknown',
  },
  zh: {
    'carbon-emissions': '碳排放量',
    'total-emissions': '总排放量',
    'avoided-emissions': '避免排放量',
    'supplier-distribution': '供应商分布',
    'certified': '已认证',
    'non-certified': '未认证',
    'supply-chain-metrics': '供应链指标',
    'shipments': '运输次数',
    'enterprises': '企业数量',
    'carbon-emissions-trend': '碳排放趋势',
    'all-years-emissions': '所有年份排放',
    'emissions-by-shipment-date': '按发货日期的排放',
    'share-by-transport-mode': '运输方式占比',
    'transport-mode-emission-factors': '运输方式排放因子',
    'history-analysis-overview': '历史分析概览',
    'total-shipments': '总运输次数',
    'potential-savings': '潜在节省',
    'kg-co2e': '千克CO2e',
    'no-date-data': '暂无按日数据。请在运输跟踪中新建货运记录；有「发货日期」则按该日期汇总，否则按保存当天汇总。',
    'no-transport-data': '暂无运输方式分布。保存带运输方式的货运后将显示各方式排放量占比。',
    'no-transport-factors-data': '暂无运输方式数据。系统将自动加载所有运输方式的碳排放因子对比。',
    'count': '数量',
    'unknown': '未知',
  }
}

function t(key, locale = 'en') {
  return i18nTexts[locale]?.[key] || i18nTexts['en'][key] || key
}

export function getCarbonEmissionsOption(summary, locale = 'en') {
  const texts = i18nTexts[locale] || i18nTexts['en']
  return {
    title: { text: texts['carbon-emissions'], left: 'center' },
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    legend: { data: [texts['total-emissions'], texts['avoided-emissions']], bottom: 10 },
    grid: { left: '3%', right: '4%', bottom: '15%', top: '15%', containLabel: true },
    xAxis: { type: 'category', data: [texts['kg-co2e']] },
    yAxis: { type: 'value' },
    series: [
      {
        name: texts['total-emissions'],
        type: 'bar',
        data: [Number(Number(summary.totalEmissionsKg || 0).toFixed(2))],
        itemStyle: { color: '#ff7875' },
      },
      {
        name: texts['avoided-emissions'],
        type: 'bar',
        data: [Number(Number(summary.estimatedAvoidedEmissionsKg || 0).toFixed(2))],
        itemStyle: { color: '#73d13d' },
      },
    ],
  }
}

export function getSupplierDistributionOption(summary, locale = 'en') {
  const texts = i18nTexts[locale] || i18nTexts['en']
  return {
    title: { text: texts['supplier-distribution'], left: 'center' },
    tooltip: { trigger: 'item' },
    legend: { orient: 'vertical', left: 'left', bottom: 10 },
    series: [
      {
        name: 'Suppliers',
        type: 'pie',
        radius: '60%',
        center: ['50%', '50%'],
        data: [
          { value: summary.certifiedSupplierCount || 0, name: texts['certified'] },
          {
            value: (summary.registeredSupplierCount || 0) - (summary.certifiedSupplierCount || 0),
            name: texts['non-certified'],
          },
        ],
        emphasis: {
          itemStyle: { shadowBlur: 10, shadowOffsetX: 0, shadowColor: 'rgba(0, 0, 0, 0.5)' },
        },
      },
    ],
  }
}

export function getSupplyChainMetricsOption(summary, locale = 'en') {
  const texts = i18nTexts[locale] || i18nTexts['en']
  return {
    title: { text: texts['supply-chain-metrics'], left: 'center' },
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    legend: { data: [texts['shipments'], texts['enterprises']], bottom: 10 },
    grid: { left: '3%', right: '4%', bottom: '15%', top: '15%', containLabel: true },
    xAxis: { type: 'category', data: [texts['count']] },
    yAxis: { type: 'value' },
    series: [
      {
        name: texts['shipments'],
        type: 'bar',
        data: [summary.shipmentCount || 0],
        itemStyle: { color: '#409eff' },
      },
      {
        name: texts['enterprises'],
        type: 'bar',
        data: [summary.enterprisesInSupplyChain || 0],
        itemStyle: { color: '#9254de' },
      },
    ],
  }
}

export function getCarbonEmissionsTrendOption(summary, availableYears, locale = 'en') {
  const texts = i18nTexts[locale] || i18nTexts['en']
  const months = locale === 'zh' ? MONTHS_ZH : MONTHS_EN
  const series = []
  const legendData = []

  legendData.push(texts['all-years-emissions'])
  series.push({
    name: texts['all-years-emissions'],
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
    title: { text: texts['carbon-emissions-trend'], left: 'center' },
    tooltip: { trigger: 'axis' },
    legend: { data: legendData, bottom: 10 },
    grid: { left: '3%', right: '4%', bottom: '15%', top: '15%', containLabel: true },
    xAxis: { type: 'category', boundaryGap: false, data: months },
    yAxis: { type: 'value', name: 'kg CO2e' },
    series,
  }
}

export function getEmissionsByDateOption(summary, locale = 'en') {
  const texts = i18nTexts[locale] || i18nTexts['en']
  const sortedEmissions = [...(summary.emissionsByShipmentDate || [])].sort(
    (a, b) => new Date(a.date) - new Date(b.date),
  )
  const dates = sortedEmissions.map((r) => r.date)
  const seriesLine = sortedEmissions.map((r) => Number(Number(r.emissionsKg).toFixed(2)))

  if (dates.length === 0) {
    return {
      color: ['#528951'],
      title: {
        text: texts['emissions-by-shipment-date'],
        left: 0,
        textStyle: { fontSize: 14, color: '#3d5340' },
      },
      graphic: [
        {
          type: 'text',
          left: 'center',
          top: 'center',
          style: {
            text: texts['no-date-data'],
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
      text: texts['emissions-by-shipment-date'],
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
      name: texts['kg-co2e'],
      show: true,
      nameGap: 12,
      axisLabel: { margin: 8 },
    },
    series: [{ type: 'line', smooth: true, data: seriesLine, areaStyle: { opacity: 0.12 } }],
  }
}

export function getEmissionsByTransportModeOption(summary, locale = 'en') {
  const texts = i18nTexts[locale] || i18nTexts['en']
  const pieDataRaw = (summary.emissionsByTransportMode || []).map((r) => ({
    name: r.transportMode,
    value: Number(Number(r.emissionsKg).toFixed(2)) || 0,
  }))
  const pieData = pieDataRaw.filter((d) => d.value > 0)

  if (pieData.length === 0) {
    return {
      color: ['#528951', '#7daf7c', '#a8c9a6', '#d4e5d3'],
      title: {
        text: texts['share-by-transport-mode'],
        left: 0,
        textStyle: { fontSize: 14, color: '#3d5340' },
      },
      graphic: [
        {
          type: 'text',
          left: 'center',
          top: 'center',
          style: {
            text: texts['no-transport-data'],
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
      text: texts['share-by-transport-mode'],
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

export function getTransportModeFactorsOption(transportModes, locale = 'en') {
  const texts = i18nTexts[locale] || i18nTexts['en']
  const transportModeData = (transportModes || []).map((mode) => ({
    name: mode.displayName || mode.mode || mode.emissionFactor || texts['unknown'],
    value: mode.emissionFactorPerKmPerTon || 0,
  }))

  if (transportModeData.length === 0 || transportModeData.every((d) => d.value === 0)) {
    return {
      color: ['#528951', '#7daf7c', '#a8c9a6', '#d4e5d3'],
      title: {
        text: texts['transport-mode-emission-factors'],
        left: 0,
        textStyle: { fontSize: 14, color: '#3d5340' },
      },
      graphic: [
        {
          type: 'text',
          left: 'center',
          top: 'center',
          style: {
            text: texts['no-transport-factors-data'],
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
      text: texts['transport-mode-emission-factors'],
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

export function getHistoryAnalysisOption(analysis, locale = 'en') {
  const texts = i18nTexts[locale] || i18nTexts['en']
  return {
    title: { text: texts['history-analysis-overview'], left: 'center' },
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      formatter: '{b}: {c}',
    },
    grid: { left: '20%', right: '20%', bottom: '15%', top: '15%', containLabel: true },
    xAxis: { type: 'category', data: [texts['total-shipments'], texts['potential-savings']] },
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
