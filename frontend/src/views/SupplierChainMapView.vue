<script setup>
import { onBeforeUnmount, onMounted, ref, reactive } from 'vue'
import Globe from 'globe.gl'

const globeEl = ref(null)
let globeInstance = null
let resizeHandler = null

// Data from backend
const suppliers = ref([])
const routes = ref([])

// Mock geographic data for suppliers (since backend doesn't provide lat/lng)
const supplierGeoData = {
  'China': { city: 'Shanghai', lat: 31.2304, lng: 121.4737, industry: 'Renewable Energy' },
  'United States': { city: 'San Francisco', lat: 37.7749, lng: -122.4194, industry: 'Sustainable Materials' },
  'Germany': { city: 'Berlin', lat: 52.52, lng: 13.405, industry: 'Solar Energy' },
  'Australia': { city: 'Sydney', lat: -33.8688, lng: 151.2093, industry: 'Biofuels' },
  'India': { city: 'Delhi', lat: 28.6139, lng: 77.209, industry: 'Sustainable Textiles' },
  'France': { city: 'Paris', lat: 48.8566, lng: 2.3522, industry: 'Green Chemicals' },
  'Brazil': { city: 'Sao Paulo', lat: -23.5558, lng: -46.6396, industry: 'Solar Energy' },
  'Denmark': { city: 'Copenhagen', lat: 55.7558, lng: 12.4913, industry: 'Wind Energy' },
  'Canada': { city: 'Toronto', lat: 43.6532, lng: -79.3832, industry: 'Clean Technology' },
  'Japan': { city: 'Tokyo', lat: 35.6762, lng: 139.6503, industry: 'Electronics' },
  'South Korea': { city: 'Seoul', lat: 37.5665, lng: 126.9780, industry: 'Manufacturing' },
  'United Kingdom': { city: 'London', lat: 51.5074, lng: -0.1278, industry: 'Financial Services' },
  'Italy': { city: 'Rome', lat: 41.9028, lng: 12.4964, industry: 'Fashion' },
  'Spain': { city: 'Madrid', lat: 40.4168, lng: -3.7038, industry: 'Renewable Energy' },
  'Russia': { city: 'Moscow', lat: 55.7558, lng: 37.6173, industry: 'Energy' },
  'South Africa': { city: 'Johannesburg', lat: -26.2041, lng: 28.0473, industry: 'Mining' },
  'Mexico': { city: 'Mexico City', lat: 19.4326, lng: -99.1332, industry: 'Manufacturing' },
  'Chile': { city: 'Santiago', lat: -33.4489, lng: -70.6693, industry: 'Mining' },
  'Morocco': { city: 'Casablanca', lat: 33.5731, lng: -7.5898, industry: 'Manufacturing' },
  'New Zealand': { city: 'Auckland', lat: -36.8485, lng: 174.7633, industry: 'Agriculture' }
}

// Mock transport mode data
const transportModeMap = {
  1: { mode: 'Air' },
  2: { mode: 'Sea' },
  3: { mode: 'Rail' },
  4: { mode: 'Truck' }
}

