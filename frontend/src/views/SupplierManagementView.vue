<script setup>
import { ref, computed, watch } from 'vue'
import { useAuth } from '../composables/useAuth'
import { formatErrorBody } from '../utils/apiError'

const { apiAuthHeader, currentUser } = useAuth()

const isLoggedIn = computed(() => Boolean(currentUser.value?.username))

const suppliers = ref([])
const loading = ref(false)
const message = ref('')
const messageKind = ref('err')

const certifiedOnly = ref(false)

const dialogOpen = ref(false)
const editingId = ref(null)
const form = ref({
  name: '',
  country: '',
  contactEmail: '',
  emissionFactorPerUnit: '',
  hasEnvironmentalCertification: false,
})

const displayedRows = computed(() => {
  if (!certifiedOnly.value) return suppliers.value
  return suppliers.value.filter((s) => s.hasEnvironmentalCertification === true)
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

async function loadList() {
  if (!isLoggedIn.value) return
  loading.value = true
  message.value = ''
  try {
    const url = certifiedOnly.value ? '/api/suppliers/certified' : '/api/suppliers'
    const res = await apiFetch(url)
    const text = await res.text()
    if (!res.ok) {
      setMsg(formatErrorBody(res.status, text))
      suppliers.value = []
      return
    }
    try {
      suppliers.value = text ? JSON.parse(text) : []
    } catch {
      setMsg('Response was not valid JSON.')
      suppliers.value = []
    }
  } catch {
    setMsg(
      'Cannot reach the server. Make sure the backend is running and /api is proxied (e.g. npm run dev).',
    )
    suppliers.value = []
  } finally {
    loading.value = false
  }
}

function openCreate() {
  // Check if user is a supplier
  if (currentUser.value?.role === 'SUPPLIER') {
    alert('You are a supplier, you can\'t create a new one')
    return
  }
  
  editingId.value = null
  form.value = {
    name: '',
    country: '',
    contactEmail: '',
    emissionFactorPerUnit: '',
    hasEnvironmentalCertification: false,
  }
  dialogOpen.value = true
}

function openEdit(row) {
  editingId.value = row.id
  form.value = {
    name: row.name ?? '',
    country: row.country ?? '',
    contactEmail: row.contactEmail ?? '',
    emissionFactorPerUnit:
      row.emissionFactorPerUnit != null && !Number.isNaN(Number(row.emissionFactorPerUnit))
        ? String(row.emissionFactorPerUnit)
        : '',
    hasEnvironmentalCertification: Boolean(row.hasEnvironmentalCertification),
  }
  dialogOpen.value = true
}

function closeDialog() {
  dialogOpen.value = false
}

function bodyFromForm() {
  const ef = form.value.emissionFactorPerUnit.trim()
  const emissionFactorPerUnit = ef === '' ? null : Number(ef)
  return {
    name: form.value.name.trim(),
    country: form.value.country.trim(),
    contactEmail: form.value.contactEmail.trim(),
    hasEnvironmentalCertification: Boolean(form.value.hasEnvironmentalCertification),
    emissionFactorPerUnit: Number.isFinite(emissionFactorPerUnit) ? emissionFactorPerUnit : null,
  }
}

async function saveSupplier() {
  const body = bodyFromForm()
  if (!body.name) {
    setMsg('Please enter a supplier name.')
    return
  }
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
      alert("No, you don't have permission to edit information about other suppliers")
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
      alert('不行，你没有权限编辑其他供应商的信息')
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
  loadList()
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
        <label class="toolbar-check">
          <input v-model="certifiedOnly" type="checkbox" @change="onToggleCertifiedFilter" />
          Certified suppliers only
        </label>
        <div class="toolbar-actions">
          <button type="button" class="btn btn--ghost" :disabled="loading" @click="loadList">
            {{ loading ? 'Loading…' : 'Refresh' }}
          </button>
          <button type="button" class="btn btn--primary" @click="openCreate">New supplier</button>
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
              <th scope="col" class="col-actions">Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="!loading && displayedRows.length === 0">
              <td colspan="6" class="empty-cell">No data</td>
            </tr>
            <tr v-for="row in displayedRows" :key="row.id">
              <td>{{ row.name || '—' }}</td>
              <td>{{ row.country || '—' }}</td>
              <td>{{ row.hasEnvironmentalCertification ? 'Yes' : 'No' }}</td>
              <td>{{ row.emissionFactorPerUnit != null ? row.emissionFactorPerUnit : '—' }}</td>
              <td class="cell-email">{{ row.contactEmail || '—' }}</td>
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
        <div class="dialog" role="dialog" aria-modal="true" aria-labelledby="supplier-dialog-title">
          <h2 id="supplier-dialog-title" class="dialog__title">
            {{ editingId != null ? 'Edit supplier' : 'New supplier' }}
          </h2>
          <form class="dialog__form" @submit.prevent="saveSupplier">
            <label class="field">
              <span class="field__label">Name *</span>
              <input v-model="form.name" class="field__input" type="text" autocomplete="organization" />
            </label>
            <label class="field">
              <span class="field__label">Country / region</span>
              <input v-model="form.country" class="field__input" type="text" />
            </label>
            <label class="field">
              <span class="field__label">Contact email</span>
              <input v-model="form.contactEmail" class="field__input" type="email" autocomplete="off" />
            </label>
            <label class="field">
              <span class="field__label">Emission factor per unit (optional)</span>
              <input
                v-model="form.emissionFactorPerUnit"
                class="field__input"
                type="text"
                inputmode="decimal"
              />
            </label>
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
