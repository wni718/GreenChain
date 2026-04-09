<script setup>
import { ref, computed, watch } from 'vue'
import { useAuth } from '../composables/useAuth'
import { formatErrorBody } from '../utils/apiError'

const { apiAuthHeader, currentUser } = useAuth()

const isLoggedIn = computed(() => Boolean(currentUser.value?.username))

const loading = ref(false)
const message = ref('')
const messageKind = ref('err')

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
      const mode =
        sh.transportMode?.displayName || sh.transportMode?.mode || ''
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
        <button type="button" class="btn btn--primary" :disabled="loading" @click="exportCsv">
          {{ loading ? 'Preparing…' : 'Download CSV report' }}
        </button>
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
