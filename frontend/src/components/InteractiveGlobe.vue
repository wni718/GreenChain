<script setup>
import { onBeforeUnmount, onMounted, ref } from 'vue'
import Globe from 'globe.gl'

const globeEl = ref(null)
let globeInstance = null
let resizeHandler = null

const cities = [
  { city: 'Shanghai', country: 'China', continent: 'Asia', lat: 31.2304, lng: 121.4737 },
  { city: 'Beijing', country: 'China', continent: 'Asia', lat: 39.9042, lng: 116.4074 },
  { city: 'Chongqing', country: 'China', continent: 'Asia', lat: 29.4316, lng: 106.9123 },
  { city: 'Hohhot', country: 'China', continent: 'Asia', lat: 40.8426, lng: 111.7492 },
  { city: 'Shenyang', country: 'China', continent: 'Asia', lat: 41.8057, lng: 123.4315 },
  { city: 'Qingdao', country: 'China', continent: 'Asia', lat: 36.0671, lng: 120.3826 },
  { city: 'Guangdong (Guangzhou)', country: 'China', continent: 'Asia', lat: 23.1291, lng: 113.2644 },
  { city: 'Xishuangbanna', country: 'China', continent: 'Asia', lat: 22.0017, lng: 100.7974 },
  { city: 'Tokyo', country: 'Japan', continent: 'Asia', lat: 35.6762, lng: 139.6503 },
  { city: 'Singapore', country: 'Singapore', continent: 'Asia', lat: 1.3521, lng: 103.8198 },
  { city: 'Delhi', country: 'India', continent: 'Asia', lat: 28.6139, lng: 77.209 },
  { city: 'Sydney', country: 'Australia', continent: 'Oceania', lat: -33.8688, lng: 151.2093 },
  { city: 'Cairo', country: 'Egypt', continent: 'Africa', lat: 30.0444, lng: 31.2357 },
  { city: 'Nairobi', country: 'Kenya', continent: 'Africa', lat: -1.2921, lng: 36.8219 },
  { city: 'Paris', country: 'France', continent: 'Europe', lat: 48.8566, lng: 2.3522 },
  { city: 'Berlin', country: 'Germany', continent: 'Europe', lat: 52.52, lng: 13.405 },
  { city: 'London', country: 'United Kingdom', continent: 'Europe', lat: 51.5074, lng: -0.1278 },
  { city: 'Dublin', country: 'Ireland', continent: 'Europe', lat: 53.3498, lng: -6.2603 },
  { city: 'Rome', country: 'Italy', continent: 'Europe', lat: 41.9028, lng: 12.4964 },
  { city: 'Istanbul', country: 'Turkey', continent: 'Europe', lat: 41.0082, lng: 28.9784 },
  { city: 'Athens', country: 'Greece', continent: 'Europe', lat: 37.9838, lng: 23.7275 },
  { city: 'Vienna', country: 'Austria', continent: 'Europe', lat: 48.2082, lng: 16.3738 },
  { city: 'Warsaw', country: 'Poland', continent: 'Europe', lat: 52.2297, lng: 21.0122 },
  { city: 'Kyiv', country: 'Ukraine', continent: 'Europe', lat: 50.4501, lng: 30.5234 },
  { city: 'Belgrade', country: 'Serbia', continent: 'Europe', lat: 44.7866, lng: 20.4489 },
  { city: 'Sarajevo', country: 'Bosnia and Herzegovina', continent: 'Europe', lat: 43.8563, lng: 18.4131 },
  { city: 'Prague', country: 'Czech Republic', continent: 'Europe', lat: 50.0755, lng: 14.4378 },
  { city: 'Budapest', country: 'Hungary', continent: 'Europe', lat: 47.4979, lng: 19.0402 },
  { city: 'New York', country: 'USA', continent: 'North America', lat: 40.7128, lng: -74.006 },
  { city: 'Los Angeles', country: 'USA', continent: 'North America', lat: 34.0522, lng: -118.2437 },
  { city: 'Seattle', country: 'USA', continent: 'North America', lat: 47.6062, lng: -122.3321 },
  { city: 'San Francisco', country: 'USA', continent: 'North America', lat: 37.7749, lng: -122.4194 },
  { city: 'San Diego', country: 'USA', continent: 'North America', lat: 32.7157, lng: -117.1611 },
  { city: 'Sacramento', country: 'USA', continent: 'North America', lat: 38.5816, lng: -121.4944 },
  { city: 'Chicago', country: 'USA', continent: 'North America', lat: 41.8781, lng: -87.6298 },
  { city: 'Houston', country: 'USA', continent: 'North America', lat: 29.7604, lng: -95.3698 },
  { city: 'Miami', country: 'USA', continent: 'North America', lat: 25.7617, lng: -80.1918 },
  { city: 'Orlando', country: 'USA', continent: 'North America', lat: 28.5383, lng: -81.3792 },
  { city: 'Tampa', country: 'USA', continent: 'North America', lat: 27.9506, lng: -82.4572 },
  { city: 'Jacksonville', country: 'USA', continent: 'North America', lat: 30.3322, lng: -81.6557 },
  { city: 'Boston', country: 'USA', continent: 'North America', lat: 42.3601, lng: -71.0589 },
  { city: 'Washington, D.C.', country: 'USA', continent: 'North America', lat: 38.9072, lng: -77.0369 },
  { city: 'Atlanta', country: 'USA', continent: 'North America', lat: 33.749, lng: -84.388 },
  { city: 'Las Vegas', country: 'USA', continent: 'North America', lat: 36.1699, lng: -115.1398 },
  { city: 'Toronto', country: 'Canada', continent: 'North America', lat: 43.6532, lng: -79.3832 },
  { city: 'Mexico City', country: 'Mexico', continent: 'North America', lat: 19.4326, lng: -99.1332 },
  { city: 'Sao Paulo', country: 'Brazil', continent: 'South America', lat: -23.5558, lng: -46.6396 },
  { city: 'Buenos Aires', country: 'Argentina', continent: 'South America', lat: -34.6037, lng: -58.3816 },
]

const GEOJSON_URL =
  'https://raw.githubusercontent.com/holtzy/D3-graph-gallery/master/DATA/world.geojson'

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
    .pointAltitude(0.02)
    .pointRadius(0.28)
    .pointColor(() => '#34c759')
    .pointsData(cities)
    .pointLabel(
      (d) =>
        `<div style="padding:4px 6px;"><b>${d.city}</b><br/>${d.country}<br/><span style="opacity:.85;">${d.continent}</span></div>`
    )

  // 国家悬浮高亮 + 名称
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
      // 国家边界加载失败时，至少保留可旋转地球和城市点
    })

  const controls = globeInstance.controls()
  controls.autoRotate = false
  controls.enableDamping = true
  controls.dampingFactor = 0.06
  controls.minDistance = 170
  controls.maxDistance = 380

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
  <section class="globe-panel" aria-label="Interactive globe">
    <div ref="globeEl" class="globe-canvas" />
  </section>
</template>

<style scoped>
.globe-panel {
  flex: 1;
  min-height: 0;
  width: 100%;
  background: radial-gradient(circle at 30% 20%, #f5fff6 0%, #eef7f0 40%, #e6f0e8 100%);
}

.globe-canvas {
  width: 100%;
  height: 100%;
  min-height: 580px;
}
</style>
