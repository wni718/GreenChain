<script setup>
import InteractiveGlobe from '../components/InteractiveGlobe.vue'
</script>

<template>
  <main class="home" aria-label="主页">
    <InteractiveGlobe />
  </main>
</template>

<style scoped>
.home {
  flex: 1;
  min-height: 0;
  background: #fff;
  display: flex;
}
</style>
