<script setup>
import { onBeforeUnmount, onMounted, ref } from 'vue'
import Globe from 'globe.gl'

const globeEl = ref(null)
let globeInstance = null
let resizeHandler = null

// Sample supplier data from different countries
const suppliers = [
  { id: 1, name: 'GreenTech Solutions', country: 'China', city: 'Shanghai', lat: 31.2304, lng: 121.4737, industry: 'Renewable Energy' },
  { id: 2, name: 'EcoMaterials Inc.', country: 'USA', city: 'San Francisco', lat: 37.7749, lng: -122.4194, industry: 'Sustainable Materials' },
  { id: 3, name: 'CleanEnergy GmbH', country: 'Germany', city: 'Berlin', lat: 52.52, lng: 13.405, industry: 'Solar Energy' },
  { id: 4, name: 'BioFuels Australia', country: 'Australia', city: 'Sydney', lat: -33.8688, lng: 151.2093, industry: 'Biofuels' },
  { id: 5, name: 'EcoTextiles India', country: 'India', city: 'Delhi', lat: 28.6139, lng: 77.209, industry: 'Sustainable Textiles' },
  { id: 6, name: 'GreenChem France', country: 'France', city: 'Paris', lat: 48.8566, lng: 2.3522, industry: 'Green Chemicals' },
  { id: 7, name: 'SolarPanels Brazil', country: 'Brazil', city: 'Sao Paulo', lat: -23.5558, lng: -46.6396, industry: 'Solar Energy' },
  { id: 8, name: 'WindPower Denmark', country: 'Denmark', city: 'Copenhagen', lat: 55.7558, lng: 12.4913, industry: 'Wind Energy' }
]

// Sample shipment routes between suppliers
const routes = [
  { startLat: 31.2304, startLng: 121.4737, endLat: 37.7749, endLng: -122.4194, weight: 5000, emissions: 1200, mode: 'Air' },
  { startLat: 37.7749, startLng: -122.4194, endLat: 52.52, endLng: 13.405, weight: 3000, emissions: 800, mode: 'Sea' },
  { startLat: 52.52, startLng: 13.405, endLat: 48.8566, endLng: 2.3522, weight: 2000, emissions: 300, mode: 'Rail' },
  { startLat: -33.8688, startLng: 151.2093, endLat: -23.5558, endLng: -46.6396, weight: 4000, emissions: 600, mode: 'Truck' },
  { startLat: 28.6139, startLng: 77.209, endLat: 31.2304, endLng: 121.4737, weight: 6000, emissions: 1500, mode: 'Air' },
  { startLat: 55.7558, startLng: 12.4913, endLat: 52.52, endLng: 13.405, weight: 1500, emissions: 200, mode: 'Truck' },
  { startLat: -23.5558, startLng: -46.6396, endLat: 37.7749, endLng: -122.4194, weight: 3500, emissions: 1000, mode: 'Sea' }
]

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
    .pointsData(suppliers)
    .pointLabel(
      (d) =>
        `<div style="padding:4px 6px;"><b>${d.name}</b><br/>${d.city}, ${d.country}<br/><span style="opacity:.85;">${d.industry}</span></div>`
    )
    .onPointClick((d) => {
      // Find routes related to this supplier
      const supplierRoutes = routes.filter(route => {
        return (route.startLat === d.lat && route.startLng === d.lng) || 
               (route.endLat === d.lat && route.endLng === d.lng)
      })
      
      // Create popup content
      let popupContent = `<div style="padding:10px; max-width: 300px;">
        <h3 style="margin-top: 0; color: #3d5340;">${d.name}</h3>
        <p><strong>Location:</strong> ${d.city}, ${d.country}</p>
        <p><strong>Industry:</strong> ${d.industry}</p>
        <h4 style="margin-top: 10px; margin-bottom: 5px; color: #3d5340;">Shipment Routes</h4>
        <ul style="margin: 0; padding-left: 20px;">
      `
      
      if (supplierRoutes.length > 0) {
        supplierRoutes.forEach(route => {
          // Determine if this is an origin or destination
          const isOrigin = route.startLat === d.lat && route.startLng === d.lng
          const otherLat = isOrigin ? route.endLat : route.startLat
          const otherLng = isOrigin ? route.endLng : route.startLng
          
          // Find the other supplier
          const otherSupplier = suppliers.find(s => s.lat === otherLat && s.lng === otherLng)
          const otherLocation = otherSupplier ? `${otherSupplier.name}, ${otherSupplier.city}, ${otherSupplier.country}` : 'Unknown Location'
          
          popupContent += `<li>${isOrigin ? 'To' : 'From'}: ${otherLocation}<br/>
            Mode: ${route.mode}<br/>
            Weight: ${route.weight} kg<br/>
            Emissions: ${route.emissions} kg CO2e</li>`
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
    .arcsData(routes)
    .arcColor(() => '#c62828')
    .arcAltitude(0.15)
    .arcStroke((d) => d.weight / 2000)
    .arcLabel(
      (d) =>
        `<div style="padding:4px 6px;">${d.weight} kg<br/>${d.emissions} kg CO2e<br/>Mode: ${d.mode}</div>`
    )
    .arcStartLat('startLat')
    .arcStartLng('startLng')
    .arcEndLat('endLat')
    .arcEndLng('endLng')

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
  controls.minDistance = 200
  controls.maxDistance = 400
  
  // Set initial zoom level to make globe smaller
  controls.object.position.set(0, 0, 300)
  controls.update()

  updateSize()
}

function updateSize() {
  if (!globeEl.value || !globeInstance) return
  const { clientWidth, clientHeight } = globeEl.value
  globeInstance.width(clientWidth)
  globeInstance.height(clientHeight)
}

onMounted(() => {
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
          <span class="legend-info">Click on suppliers for details</span>
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
