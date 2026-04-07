<script setup>
import { useRouter } from 'vue-router'
import { useAuth } from '../composables/useAuth'
import UserAvatar from './UserAvatar.vue'
import brandLogoUrl from '../assets/GreenChain_logo.jpg'

const router = useRouter()
const { currentUser } = useAuth()

function onUserChipClick() {
  if (!currentUser.value) {
    router.push({ name: 'login' })
    return
  }
  router.push({ name: 'profile' })
}

function goHome() {
  router.push({ name: 'home' })
}
</script>

<template>
  <header class="top-bar">
    <button type="button" class="brand" @click="goHome">
      <img :src="brandLogoUrl" alt="GreenChain logo" class="brand-logo" />
      <span class="brand-text">GreenChain</span>
    </button>
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
