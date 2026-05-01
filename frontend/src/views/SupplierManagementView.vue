<script setup>
import { ref, computed, watch } from 'vue'
import { useAuth } from '../composables/useAuth'
import { useFormValidation, ValidationRules } from '../composables/useFormValidation'
import { usePagination } from '../composables/usePagination'
import { useApiCache } from '../composables/useApiCache'
import Pagination from '../components/Pagination.vue'
import { formatErrorBody } from '../utils/apiError'

const { apiAuthHeader, currentUser } = useAuth()
const { get: getCache, set: setCache } = useApiCache()

const isLoggedIn = computed(() => Boolean(currentUser.value?.username))
const canModify = computed(() => currentUser.value?.role !== 'VIEWER')

const suppliers = ref([])
const loading = ref(false)
const message = ref('')
const messageKind = ref('err')

const certifiedOnly = ref(false)

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
  name: [
    ValidationRules.required('Please enter a supplier name'),
    ValidationRules.minLength(2, 'Name must be at least 2 characters'),
    ValidationRules.maxLength(100, 'Name must be at most 100 characters'),
  ],
  country: [
    ValidationRules.maxLength(100, 'Country must be at most 100 characters'),
  ],
  contactEmail: [
    ValidationRules.email('Please enter a valid email address'),
    ValidationRules.maxLength(100, 'Email must be at most 100 characters'),
  ],
  emissionFactorPerUnit: [
    ValidationRules.number('Please enter a valid number'),
    ValidationRules.nonNegativeNumber('Emission factor cannot be negative'),
  ],
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

async function loadList(pageToLoad = 0, size = pageSize.value) {
  if (!isLoggedIn.value) return
  loading.value = true
  message.value = ''
  try {
    const baseUrl = certifiedOnly.value ? '/api/suppliers/certified' : '/api/suppliers'
    const url = `${baseUrl}?page=${pageToLoad}&size=${size}`
    const res = await apiFetch(url)
    const text = await res.text()
    if (!res.ok) {
      setMsg(formatErrorBody(res.status, text))
      suppliers.value = []
      updatePageInfo({ page: 0, size, totalElements: 0 })
      return
    }
    try {
      const data = text ? JSON.parse(text) : null
      if (data && Array.isArray(data.content)) {
        suppliers.value = data.content
        updatePageInfo({
          page: data.page,
          size: data.size,
          totalElements: data.totalElements,
        })
      } else if (Array.isArray(data)) {
        suppliers.value = data
        updatePageInfo({ page: 0, size, totalElements: data.length })
      } else {
        suppliers.value = []
        updatePageInfo({ page: 0, size, totalElements: 0 })
      }
    } catch {
      setMsg('Response was not valid JSON.')
      suppliers.value = []
      updatePageInfo({ page: 0, size, totalElements: 0 })
    }
  } catch {
    setMsg(
      'Cannot reach the server. Make sure the backend is running and /api is proxied (e.g. npm run dev).',
    )
    suppliers.value = []
    updatePageInfo({ page: 0, size, totalElements: 0 })
  } finally {
    loading.value = false
  }
}

function handlePageChange(page) {
  console.log('[Pagination] handlePageChange called with:', page)
  setPage(page)
  loadList(page)
}

function handlePageSizeChange(newSize) {
  resetPagination()
  loadList(0, newSize)
}

// Watch certifiedOnly changes to reload with new filter
watch(certifiedOnly, () => {
  resetPagination()
  loadList()
})

function openCreate() {
  if (currentUser.value?.role === 'SUPPLIER') {
    alert('You are a supplier, you can\'t create a new one')
    return
  }

  editingId.value = null
  initFormFields({
    name: '',
    country: '',
    contactEmail: '',
    emissionFactorPerUnit: '',
    hasEnvironmentalCertification: false,
  })
  dialogOpen.value = true
}

function openEdit(row) {
  editingId.value = row.id
  initFormFields({
    name: row.name ?? '',
    country: row.country ?? '',
    contactEmail: row.contactEmail ?? '',
    emissionFactorPerUnit:
      row.emissionFactorPerUnit != null && !Number.isNaN(Number(row.emissionFactorPerUnit))
        ? String(row.emissionFactorPerUnit)
        : '',
    hasEnvironmentalCertification: Boolean(row.hasEnvironmentalCertification),
  })
  dialogOpen.value = true
}

