<script setup>
import { ref, computed, watch } from 'vue'
import { useAuth } from '../composables/useAuth'
import { useFormValidation, ValidationRules } from '../composables/useFormValidation'
import { usePagination } from '../composables/usePagination'
import Pagination from '../components/Pagination.vue'
import { formatErrorBody } from '../utils/apiError'

const { apiAuthHeader, currentUser } = useAuth()

const isLoggedIn = computed(() => Boolean(currentUser.value?.username))
const canModify = computed(() => currentUser.value?.role !== 'VIEWER')

const shipments = ref([])
const suppliers = ref([])
const transportModes = ref([])
const loading = ref(false)
const message = ref('')
const messageKind = ref('err')

const dialogOpen = ref(false)
const editingId = ref(null)

const {
  currentPage,
  pageSize,
  totalElements,
  totalPages,
  setPage,
  updatePageInfo,
  reset: resetPagination,
} = usePagination(15)

// 表单验证
const {
  fields: form,
  errors: formErrors,
  touched: formTouched,
  validateAll: validateForm,
  touchField: touchFormField,
  initForm: initFormFields,
  resetValidation: resetFormValidation,
} = useFormValidation({
  supplierId: [
    ValidationRules.required('Please select a supplier'),
  ],
  transportModeId: [
    ValidationRules.required('Please select a transport mode'),
  ],
  origin: [
    ValidationRules.maxLength(200, 'Origin must be at most 200 characters'),
  ],
  destination: [
    ValidationRules.maxLength(200, 'Destination must be at most 200 characters'),
  ],
  distanceKm: [
    ValidationRules.required('Please enter the distance'),
    ValidationRules.number('Please enter a valid number'),
    ValidationRules.nonNegativeNumber('Distance cannot be negative'),
  ],
  cargoWeightTons: [
    ValidationRules.required('Please enter the cargo weight'),
    ValidationRules.number('Please enter a valid number'),
    ValidationRules.positiveNumber('Cargo weight must be positive'),
  ],
  shipmentDate: [
    ValidationRules.date('Please enter a valid date'),
  ],
})

const showRecommendation = ref(false)
const recommendation = ref(null)
const smartRecommendation = ref(null)
const unifiedRecommendation = ref(null)
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

async function loadShipments(pageToLoad = 0, size = pageSize.value) {
  if (!isLoggedIn.value) return
  loading.value = true
  message.value = ''
  try {
    const url = `/api/shipments?page=${pageToLoad}&size=${size}`
    const res = await apiFetch(url)
    const text = await res.text()
    if (!res.ok) {
      setMsg(formatErrorBody(res.status, text))
      shipments.value = []
      updatePageInfo({ page: 0, size, totalElements: 0 })
      return
    }
    try {
      const data = text ? JSON.parse(text) : null
      // Handle both paginated and non-paginated responses
      if (data && Array.isArray(data.content)) {
        shipments.value = data.content
        updatePageInfo({
          page: data.page,
          size: data.size,
          totalElements: data.totalElements,
        })
      } else if (Array.isArray(data)) {
        // Backward compatibility: non-paginated response
        shipments.value = data
        updatePageInfo({ page: 0, size, totalElements: data.length })
      } else {
        shipments.value = []
        updatePageInfo({ page: 0, size, totalElements: 0 })
      }
    } catch {
      setMsg('Response was not valid JSON.')
      shipments.value = []
      updatePageInfo({ page: 0, size, totalElements: 0 })
    }
  } catch {
    setMsg('Cannot reach the server. Ensure the backend is running and /api is proxied.')
    shipments.value = []
    updatePageInfo({ page: 0, size, totalElements: 0 })
  } finally {
    loading.value = false
  }
}

function handlePageChange(page) {
  setPage(page)
  loadShipments(page)
}

function handlePageSizeChange(newSize) {
  resetPagination()
  loadShipments(0, newSize)
}

