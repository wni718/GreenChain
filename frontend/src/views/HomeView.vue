<script setup>
import InteractiveGlobe from '../components/InteractiveGlobe.vue'
</script>

<template>
  <main class="home" aria-label="Home">
    <InteractiveGlobe />
  </main>
</template>

<style scoped>
.home {
  flex: 0 0 auto;
  width: 100%;
  min-height: 0;
  background: #fff;
  display: flex;
}
</style>
