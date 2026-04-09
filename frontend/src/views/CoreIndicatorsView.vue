<script setup>
import { ref, computed, watch } from 'vue'
import { useAuth } from '../composables/useAuth'
import { formatErrorBody } from '../utils/apiError'

const { apiAuthHeader, currentUser } = useAuth()

const isLoggedIn = computed(() => Boolean(currentUser.value?.username))

const loading = ref(false)
const message = ref('')
const messageKind = ref('err')

const summary = ref(null)

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

      <div v-if="summary" class="kpi-grid" role="list">
        <article class="kpi-card" role="listitem">
          <h2 class="kpi-card__label">Total carbon emissions</h2>
          <p class="kpi-card__value">{{ summary.totalEmissionsKg }} <span class="unit">{{ summary.emissionsUnit }}</span></p>
        </article>
        <article class="kpi-card" role="listitem">
          <h2 class="kpi-card__label">Estimated avoided emissions</h2>
          <p class="kpi-card__value">{{ summary.estimatedAvoidedEmissionsKg }} <span class="unit">kg CO2e</span></p>
          <p class="kpi-card__hint">vs assuming every shipment used the highest emission factor among configured modes</p>
        </article>
        <article class="kpi-card" role="listitem">
          <h2 class="kpi-card__label">Reduction vs worst-mode baseline</h2>
          <p class="kpi-card__value">{{ summary.reductionPercentVsHighestFactorMode }}<span class="unit">%</span></p>
        </article>
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
        <article class="kpi-card kpi-card--wide" role="listitem">
          <h2 class="kpi-card__label">Enterprises in active supply chain</h2>
          <p class="kpi-card__value">{{ summary.enterprisesInSupplyChain }}</p>
          <p class="kpi-card__hint">Distinct suppliers linked to at least one shipment</p>
        </article>
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

.kpi-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 1rem;
}

.kpi-card {
  margin: 0;
  padding: 1rem 1.1rem;
  border-radius: 10px;
  border: 1px solid #c5d6c5;
  background: linear-gradient(160deg, #f8fbf8 0%, #eef4ee 100%);
}

.kpi-card--wide {
  grid-column: 1 / -1;
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
</style>