// Fetch data from backend
async function fetchData() {
  try {
    // Fetch suppliers
    const suppliersResponse = await fetch('/api/suppliers')
    const suppliersData = await suppliersResponse.json()
    
    // Enhance suppliers with geographic data
    const enhancedSuppliers = suppliersData.map(supplier => {
      const geoData = supplierGeoData[supplier.country] || { city: 'Unknown', lat: 0, lng: 0, industry: 'Unknown' }
      return {
        id: supplier.id,
        name: supplier.name,
        country: supplier.country,
        city: geoData.city,
        lat: geoData.lat,
        lng: geoData.lng,
        industry: geoData.industry,
        hasEnvironmentalCertification: supplier.hasEnvironmentalCertification,
        type: 'supplier'
      }
    })
    
    // Fetch shipments with coordinates from backend
    const shipmentsResponse = await fetch('/api/shipments/with-coordinates')
    const shipmentsData = await shipmentsResponse.json()
    
    // Convert shipments to routes using actual origin/destination coordinates from backend
    const shipmentRoutes = shipmentsData.map(shipment => {
      return {
        id: shipment.id,
        startLat: shipment.originLat || 0,
        startLng: shipment.originLng || 0,
        endLat: shipment.destLat || 0,
        endLng: shipment.destLng || 0,
        origin: shipment.origin,
        destination: shipment.destination,
        supplierName: shipment.supplierName,
        weight: (shipment.cargoWeightTons || 0) * 1000, // Convert tons to kg
        emissions: shipment.calculatedCarbonEmission || 0,
        mode: shipment.transportModeName || shipment.transportMode || 'Unknown'
      }
    })
    
    routes.value = shipmentRoutes
    
    // Extract unique origins and destinations from shipments
    const locationMap = new Map()
    
    // Add origins
    shipmentsData.forEach(shipment => {
      if (shipment.origin && shipment.originLat && shipment.originLng) {
        locationMap.set(shipment.origin, {
          name: shipment.origin,
          lat: shipment.originLat,
          lng: shipment.originLng,
          type: 'origin',
          id: `origin_${shipment.id}`
        })
      }
    })
    
    // Add destinations
    shipmentsData.forEach(shipment => {
      if (shipment.destination && shipment.destLat && shipment.destLng) {
        locationMap.set(shipment.destination, {
          name: shipment.destination,
          lat: shipment.destLat,
          lng: shipment.destLng,
          type: 'destination',
          id: `dest_${shipment.id}`
        })
      }
    })
    
    // Convert locationMap values to array and add to suppliers
    const locationPoints = Array.from(locationMap.values())
    suppliers.value = [...enhancedSuppliers, ...locationPoints]
    
    // Reinitialize globe with new data
    if (globeInstance) {
      globeEl.value.innerHTML = ''
      initGlobe()
    }
  } catch (error) {
    console.error('Error fetching data:', error)
    // Fallback to sample data if API fails
    useSampleData()
  }
}

// Use sample data as fallback
function useSampleData() {
  suppliers.value = [
    { id: 1, name: 'GreenTech Solutions', country: 'China', city: 'Shanghai', lat: 31.2304, lng: 121.4737, industry: 'Renewable Energy' },
    { id: 2, name: 'EcoMaterials Inc.', country: 'USA', city: 'San Francisco', lat: 37.7749, lng: -122.4194, industry: 'Sustainable Materials' },
    { id: 3, name: 'CleanEnergy GmbH', country: 'Germany', city: 'Berlin', lat: 52.52, lng: 13.405, industry: 'Solar Energy' },
    { id: 4, name: 'BioFuels Australia', country: 'Australia', city: 'Sydney', lat: -33.8688, lng: 151.2093, industry: 'Biofuels' },
    { id: 5, name: 'EcoTextiles India', country: 'India', city: 'Delhi', lat: 28.6139, lng: 77.209, industry: 'Sustainable Textiles' },
    { id: 6, name: 'GreenChem France', country: 'France', city: 'Paris', lat: 48.8566, lng: 2.3522, industry: 'Green Chemicals' },
    { id: 7, name: 'SolarPanels Brazil', country: 'Brazil', city: 'Sao Paulo', lat: -23.5558, lng: -46.6396, industry: 'Solar Energy' },
    { id: 8, name: 'WindPower Denmark', country: 'Denmark', city: 'Copenhagen', lat: 55.7558, lng: 12.4913, industry: 'Wind Energy' }
  ]
  
  routes.value = [
    { startLat: 31.2304, startLng: 121.4737, endLat: 37.7749, endLng: -122.4194, weight: 5000, emissions: 1200, mode: 'Air' },
    { startLat: 37.7749, startLng: -122.4194, endLat: 52.52, endLng: 13.405, weight: 3000, emissions: 800, mode: 'Sea' },
    { startLat: 52.52, startLng: 13.405, endLat: 48.8566, endLng: 2.3522, weight: 2000, emissions: 300, mode: 'Rail' },
    { startLat: -33.8688, startLng: 151.2093, endLat: -23.5558, endLng: -46.6396, weight: 4000, emissions: 600, mode: 'Truck' },
    { startLat: 28.6139, startLng: 77.209, endLat: 31.2304, endLng: 121.4737, weight: 6000, emissions: 1500, mode: 'Air' },
    { startLat: 55.7558, startLng: 12.4913, endLat: 52.52, endLng: 13.405, weight: 1500, emissions: 200, mode: 'Truck' },
    { startLat: -23.5558, startLng: -46.6396, endLat: 37.7749, endLng: -122.4194, weight: 3500, emissions: 1000, mode: 'Sea' }
  ]
}

const GEOJSON_URL = 'https://raw.githubusercontent.com/holtzy/D3-graph-gallery/master/DATA/world.geojson'

