<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const pageTitle = computed(() => String(route.meta.menuTitle || ''))
</script>

<template>
  <div class="menu-page">
    <h1 class="menu-page__title">{{ pageTitle }}</h1>
    <div class="menu-page__body" aria-hidden="true" />
  </div>
</template>

<style scoped>
.menu-page {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
  padding: 1.25rem 1.5rem;
}

.menu-page__title {
  margin: 0 0 1rem;
  font-size: 2rem;
  font-weight: 900;
  color: #5F795F;
}

.menu-page__body {
  flex: 1;
  min-height: 120px;
}
</style>
