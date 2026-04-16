<script setup>
import { useRouter } from 'vue-router'
import { useAuth } from '../composables/useAuth'
import { ref } from 'vue'
import UserAvatar from './UserAvatar.vue'
import brandLogoUrl from '../assets/GreenChain_logo.jpg'

const router = useRouter()
const { currentUser } = useAuth()
const showMenu = ref(false)

function onUserChipClick() {
  if (!currentUser.value) {
    router.push({ name: 'login' })
    return
  }
  router.push({ name: 'profile' })
}

function goHome() {
  router.push({ name: 'home' })
  showMenu.value = false
}

function navigateTo(routeName) {
  router.push({ name: routeName })
  showMenu.value = false
}

function toggleMenu() {
  showMenu.value = !showMenu.value
}

const menuItems = [
  { name: 'core-indicators', label: 'Core indicators' },
  { name: 'carbon-emission-charts', label: 'Carbon Emission Charts' },
  { name: 'supplier-chain-map', label: 'Supplier Chain Map' },
  { name: 'supplier-management', label: 'Supplier Management' },
  { name: 'shipment-tracking', label: 'Shipment Tracking' },
  { name: 'generate-report', label: 'Generate Report' },
]
</script>

<template>
  <header class="top-bar">
    <div class="left-section">
      <button type="button" class="brand" @click="goHome">
        <img :src="brandLogoUrl" alt="GreenChain logo" class="brand-logo" />
        <span class="brand-text">GreenChain</span>
      </button>
      <div class="menu-dropdown">
        <button type="button" class="menu-button" @click="toggleMenu">
          Menu
          <span class="menu-icon">▼</span>
        </button>
        <div v-if="showMenu" class="dropdown-menu">
          <button 
            v-for="item in menuItems" 
            :key="item.name"
            class="dropdown-item"
            @click="navigateTo(item.name)"
          >
            {{ item.label }}
          </button>
        </div>
      </div>
    </div>
    <button type="button" class="user-chip" @click="onUserChipClick">
      <span class="avatar" aria-hidden="true">
        <UserAvatar :size="28" />
      </span>
      <span class="user-label">{{ currentUser ? currentUser.username : 'log in' }}</span>
    </button>
  </header>
</template>

<style scoped>
.top-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 52px;
  padding: 0 1.25rem;
  background: #71ad75;
  box-sizing: border-box;
}

.left-section {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.brand {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  margin: 0;
  padding: 0;
  border: none;
  background: none;
  cursor: pointer;
}

.menu-dropdown {
  position: relative;
}

.menu-button {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.35rem 0.75rem;
  border: none;
  border-radius: 6px;
  background: #528951;
  color: #fff;
  cursor: pointer;
  font-family: inherit;
  font-size: 0.95rem;
  font-weight: 600;
  transition: background 0.15s ease, filter 0.15s ease;
}

.menu-button:hover {
  filter: brightness(1.06);
}

.menu-button:active {
  background: #3f6d3e;
  filter: brightness(0.95);
}

.menu-icon {
  font-size: 0.75rem;
  transition: transform 0.2s ease;
}

.dropdown-menu {
  position: absolute;
  top: 100%;
  left: 0;
  margin-top: 0.25rem;
  background: white;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  min-width: 200px;
  z-index: 1000;
  overflow: hidden;
}

.dropdown-item {
  display: block;
  width: 100%;
  padding: 0.75rem 1rem;
  border: none;
  background: none;
  text-align: left;
  cursor: pointer;
  font-family: inherit;
  font-size: 0.9rem;
  color: #3d5340;
  transition: background 0.12s ease;
}

.dropdown-item:hover {
  background: #f4f6f4;
}

.dropdown-item:active {
  background: #eef4ee;
}

.brand-logo {
  width: 34px;
  height: 34px;
  border-radius: 4px;
  object-fit: cover;
}

.brand-text {
  font-size: 1.25rem;
  font-weight: 600;
  letter-spacing: 0.02em;
  color: #fff;
  font-family: inherit;
}

.brand:hover {
  opacity: 0.92;
}

.user-chip {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.35rem 0.65rem 0.35rem 0.45rem;
  border: none;
  border-radius: 6px;
  background: #528951;
  color: #fff;
  cursor: pointer;
  font-family: inherit;
  font-size: 0.95rem;
  transition: background 0.15s ease, filter 0.15s ease;
}

.user-chip:hover {
  filter: brightness(1.06);
}

.user-chip:active {
  background: #3f6d3e;
  filter: brightness(0.95);
}

.avatar {
  display: flex;
  color: #fff;
}

.user-label {
  max-width: 10rem;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