function initGlobe() {
  if (!globeEl.value) return

  globeInstance = Globe()(globeEl.value)
    .backgroundColor('rgba(0,0,0,0)')
    .globeImageUrl('https://unpkg.com/three-globe/example/img/earth-blue-marble.jpg')
    .bumpImageUrl('https://unpkg.com/three-globe/example/img/earth-topology.png')
    .showAtmosphere(true)
    .atmosphereColor('#87ceeb')
    .atmosphereAltitude(0.18)
    .pointLat('lat')
    .pointLng('lng')
    .pointAltitude(0.05)
    .pointRadius(0.4)
    .pointColor(() => '#34c759')
    .pointsData(suppliers.value)
    .pointLabel(
      (d) => {
        if (d.type === 'supplier') {
          return `<div style="padding:4px 6px;"><b>${d.name}</b><br/>${d.city}, ${d.country}<br/><span style="opacity:.85;">${d.industry}</span></div>`
        } else {
          return `<div style="padding:4px 6px;"><b>${d.name}</b><br/>${d.type === 'origin' ? 'Origin' : 'Destination'}<br/><span style="opacity:.85;">${d.lat.toFixed(4)}, ${d.lng.toFixed(4)}</span></div>`
        }
      }
    )
    .onPointClick((d) => {
      // Find routes related to this location
      let relatedRoutes = []
      if (d.type === 'supplier') {
        // For suppliers, find routes by supplier name
        relatedRoutes = routes.value.filter(route => {
          return route.supplierName === d.name
        })
      } else if (d.type === 'origin') {
        // For origins, find routes where this is the origin
        relatedRoutes = routes.value.filter(route => {
          return route.origin === d.name
        })
      } else if (d.type === 'destination') {
        // For destinations, find routes where this is the destination
        relatedRoutes = routes.value.filter(route => {
          return route.destination === d.name
        })
      }
      
      // Create popup content
      let popupContent = `<div style="padding:10px; max-width: 300px;">
        <h3 style="margin-top: 0; color: #3d5340;">${d.name}</h3>
      `
      
      // Add location details based on type
      if (d.type === 'supplier') {
        popupContent += `
          <p><strong>Location:</strong> ${d.city}, ${d.country}</p>
          <p><strong>Industry:</strong> ${d.industry}</p>
          <p><strong>Certified:</strong> ${d.hasEnvironmentalCertification ? 'Yes' : 'No'}</p>
        `
      } else {
        popupContent += `
          <p><strong>Type:</strong> ${d.type === 'origin' ? 'Origin' : 'Destination'}</p>
          <p><strong>Coordinates:</strong> ${d.lat.toFixed(4)}, ${d.lng.toFixed(4)}</p>
        `
      }
      
      // Add shipment routes
      popupContent += `
        <h4 style="margin-top: 10px; margin-bottom: 5px; color: #3d5340;">Shipment Routes</h4>
        <ul style="margin: 0; padding-left: 20px;">
      `
      
      if (relatedRoutes.length > 0) {
        relatedRoutes.forEach(route => {
          if (d.type === 'origin') {
            popupContent += `<li>To: ${route.destination || 'Unknown'}<br/>
              Supplier: ${route.supplierName || 'Unknown'}<br/>
              Mode: ${route.mode}<br/>
              Weight: ${route.weight.toFixed(0)} kg<br/>
              Emissions: ${route.emissions.toFixed(2)} kg CO2e</li>`
          } else if (d.type === 'destination') {
            popupContent += `<li>From: ${route.origin || 'Unknown'}<br/>
              Supplier: ${route.supplierName || 'Unknown'}<br/>
              Mode: ${route.mode}<br/>
              Weight: ${route.weight.toFixed(0)} kg<br/>
              Emissions: ${route.emissions.toFixed(2)} kg CO2e</li>`
          } else {
            const toLocation = route.destination || 'Unknown'
            popupContent += `<li>To: ${toLocation}<br/>
              Mode: ${route.mode}<br/>
              Weight: ${route.weight.toFixed(0)} kg<br/>
              Emissions: ${route.emissions.toFixed(2)} kg CO2e</li>`
          }
        })
      } else {
        popupContent += '<li>No shipment routes found</li>'
      }
      
      popupContent += `</ul></div>`
      
      // Create and show popup
      const popup = document.createElement('div')
      popup.innerHTML = popupContent
      popup.style.cssText = `
        position: fixed;
        top: 50%;
        left: 50%;
        transform: translate(-50%, -50%);
        background: white;
        border-radius: 8px;
        box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
        z-index: 10000;
        max-width: 90%;
        max-height: 80vh;
        overflow-y: auto;
      `
      
      // Add close button
      const closeButton = document.createElement('button')
      closeButton.textContent = 'Close'
      closeButton.style.cssText = `
        margin-top: 10px;
        padding: 5px 10px;
        background: #3d5340;
        color: white;
        border: none;
        border-radius: 4px;
        cursor: pointer;
      `
      closeButton.onclick = () => {
        document.body.removeChild(popup)
      }
      
      popup.appendChild(closeButton)
      document.body.appendChild(popup)
    })

  // Add arcs for shipment routes
  globeInstance
    .arcsData(routes.value)
    .arcColor(() => '#c62828')
    .arcAltitude(0.48) // Adjust altitude to avoid routes going through the earth and pointing to sky
    .arcStroke((d) => Math.max((d.emissions / 90000) * (d.weight / 40000), 0.2)) // Calculate stroke width based on both emissions and weight, minimum 0.1
    .arcLabel(
      (d) =>
        `<div style="padding:4px 6px;"><strong>From:</strong> ${d.origin || 'Unknown'}<br/><strong>To:</strong> ${d.destination || 'Unknown'}<br/><strong>Weight:</strong> ${d.weight.toFixed(0)} kg<br/><strong>Emissions:</strong> ${d.emissions.toFixed(2)} kg CO2e<br/><strong>Mode:</strong> ${d.mode}</div>`
    )
    .arcStartLat('startLat')
    .arcStartLng('startLng')
    .arcEndLat('endLat')
    .arcEndLng('endLng')
    .arcDashLength(0) // Ensure solid lines
    .onArcClick((arc) => {
      // Get route details directly from arc data
      const originInfo = arc.origin || 'Unknown'
      const destInfo = arc.destination || 'Unknown'
      const supplierInfo = arc.supplierName || 'Unknown'
      
      // Create popup for route details
      const popup = document.createElement('div')
      popup.style.position = 'fixed'
      popup.style.top = '50%'
      popup.style.left = '50%'
      popup.style.transform = 'translate(-50%, -50%)'
      popup.style.backgroundColor = 'white'
      popup.style.padding = '16px'
      popup.style.borderRadius = '8px'
      popup.style.boxShadow = '0 2px 10px rgba(0, 0, 0, 0.1)'
      popup.style.zIndex = '1000'
      popup.style.maxWidth = '400px'
      
      // Create close button
      const closeButton = document.createElement('button')
      closeButton.textContent = '×'
      closeButton.style.position = 'absolute'
      closeButton.style.top = '8px'
      closeButton.style.right = '8px'
      closeButton.style.background = 'none'
      closeButton.style.border = 'none'
      closeButton.style.fontSize = '20px'
      closeButton.style.cursor = 'pointer'
      
      closeButton.onclick = () => {
        document.body.removeChild(popup)
      }
      
      // Populate popup content
      popup.innerHTML = `
        <h3 style="margin-top: 0; margin-bottom: 12px;">Route Details</h3>
        <div style="margin-bottom: 8px;">
          <strong>Supplier:</strong> ${supplierInfo}
        </div>
        <div style="margin-bottom: 8px;">
          <strong>From:</strong> ${originInfo}
        </div>
        <div style="margin-bottom: 8px;">
          <strong>To:</strong> ${destInfo}
        </div>
        <div style="margin-bottom: 8px;">
          <strong>Mode:</strong> ${arc.mode}
        </div>
        <div style="margin-bottom: 8px;">
          <strong>Weight:</strong> ${arc.weight.toFixed(0)} kg
        </div>
        <div style="margin-bottom: 8px;">
          <strong>Emissions:</strong> ${arc.emissions.toFixed(2)} kg CO2e
        </div>
      `
      
      popup.appendChild(closeButton)
      document.body.appendChild(popup)
    })

  // Country hover highlight + label
  fetch(GEOJSON_URL)
    .then((r) => r.json())
    .then((geojson) => {
      let hoveredCountry = null

      globeInstance
        .polygonsData(geojson.features)
        .polygonCapColor((f) =>
          f === hoveredCountry ? 'rgba(60,194,96,0.7)' : 'rgba(46,93,50,0.25)'
        )
        .polygonSideColor(() => 'rgba(0, 100, 0, 0.08)')
        .polygonStrokeColor(() => 'rgba(180, 220, 190, 0.55)')
        .polygonAltitude((f) => (f === hoveredCountry ? 0.02 : 0.004))
        .polygonLabel((f) => `<b>${f?.properties?.name ?? 'Unknown Country'}</b>`)
        .onPolygonHover((country) => {
          hoveredCountry = country || null
          globeInstance
            .polygonAltitude((f) => (f === hoveredCountry ? 0.02 : 0.004))
            .polygonCapColor((f) =>
              f === hoveredCountry ? 'rgba(60,194,96,0.7)' : 'rgba(46,93,50,0.25)'
            )
        })
    })
    .catch(() => {
      // If country borders fail to load, keep the globe and supplier points
    })

  const controls = globeInstance.controls()
  controls.autoRotate = true
  controls.autoRotateSpeed = 0.5
  controls.enableDamping = true
  controls.dampingFactor = 0.06
  controls.minDistance = 250
  controls.maxDistance = 500
  
  // Set initial zoom level to make globe smaller and improve route visibility
  controls.object.position.set(0, 0, 350)
  controls.update()

  updateSize()
}

