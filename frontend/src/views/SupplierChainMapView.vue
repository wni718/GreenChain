<script setup>
import { onBeforeUnmount, onMounted, ref, reactive } from 'vue'
import Globe from 'globe.gl'

const globeEl = ref(null)
let globeInstance = null
let resizeHandler = null

// Loading state
const loading = ref(true)
const errorMessage = ref('')

// Data from backend
const suppliers = ref([])
const routes = ref([])

// Mock geographic data for suppliers (since backend doesn't provide lat/lng)
const supplierGeoData = {
  'China': { city: 'Shanghai', lat: 31.2304, lng: 121.4737, industry: 'Renewable Energy' },
  'United States': { city: 'San Francisco', lat: 37.7749, lng: -122.4194, industry: 'Sustainable Materials' },
  'USA': { city: 'San Francisco', lat: 37.7749, lng: -122.4194, industry: 'Sustainable Materials' },
  'Germany': { city: 'Berlin', lat: 52.52, lng: 13.405, industry: 'Solar Energy' },
  'Australia': { city: 'Sydney', lat: -33.8688, lng: 151.2093, industry: 'Biofuels' },
  'India': { city: 'Delhi', lat: 28.6139, lng: 77.209, industry: 'Sustainable Textiles' },
  'France': { city: 'Paris', lat: 48.8566, lng: 2.3522, industry: 'Green Chemicals' },
  'Brazil': { city: 'Sao Paulo', lat: -23.5558, lng: -46.6396, industry: 'Solar Energy' },
  'Denmark': { city: 'Copenhagen', lat: 55.7558, lng: 12.4913, industry: 'Wind Energy' },
  'Canada': { city: 'Toronto', lat: 43.6532, lng: -79.3832, industry: 'Clean Technology' },
  'Japan': { city: 'Tokyo', lat: 35.6762, lng: 139.6503, industry: 'Electronics' },
  'South Korea': { city: 'Seoul', lat: 37.5665, lng: 126.9780, industry: 'Manufacturing' },
  'Korea': { city: 'Seoul', lat: 37.5665, lng: 126.9780, industry: 'Manufacturing' },
  'Republic of Korea': { city: 'Seoul', lat: 37.5665, lng: 126.9780, industry: 'Manufacturing' },
  'United Kingdom': { city: 'London', lat: 51.5074, lng: -0.1278, industry: 'Financial Services' },
  'UK': { city: 'London', lat: 51.5074, lng: -0.1278, industry: 'Financial Services' },
  'Britain': { city: 'London', lat: 51.5074, lng: -0.1278, industry: 'Financial Services' },
  'Great Britain': { city: 'London', lat: 51.5074, lng: -0.1278, industry: 'Financial Services' },
  'Italy': { city: 'Rome', lat: 41.9028, lng: 12.4964, industry: 'Fashion' },
  'Spain': { city: 'Madrid', lat: 40.4168, lng: -3.7038, industry: 'Renewable Energy' },
  'Russia': { city: 'Moscow', lat: 55.7558, lng: 37.6173, industry: 'Energy' },
  'Russian Federation': { city: 'Moscow', lat: 55.7558, lng: 37.6173, industry: 'Energy' },
  'South Africa': { city: 'Johannesburg', lat: -26.2041, lng: 28.0473, industry: 'Mining' },
  'Mexico': { city: 'Mexico City', lat: 19.4326, lng: -99.1332, industry: 'Manufacturing' },
  'Chile': { city: 'Santiago', lat: -33.4489, lng: -70.6693, industry: 'Mining' },
  'Morocco': { city: 'Casablanca', lat: 33.5731, lng: -7.5898, industry: 'Manufacturing' },
  'New Zealand': { city: 'Auckland', lat: -36.8485, lng: 174.7633, industry: 'Agriculture' },
  'Sweden': { city: 'Stockholm', lat: 59.3293, lng: 18.0686, industry: 'Environmental Technology' },
  'Norway': { city: 'Oslo', lat: 59.9139, lng: 10.7522, industry: 'Energy' },
  'Finland': { city: 'Helsinki', lat: 60.1699, lng: 24.9384, industry: 'Clean Technology' },
  'Netherlands': { city: 'Amsterdam', lat: 52.3676, lng: 4.9041, industry: 'Logistics' },
  'Holland': { city: 'Amsterdam', lat: 52.3676, lng: 4.9041, industry: 'Logistics' },
  'Belgium': { city: 'Brussels', lat: 50.8503, lng: 4.3517, industry: 'Chemicals' },
  'Switzerland': { city: 'Zurich', lat: 47.3769, lng: 8.5417, industry: 'Pharmaceuticals' },
  'Austria': { city: 'Vienna', lat: 48.2082, lng: 16.3738, industry: 'Engineering' },
  'Poland': { city: 'Warsaw', lat: 52.2297, lng: 21.0122, industry: 'Manufacturing' },
  'Portugal': { city: 'Lisbon', lat: 38.7223, lng: -9.1393, industry: 'Renewable Energy' },
  'Greece': { city: 'Athens', lat: 37.9838, lng: 23.7275, industry: 'Shipping' },
  'Turkey': { city: 'Istanbul', lat: 41.0082, lng: 28.9783, industry: 'Manufacturing' },
  'Indonesia': { city: 'Jakarta', lat: -6.2088, lng: 106.8456, industry: 'Resources' },
  'Thailand': { city: 'Bangkok', lat: 13.7563, lng: 100.5018, industry: 'Electronics' },
  'Vietnam': { city: 'Ho Chi Minh City', lat: 10.8231, lng: 106.6297, industry: 'Manufacturing' },
  'Malaysia': { city: 'Kuala Lumpur', lat: 3.139, lng: 101.6869, industry: 'Electronics' },
  'Singapore': { city: 'Singapore', lat: 1.3521, lng: 103.8198, industry: 'Logistics' },
  'Argentina': { city: 'Buenos Aires', lat: -34.6037, lng: -58.3816, industry: 'Agriculture' },
  'Colombia': { city: 'Bogota', lat: 4.711, lng: -74.0721, industry: 'Mining' },
  'Peru': { city: 'Lima', lat: -12.0464, lng: -77.0428, industry: 'Mining' },
  'Egypt': { city: 'Cairo', lat: 30.0444, lng: 31.2357, industry: 'Textiles' },
  'Saudi Arabia': { city: 'Riyadh', lat: 24.7136, lng: 46.6753, industry: 'Energy' },
  'UAE': { city: 'Dubai', lat: 25.2048, lng: 55.2708, industry: 'Logistics' },
  'United Arab Emirates': { city: 'Dubai', lat: 25.2048, lng: 55.2708, industry: 'Logistics' },
  'Nigeria': { city: 'Lagos', lat: 6.5244, lng: 3.3792, industry: 'Oil & Gas' },
  'Kenya': { city: 'Nairobi', lat: -1.2921, lng: 36.8219, industry: 'Agriculture' },
  'Iran': { city: 'Tehran', lat: 35.6892, lng: 51.389, industry: 'Oil & Gas' },
  'Iraq': { city: 'Baghdad', lat: 33.3152, lng: 44.3661, industry: 'Oil & Gas' },
  'Pakistan': { city: 'Karachi', lat: 24.8607, lng: 67.0011, industry: 'Textiles' },
  'Bangladesh': { city: 'Dhaka', lat: 23.8103, lng: 90.4125, industry: 'Textiles' },
  'Philippines': { city: 'Manila', lat: 14.5995, lng: 120.9842, industry: 'Electronics' },
  'Taiwan': { city: 'Taipei', lat: 25.033, lng: 121.5654, industry: 'Electronics' },
  'Czech Republic': { city: 'Prague', lat: 50.0755, lng: 14.4378, industry: 'Manufacturing' },
  'Hungary': { city: 'Budapest', lat: 47.4979, lng: 19.0402, industry: 'Manufacturing' },
  'Romania': { city: 'Bucharest', lat: 44.4268, lng: 26.1025, industry: 'IT Services' },
  'Ireland': { city: 'Dublin', lat: 53.3498, lng: -6.2603, industry: 'IT Services' }
}

