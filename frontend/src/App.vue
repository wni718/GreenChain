<script setup>
import { onBeforeUnmount, ref } from 'vue'
import logoUrl from './assets/GreenChain_logo.jpg'

const showSplash = ref(true)
const fadingOut = ref(false)
const logoReady = ref(false)

let hideTimer = null

function onBrandAnimEnd() {
  if (fadingOut.value) return
  // After title animation, fade out over 2s
  fadingOut.value = true
  hideTimer = window.setTimeout(() => {
    showSplash.value = false
  }, 2000)
}

function onLogoReady() {
  logoReady.value = true
}

onBeforeUnmount(() => {
  if (hideTimer) window.clearTimeout(hideTimer)
})
</script>

<template>
  <div v-if="showSplash" class="splash" :class="{ 'splash--fade': fadingOut }" aria-hidden="true">
    <div class="splash__content" :class="{ 'splash__content--ready': logoReady }">
      <img
        :src="logoUrl"
        alt="GreenChain logo"
        class="splash__logo"
        @load="onLogoReady"
        @error="onLogoReady"
      />
      <div
        class="splash__brand"
        :class="{ 'splash__brand--animate': logoReady }"
        @animationend="onBrandAnimEnd"
      >
        GreenChain
      </div>
    </div>
  </div>
  <router-view />
</template>

<style scoped>
.splash {
  position: fixed;
  inset: 0;
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #ffffff;
  opacity: 1;
  transition: opacity 2s ease;
}

.splash__content {
  display: flex;
  flex-direction: column;
  align-items: center;
  opacity: 0;
}

.splash__content--ready {
  opacity: 1;
}

.splash--fade {
  opacity: 0;
}

.splash__logo {
  width: min(320px, 65vw);
  height: auto;
}

.splash__brand {
  margin-top: 14px;
  font-size: clamp(28px, 4vw, 40px);
  font-weight: 700;
  color: #3cc260;
  letter-spacing: 0.4px;
}

.splash__brand--animate {
  animation: brand-pop 1.2s ease-out forwards;
}

@keyframes brand-pop {
  0% {
    transform: scale(0.95);
  }
  45% {
    transform: scale(1.35);
  }
  100% {
    transform: scale(1);
  }
}
</style>