async function loadFormOptions() {
  if (!isLoggedIn.value) return
  try {
    let supRes
    if (currentUser.value?.role === 'SUPPLIER') {
      supRes = await apiFetch('/api/suppliers/me')
    } else {
      supRes = await apiFetch('/api/suppliers')
    }

    const modeRes = await apiFetch('/api/transport-modes')

    const supText = await supRes.text()
    const modeText = await modeRes.text()

    if (supRes.ok) {
      try {
        if (currentUser.value?.role === 'SUPPLIER') {
          const supplierData = supText ? JSON.parse(supText) : null
          suppliers.value = supplierData ? [supplierData] : []
        } else {
          suppliers.value = supText ? JSON.parse(supText) : []
        }
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
  initFormFields({
    supplierId: '',
    transportModeId: '',
    origin: '',
    destination: '',
    distanceKm: '',
    cargoWeightTons: '',
    shipmentDate: todayIsoDate(),
  })

  dialogOpen.value = true
  loadFormOptions()

  setTimeout(() => {
    if (currentUser.value?.role === 'SUPPLIER' && suppliers.value.length > 0) {
      form.supplierId = String(suppliers.value[0].id)
    }
  }, 100)
}

function openEdit(row) {
  editingId.value = row.id
  initFormFields({
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
  })
  dialogOpen.value = true
  loadFormOptions()
}

function closeDialog() {
  dialogOpen.value = false
  editingId.value = null
  showRecommendation.value = false
  recommendation.value = null
  smartRecommendation.value = null
  unifiedRecommendation.value = null
  resetFormValidation()
}

async function getRecommendation() {
  if (!form.transportModeId) {
    setMsg('Please select the current transport mode first.')
    return
  }

  const selectedMode = transportModes.value.find(m => String(m.id) === form.transportModeId)
  if (!selectedMode || !selectedMode.mode) {
    setMsg('Unable to get current transport mode information.')
    return
  }

  recommendationLoading.value = true
  showRecommendation.value = false
  recommendation.value = null
  message.value = ''

  try {
    const distance = form.distanceKm?.trim() || ''
    const weight = form.cargoWeightTons?.trim() || ''

    let url = `/api/recommend?current_mode=${encodeURIComponent(selectedMode.mode)}`
    if (distance) url += `&distance_km=${encodeURIComponent(distance)}`
    if (weight) url += `&cargo_weight_tons=${encodeURIComponent(weight)}`
    if (form.supplierId) url += `&supplier_id=${encodeURIComponent(form.supplierId)}`

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

async function getSmartRecommendation() {
  const origin = form.origin?.trim() || ''
  const destination = form.destination?.trim() || ''
  const weight = form.cargoWeightTons?.trim() || ''

  if (!origin && !destination) {
    setMsg('Please enter origin or destination for smart recommendation.')
    return
  }

  recommendationLoading.value = true
  smartRecommendation.value = null

  try {
    let url = `/api/recommend/smart?`
    if (origin) url += `origin=${encodeURIComponent(origin)}&`
    if (destination) url += `destination=${encodeURIComponent(destination)}&`
    if (weight) url += `cargo_weight_tons=${encodeURIComponent(weight)}`
    if (form.supplierId) url += `&supplier_id=${encodeURIComponent(form.supplierId)}`

    const res = await apiFetch(url)
    const text = await res.text()

    if (!res.ok) {
      setMsg(formatErrorBody(res.status, text))
      return
    }

    try {
      smartRecommendation.value = JSON.parse(text)
    } catch {
      setMsg('Failed to parse smart recommendation result.')
    }
  } catch {
    setMsg('Cannot connect to the server. Please check if the backend is running.')
  } finally {
    recommendationLoading.value = false
  }
}

async function getUnifiedRecommendation() {
  const origin = form.origin?.trim() || ''
  const destination = form.destination?.trim() || ''
  const weight = form.cargoWeightTons?.trim() || ''

  if (!form.transportModeId) {
    setMsg('Please select the current transport mode first.')
    return
  }

  const selectedMode = transportModes.value.find(m => String(m.id) === form.transportModeId)
  if (!selectedMode || !selectedMode.mode) {
    setMsg('Unable to get current transport mode information.')
    return
  }

  recommendationLoading.value = true
  unifiedRecommendation.value = null
  showRecommendation.value = false
  recommendation.value = null
  smartRecommendation.value = null
  message.value = ''

  try {
    const distance = form.distanceKm?.trim() || ''
    const weight = form.cargoWeightTons?.trim() || ''

    const [recommendRes, smartRecommendRes] = await Promise.all([
      apiFetch(`/api/recommend?current_mode=${encodeURIComponent(selectedMode.mode)}${distance ? `&distance_km=${encodeURIComponent(distance)}` : ''}${weight ? `&cargo_weight_tons=${encodeURIComponent(weight)}` : ''}${form.supplierId ? `&supplier_id=${encodeURIComponent(form.supplierId)}` : ''}`),
      apiFetch(`/api/recommend/smart?${origin ? `origin=${encodeURIComponent(origin)}&` : ''}${destination ? `destination=${encodeURIComponent(destination)}&` : ''}${weight ? `cargo_weight_tons=${encodeURIComponent(weight)}` : ''}${form.supplierId ? `&supplier_id=${encodeURIComponent(form.supplierId)}` : ''}`)
    ])

    const [recommendText, smartRecommendText] = await Promise.all([
      recommendRes.text(),
      smartRecommendRes.text()
    ])

    let recommendData = null
    let smartRecommendData = null

    if (recommendRes.ok) {
      try {
        recommendData = JSON.parse(recommendText)
      } catch {}
    }

    if (smartRecommendRes.ok) {
      try {
        smartRecommendData = JSON.parse(smartRecommendText)
      } catch {}
    }

    unifiedRecommendation.value = {
      algorithm: recommendData,
      smart: smartRecommendData,
      best: (() => {
        if (recommendData && smartRecommendData) {
          const algoEmission = recommendData.recommended_emission || 0
          const smartEmission = smartRecommendData.current_emission || 0
          return algoEmission < smartEmission ? recommendData : smartRecommendData
        }
        return recommendData || smartRecommendData
      })()
    }

    showRecommendation.value = true
  } catch {
    setMsg('Cannot connect to the server. Please check if the backend is running.')
  } finally {
    recommendationLoading.value = false
  }
}

async function saveShipment() {
  // 标记所有字段为已访问并验证
  touchFormField('supplierId')
  touchFormField('transportModeId')
  touchFormField('distanceKm')
  touchFormField('cargoWeightTons')
  touchFormField('shipmentDate')

  const isValid = await validateForm()
  if (!isValid) {
    setMsg('Please fix the errors above before submitting.')
    return
  }

  const distanceKm = Number(form.distanceKm)
  const cargoWeightTons = Number(form.cargoWeightTons)

  const body = {
    supplier: { id: Number(form.supplierId) },
    transportMode: { id: Number(form.transportModeId) },
    origin: form.origin?.trim() || '',
    destination: form.destination?.trim() || '',
    distanceKm,
    cargoWeightTons,
    shipmentDate: form.shipmentDate?.trim() || null,
  }

  message.value = ''
  try {
    const isEdit = editingId.value != null
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
      alert("No, you don't have permission to edit the transportation records of other suppliers")
      closeDialog()
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
      alert('You do not have permission to edit other suppliers\' transportation records')
      closeDialog()
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
          <button v-if="canModify" type="button" class="btn btn--primary" @click="openCreate">New shipment</button>
        </div>
      </div>

      <div class="table-wrap">
        <table class="data-table">
          <thead>
            <tr>
              <th scope="col">#</th>
              <th scope="col" class="col-id">ID</th>
              <th scope="col">Supplier</th>
              <th scope="col">Mode</th>
              <th scope="col">Origin</th>
              <th scope="col">Destination</th>
              <th scope="col">Distance (km)</th>
              <th scope="col">Weight (t)</th>
              <th scope="col">Date</th>
              <th scope="col">CO2e (kg)</th>
              <th v-if="canModify" scope="col" class="col-actions">Actions</th>
            </tr>
          </thead>
          <tbody>
            <!-- Loading skeleton -->
            <template v-if="loading">
              <tr v-for="i in 5" :key="'skeleton-' + i">
                <td colspan="11">
                  <div class="skeleton-table-cell skeleton-cell--full"></div>
                </td>
              </tr>
            </template>
            <!-- Empty state -->
            <tr v-else-if="shipments.length === 0">
              <td colspan="11" class="empty-cell">
                No shipments yet. Please add suppliers first, then click "New shipment" to create one.
              </td>
            </tr>
            <!-- Data rows -->
            <tr v-else v-for="(row, index) in shipments" :key="row.id">
              <td>{{ index + 1 }}</td>
              <td class="col-id">{{ row.id }}</td>
              <td>{{ supplierLabel(row) }}</td>
              <td>{{ modeLabel(row) }}</td>
              <td>{{ row.origin || '—' }}</td>
              <td>{{ row.destination || '—' }}</td>
              <td>{{ row.distanceKm != null ? Number(row.distanceKm).toFixed(2) : '—' }}</td>
              <td>{{ row.cargoWeightTons != null ? Number(row.cargoWeightTons).toFixed(2) : '—' }}</td>
              <td>{{ row.shipmentDate || '—' }}</td>
              <td>{{ row.calculatedCarbonEmission != null ? Number(row.calculatedCarbonEmission).toFixed(2) : '—' }}</td>
              <td v-if="canModify" class="col-actions">
                <button type="button" class="link-btn" @click="openEdit(row)">Edit</button>
                <button type="button" class="link-btn link-btn--danger" @click="removeRow(row)">
                  Delete
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <Pagination
        v-if="totalPages > 0"
        :current-page="currentPage"
        :total-pages="totalPages"
        :total-elements="totalElements"
        :page-size="pageSize"
        @page-change="handlePageChange"
        @page-size-change="handlePageSizeChange"
      />

      <div v-if="dialogOpen" class="dialog-backdrop" role="presentation" @click.self="closeDialog">
        <div class="dialog" role="dialog" aria-modal="true" aria-labelledby="shipment-dialog-title">
          <h2 id="shipment-dialog-title" class="dialog__title">
            {{ editingId != null ? 'Edit shipment' : 'New shipment' }}
          </h2>
          <form class="dialog__form" @submit.prevent="saveShipment" novalidate>
            <!-- Supplier Field -->
            <div class="form-field" :class="{ 'form-field--error': formTouched.supplierId && formErrors.supplierId }">
              <label for="field-supplier" class="form-field__label">
                Supplier <span class="form-field__required">*</span>
              </label>
              <div class="form-field__input-wrapper">
                <select
                  id="field-supplier"
                  v-model="form.supplierId"
                  class="form-field__input form-field__select"
                  :class="{ 'form-field__input--error': formTouched.supplierId && formErrors.supplierId }"
                  @blur="touchFormField('supplierId')"
                >
                  <option disabled value="">Select supplier</option>
                  <option v-for="s in suppliers" :key="s.id" :value="String(s.id)">
                    {{ s.name || `ID ${s.id}` }}
                  </option>
                </select>
              </div>
              <p v-if="formTouched.supplierId && formErrors.supplierId" class="form-field__message form-field__message--error" role="alert">
                {{ formErrors.supplierId }}
              </p>
            </div>

            <!-- Transport Mode Field -->
            <div class="form-field" :class="{ 'form-field--error': formTouched.transportModeId && formErrors.transportModeId }">
              <label for="field-mode" class="form-field__label">
                Transport mode <span class="form-field__required">*</span>
              </label>
              <div class="form-field__input-wrapper">
                <select
                  id="field-mode"
                  v-model="form.transportModeId"
                  class="form-field__input form-field__select"
                  :class="{ 'form-field__input--error': formTouched.transportModeId && formErrors.transportModeId }"
                  @blur="touchFormField('transportModeId')"
                >
                  <option disabled value="">Select mode</option>
                  <option v-for="m in transportModes" :key="m.id" :value="String(m.id)">
                    {{ m.displayName || m.mode || `ID ${m.id}` }}
                  </option>
                </select>
              </div>
              <p v-if="formTouched.transportModeId && formErrors.transportModeId" class="form-field__message form-field__message--error" role="alert">
                {{ formErrors.transportModeId }}
              </p>
            </div>

            <!-- Recommendation Buttons -->
            <div style="display: flex; gap: 0.5rem; flex-wrap: wrap;">
              <button
                type="button"
                class="btn btn--ghost"
                @click="getRecommendation"
                :disabled="!form.transportModeId || recommendationLoading"
                style="flex: 1;"
              >
                {{ recommendationLoading ? 'Recommending...' : 'Recommend' }}
              </button>
              <button
                type="button"
                class="btn btn--ghost"
                @click="getSmartRecommendation"
                :disabled="recommendationLoading"
                style="flex: 1;"
              >
                {{ recommendationLoading ? 'Loading...' : 'Smart' }}
              </button>
              <button
                type="button"
                class="btn btn--primary"
                @click="getUnifiedRecommendation"
                :disabled="!form.transportModeId || recommendationLoading"
                style="flex: 1;"
              >
                {{ recommendationLoading ? 'Analyzing...' : 'Unified' }}
              </button>
            </div>

            <!-- Recommendation Results -->
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
            </div>

            <div v-if="smartRecommendation" style="margin-top: 0.5rem; padding: 0.75rem; background: #fff8f0; border-radius: 6px; border: 1px solid #ffe0b2;">
              <h4 style="margin: 0 0 0.5rem; color: #e65100; font-size: 0.9rem;">Smart Recommendation (Based on History)</h4>
              <p style="margin: 0 0 0.25rem; font-size: 0.85rem;">
                <strong>Recommended transport mode:</strong> {{ smartRecommendation.best_mode || 'N/A' }}
              </p>
              <p style="margin: 0 0 0.25rem; font-size: 0.85rem;">
                <strong>Based on:</strong> {{ smartRecommendation.saving || 'Historical data' }}
              </p>
              <p style="margin: 0; font-size: 0.85rem;">
                <strong>Estimated emission:</strong> {{ smartRecommendation.current_emission ? smartRecommendation.current_emission.toFixed(2) : '0' }} {{ smartRecommendation.saving_amount_unit || 'kg CO2e' }}
              </p>
            </div>

            <div v-if="showRecommendation && unifiedRecommendation" style="margin-top: 0.5rem; padding: 0.75rem; background: #f0f5ff; border-radius: 6px; border: 1px solid #c3d4ff;">
              <h4 style="margin: 0 0 0.5rem; color: #1e40af; font-size: 0.9rem;">Unified Recommendation</h4>
              <div v-if="unifiedRecommendation.best" style="margin-bottom: 0.75rem;">
                <p style="margin: 0 0 0.25rem; font-size: 0.85rem;">
                  <strong>Best Recommendation:</strong> {{ unifiedRecommendation.best.best_mode || 'N/A' }}
                </p>
                <p style="margin: 0 0 0.25rem; font-size: 0.85rem;">
                  <strong>Estimated emission:</strong> {{ unifiedRecommendation.best.recommended_emission || unifiedRecommendation.best.current_emission ? (unifiedRecommendation.best.recommended_emission || unifiedRecommendation.best.current_emission).toFixed(2) : '0' }} kg CO2e
                </p>
              </div>
            </div>

            <!-- Origin Field -->
            <div class="form-field" :class="{ 'form-field--error': formTouched.origin && formErrors.origin }">
              <label for="field-origin" class="form-field__label">Origin</label>
              <div class="form-field__input-wrapper">
                <input
                  id="field-origin"
                  v-model="form.origin"
                  class="form-field__input"
                  type="text"
                  autocomplete="off"
                  :class="{ 'form-field__input--error': formTouched.origin && formErrors.origin }"
                  placeholder="Enter origin location"
                  @blur="touchFormField('origin')"
                />
              </div>
              <p v-if="formTouched.origin && formErrors.origin" class="form-field__message form-field__message--error" role="alert">
                {{ formErrors.origin }}
              </p>
            </div>

            <!-- Destination Field -->
            <div class="form-field" :class="{ 'form-field--error': formTouched.destination && formErrors.destination }">
              <label for="field-destination" class="form-field__label">Destination</label>
              <div class="form-field__input-wrapper">
                <input
                  id="field-destination"
                  v-model="form.destination"
                  class="form-field__input"
                  type="text"
                  autocomplete="off"
                  :class="{ 'form-field__input--error': formTouched.destination && formErrors.destination }"
                  placeholder="Enter destination location"
                  @blur="touchFormField('destination')"
                />
              </div>
              <p v-if="formTouched.destination && formErrors.destination" class="form-field__message form-field__message--error" role="alert">
                {{ formErrors.destination }}
              </p>
            </div>

            <!-- Distance Field -->
            <div class="form-field" :class="{ 'form-field--error': formTouched.distanceKm && formErrors.distanceKm }">
              <label for="field-distance" class="form-field__label">
                Distance (km) <span class="form-field__required">*</span>
              </label>
              <div class="form-field__input-wrapper">
                <input
                  id="field-distance"
                  v-model="form.distanceKm"
                  class="form-field__input"
                  type="text"
                  inputmode="decimal"
                  :class="{ 'form-field__input--error': formTouched.distanceKm && formErrors.distanceKm }"
                  placeholder="Enter distance in kilometers"
                  @blur="touchFormField('distanceKm')"
                />
                <span
                  v-if="formTouched.distanceKm && !formErrors.distanceKm && form.distanceKm"
                  class="form-field__icon form-field__icon--success"
                  aria-hidden="true"
                >
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
                    <path d="M20 6L9 17l-5-5" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
                  </svg>
                </span>
                <span
                  v-else-if="formTouched.distanceKm && formErrors.distanceKm"
                  class="form-field__icon form-field__icon--error"
                  aria-hidden="true"
                >
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
                    <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
                    <path d="M12 8v4" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                    <circle cx="12" cy="16" r="1" fill="currentColor"/>
                  </svg>
                </span>
              </div>
              <p v-if="formTouched.distanceKm && formErrors.distanceKm" class="form-field__message form-field__message--error" role="alert">
                {{ formErrors.distanceKm }}
              </p>
            </div>

            <!-- Cargo Weight Field -->
            <div class="form-field" :class="{ 'form-field--error': formTouched.cargoWeightTons && formErrors.cargoWeightTons }">
              <label for="field-weight" class="form-field__label">
                Cargo weight (tons) <span class="form-field__required">*</span>
              </label>
              <div class="form-field__input-wrapper">
                <input
                  id="field-weight"
                  v-model="form.cargoWeightTons"
                  class="form-field__input"
                  type="text"
                  inputmode="decimal"
                  :class="{ 'form-field__input--error': formTouched.cargoWeightTons && formErrors.cargoWeightTons }"
                  placeholder="Enter cargo weight in tons"
                  @blur="touchFormField('cargoWeightTons')"
                />
                <span
                  v-if="formTouched.cargoWeightTons && !formErrors.cargoWeightTons && form.cargoWeightTons"
                  class="form-field__icon form-field__icon--success"
                  aria-hidden="true"
                >
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
                    <path d="M20 6L9 17l-5-5" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
                  </svg>
                </span>
                <span
                  v-else-if="formTouched.cargoWeightTons && formErrors.cargoWeightTons"
                  class="form-field__icon form-field__icon--error"
                  aria-hidden="true"
                >
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
                    <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
                    <path d="M12 8v4" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                    <circle cx="12" cy="16" r="1" fill="currentColor"/>
                  </svg>
                </span>
              </div>
              <p v-if="formTouched.cargoWeightTons && formErrors.cargoWeightTons" class="form-field__message form-field__message--error" role="alert">
                {{ formErrors.cargoWeightTons }}
              </p>
            </div>

            <!-- Shipment Date Field -->
            <div class="form-field" :class="{ 'form-field--error': formTouched.shipmentDate && formErrors.shipmentDate }">
              <label for="field-date" class="form-field__label">Shipment date</label>
              <div class="form-field__input-wrapper">
                <input
                  id="field-date"
                  v-model="form.shipmentDate"
                  class="form-field__input"
                  type="date"
                  :class="{ 'form-field__input--error': formTouched.shipmentDate && formErrors.shipmentDate }"
                  @blur="touchFormField('shipmentDate')"
                />
              </div>
              <p v-if="formTouched.shipmentDate && formErrors.shipmentDate" class="form-field__message form-field__message--error" role="alert">
                {{ formErrors.shipmentDate }}
              </p>
            </div>

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

.col-id {
  display: none;
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

.dialog__actions {
  display: flex;
  justify-content: flex-end;
  gap: 0.5rem;
  margin-top: 0.5rem;
}

/* Form Field Styles */
.form-field {
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
}

.form-field__label {
  font-size: 0.85rem;
  font-weight: 650;
  color: #4a5c4a;
  display: flex;
  align-items: center;
  gap: 0.25rem;
}

.form-field__required {
  color: #dc2626;
  font-weight: 700;
  font-size: 0.9em;
}

.form-field__input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.form-field__input {
  width: 100%;
  padding: 0.5rem 0.6rem;
  border: 1.5px solid #b5c4b5;
  border-radius: 6px;
  font-size: 0.95rem;
  font-family: inherit;
  color: #1a2e1a;
  background: #fafcf9;
  transition: border-color 0.15s ease, box-shadow 0.15s ease;
  outline: none;
}

.form-field__input:focus {
  border-color: #528951;
  background: #fff;
  box-shadow: 0 0 0 3px rgba(82, 137, 81, 0.12);
}

.form-field__input--error {
  border-color: #dc2626;
  background: #fef2f2;
}

.form-field__input--error:focus {
  border-color: #dc2626;
  box-shadow: 0 0 0 3px rgba(220, 38, 38, 0.1);
}

.form-field__input--success {
  border-color: #16a34a;
  background: #f0fdf4;
}

.form-field__select {
  padding-right: 2.25rem;
  cursor: pointer;
  appearance: none;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='12' viewBox='0 0 24 24' fill='none' stroke='%235f795f' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'%3E%3Cpath d='M6 9l6 6 6-6'/%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 0.6rem center;
}

.form-field__icon {
  position: absolute;
  right: 0.6rem;
  display: flex;
  align-items: center;
  justify-content: center;
  pointer-events: none;
}

.form-field__icon--success {
  color: #16a34a;
}

.form-field__icon--error {
  color: #dc2626;
}

.form-field__message {
  margin: 0;
  font-size: 0.8rem;
  line-height: 1.4;
}

.form-field__message--error {
  color: #dc2626;
}

.form-field__message--hint {
  color: #6b7d6b;
}

/* Error shake animation */
.form-field--error .form-field__input {
  animation: shake 0.4s ease-in-out;
}

@keyframes shake {
  0%, 100% { transform: translateX(0); }
  20%, 60% { transform: translateX(-4px); }
  40%, 80% { transform: translateX(4px); }
}

/* Table skeleton loading */
.skeleton-table-cell {
  height: 1rem;
  border-radius: 4px;
  background: linear-gradient(90deg, #e8efe8 0%, #d4e4d4 40%, #e8efe8 80%);
  background-size: 200% 100%;
  animation: shimmer 1.5s ease-in-out infinite;
}

.skeleton-cell--full {
  height: 1rem;
  width: 100%;
}

@keyframes shimmer {
  0% {
    background-position: 200% 0;
  }
  100% {
    background-position: -200% 0;
  }
}
</style>
