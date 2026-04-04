<script setup>
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuth } from '../composables/useAuth'
import UserAvatar from '../components/UserAvatar.vue'

const router = useRouter()
const { currentUser, logout, setLoggedIn } = useAuth()

/** 与头像直径一致，便于用户名、邮箱在头像正下方居中 */
const avatarSizePx = 208

function onLogout() {
  logout()
  router.push({ name: 'home' })
}

/** 旧会话或未写入邮箱时，从后端按用户名补全 */
onMounted(async () => {
  const u = currentUser.value
  if (!u?.username) return
  if (u.email && String(u.email).trim() !== '') return
  try {
    const r = await fetch('/api/auth/account/' + encodeURIComponent(u.username))
    if (!r.ok) return
    const p = await r.json()
    setLoggedIn({
      username: p.username ?? u.username,
      email: p.email != null && p.email !== '' ? String(p.email) : '',
    })
  } catch {
    /* ignore */
  }
})
</script>

<template>
  <main class="profile-page" aria-label="用户主页">
    <section v-if="currentUser" class="profile">
      <div class="profile-head">
        <UserAvatar :size="avatarSizePx" class="profile-avatar" />
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
  flex: 1;
  padding: 1.5rem 1.25rem;
  background: #fff;
}

.profile-head {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 0.65rem;
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
