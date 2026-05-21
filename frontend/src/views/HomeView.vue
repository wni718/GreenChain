<script setup>
import { useRouter } from 'vue-router'
import { useI18n } from '../composables/useI18n'

const router = useRouter()
const { t } = useI18n()

function navigateTo(routeName) {
  router.push({ name: routeName })
}
</script>

<template>
  <main class="home" aria-label="Home">
    <div class="home-content">
      <h1 class="home-title">{{ t('welcome') }}</h1>
      <p class="home-description">{{ t('description') }}</p>
      <div class="home-features">
        <div class="feature-card" @click="navigateTo('core-indicators')">
          <h2>{{ t('core-indicators') }}</h2>
          <p>{{ t('core-indicators-desc') }}</p>
        </div>
        <div class="feature-card" @click="navigateTo('carbon-emission-charts')">
          <h2>{{ t('carbon-emission-charts') }}</h2>
          <p>{{ t('carbon-emission-charts-desc') }}</p>
        </div>
        <div class="feature-card" @click="navigateTo('supplier-chain-map')">
          <h2>{{ t('supplier-chain-map') }}</h2>
          <p>{{ t('supplier-chain-map-desc') }}</p>
        </div>
        <div class="feature-card" @click="navigateTo('shipment-tracking')">
          <h2>{{ t('shipment-tracking') }}</h2>
          <p>{{ t('shipment-tracking-desc') }}</p>
        </div>
      </div>
    </div>
  </main>
</template>

<style scoped>
.home {
  flex: 1;
  width: 100%;
  min-height: 100vh;
  background: radial-gradient(circle at 30% 20%, #f5fff6 0%, #eef7f0 40%, #e6f0e8 100%);
  display: flex;
  align-items: center;
  justify-content: center;
}

.home-content {
  max-width: 1200px;
  padding: 2rem;
  text-align: center;
}

.home-title {
  font-size: 3rem;
  font-weight: 900;
  color: #5f795f;
  margin-bottom: 1rem;
}

.home-description {
  font-size: 1.25rem;
  color: #4a5c4a;
  margin-bottom: 3rem;
  max-width: 800px;
  margin-left: auto;
  margin-right: auto;
}

.home-features {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 2rem;
  margin-top: 2rem;
}

.feature-card {
  background: rgba(255, 255, 255, 0.9);
  padding: 2rem;
  border-radius: 10px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s ease;
  cursor: pointer;
}

.feature-card:hover {
  transform: translateY(-5px);
}

.feature-card h2 {
  font-size: 1.5rem;
  font-weight: 700;
  color: #3d5340;
  margin-bottom: 1rem;
}

.feature-card p {
  color: #5a6b5a;
  font-size: 1rem;
}

@media (max-width: 768px) {
  .home-title {
    font-size: 2.5rem;
  }
  
  .home-description {
    font-size: 1.1rem;
  }
  
  .home-features {
    grid-template-columns: 1fr;
  }
}
</style>
