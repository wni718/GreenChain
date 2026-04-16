<script setup>
</script>

<template>
  <main class="home" aria-label="Home">
    <div class="home-content">
      <h1 class="home-title">Welcome to GreenChain</h1>
      <p class="home-description">
        GreenChain is a comprehensive supply chain sustainability platform that helps
        businesses track, analyze, and optimize their environmental impact.
      </p>
      <div class="home-features">
        <div class="feature-card">
          <h2>Core Indicators</h2>
          <p>Monitor key sustainability metrics and carbon emissions data</p>
        </div>
        <div class="feature-card">
          <h2>Carbon Emission Charts</h2>
          <p>Visualize emissions trends and transportation mode distribution</p>
        </div>
        <div class="feature-card">
          <h2>Supplier Chain Map</h2>
          <p>Interactive global map of suppliers and shipment routes</p>
        </div>
        <div class="feature-card">
          <h2>Shipment Tracking</h2>
          <p>Track and manage your supply chain shipments</p>
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
