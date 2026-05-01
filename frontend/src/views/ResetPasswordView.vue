<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useFormValidation, ValidationRules } from '../composables/useFormValidation'
import { formatErrorBody } from '../utils/apiError'

const router = useRouter()

// 表单验证
const {
  fields,
  errors,
  touched,
  validateAll,
  touchField,
  resetValidation,
} = useFormValidation({
  email: [
    ValidationRules.required('Please enter your email address'),
    ValidationRules.email('Please enter a valid email address'),
  ],
  newPassword: [
    ValidationRules.required('Please enter a new password'),
    ValidationRules.minLength(6, 'Password must be at least 6 characters'),
  ],
  confirmPassword: [
    ValidationRules.required('Please confirm your password'),
  ],
})

const done = ref(false)
const message = ref('')
const successText = ref('')
const isSubmitting = ref(false)

// 监听字段失焦来触发表单验证
function onFieldBlur(field) {
  touchField(field)
}

// 密码匹配验证
function passwordMatch() {
  if (fields.confirmPassword && fields.newPassword !== fields.confirmPassword) {
    return 'Passwords do not match'
  }
  return ''
}

async function onSubmit() {
  // 标记所有字段为已访问
  touchField('email')
  touchField('newPassword')
  touchField('confirmPassword')

  // 验证所有字段
  const isValid = await validateAll()

  // 额外验证：密码匹配
  const matchError = passwordMatch()
  if (matchError) {
    touched.confirmPassword = true
  }

  if (!isValid || matchError) {
    message.value = 'Please fix the errors above before submitting.'
    return
  }

  message.value = ''
  isSubmitting.value = true

  try {
    const res = await fetch('/api/auth/reset-password', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        email: fields.email,
        newPassword: fields.newPassword,
        confirmPassword: fields.confirmPassword,
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
  } finally {
    isSubmitting.value = false
  }
}

function goLogin() {
  resetValidation()
  message.value = ''
  router.push({ name: 'login' })
}
</script>

<template>
  <div class="auth-form-wrap">
    <!-- Success State -->
    <div v-if="done" class="auth-form-panel">
      <h1 class="auth-form-title">Reset Password</h1>
      <p class="auth-form-msg auth-form-msg--info">{{ successText }}</p>
      <button type="button" class="auth-form-submit" @click="goLogin">Log in</button>
    </div>

    <!-- Reset Form -->
    <form v-else class="auth-form-panel" @submit.prevent="onSubmit" novalidate>
      <h1 class="auth-form-title">Reset Password</h1>

      <!-- Email Field -->
      <div class="field-wrapper" :class="{ 'field-wrapper--error': touched.email && errors.email }">
        <input
          v-model="fields.email"
          type="email"
          autocomplete="email"
          class="auth-form-input"
          :class="{
            'input--error': touched.email && errors.email,
            'input--success': touched.email && !errors.email && fields.email
          }"
          placeholder="Email Address"
          aria-label="Email Address"
          @blur="onFieldBlur('email')"
        />
        <p v-if="touched.email && errors.email" class="field-error">{{ errors.email }}</p>
      </div>

      <!-- New Password Field -->
      <div class="field-wrapper" :class="{ 'field-wrapper--error': touched.newPassword && errors.newPassword }">
        <input
          v-model="fields.newPassword"
          type="password"
          autocomplete="new-password"
          class="auth-form-input"
          :class="{
            'input--error': touched.newPassword && errors.newPassword,
            'input--success': touched.newPassword && !errors.newPassword && fields.newPassword
          }"
          placeholder="New password"
          aria-label="New password"
          @blur="onFieldBlur('newPassword')"
        />
        <p v-if="touched.newPassword && errors.newPassword" class="field-error">{{ errors.newPassword }}</p>
      </div>

      <!-- Confirm Password Field -->
      <div class="field-wrapper" :class="{ 'field-wrapper--error': touched.confirmPassword && (errors.confirmPassword || (fields.confirmPassword && fields.newPassword !== fields.confirmPassword)) }">
        <input
          v-model="fields.confirmPassword"
          type="password"
          autocomplete="new-password"
          class="auth-form-input"
          :class="{
            'input--error': touched.confirmPassword && (errors.confirmPassword || (fields.newPassword !== fields.confirmPassword)),
            'input--success': touched.confirmPassword && !errors.confirmPassword && fields.confirmPassword && fields.newPassword === fields.confirmPassword
          }"
          placeholder="Confirm password"
          aria-label="Confirm password"
          @blur="onFieldBlur('confirmPassword')"
        />
        <p v-if="touched.confirmPassword && (errors.confirmPassword || (fields.confirmPassword && fields.newPassword !== fields.confirmPassword))" class="field-error">
          {{ fields.newPassword !== fields.confirmPassword ? 'Passwords do not match' : errors.confirmPassword }}
        </p>
      </div>

      <p v-if="message" class="auth-form-msg auth-form-msg--err">{{ message }}</p>

      <button type="button" class="auth-form-link" @click="goLogin">Back to Log in</button>
      <button type="submit" class="auth-form-submit" :disabled="isSubmitting">
        {{ isSubmitting ? 'Resetting...' : 'Reset password' }}
      </button>
    </form>
  </div>
</template>

<style scoped>
/* 验证状态样式 */
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