function updateSize() {
  if (!globeEl.value || !globeInstance) return
  const { clientWidth, clientHeight } = globeEl.value
  globeInstance.width(clientWidth)
  globeInstance.height(clientHeight)
}

onMounted(async () => {
  // Fetch data from backend first
  await fetchData()
  // Then initialize the globe
  initGlobe()
  resizeHandler = () => updateSize()
  window.addEventListener('resize', resizeHandler)
})

onBeforeUnmount(() => {
  if (resizeHandler) {
    window.removeEventListener('resize', resizeHandler)
  }
  if (globeEl.value) {
    globeEl.value.innerHTML = ''
  }
  globeInstance = null
})
</script>

<template>
  <div class="supplier-chain-map">
    <header class="page__head">
      <h1 class="page__title">Supplier Chain Map</h1>
      <p class="page__description">Interactive visualization of global supply chain network</p>
    </header>
    
    <div class="globe-container">
      <div ref="globeEl" class="globe-canvas" />
      
      <div class="map-legend">
        <h3>Legend</h3>
        <div class="legend-item">
          <span class="legend-dot supplier-dot"></span>
          <span>Supplier Location</span>
        </div>
        <div class="legend-item">
          <span class="legend-line"></span>
          <span>Shipment Route</span>
        </div>
        <div class="legend-item">
          <span class="legend-info">Click on suppliers or routes for details</span>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.supplier-chain-map {
  flex: 1;
  width: 100%;
  height: 100vh;
  background: #fff;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.page__head {
  padding: 1rem 1.5rem;
  border-bottom: 1px solid #c5d6c5;
}