function closeDialog() {
  dialogOpen.value = false
  editingId.value = null
  resetFormValidation()
}

function bodyFromForm() {
  const ef = form.emissionFactorPerUnit?.trim() || ''
  const emissionFactorPerUnit = ef === '' ? null : Number(ef)
  return {
    name: form.name?.trim() || '',
    country: form.country?.trim() || '',
    contactEmail: form.contactEmail?.trim() || '',
    hasEnvironmentalCertification: Boolean(form.hasEnvironmentalCertification),
    emissionFactorPerUnit: Number.isFinite(emissionFactorPerUnit) ? emissionFactorPerUnit : null,
  }
}

async function saveSupplier() {
  // 标记所有字段为已访问并验证
  touchFormField('name')
  touchFormField('country')
  touchFormField('contactEmail')
  touchFormField('emissionFactorPerUnit')

  const isValid = await validateForm()
  if (!isValid) {
    setMsg('Please fix the errors above before submitting.')
    return
  }

  const body = bodyFromForm()

  message.value = ''
  try {
    const isEdit = editingId.value != null
    const res = await apiFetch(
      isEdit ? `/api/suppliers/${editingId.value}` : '/api/suppliers',
      {
        method: isEdit ? 'PUT' : 'POST',
        body: JSON.stringify(body),
      },
    )
    const text = await res.text()
    if (res.status === 401) {
      setMsg('Unauthorized. Please log in again.')
      return
    }
    if (res.status === 403) {
      alert("You don't have permission to edit information about other suppliers")
      closeDialog()
      return
    }
    if (!res.ok) {
      setMsg(formatErrorBody(res.status, text))
      return
    }
    setMsg(isEdit ? 'Changes saved.' : 'Supplier created.', 'ok')
    closeDialog()
    await loadList()
  } catch {
    setMsg('Request failed. Check your network connection.')
  }
}

async function removeRow(row) {
  if (
    !window.confirm(
      `Delete supplier "${row.name || row.id}"? Related shipments may be affected.`,
    )
  )
    return
  message.value = ''
  try {
    const res = await apiFetch(`/api/suppliers/${row.id}`, { method: 'DELETE' })
    const text = await res.text()
    if (res.status === 401) {
      setMsg('Unauthorized. Please log in again.')
      return
    }
    if (res.status === 403) {
      alert('You do not have permission to edit other suppliers\' information')
      closeDialog()
      return
    }
    if (!res.ok && res.status !== 204) {
      setMsg(formatErrorBody(res.status, text))
      return
    }
    setMsg('Deleted.', 'ok')
    await loadList()
  } catch {
    setMsg('Delete failed.')
  }
}

function onToggleCertifiedFilter() {
  // Handled by watch on certifiedOnly
}

watch(
  isLoggedIn,
  (loggedIn) => {
    if (loggedIn) loadList()
    else {
      suppliers.value = []
      message.value = ''
      dialogOpen.value = false
    }
  },
  { immediate: true },
)
</script>

