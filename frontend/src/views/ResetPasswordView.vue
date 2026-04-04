<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { formatErrorBody } from '../utils/apiError'

const router = useRouter()

const email = ref('')
const newPassword = ref('')
const confirmPassword = ref('')
const done = ref(false)
const message = ref('')
const successText = ref('')

async function onSubmit() {
  message.value = ''
  if (newPassword.value !== confirmPassword.value) {
    message.value = '两次输入的新密码不一致'
    return
  }
  try {
    const res = await fetch('/api/auth/reset-password', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        email: email.value,
        newPassword: newPassword.value,
        confirmPassword: confirmPassword.value,
      }),
    })
    const text = await res.text()
    if (res.ok) {
      successText.value = text.trim() || '密码已更新，请使用新密码登录。'
      done.value = true
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
    <form v-if="!done" class="auth-form-panel" @submit.prevent="onSubmit">
      <h1 class="auth-form-title">Reset Password</h1>
      <input
        v-model="email"
        type="text"
        autocomplete="email"
        class="auth-form-input"
        placeholder="Email Address"
        aria-label="Email Address"
      />
      <input
        v-model="newPassword"
        type="password"
        autocomplete="new-password"
        class="auth-form-input"
        placeholder="New password"
        aria-label="New password"
      />
      <input
        v-model="confirmPassword"
        type="password"
        autocomplete="new-password"
        class="auth-form-input"
        placeholder="Confirm password"
        aria-label="Confirm password"
      />
      <p v-if="message" class="auth-form-msg auth-form-msg--err">{{ message }}</p>
      <button type="button" class="auth-form-link" @click="goLogin">Back to Log in</button>
      <button type="submit" class="auth-form-submit">Reset password</button>
    </form>
    <div v-else class="auth-form-panel">
      <h1 class="auth-form-title">Reset Password</h1>
      <p class="auth-form-msg auth-form-msg--info">{{ successText }}</p>
      <button type="button" class="auth-form-submit" @click="goLogin">Log in</button>
    </div>
  </div>
</template>
