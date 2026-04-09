<script setup>
import { RouterLink } from 'vue-router'

const items = [
  { name: 'core-indicators', label: 'Core indicators' },
  { name: 'carbon-emission-charts', label: 'Carbon Emission Charts' },
  { name: 'supplier-chain-map', label: 'Supplier Chain Map' },
  { name: 'supplier-management', label: 'Supplier Management' },
  { name: 'shipment-tracking', label: 'Shipment Tracking' },
  { name: 'generate-report', label: 'Generate Report' },
]
</script>

<template>
  <aside class="sidebar" aria-label="Main menu">
    <div class="sidebar-title">Menu</div>
    <nav class="sidebar-nav">
      <RouterLink
        v-for="item in items"
        :key="item.name"
        :to="{ name: item.name }"
        class="sidebar-link"
      >
        {{ item.label }}
      </RouterLink>
    </nav>
  </aside>
</template>

<style scoped>
.sidebar {
  width: 240px;
  flex-shrink: 0;
  min-height: 0;
  background: #eef4ee;
  border-right: 1px solid #c5d6c5;
  padding: 1.25rem 0;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
}

.sidebar-title {
  padding: 0 1.25rem;
  font-size: 2rem;
  font-weight: 700;
  letter-spacing: 0.06em;
  color: #528951;
  user-select: none;
  pointer-events: none;
}

.sidebar-nav {
  display: flex;
  flex-direction: column;
  gap: 0.15rem;
}

.sidebar-link {
  display: block;
  padding: 0.55rem 1.25rem 0.45rem;
  font-size: 0.9rem;
  font-weight: 750;
  color: #748372;
  text-decoration: none;
  transition: color 0.12s ease, background 0.12s ease;
}

.sidebar-link::after {
  content: '';
  display: block;
  height: 1px;
  margin: 0.45rem 0.01rem 0;
  background: #a5b2a5;
  transition: background-color 0.12s ease;
}

.sidebar-link:hover {
  background: rgba(82, 137, 81, 0.12);
}

.sidebar-link.router-link-active {
  color: #1a241a;
  background: rgba(72, 105, 72, 0.35);
}

.sidebar-link.router-link-active::after {
  background: #6f806f;
}
</style>
