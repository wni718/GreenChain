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
    message.value = 'The two new passwords do not match.'
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
      successText.value = text.trim() || 'Your password has been updated. Please sign in with the new password.'
      done.value = true
      return
    }
    message.value = formatErrorBody(res.status, text)
  } catch {
    message.value =
      'Cannot reach the server. Check that the backend is on port 8080 and that you run npm run dev (or npm run preview) so /api is proxied.'
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