// City coordinates for origin/destination locations
const cityCoordinates = {
  // USA
  'New York, USA': { lat: 40.7128, lng: -74.0060 },
  'Los Angeles, USA': { lat: 34.0522, lng: -118.2437 },
  'Chicago, USA': { lat: 41.8781, lng: -87.6298 },
  'Dallas, USA': { lat: 32.7767, lng: -96.7970 },
  'Long Beach, USA': { lat: 33.7701, lng: -118.1937 },
  'Seattle, USA': { lat: 47.6062, lng: -122.3321 },
  'San Francisco, USA': { lat: 37.7749, lng: -122.4194 },
  
  // Canada
  'Toronto, Canada': { lat: 43.6532, lng: -79.3832 },
  'Vancouver, Canada': { lat: 49.2827, lng: -123.1207 },
  
  // Mexico
  'Mexico City, Mexico': { lat: 19.4326, lng: -99.1332 },
  'Guadalajara, Mexico': { lat: 20.6597, lng: -103.3496 },
  
  // Europe
  'Rotterdam, Netherlands': { lat: 51.9225, lng: 4.4792 },
  'Berlin, Germany': { lat: 52.5200, lng: 13.4050 },
  'Paris, France': { lat: 48.8566, lng: 2.3522 },
  'Milan, Italy': { lat: 45.4642, lng: 9.1900 },
  'Rome, Italy': { lat: 41.9028, lng: 12.4964 },
  'Hamburg, Germany': { lat: 53.5511, lng: 9.9937 },
  'Munich, Germany': { lat: 48.1351, lng: 11.5820 },
  'Vienna, Austria': { lat: 48.2082, lng: 16.3738 },
  'Madrid, Spain': { lat: 40.4168, lng: -3.7038 },
  'Barcelona, Spain': { lat: 41.3851, lng: 2.1734 },
  'Marseille, France': { lat: 43.2965, lng: 5.3698 },
  'Lyon, France': { lat: 45.7640, lng: 4.8357 },
  'Zurich, Switzerland': { lat: 47.3769, lng: 8.5417 },
  'Warsaw, Poland': { lat: 52.2297, lng: 21.0122 },
  'Brussels, Belgium': { lat: 50.8503, lng: 4.3517 },
  'Amsterdam, Netherlands': { lat: 52.3676, lng: 4.9041 },
  'Frankfurt, Germany': { lat: 50.1109, lng: 8.6821 },
  
  // Asia
  'Shanghai, China': { lat: 31.2304, lng: 121.4737 },
  'Beijing, China': { lat: 39.9042, lng: 116.4074 },
  'Guangzhou, China': { lat: 23.1291, lng: 113.2644 },
  'Shenzhen, China': { lat: 22.5431, lng: 114.0579 },
  'Tokyo, Japan': { lat: 35.6762, lng: 139.6503 },
  'Osaka, Japan': { lat: 34.6937, lng: 135.5023 },
  'Singapore': { lat: 1.3521, lng: 103.8198 },
  'Bangkok, Thailand': { lat: 13.7563, lng: 100.5018 },
  'Dubai, UAE': { lat: 25.2048, lng: 55.2708 },
  'Seoul, South Korea': { lat: 37.5665, lng: 126.9780 },
  'Mumbai, India': { lat: 19.0760, lng: 72.8777 },
  'Delhi, India': { lat: 28.6139, lng: 77.2090 },
  'Hong Kong': { lat: 22.3193, lng: 114.1694 },
  'Taipei, Taiwan': { lat: 25.0330, lng: 121.5654 },
  'Manila, Philippines': { lat: 14.5995, lng: 120.9842 },
  'Jakarta, Indonesia': { lat: -6.2088, lng: 106.8456 },
  'Kuala Lumpur, Malaysia': { lat: 3.1390, lng: 101.6869 },
  'Ho Chi Minh City, Vietnam': { lat: 10.8231, lng: 106.6297 },
  'Hanoi, Vietnam': { lat: 21.0285, lng: 105.8542 },
  
  // Oceania
  'Sydney, Australia': { lat: -33.8688, lng: 151.2093 },
  'Melbourne, Australia': { lat: -37.8136, lng: 144.9631 },
  'Brisbane, Australia': { lat: -27.4698, lng: 153.0251 },
  'Perth, Australia': { lat: -31.9505, lng: 115.8605 },
  'Auckland, New Zealand': { lat: -36.8485, lng: 174.7633 },
  'Wellington, New Zealand': { lat: -41.2865, lng: 174.7762 },
  
  // South America
  'Sao Paulo, Brazil': { lat: -23.5558, lng: -46.6396 },
  'Rio de Janeiro, Brazil': { lat: -22.9068, lng: -43.1729 },
  'Buenos Aires, Argentina': { lat: -34.6037, lng: -58.3816 },
  'Santiago, Chile': { lat: -33.4489, lng: -70.6693 },
  'Lima, Peru': { lat: -12.0464, lng: -77.0428 },
  'Bogota, Colombia': { lat: 4.7110, lng: -74.0721 },
  
  // Africa & Middle East
  'Cairo, Egypt': { lat: 30.0444, lng: 31.2357 },
  'Lagos, Nigeria': { lat: 6.5244, lng: 3.3792 },
  'Nairobi, Kenya': { lat: -1.2921, lng: 36.8219 },
  'Johannesburg, South Africa': { lat: -26.2041, lng: 28.0473 },
  'Cape Town, South Africa': { lat: -33.9249, lng: 18.4241 },
  'Casablanca, Morocco': { lat: 33.5731, lng: -7.5898 },
  'Rabat, Morocco': { lat: 34.0209, lng: -6.8416 },
  'Fes, Morocco': { lat: 34.0181, lng: -5.0078 },
  'Tangier, Morocco': { lat: 35.7595, lng: -5.8340 },
  'Riyadh, Saudi Arabia': { lat: 24.7136, lng: 46.6753 },
  'Jeddah, Saudi Arabia': { lat: 21.5433, lng: 39.1728 },
  'Tehran, Iran': { lat: 35.6892, lng: 51.3890 },
  'Istanbul, Turkey': { lat: 41.0082, lng: 28.9783 },
  'Ankara, Turkey': { lat: 39.9334, lng: 32.8597 },
  
  // Europe (additional)
  'London, UK': { lat: 51.5074, lng: -0.1278 },
  'Birmingham, UK': { lat: 52.4862, lng: -1.8904 },
  'Manchester, UK': { lat: 53.4808, lng: -2.2426 },
  'Liverpool, UK': { lat: 53.4084, lng: -2.9916 },
  'Edinburgh, UK': { lat: 55.9533, lng: -3.1883 },
  'Dublin, Ireland': { lat: 53.3498, lng: -6.2603 },
  'Copenhagen, Denmark': { lat: 55.6761, lng: 12.5683 },
  'Stockholm, Sweden': { lat: 59.3293, lng: 18.0686 },
  'Oslo, Norway': { lat: 59.9139, lng: 10.7522 },
  'Helsinki, Finland': { lat: 60.1699, lng: 24.9384 },
  'Lisbon, Portugal': { lat: 38.7223, lng: -9.1393 },
  'Porto, Portugal': { lat: 41.1579, lng: -8.6291 },
  'Athens, Greece': { lat: 37.9838, lng: 23.7275 },
  'Barcelona, Spain': { lat: 41.3851, lng: 2.1734 },
  'Valencia, Spain': { lat: 39.4699, lng: -0.3763 },
  'Seville, Spain': { lat: 37.3891, lng: -5.9845 },
  'Milan, Italy': { lat: 45.4642, lng: 9.1900 },
  'Naples, Italy': { lat: 40.8518, lng: 14.2681 },
  'Rome, Italy': { lat: 41.9028, lng: 12.4964 },
  'Florence, Italy': { lat: 43.7696, lng: 11.2558 },
  'Prague, Czech Republic': { lat: 50.0755, lng: 14.4378 },
  'Budapest, Hungary': { lat: 47.4979, lng: 19.0402 },
  'Bucharest, Romania': { lat: 44.4268, lng: 26.1025 },
  'Vienna, Austria': { lat: 48.2082, lng: 16.3738 },
  'Salzburg, Austria': { lat: 47.8095, lng: 13.0550 },
  'Warsaw, Poland': { lat: 52.2297, lng: 21.0122 },
  'Krakow, Poland': { lat: 50.0647, lng: 19.9450 },
  'Brussels, Belgium': { lat: 50.8503, lng: 4.3517 },
  'Antwerp, Belgium': { lat: 51.2194, lng: 4.4025 },
  'Geneva, Switzerland': { lat: 46.2044, lng: 6.1432 },
  'Zurich, Switzerland': { lat: 47.3769, lng: 8.5417 },
  'Berlin, Germany': { lat: 52.5200, lng: 13.4050 },
  'Hamburg, Germany': { lat: 53.5511, lng: 9.9937 },
  'Munich, Germany': { lat: 48.1351, lng: 11.5820 },
  'Frankfurt, Germany': { lat: 50.1109, lng: 8.6821 },
  'Cologne, Germany': { lat: 50.9375, lng: 6.9603 },
  'Dresden, Germany': { lat: 51.0504, lng: 13.7373 },
  'Leipzig, Germany': { lat: 51.3397, lng: 12.3731 },
  'Dusseldorf, Germany': { lat: 51.2277, lng: 6.7735 },
  'Stuttgart, Germany': { lat: 48.7758, lng: 9.1829 },
  'Nuremberg, Germany': { lat: 49.4521, lng: 11.0767 },
  'Paris, France': { lat: 48.8566, lng: 2.3522 },
  'Marseille, France': { lat: 43.2965, lng: 5.3698 },
  'Lyon, France': { lat: 45.7640, lng: 4.8357 },
  'Nice, France': { lat: 43.7102, lng: 7.2620 },
  'Toulouse, France': { lat: 43.6047, lng: 1.4442 },
  'Strasbourg, France': { lat: 48.5734, lng: 7.7521 },
  'Bordeaux, France': { lat: 44.8378, lng: -0.5792 }
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
  loading.value = true
  errorMessage.value = ''
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
    // Fall back to cityCoordinates if backend doesn't provide coordinates
    const shipmentRoutes = shipmentsData.map(shipment => {
      let startLat = shipment.originLat
      let startLng = shipment.originLng
      let endLat = shipment.destLat
      let endLng = shipment.destLng

      // Fall back to cityCoordinates if backend coordinates are missing or zero
      if ((!startLat || startLat === 0) && shipment.origin) {
        const originCoords = cityCoordinates[shipment.origin]
        if (originCoords) {
          startLat = originCoords.lat
          startLng = originCoords.lng
        }
      }
      if ((!endLat || endLat === 0) && shipment.destination) {
        const destCoords = cityCoordinates[shipment.destination]
        if (destCoords) {
          endLat = destCoords.lat
          endLng = destCoords.lng
        }
      }

      return {
        id: shipment.id,
        startLat: startLat || 0,
        startLng: startLng || 0,
        endLat: endLat || 0,
        endLng: endLng || 0,
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
      let lat = shipment.originLat
      let lng = shipment.originLng

      // Fall back to cityCoordinates if backend coordinates are missing or zero
      if ((!lat || lat === 0) && shipment.origin) {
        const originCoords = cityCoordinates[shipment.origin]
        if (originCoords) {
          lat = originCoords.lat
          lng = originCoords.lng
        }
      }

      if (shipment.origin && lat && lng) {
        locationMap.set(shipment.origin, {
          name: shipment.origin,
          lat: lat,
          lng: lng,
          type: 'origin',
          id: `origin_${shipment.id}`
        })
      }
    })

    // Add destinations
    shipmentsData.forEach(shipment => {
      let lat = shipment.destLat
      let lng = shipment.destLng

      // Fall back to cityCoordinates if backend coordinates are missing or zero
      if ((!lat || lat === 0) && shipment.destination) {
        const destCoords = cityCoordinates[shipment.destination]
        if (destCoords) {
          lat = destCoords.lat
          lng = destCoords.lng
        }
      }

      if (shipment.destination && lat && lng) {
        locationMap.set(shipment.destination, {
          name: shipment.destination,
          lat: lat,
          lng: lng,
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
    errorMessage.value = 'Failed to load map data. Please check your connection and try again.'
    // Fallback to sample data if API fails
    useSampleData()
  } finally {
    loading.value = false
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

  try {
    globeInstance = Globe()(globeEl.value)
    if (!globeInstance) return

    globeInstance
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
          // For suppliers, find routes by supplier name (case-insensitive partial match)
          const searchName = d.name.toLowerCase()
          relatedRoutes = routes.value.filter(route => {
            if (!route.supplierName) return false
            const routeSupplierName = route.supplierName.toLowerCase()
            return routeSupplierName === searchName ||
                   routeSupplierName.includes(searchName) ||
                   searchName.includes(routeSupplierName)
          })
        } else if (d.type === 'origin') {
          // For origins, find routes where this is the origin
          relatedRoutes = routes.value.filter(route => {
            if (!route.origin) return false
            return route.origin.toLowerCase().includes(d.name.toLowerCase()) ||
                   d.name.toLowerCase().includes(route.origin.toLowerCase())
          })
        } else if (d.type === 'destination') {
          // For destinations, find routes where this is the destination
          relatedRoutes = routes.value.filter(route => {
            if (!route.destination) return false
            return route.destination.toLowerCase().includes(d.name.toLowerCase()) ||
                   d.name.toLowerCase().includes(route.destination.toLowerCase())
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
  } catch (error) {
    console.error('Error initializing globe:', error)
    globeInstance = null
    return
  }

  // Add arcs for shipment routes
  if (globeInstance) {
    try {
      globeInstance
        .arcsData(routes.value)
        .arcColor(() => '#c62828')
        .arcAltitude(0.52) // Adjust altitude to avoid routes going through the earth and pointing to sky
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
    } catch (error) {
      console.error('Error adding arcs:', error)
    }
  }

  // Country hover highlight + label
  fetch(GEOJSON_URL)
    .then((r) => r.json())
    .then((geojson) => {
      let hoveredCountry = null

      if (globeInstance) {
        try {
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
              if (globeInstance) {
                try {
                  globeInstance
                    .polygonAltitude((f) => (f === hoveredCountry ? 0.02 : 0.004))
                    .polygonCapColor((f) =>
                      f === hoveredCountry ? 'rgba(60,194,96,0.7)' : 'rgba(46,93,50,0.25)'
                    )
                } catch (error) {
                  console.error('Error updating polygon hover:', error)
                }
              }
            })
        } catch (error) {
          console.error('Error adding polygons:', error)
        }
      }
    })
    .catch(() => {
      // If country borders fail to load, keep the globe and supplier points
    })

  if (globeInstance) {
    try {
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
    } catch (error) {
      console.error('Error setting controls:', error)
    }
  }

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
      <!-- Loading skeleton -->
      <div v-if="loading" class="globe-skeleton">
        <div class="loading-spinner"></div>
        <p class="skeleton-text">Loading...</p>
      </div>

      <!-- Error message -->
      <div v-else-if="errorMessage" class="globe-error">
        <div class="error-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="10"/>
            <line x1="12" y1="8" x2="12" y2="12"/>
            <line x1="12" y1="16" x2="12.01" y2="16"/>
          </svg>
        </div>
        <p class="error-message">{{ errorMessage }}</p>
        <button class="retry-btn" @click="fetchData">Retry</button>
      </div>

      <!-- Globe -->
      <div v-show="!loading && !errorMessage" ref="globeEl" class="globe-canvas" />

      <div v-if="!loading && !errorMessage" class="map-legend">
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
  width: 100%;
  min-height: 500px;
  background: radial-gradient(circle at 30% 20%, #f5fff6 0%, #eef7f0 40%, #e6f0e8 100%);
  max-width: 1280px;
  margin: 0 auto;
}

.globe-canvas {
  width: 100%;
  height: 100%;
  min-height: 500px;
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

/* Globe loading skeleton */
.globe-skeleton {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  min-height: 400px;
  background: radial-gradient(circle at 30% 20%, #f5fff6 0%, #eef7f0 40%, #e6f0e8 100%);
}

.loading-spinner {
  width: 48px;
  height: 48px;
  border: 4px solid #e8efe8;
  border-top-color: #34c759;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.skeleton-text {
  margin-top: 1.5rem;
  font-size: 1rem;
  color: #6b7d6b;
}

/* Globe error state */
.globe-error {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  min-height: 400px;
  text-align: center;
}

.error-icon {
  width: 48px;
  height: 48px;
  color: #dc2626;
  margin-bottom: 1rem;
}

.error-icon svg {
  width: 100%;
  height: 100%;
}

.error-message {
  margin: 0 0 1rem;
  font-size: 1rem;
  color: #991b1b;
}

.retry-btn {
  padding: 0.5rem 1.5rem;
  background: #5f795f;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 0.9rem;
  font-weight: 600;
  cursor: pointer;
  transition: background-color 0.2s;
}

.retry-btn:hover {
  background: #4a5c4a;
}
</style>