<template>
  <div class="supplier-page">
    <template v-if="!isLoggedIn">
      <header class="supplier-page__head">
        <h1 class="supplier-page__title">Supplier Management</h1>
      </header>
      <p class="guest-notice" role="status">You need to log in to be able to view this page</p>
    </template>

    <template v-else>
      <header class="supplier-page__head">
        <h1 class="supplier-page__title">Supplier Management</h1>
      </header>

      <p
        v-if="message"
        class="supplier-page__alert"
        :class="{ 'supplier-page__alert--ok': messageKind === 'ok' }"
        role="status"
      >
        {{ message }}
      </p>

      <div class="toolbar">
        <label v-if="canModify" class="toolbar-check">
          <input v-model="certifiedOnly" type="checkbox" @change="onToggleCertifiedFilter" />
          Certified suppliers only
        </label>
        <div class="toolbar-actions">
          <button type="button" class="btn btn--ghost" :disabled="loading" @click="loadList">
            {{ loading ? 'Loading…' : 'Refresh' }}
          </button>
          <button v-if="canModify" type="button" class="btn btn--primary" @click="openCreate">New supplier</button>
        </div>
      </div>

      <div class="table-wrap">
        <table class="data-table">
          <thead>
            <tr>
              <th scope="col">Name</th>
              <th scope="col">Country / region</th>
              <th scope="col">Certified</th>
              <th scope="col">Emission factor</th>
              <th scope="col">Contact email</th>
              <th v-if="canModify" scope="col" class="col-actions">Actions</th>
            </tr>
          </thead>
          <tbody>
            <!-- Loading skeleton -->
            <template v-if="loading">
              <tr v-for="i in 5" :key="'skeleton-' + i">
                <td colspan="6">
                  <div class="skeleton-table-cell skeleton-cell--full"></div>
                </td>
              </tr>
            </template>
            <!-- Empty state -->
            <tr v-else-if="suppliers.length === 0">
              <td colspan="6" class="empty-cell">No data</td>
            </tr>
            <!-- Data rows -->
            <tr v-else v-for="row in suppliers" :key="row.id">
              <td>{{ row.name || '—' }}</td>
              <td>{{ row.country || '—' }}</td>
              <td>{{ row.hasEnvironmentalCertification ? 'Yes' : 'No' }}</td>
              <td>{{ row.emissionFactorPerUnit != null ? row.emissionFactorPerUnit : '—' }}</td>
              <td class="cell-email">{{ row.contactEmail || '—' }}</td>
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
        <div class="dialog" role="dialog" aria-modal="true" aria-labelledby="supplier-dialog-title">
          <h2 id="supplier-dialog-title" class="dialog__title">
            {{ editingId != null ? 'Edit supplier' : 'New supplier' }}
          </h2>
          <form class="dialog__form" @submit.prevent="saveSupplier" novalidate>
            <!-- Name Field -->
            <div class="form-field" :class="{ 'form-field--error': formTouched.name && formErrors.name }">
              <label for="supplier-name" class="form-field__label">
                Name <span class="form-field__required">*</span>
              </label>
              <div class="form-field__input-wrapper">
                <input
                  id="supplier-name"
                  v-model="form.name"
                  class="form-field__input"
                  type="text"
                  autocomplete="organization"
                  :class="{ 'form-field__input--error': formTouched.name && formErrors.name }"
                  placeholder="Enter supplier name"
                  @blur="touchFormField('name')"
                />
                <span
                  v-if="formTouched.name && !formErrors.name && form.name"
                  class="form-field__icon form-field__icon--success"
                  aria-hidden="true"
                >
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
                    <path d="M20 6L9 17l-5-5" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
                  </svg>
                </span>
                <span
                  v-else-if="formTouched.name && formErrors.name"
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
              <p v-if="formTouched.name && formErrors.name" class="form-field__message form-field__message--error" role="alert">
                {{ formErrors.name }}
              </p>
            </div>

            <!-- Country Field -->
            <div class="form-field" :class="{ 'form-field--error': formTouched.country && formErrors.country }">
              <label for="supplier-country" class="form-field__label">Country / region</label>
              <div class="form-field__input-wrapper">
                <input
                  id="supplier-country"
                  v-model="form.country"
                  class="form-field__input"
                  type="text"
                  :class="{ 'form-field__input--error': formTouched.country && formErrors.country }"
                  placeholder="Enter country or region"
                  @blur="touchFormField('country')"
                />
              </div>
              <p v-if="formTouched.country && formErrors.country" class="form-field__message form-field__message--error" role="alert">
                {{ formErrors.country }}
              </p>
            </div>

            <!-- Email Field -->
            <div class="form-field" :class="{ 'form-field--error': formTouched.contactEmail && formErrors.contactEmail }">
              <label for="supplier-email" class="form-field__label">Contact email</label>
              <div class="form-field__input-wrapper">
                <input
                  id="supplier-email"
                  v-model="form.contactEmail"
                  class="form-field__input"
                  type="email"
                  autocomplete="off"
                  :class="{ 'form-field__input--error': formTouched.contactEmail && formErrors.contactEmail }"
                  placeholder="contact@supplier.com"
                  @blur="touchFormField('contactEmail')"
                />
                <span
                  v-if="formTouched.contactEmail && !formErrors.contactEmail && form.contactEmail"
                  class="form-field__icon form-field__icon--success"
                  aria-hidden="true"
                >
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
                    <path d="M20 6L9 17l-5-5" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
                  </svg>
                </span>
                <span
                  v-else-if="formTouched.contactEmail && formErrors.contactEmail"
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
              <p v-if="formTouched.contactEmail && formErrors.contactEmail" class="form-field__message form-field__message--error" role="alert">
                {{ formErrors.contactEmail }}
              </p>
            </div>

            <!-- Emission Factor Field -->
            <div class="form-field" :class="{ 'form-field--error': formTouched.emissionFactorPerUnit && formErrors.emissionFactorPerUnit }">
              <label for="supplier-emission" class="form-field__label">
                Emission factor per unit
              </label>
              <div class="form-field__input-wrapper">
                <input
                  id="supplier-emission"
                  v-model="form.emissionFactorPerUnit"
                  class="form-field__input"
                  type="text"
                  inputmode="decimal"
                  :class="{ 'form-field__input--error': formTouched.emissionFactorPerUnit && formErrors.emissionFactorPerUnit }"
                  placeholder="e.g., 0.5"
                  @blur="touchFormField('emissionFactorPerUnit')"
                />
                <span
                  v-if="formTouched.emissionFactorPerUnit && !formErrors.emissionFactorPerUnit && form.emissionFactorPerUnit"
                  class="form-field__icon form-field__icon--success"
                  aria-hidden="true"
                >
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
                    <path d="M20 6L9 17l-5-5" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
                  </svg>
                </span>
                <span
                  v-else-if="formTouched.emissionFactorPerUnit && formErrors.emissionFactorPerUnit"
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
              <p v-if="formTouched.emissionFactorPerUnit && formErrors.emissionFactorPerUnit" class="form-field__message form-field__message--error" role="alert">
                {{ formErrors.emissionFactorPerUnit }}
              </p>
              <p v-else class="form-field__message form-field__message--hint">
                Optional: emissions per production unit
              </p>
            </div>

            <!-- Environmental Certification Checkbox -->
            <label class="field field--inline">
              <input v-model="form.hasEnvironmentalCertification" type="checkbox" />
              <span class="field__label">Has environmental certification</span>
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
.supplier-page {
  flex: 0 0 auto;
  width: 100%;
  padding: 1.25rem 1.5rem;
  background: #fff;
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.supplier-page__head {
  margin: 0;
}

.supplier-page__title {
  margin: 0 0 0.5rem;
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

.inline-code {
  font-family: ui-monospace, monospace;
  font-size: 0.85em;
  background: #eef4ee;
  padding: 0.1em 0.35em;
  border-radius: 4px;
}

.supplier-page__alert {
  margin: 0;
  padding: 0.65rem 0.9rem;
  border-radius: 6px;
  background: #fef2f2;
  color: #991b1b;
  font-size: 0.9rem;
}

.supplier-page__alert--ok {
  background: #ecfdf3;
  color: #166434;
}

.toolbar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: 0.75rem;
}

.toolbar-check {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.9rem;
  color: #3d4a3d;
  cursor: pointer;
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
  font-size: 0.9rem;
}

.data-table th,
.data-table td {
  padding: 0.55rem 0.75rem;
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

.cell-email {
  max-width: 14rem;
  overflow-wrap: anywhere;
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
  background: #fff;
  border-radius: 10px;
  padding: 1.25rem 1.35rem;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.12);
  border: 1px solid #c5d6c5;
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

/* Inline field (for checkbox) */
.field {
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
}

.field--inline {
  flex-direction: row;
  align-items: center;
  gap: 0.5rem;
}

.field__label {
  font-size: 0.85rem;
  font-weight: 600;
  color: #4a5c4a;
  cursor: pointer;
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
