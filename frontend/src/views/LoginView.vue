<script setup>
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuth } from '../composables/useAuth'
import { useFormValidation, ValidationRules } from '../composables/useFormValidation'
import { formatErrorBody } from '../utils/apiError'

const route = useRoute()
const router = useRouter()
const { setLoggedIn } = useAuth()

// 表单验证
const {
  fields,
  errors,
  touched,
  validateAll,
  touchField,
  resetValidation,
} = useFormValidation({
  username: [
    ValidationRules.required('Please enter your username or email'),
    ValidationRules.minLength(3, 'Username must be at least 3 characters'),
  ],
  password: [
    ValidationRules.required('Please enter your password'),
    ValidationRules.minLength(6, 'Password must be at least 6 characters'),
  ],
})

const message = ref('')
const isSubmitting = ref(false)

// 监听字段失焦来触发表单验证
function onFieldBlur(field) {
  touchField(field)
}

async function onSubmit() {
  // 标记所有字段为已访问
  touchField('username')
  touchField('password')

  // 验证所有字段
  const isValid = await validateAll()
  if (!isValid) {
    message.value = 'Please fix the errors above before submitting.'
    return
  }

  message.value = ''
  isSubmitting.value = true

  try {
    const loginData = {
      username: fields.username,
      password: fields.password,
    }
    if (fields.username.includes('@')) {
      loginData.email = fields.username
    }

    const res = await fetch('/api/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(loginData),
    })

    if (res.ok) {
      const profile = await res.json()
      setLoggedIn({
        username: profile.username ?? fields.username,
        email: profile.email != null && profile.email !== '' ? String(profile.email) : '',
        role: profile.role != null ? String(profile.role) : '',
        password: fields.password,
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
  } finally {
    isSubmitting.value = false
  }
}

function goRegister() {
  resetValidation()
  message.value = ''
  router.push({ name: 'register' })
}

function goReset() {
  resetValidation()
  message.value = ''
  router.push({ name: 'reset-password' })
}
</script>

<template>
  <div class="auth-form-wrap">
    <form class="auth-form-panel" @submit.prevent="onSubmit" novalidate>
      <h1 class="auth-form-title">Log in</h1>

      <!-- Username Field -->
      <div class="field-wrapper" :class="{ 'field-wrapper--error': touched.username && errors.username }">
        <input
          v-model="fields.username"
          type="text"
          autocomplete="username"
          class="auth-form-input"
          :class="{
            'input--error': touched.username && errors.username,
            'input--success': touched.username && !errors.username && fields.username
          }"
          placeholder="Username or Email"
          aria-label="Username or Email"
          @blur="onFieldBlur('username')"
        />
        <p v-if="touched.username && errors.username" class="field-error">{{ errors.username }}</p>
      </div>

      <!-- Password Field -->
      <div class="field-wrapper" :class="{ 'field-wrapper--error': touched.password && errors.password }">
        <input
          v-model="fields.password"
          type="password"
          autocomplete="current-password"
          class="auth-form-input"
          :class="{
            'input--error': touched.password && errors.password,
            'input--success': touched.password && !errors.password && fields.password
          }"
          placeholder="Password"
          aria-label="Password"
          @blur="onFieldBlur('password')"
        />
        <p v-if="touched.password && errors.password" class="field-error">{{ errors.password }}</p>
      </div>

      <p v-if="message" class="auth-form-msg auth-form-msg--err">{{ message }}</p>

      <button type="button" class="auth-form-link" @click="goReset">Forgot the password?</button>
      <button type="button" class="auth-form-link" @click="goRegister">Don't have an account?</button>
      <button type="submit" class="auth-form-submit" :disabled="isSubmitting">
        {{ isSubmitting ? 'Signing in...' : 'Log in' }}
      </button>
    </form>
  </div>
</template>

<style scoped>
/* 验证状态样式 - 在全局样式基础上增强 */
.field-wrapper {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.input--error {
  border: 2px solid #dc2626 !important;
  background: #fef2f2 !important;
}

.input--success {
  border: 2px solid #16a34a !important;
  background: #f0fdf4 !important;
}

.field-wrapper--error .auth-form-input {
  animation: shake 0.4s ease-in-out;
}

@keyframes shake {
  0%, 100% { transform: translateX(0); }
  20%, 60% { transform: translateX(-4px); }
  40%, 80% { transform: translateX(4px); }
}

.field-error {
  margin: 0;
  font-size: 0.8rem;
  color: #dc2626;
  font-weight: 600;
  padding-left: 0.25rem;
}

.auth-form-submit:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}
</style>
