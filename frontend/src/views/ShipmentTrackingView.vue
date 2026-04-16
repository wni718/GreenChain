<script setup>
import { ref, computed, watch } from 'vue'
import { useAuth } from '../composables/useAuth'
import { formatErrorBody } from '../utils/apiError'

const { apiAuthHeader, currentUser } = useAuth()

const isLoggedIn = computed(() => Boolean(currentUser.value?.username))

const shipments = ref([])
const suppliers = ref([])
const transportModes = ref([])
const loading = ref(false)
const message = ref('')
const messageKind = ref('err')

const dialogOpen = ref(false)
const editingId = ref(null)
const form = ref({
  supplierId: '',
  transportModeId: '',
  origin: '',
  destination: '',
  distanceKm: '',
  cargoWeightTons: '',
  shipmentDate: '',
})

const showRecommendation = ref(false)
const recommendation = ref(null)
const recommendationLoading = ref(false)

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

function modeLabel(row) {
  const tm = row.transportMode
  if (!tm) return '—'
  return tm.displayName || tm.mode || '—'
}

function supplierLabel(row) {
  const s = row.supplier
  if (!s) return '—'
  return s.name || `ID ${s.id}` || '—'
}

function todayIsoDate() {
  const d = new Date()
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}

async function loadShipments() {
  if (!isLoggedIn.value) return
  loading.value = true
  message.value = ''
  try {
    const res = await apiFetch('/api/shipments')
    const text = await res.text()
    if (!res.ok) {
      setMsg(formatErrorBody(res.status, text))
      shipments.value = []
      return
    }
    try {
      shipments.value = text ? JSON.parse(text) : []
    } catch {
      setMsg('Response was not valid JSON.')
      shipments.value = []
    }
  } catch {
    setMsg('Cannot reach the server. Ensure the backend is running and /api is proxied.')
    shipments.value = []
  } finally {
    loading.value = false
  }
}

async function loadFormOptions() {
  if (!isLoggedIn.value) return
  try {
    const [supRes, modeRes] = await Promise.all([
      apiFetch('/api/suppliers'),
      apiFetch('/api/transport-modes'),
    ])
    const supText = await supRes.text()
    const modeText = await modeRes.text()
    if (supRes.ok) {
      try {
        suppliers.value = supText ? JSON.parse(supText) : []
      } catch {
        suppliers.value = []
      }
    } else {
      suppliers.value = []
    }
    if (modeRes.ok) {
      try {
        transportModes.value = modeText ? JSON.parse(modeText) : []
      } catch {
        transportModes.value = []
      }
    } else {
      transportModes.value = []
    }
  } catch {
    suppliers.value = []
    transportModes.value = []
  }
}

function shipmentDateInputValue(row) {
  const sd = row.shipmentDate
  if (sd == null) return ''
  if (typeof sd === 'string') return sd.slice(0, 10)
  if (typeof sd === 'object' && sd.year != null) {
    const m = String(sd.monthValue ?? sd.month ?? 1).padStart(2, '0')
    const d = String(sd.dayOfMonth ?? sd.day ?? 1).padStart(2, '0')
    return `${sd.year}-${m}-${d}`
  }
  return ''
}

function openCreate() {
  editingId.value = null
  form.value = {
    supplierId: '',
    transportModeId: '',
    origin: '',
    destination: '',
    distanceKm: '',
    cargoWeightTons: '',
    shipmentDate: todayIsoDate(),
  }
  dialogOpen.value = true
  loadFormOptions()
}

function openEdit(row) {
  editingId.value = row.id
  form.value = {
    supplierId: row.supplier?.id != null ? String(row.supplier.id) : '',
    transportModeId: row.transportMode?.id != null ? String(row.transportMode.id) : '',
    origin: row.origin ?? '',
    destination: row.destination ?? '',
    distanceKm:
      row.distanceKm != null && !Number.isNaN(Number(row.distanceKm)) ? String(row.distanceKm) : '',
    cargoWeightTons:
      row.cargoWeightTons != null && !Number.isNaN(Number(row.cargoWeightTons))
        ? String(row.cargoWeightTons)
        : '',
    shipmentDate: shipmentDateInputValue(row) || '',
  }
  dialogOpen.value = true
  loadFormOptions()
}

function closeDialog() {
  dialogOpen.value = false
  editingId.value = null
  showRecommendation.value = false
  recommendation.value = null
}

