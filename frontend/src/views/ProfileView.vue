<script setup>
import { computed, onMounted, ref, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useAuth } from '../composables/useAuth'
import UserAvatar from '../components/UserAvatar.vue'

const router = useRouter()
const { currentUser, logout, setLoggedIn, apiAuthHeader } = useAuth()

/** Match avatar column width so role, avatar, name, and email align */
const avatarSizePx = 208

const roleLabel = computed(() => {
  const r = currentUser.value?.role ? String(currentUser.value.role) : ''
  if (!r) return ''
  if (r === 'SUSTAINABILITY_MANAGER') return 'Sustainability manager'
  if (r === 'SUPPLIER') return 'Supplier'
  if (r === 'VIEWER') return 'Viewer'
  if (r === 'ADMIN') return 'Admin'
  return r
})

// Supplier-specific state
const supplierInfo = ref(null)
const supplierShipments = ref([])
const supplierEmissions = ref(0)
const loading = ref(false)
const message = ref('')
const messageKind = ref('err')
const editMode = ref(false)
const editForm = ref({})

// Transport recommendation state
const showRecommendation = ref(false)
const recommendation = ref(null)
const smartRecommendation = ref(null)
const unifiedRecommendation = ref(null)
const recommendationLoading = ref(false)
const recommendationForm = ref({
  origin: '',
  destination: '',
  distanceKm: '',
  cargoWeightTons: ''
})

