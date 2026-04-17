<script setup>
import { ref, computed, watch } from 'vue'
import { useAuth } from '../composables/useAuth'
import { formatErrorBody } from '../utils/apiError'
import jsPDF from 'jspdf'
import html2canvas from 'html2canvas'
import * as echarts from 'echarts'
import {
  getCarbonEmissionsOption,
  getSupplierDistributionOption,
  getSupplyChainMetricsOption,
  getCarbonEmissionsTrendOption,
  getEmissionsByDateOption,
  getEmissionsByTransportModeOption,
  getTransportModeFactorsOption,
} from '../utils/chartConfig'

const { apiAuthHeader, currentUser } = useAuth()

const isLoggedIn = computed(() => Boolean(currentUser.value?.username))

const loading = ref(false)
const csvLoading = ref(false)
const pdfLoading = ref(false)
const message = ref('')
const messageKind = ref('err')

function setMsg(text, kind = 'err') {
  message.value = text
  messageKind.value = kind
}

async function renderChartToImage(container, ChartClass, option) {
  const el = document.createElement('div')
  el.style.cssText = 'width: 700px; height: 300px;'
  container.appendChild(el)
  const chart = ChartClass.init(el)
  chart.setOption(option)
  chart.resize()
  await new Promise(resolve => setTimeout(resolve, 500))
  const img = chart.getDataURL({ type: 'png', pixelRatio: 2 })
  chart.dispose()
  container.removeChild(el)
  return img
}

async function generateChartsHtml(summary, suppliers, shipments, transportModes) {
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

  const chartImages = []

  const allYearsOption = getCarbonEmissionsTrendOption(summary, [])
  allYearsOption.title.text = 'Carbon Emissions Trend (All Years)'
  allYearsOption.legend.data = ['All Years Emissions']
  allYearsOption.series = [allYearsOption.series[0]]
  chartImages.push({
    title: 'Carbon Emissions Trend (All Years)',
    img: await renderChartToImage(chartContainer, echarts, allYearsOption),
  })

  const years = (summary.emissionsByYearMonth || []).map(y => y.year).sort()
  if (years.length > 0) {
    const yearlyOption = getCarbonEmissionsTrendOption(summary, years)
    yearlyOption.legend.data = yearlyOption.legend.data.filter(d => d !== 'All Years Emissions')
    yearlyOption.series = yearlyOption.series.filter(s => s.name !== 'All Years Emissions')
    for (const [i, year] of years.entries()) {
      const yearSeries = yearlyOption.series[i]
      if (yearSeries) {
        yearSeries.name = `${year} Emissions`
        chartImages.push({
          title: `Carbon Emissions Trend (${year})`,
          img: await renderChartToImage(chartContainer, echarts, {
            ...yearlyOption,
            title: { ...yearlyOption.title, text: `Carbon Emissions Trend (${year})` },
            series: [yearSeries],
            legend: { ...yearlyOption.legend, data: [`${year} Emissions`] },
          }),
        })
      }
    }
  }

  chartImages.push({ title: 'Carbon Emissions', img: await renderChartToImage(chartContainer, echarts, getCarbonEmissionsOption(summary)) })
  chartImages.push({ title: 'Supplier Distribution', img: await renderChartToImage(chartContainer, echarts, getSupplierDistributionOption(summary)) })
  chartImages.push({ title: 'Supply Chain Metrics', img: await renderChartToImage(chartContainer, echarts, getSupplyChainMetricsOption(summary)) })
  chartImages.push({ title: 'Emissions by Shipment Date', img: await renderChartToImage(chartContainer, echarts, getEmissionsByDateOption(summary)) })
  chartImages.push({ title: 'Share by Transport Mode', img: await renderChartToImage(chartContainer, echarts, getEmissionsByTransportModeOption(summary)) })
  chartImages.push({ title: 'Transport Mode Emission Factors', img: await renderChartToImage(chartContainer, echarts, getTransportModeFactorsOption(transportModes)) })

  document.body.removeChild(chartContainer)

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
  csvLoading.value = true
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
      'sequence,supplierId,supplierName,transportMode,distanceKm,cargoWeightTons,origin,destination,shipmentDate,calculatedCarbonEmission',
    )
    for (const [index, sh] of shipments.entries()) {
      const sid = sh.supplier?.id
      const sname = sh.supplier?.name
      const mode = sh.transportMode?.displayName || sh.transportMode?.mode || ''
      lines.push(
        [
          csvEscape(index + 1),
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
    csvLoading.value = false
  }
}

async function exportPdf() {
  if (!isLoggedIn.value) return
  pdfLoading.value = true
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
    pdfLoading.value = false
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
          <button type="button" class="btn btn--primary" :disabled="csvLoading" @click="exportCsv">
            {{ csvLoading ? 'Preparing…' : 'Download CSV report' }}
          </button>
          <button type="button" class="btn btn--primary" :disabled="pdfLoading" @click="exportPdf">
            {{ pdfLoading ? 'Preparing…' : 'Download PDF report' }}
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