async function getRecommendation() {
  const mid = form.value.transportModeId.trim()
  if (!mid) {
    setMsg('Please select the current transport mode first.')
    return
  }
  
  // Find the corresponding transport mode object and get its mode property
  const selectedMode = transportModes.value.find(m => String(m.id) === mid)
  if (!selectedMode || !selectedMode.mode) {
    setMsg('Unable to get current transport mode information.')
    return
  }
  
  recommendationLoading.value = true
  showRecommendation.value = false
  recommendation.value = null
  message.value = ''
  
  try {
    // Get distance and weight from form
    const distance = form.value.distanceKm.trim()
    const weight = form.value.cargoWeightTons.trim()
    
    // Build URL with parameters
    let url = `/api/recommend?current_mode=${encodeURIComponent(selectedMode.mode)}`
    if (distance) url += `&distance_km=${encodeURIComponent(distance)}`
    if (weight) url += `&cargo_weight_tons=${encodeURIComponent(weight)}`
    
    const res = await apiFetch(url)
    const text = await res.text()
    
    if (!res.ok) {
      setMsg(formatErrorBody(res.status, text))
      return
    }
    
    try {
      recommendation.value = JSON.parse(text)
      showRecommendation.value = true
    } catch {
      setMsg('Failed to parse recommendation result.')
    }
  } catch {
    setMsg('Cannot connect to the server. Please check if the backend is running.')
  } finally {
    recommendationLoading.value = false
  }
}

async function saveShipment() {
  const sid = form.value.supplierId.trim()
  const mid = form.value.transportModeId.trim()
  const dist = form.value.distanceKm.trim()
  const wt = form.value.cargoWeightTons.trim()

  if (!sid || !mid) {
    setMsg('Please select a supplier and transport mode.')
    return
  }
  const distanceKm = Number(dist)
  const cargoWeightTons = Number(wt)
  if (!Number.isFinite(distanceKm) || distanceKm < 0) {
    setMsg('Please enter a valid distance (km).')
    return
  }
  if (!Number.isFinite(cargoWeightTons) || cargoWeightTons < 0) {
    setMsg('Please enter a valid cargo weight (tons).')
    return
  }

  const body = {
    supplier: { id: Number(sid) },
    transportMode: { id: Number(mid) },
    origin: form.value.origin.trim(),
    destination: form.value.destination.trim(),
    distanceKm,
    cargoWeightTons,
    shipmentDate: form.value.shipmentDate.trim() || null,
  }

  message.value = ''
  try {
    const isEdit = editingId.value != null
    // Use POST /{id}/update for edits: some setups mishandle PUT and Spring returns
    // "No static resource api/shipments/{id}" when no handler matches.
    const res = await apiFetch(
      isEdit ? `/api/shipments/${editingId.value}/update` : '/api/shipments',
      {
        method: 'POST',
        body: JSON.stringify(body),
      },
    )
    const text = await res.text()
    if (res.status === 401) {
      setMsg('Unauthorized. Please log in again.')
      return
    }
    if (res.status === 403) {
      setMsg('Access denied.')
      return
    }
    if (!res.ok) {
      setMsg(formatErrorBody(res.status, text))
      return
    }
    setMsg(isEdit ? 'Changes saved.' : 'Shipment saved.', 'ok')
    closeDialog()
    await loadShipments()
  } catch {
    setMsg('Request failed. Check your network connection.')
  }
}

async function removeRow(row) {
  if (!window.confirm(`Delete shipment #${row.id}?`)) return
  message.value = ''
  try {
    const res = await apiFetch(`/api/shipments/${row.id}`, { method: 'DELETE' })
    const text = await res.text()
    if (res.status === 401) {
      setMsg('Unauthorized. Please log in again.')
      return
    }
    if (res.status === 403) {
      setMsg('Access denied.')
      return
    }
    if (!res.ok && res.status !== 204) {
      setMsg(formatErrorBody(res.status, text))
      return
    }
    setMsg('Deleted.', 'ok')
    await loadShipments()
  } catch {
    setMsg('Delete failed.')
  }
}

watch(
  isLoggedIn,
  (loggedIn) => {
    if (loggedIn) {
      loadShipments()
      loadFormOptions()
    } else {
      shipments.value = []
      suppliers.value = []
      transportModes.value = []
      message.value = ''
      dialogOpen.value = false
    }
  },
  { immediate: true },
)
</script>