// City options by country
const cityOptionsByCountry = {
  'United States': [
    'New York', 'Los Angeles', 'Chicago', 'Houston', 'Phoenix', 'Philadelphia', 'San Antonio', 'San Diego', 'Dallas', 'San Jose',
    'Austin', 'Jacksonville', 'Fort Worth', 'Columbus', 'Charlotte', 'San Francisco', 'Indianapolis', 'Seattle', 'Denver', 'Washington'
  ],
  'Canada': [
    'Toronto', 'Montreal', 'Vancouver', 'Calgary', 'Edmonton', 'Ottawa', 'Winnipeg', 'Quebec City', 'Hamilton', 'Kitchener',
    'London', 'Victoria', 'Halifax', 'Oshawa', 'Windsor', 'Saskatoon', 'Regina', 'St. Johns', 'Barrie', 'Kelowna'
  ],
  'Mexico': [
    'Mexico City', 'Guadalajara', 'Monterrey', 'Puebla', 'Tijuana', 'León', 'Juárez', 'Zapopan', 'Merida', 'Culiacán',
    'Chihuahua', 'Saltillo', 'Torreón', 'Mazatlán', 'Aguascalientes', 'Hermosillo', 'San Luis Potosí', 'Toluca', 'Tampico', 'Morelia'
  ],
  'Germany': [
    'Berlin', 'Hamburg', 'Munich', 'Cologne', 'Frankfurt', 'Stuttgart', 'Düsseldorf', 'Dortmund', 'Essen', 'Leipzig',
    'Bremen', 'Dresden', 'Hanover', 'Nuremberg', 'Duisburg', 'Bochum', 'Wuppertal', 'Bielefeld', 'Bonn', 'Münster'
  ],
  'France': [
    'Paris', 'Marseille', 'Lyon', 'Toulouse', 'Nice', 'Nantes', 'Strasbourg', 'Montpellier', 'Bordeaux', 'Lille',
    'Rennes', 'Reims', 'Le Havre', 'Saint-Étienne', 'Toulon', 'Grenoble', 'Dijon', 'Angers', 'Nîmes', 'Villeurbanne'
  ],
  'Italy': [
    'Rome', 'Milan', 'Naples', 'Turin', 'Palermo', 'Genoa', 'Bologna', 'Florence', 'Venice', 'Verona',
    'Bari', 'Catania', 'Messina', 'Padua', 'Trieste', 'Taranto', 'Prato', 'Modena', 'Reggio Emilia', 'Parma'
  ],
  'Sweden': [
    'Stockholm', 'Gothenburg', 'Malmö', 'Uppsala', 'Linköping', 'Västerås', 'Örebro', 'Helsingborg', 'Jönköping', 'Norrköping',
    'Lund', 'Umeå', 'Gävle', 'Södertälje', 'Eskilstuna', 'Borås', 'Täby', 'Halmstad', 'Växjö', 'Karlstad'
  ],
  'Japan': [
    'Tokyo', 'Yokohama', 'Osaka', 'Nagoya', 'Sapporo', 'Fukuoka', 'Kobe', 'Kyoto', 'Kawasaki', 'Saitama',
    'Hiroshima', 'Sendai', 'Chiba', 'Kitakyushu', 'Tokushima', 'Okayama', 'Fukushima', 'Nagasaki', 'Kumamoto', 'Kagoshima'
  ],
  'China': [
    'Shanghai', 'Beijing', 'Guangzhou', 'Shenzhen', 'Chengdu', 'Wuhan', 'Tianjin', 'Chongqing', 'Nanjing', 'Hangzhou',
    'Suzhou', 'Dalian', 'Qingdao', 'Xiamen', 'Changsha', 'Nanning', 'Kunming', 'Xian', 'Wuxi', 'Fuzhou'
  ],
  'India': [
    'Mumbai', 'Delhi', 'Bangalore', 'Hyderabad', 'Chennai', 'Kolkata', 'Ahmedabad', 'Pune', 'Jaipur', 'Lucknow',
    'Kanpur', 'Nagpur', 'Indore', 'Thane', 'Bhopal', 'Visakhapatnam', 'Pimpri-Chinchwad', 'Patna', 'Vadodara', 'Ghaziabad'
  ],
  'South Korea': [
    'Seoul', 'Busan', 'Incheon', 'Daegu', 'Daejeon', 'Gwangju', 'Suwon', 'Ulsan', 'Changwon', 'Goyang',
    'Ansan', 'Seongnam', 'Bucheon', 'Jeonju', 'Cheongju', 'Pohang', 'Gimhae', 'Dangjin', 'Guri', 'Yongin'
  ],
  'Australia': [
    'Sydney', 'Melbourne', 'Brisbane', 'Perth', 'Adelaide', 'Gold Coast', 'Newcastle', 'Canberra', 'Wollongong', 'Logan City',
    'Geelong', 'Hobart', 'Townsville', 'Cairns', 'Darwin', 'Toowoomba', 'Ballarat', 'Bendigo', 'Albury-Wodonga', 'Launceston'
  ],
  'New Zealand': [
    'Auckland', 'Wellington', 'Christchurch', 'Hamilton', 'Tauranga', 'Napier-Hastings', 'Dunedin', 'Palmerston North', 'Nelson', 'Rotorua',
    'New Plymouth', 'Whangarei', 'Invercargill', 'Gisborne', 'Whanganui', 'Timaru', 'Blenheim', 'Queenstown', 'Rangiora', 'Taupo'
  ],
  'Brazil': [
    'São Paulo', 'Rio de Janeiro', 'Brasília', 'Salvador', 'Fortaleza', 'Belo Horizonte', 'Manaus', 'Curitiba', 'Recife', 'Porto Alegre',
    'Goiânia', 'Belém', 'Guarulhos', 'Campinas', 'São Luís', 'São Gonçalo', 'Maceió', 'Duque de Caxias', 'Natal', 'Nova Iguaçu'
  ],
  'Chile': [
    'Santiago', 'Valparaíso', 'Concepción', 'La Serena', 'Antofagasta', 'Iquique', 'Talca', 'Temuco', 'Arica', 'Coquimbo',
    'Rancagua', 'Chillán', 'Osorno', 'Puerto Montt', 'Viña del Mar', 'Valdivia', 'Quilpué', 'San Bernardo', 'Puente Alto', 'Maipú'
  ],
  'South Africa': [
    'Johannesburg', 'Cape Town', 'Durban', 'Pretoria', 'Port Elizabeth', 'Bloemfontein', 'East London', 'Pietermaritzburg', 'Kimberley', 'Polokwane',
    'Benoni', 'Brakpan', 'Germiston', 'Krugersdorp', 'Randburg', 'Roodepoort', 'Soweto', 'Springs', 'Vereeniging', 'Welkom'
  ],
  'Morocco': [
    'Casablanca', 'Rabat', 'Fes', 'Marrakech', 'Tangier', 'Agadir', 'Meknes', 'Oujda', 'Kenitra', 'Tetouan',
    'Safi', 'Mohammedia', 'Khouribga', 'El Jadida', 'Beni Mellal', 'Nador', 'Taza', 'Larache', 'Settat', 'Khemisset'
  ],
  'UK': [
    'London', 'Birmingham', 'Manchester', 'Leeds', 'Glasgow', 'Sheffield', 'Bradford', 'Liverpool', 'Edinburgh', 'Bristol',
    'Cardiff', 'Newcastle upon Tyne', 'Leicester', 'Nottingham', 'Portsmouth', 'Brighton', 'Southampton', 'Swansea', 'Aberdeen', 'Plymouth'
  ]
}

