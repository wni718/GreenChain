<script setup>
import { ref, computed } from 'vue'
import { defaultAvatarImageUrl } from '../utils/defaultAvatar'

const props = defineProps({
  size: { type: Number, default: 28 },
})

const imgFailed = ref(false)
const showPhoto = computed(() => Boolean(defaultAvatarImageUrl) && !imgFailed.value)
</script>

<template>
  <span class="avatar-root" :style="{ width: props.size + 'px', height: props.size + 'px' }">
    <img
      v-if="showPhoto"
      :src="defaultAvatarImageUrl"
      alt=""
      class="avatar-img"
      @error="imgFailed = true"
    />
    <svg
      v-else
      class="avatar-svg"
      viewBox="0 0 32 32"
      :width="props.size"
      :height="props.size"
      fill="none"
      xmlns="http://www.w3.org/2000/svg"
      aria-hidden="true"
    >
      <circle cx="16" cy="16" r="15" stroke="currentColor" stroke-width="1.5" fill="rgba(255,255,255,0.2)" />
      <circle cx="16" cy="12" r="5" fill="currentColor" opacity="0.9" />
      <path
        d="M7 28c1.2-5 4.5-8 9-8s7.8 3 9 8"
        stroke="currentColor"
        stroke-width="1.5"
        stroke-linecap="round"
        fill="none"
        opacity="0.9"
      />
    </svg>
  </span>
</template>

<style scoped>
.avatar-root {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  overflow: hidden;
  border-radius: 50%;
  color: inherit;
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.avatar-svg {
  display: block;
}
</style>