<template>
  <div class="page">
    <template v-if="!isLoggedIn">
      <header class="page__head">
        <h1 class="page__title">Shipment Tracking</h1>
      </header>
      <p class="guest-notice" role="status">You need to log in to track shipments.</p>
    </template>

    <template v-else>
      <header class="page__head">
        <h1 class="page__title">Shipment Tracking</h1>
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
        <div class="toolbar-actions">
          <button type="button" class="btn btn--ghost" :disabled="loading" @click="loadShipments">
            {{ loading ? 'Loading…' : 'Refresh' }}
          </button>
          <button type="button" class="btn btn--primary" @click="openCreate">New shipment</button>
        </div>
      </div>

      <div class="table-wrap">
        <table class="data-table">
          <thead>
            <tr>
              <th scope="col">ID</th>
              <th scope="col">Supplier</th>
              <th scope="col">Mode</th>
              <th scope="col">Origin</th>
              <th scope="col">Destination</th>
              <th scope="col">Distance (km)</th>
              <th scope="col">Weight (t)</th>
              <th scope="col">Date</th>
              <th scope="col">CO2e (kg)</th>
              <th scope="col" class="col-actions">Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="!loading && shipments.length === 0">
              <td colspan="10" class="empty-cell">
                No shipments yet. Please add suppliers first, then click "New shipment" to create one.
              </td>
            </tr>
            <tr v-for="row in shipments" :key="row.id">
              <td>{{ row.id }}</td>
              <td>{{ supplierLabel(row) }}</td>
              <td>{{ modeLabel(row) }}</td>
              <td>{{ row.origin || '—' }}</td>
              <td>{{ row.destination || '—' }}</td>
              <td>{{ row.distanceKm != null ? Number(row.distanceKm).toFixed(2) : '—' }}</td>
              <td>{{ row.cargoWeightTons != null ? Number(row.cargoWeightTons).toFixed(2) : '—' }}</td>
              <td>{{ row.shipmentDate || '—' }}</td>
              <td>{{ row.calculatedCarbonEmission != null ? Number(row.calculatedCarbonEmission).toFixed(2) : '—' }}</td>
              <td class="col-actions">
                <button type="button" class="link-btn" @click="openEdit(row)">Edit</button>
                <button type="button" class="link-btn link-btn--danger" @click="removeRow(row)">
                  Delete
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div v-if="dialogOpen" class="dialog-backdrop" role="presentation" @click.self="closeDialog">
        <div class="dialog" role="dialog" aria-modal="true" aria-labelledby="shipment-dialog-title">
          <h2 id="shipment-dialog-title" class="dialog__title">
            {{ editingId != null ? 'Edit shipment' : 'New shipment' }}
          </h2>
          <form class="dialog__form" @submit.prevent="saveShipment">
            <label class="field">
              <span class="field__label">Supplier *</span>
              <select v-model="form.supplierId" class="field__input" required>
                <option disabled value="">Select supplier</option>
                <option v-for="s in suppliers" :key="s.id" :value="String(s.id)">
                  {{ s.name || `ID ${s.id}` }}
                </option>
              </select>
            </label>
            <label class="field">
              <span class="field__label">Transport mode *</span>
              <div style="display: flex; gap: 0.5rem;">
                <select v-model="form.transportModeId" class="field__input" required style="flex: 1;">
                  <option disabled value="">Select mode</option>
                  <option v-for="m in transportModes" :key="m.id" :value="String(m.id)">
                    {{ m.displayName || m.mode || `ID ${m.id}` }}
                  </option>
                </select>
                <button 
                  type="button" 
                  class="btn btn--ghost" 
                  @click="getRecommendation"
                  :disabled="!form.transportModeId || recommendationLoading"
                  style="white-space: nowrap;"
                >
                  {{ recommendationLoading ? 'Recommending...' : 'Recommend' }}
                </button>
              </div>
              <!-- Recommendation result display -->
              <div v-if="showRecommendation && recommendation" style="margin-top: 0.5rem; padding: 0.75rem; background: #f0f8f0; border-radius: 6px; border: 1px solid #d0e8d0;">
                <h4 style="margin: 0 0 0.5rem; color: #3d5340; font-size: 0.9rem;">Recommendation Result</h4>
                <p style="margin: 0 0 0.25rem; font-size: 0.85rem;">
                  <strong>Recommended transport mode:</strong> {{ recommendation.best_mode || 'No recommendation' }}
                </p>
                <p style="margin: 0 0 0.25rem; font-size: 0.85rem;">
                  <strong>Estimated carbon reduction:</strong> {{ recommendation.saving || '0%' }}
                </p>
                <p style="margin: 0 0 0.25rem; font-size: 0.85rem;">
                  <strong>Reduction amount:</strong> {{ recommendation.saving_amount ? recommendation.saving_amount.toFixed(2) : '0' }} {{ recommendation.saving_amount_unit || 'kg CO2e' }}
                </p>
                <p style="margin: 0 0 0.25rem; font-size: 0.85rem;">
                  <strong>Current emission:</strong> {{ recommendation.current_emission ? recommendation.current_emission.toFixed(2) : '0' }} kg CO2e
                </p>
                <p style="margin: 0 0 0.25rem; font-size: 0.85rem;">
                  <strong>Recommended emission:</strong> {{ recommendation.recommended_emission ? recommendation.recommended_emission.toFixed(2) : '0' }} kg CO2e
                </p>
                <div style="margin-top: 0.5rem; padding-top: 0.5rem; border-top: 1px solid #d0e8d0;">
                  <p style="margin: 0 0 0.25rem; font-size: 0.85rem;">
                    <strong>Time factor:</strong> {{ recommendation.time_factor ? (recommendation.time_factor < 1 ? 'Faster' : recommendation.time_factor > 1 ? 'Slower' : 'Same') : 'N/A' }}
                  </p>
                  <p style="margin: 0; font-size: 0.85rem;">
                    <strong>Cost factor:</strong> {{ recommendation.cost_factor ? (recommendation.cost_factor < 1 ? 'Cheaper' : recommendation.cost_factor > 1 ? 'More expensive' : 'Same') : 'N/A' }}
                  </p>
                </div>
              </div>
            </label>
            <label class="field">
              <span class="field__label">Origin</span>
              <input v-model="form.origin" class="field__input" type="text" autocomplete="off" />
            </label>
            <label class="field">
              <span class="field__label">Destination</span>
              <input v-model="form.destination" class="field__input" type="text" autocomplete="off" />
            </label>
            <label class="field">
              <span class="field__label">Distance (km) *</span>
              <input
                v-model="form.distanceKm"
                class="field__input"
                type="text"
                inputmode="decimal"
                required
              />
            </label>
            <label class="field">
              <span class="field__label">Cargo weight (tons) *</span>
              <input
                v-model="form.cargoWeightTons"
                class="field__input"
                type="text"
                inputmode="decimal"
                required
              />
            </label>
            <label class="field">
              <span class="field__label">Shipment date</span>
              <input v-model="form.shipmentDate" class="field__input" type="date" />
            </label>
            <div class="dialog__actions">
              <button type="button" class="btn btn--ghost" @click="closeDialog">Cancel</button>
              <button type="submit" class="btn btn--primary">Save</button>
            </div>
          </form>
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