// Computed city options based on supplier's country
const originCityOptions = computed(() => {
  if (!supplierInfo.value || !supplierInfo.value.country) {
    return []
  }
  return cityOptionsByCountry[supplierInfo.value.country] || []
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

async function loadSupplierInfo() {
  if (!currentUser.value || currentUser.value.role !== 'SUPPLIER') return
  loading.value = true
  try {
    const res = await apiFetch('/api/suppliers/me')
    const text = await res.text()
    if (!res.ok) {
      setMsg('Failed to load supplier information')
      return
    }
    supplierInfo.value = text ? JSON.parse(text) : null
    editForm.value = { ...supplierInfo.value }
  } catch {
    setMsg('Failed to load supplier information')
  } finally {
    loading.value = false
  }
}

async function loadSupplierShipments() {
  if (!currentUser.value || currentUser.value.role !== 'SUPPLIER') return
  loading.value = true
  try {
    const res = await apiFetch('/api/shipments/my')
    const text = await res.text()
    if (!res.ok) {
      setMsg('Failed to load shipments')
      return
    }
    supplierShipments.value = text ? JSON.parse(text) : []
    calculateSupplierEmissions()
  } catch {
    setMsg('Failed to load shipments')
  } finally {
    loading.value = false
  }
}

function calculateSupplierEmissions() {
  supplierEmissions.value = supplierShipments.value.reduce((total, shipment) => {
    return total + (shipment.calculatedCarbonEmission || 0)
  }, 0)
}

async function saveSupplierInfo() {
  if (!supplierInfo.value) return
  loading.value = true
  try {
    const res = await apiFetch('/api/suppliers/me', {
      method: 'PUT',
      body: JSON.stringify(editForm.value)
    })
    const text = await res.text()
    if (!res.ok) {
      setMsg('Failed to save supplier information')
      return
    }
    supplierInfo.value = text ? JSON.parse(text) : null
    editMode.value = false
    setMsg('Changes saved successfully', 'ok')
  } catch {
    setMsg('Failed to save supplier information')
  } finally {
    loading.value = false
  }
}

// Transport recommendation methods
async function getRecommendation() {
  const origin = recommendationForm.value.origin.trim()
  const destination = recommendationForm.value.destination.trim()
  const distanceKm = Number(typeof recommendationForm.value.distanceKm === 'string' ? recommendationForm.value.distanceKm.trim() : recommendationForm.value.distanceKm)
  const cargoWeightTons = Number(typeof recommendationForm.value.cargoWeightTons === 'string' ? recommendationForm.value.cargoWeightTons.trim() : recommendationForm.value.cargoWeightTons)

  if (!origin || !destination) {
    setMsg('Please enter origin and destination.')
    return
  }
  if (!Number.isFinite(distanceKm) || distanceKm < 0) {
    setMsg('Please enter a valid distance (km).')
    return
  }
  if (!Number.isFinite(cargoWeightTons) || cargoWeightTons < 0) {
    setMsg('Please enter a valid cargo weight (tons).')
    return
  }

  recommendationLoading.value = true
  message.value = ''
  try {
    // Get supplier ID if available
    let supplierId = null
    if (supplierInfo.value && supplierInfo.value.id) {
      supplierId = supplierInfo.value.id
    }

    // Get smart recommendation (based on history)
    let smartRes = await apiFetch(`/api/recommend/smart?origin=${encodeURIComponent(origin)}&destination=${encodeURIComponent(destination)}&cargo_weight_tons=${encodeURIComponent(cargoWeightTons)}${supplierId ? `&supplier_id=${encodeURIComponent(supplierId)}` : ''}`)

    if (smartRes.ok) {
      const smartData = await smartRes.json()
      smartRecommendation.value = smartData
    }

    // For algorithm recommendation, we need to use a default current mode
    // since the API requires it
    const defaultMode = 'TRUCK'
    let recRes = await apiFetch(`/api/recommend?current_mode=${encodeURIComponent(defaultMode)}&distance_km=${encodeURIComponent(distanceKm)}&cargo_weight_tons=${encodeURIComponent(cargoWeightTons)}${supplierId ? `&supplier_id=${encodeURIComponent(supplierId)}` : ''}`)

    if (recRes.ok) {
      const recData = await recRes.json()
      recommendation.value = recData
    }

    // Calculate unified recommendation (similar to ShipmentTrackingView.vue)
    if (recommendation.value && smartRecommendation.value) {
      const algoEmission = recommendation.value.recommended_emission || 0
      const smartEmission = smartRecommendation.value.current_emission || 0
      const best = algoEmission < smartEmission ? recommendation.value : smartRecommendation.value
      
      unifiedRecommendation.value = {
        transportMode: {
          mode: best.best_mode,
          displayName: best.best_mode
        },
        emission: best.recommended_emission || best.current_emission || 0,
        savingsPercentage: best.saving ? parseFloat(best.saving.replace('%', '')) : 0
      }
    }

    showRecommendation.value = true
  } catch {
    setMsg('Failed to get recommendations')
  } finally {
    recommendationLoading.value = false
  }
}

function resetRecommendation() {
  showRecommendation.value = false
  recommendation.value = null
  smartRecommendation.value = null
  unifiedRecommendation.value = null
  recommendationForm.value = {
    origin: '',
    destination: '',
    distanceKm: '',
    cargoWeightTons: ''
  }
}

function onLogout() {
  logout()
  router.push({ name: 'home' })
}

/** Backfill email and/or role from API when missing in session */
onMounted(async () => {
  const u = currentUser.value
  if (!u?.username) return
  const hasEmail = u.email && String(u.email).trim() !== ''
  const hasRole = u.role && String(u.role).trim() !== ''
  if (hasEmail && hasRole) {
    if (u.role === 'SUPPLIER') {
      await loadSupplierInfo()
      await loadSupplierShipments()
    }
    return
  }
  try {
    const r = await fetch('/api/auth/account/' + encodeURIComponent(u.username))
    if (!r.ok) return
    const p = await r.json()
    const updatedUser = {
      username: p.username ?? u.username,
      email: hasEmail ? u.email : p.email != null && p.email !== '' ? String(p.email) : '',
      role: hasRole ? u.role : p.role != null ? String(p.role) : '',
    }
    setLoggedIn(updatedUser)
    if (updatedUser.role === 'SUPPLIER') {
      await loadSupplierInfo()
      await loadSupplierShipments()
    }
  } catch {
    /* ignore */
  }
})
</script>

<template>
  <main class="profile-page" aria-label="Profile">
    <section v-if="currentUser" class="profile">
      <div class="profile-head">
        <div class="profile-avatar-col" :style="{ width: avatarSizePx + 'px' }">
          <p v-if="roleLabel" class="profile-role">{{ roleLabel }}</p>
          <UserAvatar :size="avatarSizePx" class="profile-avatar" />
        </div>
        <div class="profile-text" :style="{ width: avatarSizePx + 'px' }">
          <p class="profile-name">{{ currentUser.username }}</p>
          <p class="profile-email">{{ currentUser.email || '—' }}</p>
        </div>
        <div class="profile-actions" :style="{ width: avatarSizePx + 'px' }">
          <button type="button" class="logout-btn" @click="onLogout">Log out</button>
        </div>
      </div>

      <!-- Supplier-specific section -->
      <div v-if="currentUser.role === 'SUPPLIER'" class="supplier-section">
        <h2 class="supplier-section__title">Supplier Profile</h2>
        
        <p v-if="message" class="supplier-section__alert" :class="{ 'supplier-section__alert--ok': messageKind === 'ok' }" role="status">
          {{ message }}
        </p>

        <!-- Supplier Info -->
        <div v-if="supplierInfo && !editMode" class="supplier-info">
          <div class="supplier-info__row">
            <span class="supplier-info__label">Company Name:</span>
            <span class="supplier-info__value">{{ supplierInfo.name || '—' }}</span>
          </div>
          <div class="supplier-info__row">
            <span class="supplier-info__label">Country:</span>
            <span class="supplier-info__value">{{ supplierInfo.country || '—' }}</span>
          </div>
          <div class="supplier-info__row">
            <span class="supplier-info__label">Contact Email:</span>
            <span class="supplier-info__value">{{ supplierInfo.contactEmail || '—' }}</span>
          </div>
          <div class="supplier-info__row">
            <span class="supplier-info__label">Emission Factor:</span>
            <span class="supplier-info__value">{{ supplierInfo.emissionFactorPerUnit != null ? supplierInfo.emissionFactorPerUnit : '—' }}</span>
          </div>
          <div class="supplier-info__row">
            <span class="supplier-info__label">Environmental Certification:</span>
            <span class="supplier-info__value">{{ supplierInfo.hasEnvironmentalCertification ? 'Yes' : 'No' }}</span>
          </div>
          <button type="button" class="btn btn--primary" @click="editMode = true">Edit Profile</button>
        </div>

        <!-- Edit Form -->
        <div v-else-if="supplierInfo && editMode" class="supplier-edit-form">
          <form @submit.prevent="saveSupplierInfo">
            <div class="form-row">
              <label class="form-label">
                Company Name
                <input v-model="editForm.name" class="form-input" type="text" />
              </label>
            </div>
            <div class="form-row">
              <label class="form-label">
                Country
                <input v-model="editForm.country" class="form-input" type="text" />
              </label>
            </div>
            <div class="form-row">
              <label class="form-label">
                Contact Email
                <input v-model="editForm.contactEmail" class="form-input" type="email" />
              </label>
            </div>
            <div class="form-row">
              <label class="form-label">
                Emission Factor per unit
                <input v-model="editForm.emissionFactorPerUnit" class="form-input" type="number" step="0.01" />
              </label>
            </div>
            <div class="form-row">
              <label class="form-checkbox">
                <input v-model="editForm.hasEnvironmentalCertification" type="checkbox" />
                <span>Has environmental certification</span>
              </label>
            </div>
            <div class="form-actions">
              <button type="button" class="btn btn--ghost" @click="editMode = false">Cancel</button>
              <button type="submit" class="btn btn--primary" :disabled="loading">
                {{ loading ? 'Saving…' : 'Save' }}
              </button>
            </div>
          </form>
        </div>

        <!-- Shipment Summary -->
        <div class="shipment-summary">
          <h3 class="shipment-summary__title">My Shipments</h3>
          <div class="shipment-summary__stats">
            <div class="stat-item">
              <span class="stat-label">Total Shipments:</span>
              <span class="stat-value">{{ supplierShipments.length }}</span>
            </div>
            <div class="stat-item">
              <span class="stat-label">Total CO2e:</span>
              <span class="stat-value">{{ supplierEmissions.toFixed(2) }} kg</span>
            </div>
          </div>
        </div>

        <!-- Shipment List -->
        <div class="shipment-list">
          <h3 class="shipment-list__title">Recent Shipments</h3>
          <div v-if="supplierShipments.length === 0" class="empty-state">
            No shipments found. Customers will create shipments associated with your company.
          </div>
          <div v-else class="shipment-table">
            <table>
              <thead>
                <tr>
                  <th>#</th>
                  <th>Origin</th>
                  <th>Destination</th>
                  <th>Mode</th>
                  <th>Date</th>
                  <th>CO2e (kg)</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(shipment, index) in supplierShipments" :key="shipment.id">
                  <td>{{ index + 1 }}</td>
                  <td>{{ shipment.origin || '—' }}</td>
                  <td>{{ shipment.destination || '—' }}</td>
                  <td>{{ shipment.transportMode?.displayName || shipment.transportMode?.mode || '—' }}</td>
                  <td>{{ shipment.shipmentDate || '—' }}</td>
                  <td>{{ shipment.calculatedCarbonEmission ? shipment.calculatedCarbonEmission.toFixed(2) : '—' }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <!-- Transport Recommendation -->
        <div class="transport-recommendation">
          <h3 class="transport-recommendation__title">Transport Mode Recommendation</h3>
          <div v-if="!showRecommendation" class="recommendation-form">
            <form @submit.prevent="getRecommendation">
              <div class="form-row">
                <label class="form-label">
                  Origin
                  <select v-model="recommendationForm.origin" class="form-input">
                    <option value="">Select a city</option>
                    <option v-for="city in originCityOptions" :key="city" :value="city">
                      {{ city }}, {{ supplierInfo?.country || '' }}
                    </option>
                  </select>
                </label>
              </div>
              <div class="form-row">
                <label class="form-label">
                  Destination
                  <input v-model="recommendationForm.destination" class="form-input" type="text" placeholder="e.g., Delhi, India" />
                </label>
              </div>
              <div class="form-row">
                <label class="form-label">
                  Distance (km)
                  <input v-model="recommendationForm.distanceKm" class="form-input" type="number" step="0.1" min="0" />
                </label>
              </div>
              <div class="form-row">
                <label class="form-label">
                  Cargo Weight (tons)
                  <input v-model="recommendationForm.cargoWeightTons" class="form-input" type="number" step="0.1" min="0" />
                </label>
              </div>
              <div class="form-actions">
                <button type="submit" class="btn btn--primary" :disabled="recommendationLoading">
                  {{ recommendationLoading ? 'Calculating…' : 'Get Recommendation' }}
                </button>
              </div>
            </form>
          </div>
          <div v-else class="recommendation-results">
            <h4 class="recommendation-results__title">Recommended Transport Modes</h4>
            
            <div v-if="unifiedRecommendation" class="recommendation-card recommendation-card--primary">
              <h5 class="recommendation-card__title">Unified Recommendation</h5>
              <div class="recommendation-card__content">
                <p><strong>Recommended Mode:</strong> {{ unifiedRecommendation.transportMode?.displayName || unifiedRecommendation.transportMode?.mode || '—' }}</p>
                <p><strong>Estimated CO2e:</strong> {{ unifiedRecommendation.emission ? unifiedRecommendation.emission.toFixed(2) : '—' }} kg</p>
                <p v-if="unifiedRecommendation.savingsPercentage !== undefined"><strong>Potential Savings:</strong> {{ unifiedRecommendation.savingsPercentage.toFixed(2) }}%</p>
              </div>
            </div>
            
            <div v-if="recommendation" class="recommendation-card">
              <h5 class="recommendation-card__title">Algorithm Recommendation</h5>
              <div class="recommendation-card__content">
                <p><strong>Recommended Mode:</strong> {{ recommendation.best_mode || '—' }}</p>
                <p><strong>Estimated CO2e:</strong> {{ recommendation.recommended_emission ? recommendation.recommended_emission.toFixed(2) : '—' }} kg</p>
                <p v-if="recommendation.saving"><strong>Potential Savings:</strong> {{ recommendation.saving }}</p>
              </div>
            </div>
            
            <div v-if="smartRecommendation" class="recommendation-card">
              <h5 class="recommendation-card__title">Smart Recommendation</h5>
              <div class="recommendation-card__content">
                <p><strong>Recommended Mode:</strong> {{ smartRecommendation.best_mode || '—' }}</p>
                <p><strong>Estimated CO2e:</strong> {{ smartRecommendation.current_emission ? smartRecommendation.current_emission.toFixed(2) : '—' }} kg</p>
                <p v-if="smartRecommendation.saving"><strong>Potential Savings:</strong> {{ smartRecommendation.saving }}</p>
              </div>
            </div>
            
            <div class="recommendation-actions">
              <button type="button" class="btn btn--ghost" @click="resetRecommendation">Get New Recommendation</button>
            </div>
          </div>
        </div>
      </div>
    </section>
  </main>
</template>

<style scoped>
.profile-page {
  flex: 0 0 auto;
  width: 100%;
  padding: 1.5rem 1.25rem;
  background: #fff;
}

.profile-head {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 0.65rem;
}

.profile-avatar-col {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
}

.profile-role {
  margin: 0;
  font-size: 0.95rem;
  font-weight: 750;
  letter-spacing: 0.02em;
  color: #3d5c3d;
  text-align: center;
}

.profile-avatar {
  color: #528951;
}

.profile-text {
  text-align: center;
}

.profile-name {
  margin: 0;
  font-size: 1.15rem;
  font-weight: 700;
  color: #2d3a2d;
  overflow-wrap: anywhere;
}

.profile-email {
  margin: 0;
  font-size: 0.95rem;
  color: #4a5c4a;
  word-break: break-all;
}

.profile-actions {
  display: flex;
  justify-content: center;
}

.logout-btn {
  padding: 0.55rem 1.25rem;
  border: none;
  border-radius: 4px;
  background: #dc2626;
  color: #fff;
  font-size: 0.95rem;
  font-weight: 600;
  cursor: pointer;
  font-family: inherit;
}

.logout-btn:hover {
  background: #b91c1c;
}

.logout-btn:active {
  background: #991b1b;
}

/* Supplier-specific styles */
.supplier-section {
  margin-top: 2rem;
  padding: 1.5rem;
  background: #f8faf8;
  border-radius: 8px;
  border: 1px solid #e0eae0;
}

.supplier-section__title {
  margin: 0 0 1.5rem;
  font-size: 1.5rem;
  font-weight: 700;
  color: #5f795f;
}

.supplier-section__alert {
  margin: 0 0 1rem;
  padding: 0.75rem 1rem;
  border-radius: 6px;
  background: #fef2f2;
  color: #991b1b;
  font-size: 0.9rem;
}

.supplier-section__alert--ok {
  background: #ecfdf3;
  color: #166434;
}

.supplier-info {
  margin-bottom: 2rem;
}

.supplier-info__row {
  display: flex;
  margin-bottom: 0.75rem;
  align-items: center;
}

.supplier-info__label {
  flex: 0 0 120px;
  font-weight: 600;
  color: #3d5c3d;
}

.supplier-info__value {
  flex: 1;
  color: #2d3a2d;
}

.supplier-edit-form {
  margin-bottom: 2rem;
}

.form-row {
  margin-bottom: 1rem;
}

.form-label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 600;
  color: #3d5c3d;
}

.form-input {
  width: 100%;
  padding: 0.5rem;
  border: 1px solid #b5c4b5;
  border-radius: 6px;
  font-size: 0.95rem;
  font-family: inherit;
}

.form-checkbox {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.form-actions {
  display: flex;
  gap: 1rem;
  margin-top: 1.5rem;
}

.shipment-summary {
  margin-bottom: 2rem;
  padding: 1rem;
  background: #fff;
  border-radius: 6px;
  border: 1px solid #e0eae0;
}

.shipment-summary__title {
  margin: 0 0 1rem;
  font-size: 1.1rem;
  font-weight: 600;
  color: #3d5c3d;
}

.shipment-summary__stats {
  display: flex;
  gap: 2rem;
}

.stat-item {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.stat-label {
  font-size: 0.85rem;
  color: #6b7d6b;
}

.stat-value {
  font-size: 1.2rem;
  font-weight: 700;
  color: #2d3a2d;
}

.shipment-list {
  margin-bottom: 1rem;
}

.shipment-list__title {
  margin: 0 0 1rem;
  font-size: 1.1rem;
  font-weight: 600;
  color: #3d5c3d;
}

.empty-state {
  padding: 2rem;
  text-align: center;
  color: #6b7d6b;
  background: #fff;
  border-radius: 6px;
  border: 1px solid #e0eae0;
}

.shipment-table {
  overflow: auto;
  background: #fff;
  border-radius: 6px;
  border: 1px solid #e0eae0;
}

.shipment-table table {
  width: 100%;
  border-collapse: collapse;
  font-size: 0.9rem;
}

.shipment-table th,
.shipment-table td {
  padding: 0.75rem;
  text-align: left;
  border-bottom: 1px solid #e0eae0;
}

.shipment-table th {
  background: #eef4ee;
  color: #3a4d3a;
  font-weight: 700;
}

.shipment-table tr:last-child td {
  border-bottom: none;
}

.btn {
  padding: 0.5rem 1rem;
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

/* Transport Recommendation Styles */
.transport-recommendation {
  margin-top: 2rem;
  padding: 1.5rem;
  background-color: #f8f9fa;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.transport-recommendation__title {
  margin-top: 0;
  margin-bottom: 1.5rem;
  font-size: 1.25rem;
  font-weight: 600;
  color: #2c3e50;
}

.recommendation-form {
  max-width: 600px;
}

.recommendation-form .form-row {
  margin-bottom: 1rem;
}

.recommendation-form .form-label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 500;
  color: #495057;
}

.recommendation-form .form-input {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #ced4da;
  border-radius: 4px;
  font-size: 1rem;
  transition: border-color 0.15s ease-in-out, box-shadow 0.15s ease-in-out;
}

.recommendation-form .form-input:focus {
  outline: none;
  border-color: #3cc260;
  box-shadow: 0 0 0 0.2rem rgba(60, 194, 96, 0.25);
}

.recommendation-form .form-actions {
  margin-top: 1.5rem;
  display: flex;
  justify-content: flex-start;
}

.recommendation-results {
  margin-top: 1rem;
}

.recommendation-results__title {
  margin-top: 0;
  margin-bottom: 1rem;
  font-size: 1.1rem;
  font-weight: 600;
  color: #2c3e50;
}

.recommendation-card {
  padding: 1rem;
  margin-bottom: 1rem;
  background-color: #ffffff;
  border: 1px solid #e9ecef;
  border-radius: 6px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.recommendation-card--primary {
  border-left: 4px solid #3cc260;
  background-color: #f0f9f4;
}

.recommendation-card__title {
  margin-top: 0;
  margin-bottom: 0.75rem;
  font-size: 1rem;
  font-weight: 600;
  color: #2c3e50;
}

.recommendation-card__content p {
  margin: 0.5rem 0;
  color: #495057;
}

.recommendation-actions {
  margin-top: 1.5rem;
  display: flex;
  justify-content: flex-start;
}

.btn {
  display: inline-block;
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: 4px;
  font-size: 1rem;
  font-weight: 500;
  text-align: center;
  text-decoration: none;
  cursor: pointer;
  transition: background-color 0.15s ease-in-out, border-color 0.15s ease-in-out, box-shadow 0.15s ease-in-out;
}

.btn--primary {
  background-color: #3cc260;
  color: #ffffff;
}

.btn--primary:hover {
  background-color: #34a853;
}

.btn--primary:disabled {
  background-color: #a8dadc;
  cursor: not-allowed;
}

.btn--ghost {
  background-color: transparent;
  color: #3cc260;
  border: 1px solid #3cc260;
}

.btn--ghost:hover {
  background-color: #f0f9f4;
}
</style>
