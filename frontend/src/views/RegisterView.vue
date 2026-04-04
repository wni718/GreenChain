<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuth } from '../composables/useAuth'
import { formatErrorBody } from '../utils/apiError'

const router = useRouter()
const { setLoggedIn } = useAuth()

const email = ref('')
const username = ref('')
const password = ref('')
const message = ref('')

async function onSubmit() {
  message.value = ''
  try {
    const res = await fetch('/api/auth/register', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        username: username.value,
        email: email.value,
        password: password.value,
      }),
    })
    const text = await res.text()
    if (res.ok) {
      let user = null
      try {
        user = text ? JSON.parse(text) : null
      } catch {
        user = null
      }
      setLoggedIn({
        username: user?.username ?? username.value,
        email: user?.email ?? email.value,
      })
      router.push({ name: 'home' })
      return
    }
    message.value = formatErrorBody(res.status, text)
  } catch {
    message.value =
      '无法连接服务器。请确认：1) 后端已在 8080 启动；2) 使用 npm run dev（或 npm run preview）以便将 /api 代理到后端。'
  }
}

function goLogin() {
  router.push({ name: 'login' })
}
</script>

<template>
  <div class="auth-form-wrap">
    <form class="auth-form-panel" @submit.prevent="onSubmit">
      <h1 class="auth-form-title">Register</h1>
      <input
        v-model="email"
        type="text"
        autocomplete="email"
        class="auth-form-input"
        placeholder="Email Address"
        aria-label="Email Address"
      />
      <input
        v-model="username"
        type="text"
        autocomplete="username"
        class="auth-form-input"
        placeholder="Username"
        aria-label="Username"
      />
      <input
        v-model="password"
        type="password"
        autocomplete="new-password"
        class="auth-form-input"
        placeholder="Password"
        aria-label="Password"
      />
      <p v-if="message" class="auth-form-msg auth-form-msg--err">{{ message }}</p>
      <button type="button" class="auth-form-link" @click="goLogin">Back to Log in</button>
      <button type="submit" class="auth-form-submit">Register</button>
    </form>
  </div>
</template>
