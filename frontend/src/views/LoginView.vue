<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuth } from '../composables/useAuth'
import { formatErrorBody } from '../utils/apiError'

const router = useRouter()
const { setLoggedIn } = useAuth()

const emailOrUser = ref('')
const password = ref('')
const message = ref('')

async function onSubmit() {
  message.value = ''
  try {
    const res = await fetch('/api/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        username: emailOrUser.value,
        password: password.value,
      }),
    })
    if (res.ok) {
      const profile = await res.json()
      setLoggedIn({
        username: profile.username ?? emailOrUser.value,
        email: profile.email != null && profile.email !== '' ? String(profile.email) : '',
      })
      router.push({ name: 'home' })
      return
    }
    const text = await res.text()
    message.value = formatErrorBody(res.status, text)
  } catch {
    message.value =
      '无法连接服务器。请确认：1) 后端已在 8080 启动；2) 使用 npm run dev（或 npm run preview）以便将 /api 代理到后端。'
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
        v-model="emailOrUser"
        type="text"
        autocomplete="username"
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