.toolbar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: flex-end;
  gap: 0.75rem;
}

.toolbar-actions {
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
  border: 1px solid transparent;
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

.btn--ghost {
  background: #fff;
  color: #3d5340;
  border-color: #b5c4b5;
}

.btn--ghost:hover:not(:disabled) {
  background: #f4faf4;
}

.table-wrap {
  overflow: auto;
  border: 1px solid #c5d6c5;
  border-radius: 8px;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 0.88rem;
}

.data-table th,
.data-table td {
  padding: 0.5rem 0.65rem;
  text-align: left;
  border-bottom: 1px solid #e0eae0;
}

.data-table th {
  background: #eef4ee;
  color: #3a4d3a;
  font-weight: 750;
}

.data-table tr:last-child td {
  border-bottom: none;
}

.empty-cell {
  text-align: center;
  color: #6b7d6b;
  padding: 1.5rem;
}

.col-actions {
  white-space: nowrap;
  width: 1%;
}

.link-btn {
  margin-right: 0.5rem;
  padding: 0;
  border: none;
  background: none;
  color: #2f6b2e;
  font-weight: 600;
  cursor: pointer;
  font-family: inherit;
  font-size: inherit;
  text-decoration: underline;
}

.link-btn--danger {
  color: #b91c1c;
}

.dialog-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(20, 30, 20, 0.35);
  display: flex;
  align-items: flex-start;
  justify-content: center;
  padding: 2rem 1rem;
  z-index: 50;
}

.dialog {
  width: 100%;
  max-width: 420px;
  max-height: 85vh;
  background: #fff;
  border-radius: 10px;
  padding: 1.25rem 1.35rem;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.12);
  border: 1px solid #c5d6c5;
  overflow-y: auto;
}

.dialog__title {
  margin: 0 0 1rem;
  font-size: 1.25rem;
  color: #3d5340;
}

.dialog__form {
  display: flex;
  flex-direction: column;
  gap: 0.85rem;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
}

.field__label {
  font-size: 0.85rem;
  font-weight: 650;
  color: #4a5c4a;
}

.field__input {
  padding: 0.45rem 0.55rem;
  border: 1px solid #b5c4b5;
  border-radius: 6px;
  font-size: 0.95rem;
  font-family: inherit;
}

.dialog__actions {
  display: flex;
  justify-content: flex-end;
  gap: 0.5rem;
  margin-top: 0.5rem;
}
</style>
