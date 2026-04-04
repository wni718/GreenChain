<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import AppHeader from '../components/AppHeader.vue'
import AppSidebar from '../components/AppSidebar.vue'

const route = useRoute()
const showAppMenu = computed(() => Boolean(route.meta.showAppMenu))
</script>

<template>
  <div class="layout">
    <AppHeader />
    <div class="body" :class="{ 'body--with-menu': showAppMenu }">
      <template v-if="showAppMenu">
        <AppSidebar />
        <div class="main-content">
          <router-view />
        </div>
      </template>
      <router-view v-else />
    </div>
  </div>
</template>

<style scoped>
.layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f4f6f4;
}

.body {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.body--with-menu {
  flex-direction: row;
  align-items: stretch;
}

.main-content {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  background: #fff;
  overflow: auto;
}
</style>
