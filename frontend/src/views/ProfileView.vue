<script setup>
import { computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuth } from '../composables/useAuth'
import UserAvatar from '../components/UserAvatar.vue'

const router = useRouter()
const { currentUser, logout, setLoggedIn } = useAuth()

/** Match avatar column width so role, avatar, name, and email align */
const avatarSizePx = 208

const roleLabel = computed(() => {
  const r = currentUser.value?.role ? String(currentUser.value.role) : ''
  if (!r) return ''
  if (r === 'SUSTAINABILITY_MANAGER') return 'Sustainability manager'
  if (r === 'SUPPLIER') return 'Supplier'
  if (r === 'VIEWER') return 'Viewer'
  if (r === 'ADMIN') return 'Admin'
  return r
})

function onLogout() {
  logout()
  router.push({ name: 'home' })
}

/** Backfill email and/or role from API when missing in session */
onMounted(async () => {
  const u = currentUser.value
  if (!u?.username) return
  const hasEmail = u.email && String(u.email).trim() !== ''
  const hasRole = u.role && String(u.role).trim() !== ''
  if (hasEmail && hasRole) return
  try {
    const r = await fetch('/api/auth/account/' + encodeURIComponent(u.username))
    if (!r.ok) return
    const p = await r.json()
    setLoggedIn({
      username: p.username ?? u.username,
      email: hasEmail ? u.email : p.email != null && p.email !== '' ? String(p.email) : '',
      role: hasRole ? u.role : p.role != null ? String(p.role) : '',
    })
  } catch {
    /* ignore */
  }
})
</script>

<template>
  <main class="profile-page" aria-label="Profile">
    <section v-if="currentUser" class="profile">
      <div class="profile-head">
        <div class="profile-avatar-col" :style="{ width: avatarSizePx + 'px' }">
          <p v-if="roleLabel" class="profile-role">{{ roleLabel }}</p>
          <UserAvatar :size="avatarSizePx" class="profile-avatar" />
        </div>
        <div class="profile-text" :style="{ width: avatarSizePx + 'px' }">
          <p class="profile-name">{{ currentUser.username }}</p>
          <p class="profile-email">{{ currentUser.email || '—' }}</p>
        </div>
        <div class="profile-actions" :style="{ width: avatarSizePx + 'px' }">
          <button type="button" class="logout-btn" @click="onLogout">Log out</button>
        </div>
      </div>
    </section>
  </main>
</template>

<style scoped>
.profile-page {
  flex: 0 0 auto;
  width: 100%;
  padding: 1.5rem 1.25rem;
  background: #fff;
}

.profile-head {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 0.65rem;
}

.profile-avatar-col {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
}

.profile-role {
  margin: 0;
  font-size: 0.95rem;
  font-weight: 750;
  letter-spacing: 0.02em;
  color: #3d5c3d;
  text-align: center;
}

.profile-avatar {
  color: #528951;
}

.profile-text {
  text-align: center;
}

.profile-name {
  margin: 0;
  font-size: 1.15rem;
  font-weight: 700;
  color: #2d3a2d;
  overflow-wrap: anywhere;
}

.profile-email {
  margin: 0;
  font-size: 0.95rem;
  color: #4a5c4a;
  word-break: break-all;
}

.profile-actions {
  display: flex;
  justify-content: center;
}

.logout-btn {
  padding: 0.55rem 1.25rem;
  border: none;
  border-radius: 4px;
  background: #dc2626;
  color: #fff;
  font-size: 0.95rem;
  font-weight: 600;
  cursor: pointer;
  font-family: inherit;
}

.logout-btn:hover {
  background: #b91c1c;
}

.logout-btn:active {
  background: #991b1b;
}
</style>
