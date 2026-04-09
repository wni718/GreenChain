<script setup>
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuth } from '../composables/useAuth'
import { formatErrorBody } from '../utils/apiError'

const route = useRoute()
const router = useRouter()
const { setLoggedIn } = useAuth()

const email = ref('')
const password = ref('')
const message = ref('')

async function onSubmit() {
  message.value = ''
  try {
    const res = await fetch('/api/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        email: email.value,
        password: password.value,
      }),
    })
    if (res.ok) {
      const profile = await res.json()
      setLoggedIn({
        username: profile.username ?? email.value,
        email: profile.email != null && profile.email !== '' ? String(profile.email) : '',
        role: profile.role != null ? String(profile.role) : '',
        password: password.value,
      })
      const redir = route.query.redirect
      if (typeof redir === 'string' && redir.startsWith('/') && !redir.startsWith('//')) {
        router.push(redir)
      } else {
        router.push({ name: 'home' })
      }
      return
    }
    const text = await res.text()
    message.value = formatErrorBody(res.status, text)
  } catch {
    message.value =
      'Cannot reach the server. Check that the backend is on port 8080 and that you run npm run dev (or npm run preview) so /api is proxied.'
  }
}

function goRegister() {
  router.push({ name: 'register' })
}

function goReset() {
  router.push({ name: 'reset-password' })
}
</script>

<template>
  <div class="auth-form-wrap">
    <form class="auth-form-panel" @submit.prevent="onSubmit">
      <h1 class="auth-form-title">Log in</h1>
      <input
        v-model="email"
        type="email"
        autocomplete="email"
        class="auth-form-input"
        placeholder="Email Address"
        aria-label="Email Address"
      />
      <input
        v-model="password"
        type="password"
        autocomplete="current-password"
        class="auth-form-input"
        placeholder="Password"
        aria-label="Password"
      />
      <p v-if="message" class="auth-form-msg auth-form-msg--err">{{ message }}</p>
      <button type="button" class="auth-form-link" @click="goReset">Forgot the password?</button>
      <button type="button" class="auth-form-link" @click="goRegister">Don't have an account?</button>
      <button type="submit" class="auth-form-submit">Log in</button>
    </form>
  </div>
</template>