.page__title {
  margin: 0 0 0.25rem;
  font-size: 1.5rem;
  font-weight: 900;
  color: #5f795f;
}

.page__description {
  margin: 0;
  font-size: 0.9rem;
  color: #4a5c4a;
}

.globe-container {
  flex: 1;
  position: relative;
  background: radial-gradient(circle at 30% 20%, #f5fff6 0%, #eef7f0 40%, #e6f0e8 100%);
  max-width: 1280px;
  margin: 0 auto;
  padding: 1rem;
  display: flex;
  align-items: center;
  justify-content: center;
}

.globe-canvas {
  width: 100%;
  height: 100%;
  min-height: 400px;
  max-height: 700px;
}

.map-legend {
  position: absolute;
  top: 1.5rem;
  right: 1.5rem;
  background: rgba(255, 255, 255, 0.9);
  padding: 1rem;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  max-width: 200px;
}

.map-legend h3 {
  margin: 0 0 0.75rem;
  font-size: 1rem;
  font-weight: 700;
  color: #3d5340;
}

.legend-item {
  display: flex;
  align-items: center;
  margin-bottom: 0.5rem;
  font-size: 0.9rem;
  color: #5a6b5a;
}

.legend-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  margin-right: 0.5rem;
}

.supplier-dot {
  background-color: #34c759;
}

.legend-line {
  width: 20px;
  height: 2px;
  background-color: #c62828;
  margin-right: 0.5rem;
}

.legend-info {
  font-style: italic;
  font-size: 0.85rem;
  color: #6b7d6b;
}

@media (max-width: 768px) {
  .map-legend {
    position: relative;
    top: 0;
    right: 0;
    margin: 1rem;
    max-width: none;
  }
  
  .globe-canvas {
    min-height: 500px;
  }
}
</style>
