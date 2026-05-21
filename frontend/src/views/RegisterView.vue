<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuth } from '../composables/useAuth'
import { useFormValidation, ValidationRules } from '../composables/useFormValidation'
import { useI18n } from '../composables/useI18n'
import { formatErrorBody } from '../utils/apiError'

const router = useRouter()
const { setLoggedIn } = useAuth()
const { t } = useI18n()

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
    ValidationRules.required(t('please-enter-username')),
    ValidationRules.email(t('please-enter-username')),
  ],
  username: [
    ValidationRules.required(t('please-enter-username')),
    ValidationRules.minLength(3, t('username-min-length')),
    ValidationRules.maxLength(50, t('username-min-length')),
    ValidationRules.pattern(/^[a-zA-Z0-9_]+$/, t('username-min-length')),
  ],
  password: [
    ValidationRules.required(t('please-enter-password')),
    ValidationRules.minLength(6, t('password-min-length')),
    ValidationRules.maxLength(100, t('password-min-length')),
  ],
  confirmPassword: [
    ValidationRules.required(t('please-enter-password')),
  ],
})

const role = ref('VIEWER')
const message = ref('')
const isSubmitting = ref(false)

// 监听字段失焦来触发表单验证
function onFieldBlur(field) {
  touchField(field)
}

async function onSubmit() {
  // 标记所有字段为已访问
  touchField('email')
  touchField('username')
  touchField('password')
  touchField('confirmPassword')

  // 验证所有字段
  const isValid = await validateAll()

  // 额外验证：密码匹配
  const matchError = passwordMatch()
  if (matchError) {
    touched.confirmPassword = true
  }

  if (!isValid || matchError) {
    if (matchError && isValid) {
      message.value = matchError
    } else {
      message.value = t('fix-errors')
    }
    return
  }

  message.value = ''
  isSubmitting.value = true

  try {
    const res = await fetch('/api/auth/register', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        username: fields.username,
        email: fields.email,
        password: fields.password,
        role: role.value,
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
        username: user?.username ?? fields.username,
        email: user?.email ?? fields.email,
        role: user?.role ?? role.value,
        password: fields.password,
      })

      router.push({ name: 'home' })
      return
    }

    message.value = formatErrorBody(res.status, text)
  } catch {
    message.value = t('server-error')
  } finally {
    isSubmitting.value = false
  }
}

// 密码匹配验证
function passwordMatch() {
  if (fields.confirmPassword && fields.password !== fields.confirmPassword) {
    return t('passwords-mismatch')
  }
  return ''
}

function goLogin() {
  resetValidation()
  message.value = ''
  router.push({ name: 'login' })
}
</script>

<template>
  <div class="auth-form-wrap">
    <form class="auth-form-panel" @submit.prevent="onSubmit" novalidate>
      <h1 class="auth-form-title">{{ t('register') }}</h1>

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
          :placeholder="t('email')"
          :aria-label="t('email')"
          @blur="onFieldBlur('email')"
        />
        <p v-if="touched.email && errors.email" class="field-error">{{ errors.email }}</p>
      </div>

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
          :placeholder="t('username')"
          :aria-label="t('username')"
          @blur="onFieldBlur('username')"
        />
        <p v-if="touched.username && errors.username" class="field-error">{{ errors.username }}</p>
      </div>

      <!-- Password Field -->
      <div class="field-wrapper" :class="{ 'field-wrapper--error': touched.password && errors.password }">
        <input
          v-model="fields.password"
          type="password"
          autocomplete="new-password"
          class="auth-form-input"
          :class="{
            'input--error': touched.password && errors.password,
            'input--success': touched.password && !errors.password && fields.password
          }"
          :placeholder="t('password')"
          :aria-label="t('password')"
          @blur="onFieldBlur('password')"
        />
        <p v-if="touched.password && errors.password" class="field-error">{{ errors.password }}</p>
      </div>

      <!-- Confirm Password Field -->
      <div class="field-wrapper" :class="{ 'field-wrapper--error': touched.confirmPassword && (errors.confirmPassword || (fields.confirmPassword && fields.password !== fields.confirmPassword)) }">
        <input
          v-model="fields.confirmPassword"
          type="password"
          autocomplete="new-password"
          class="auth-form-input"
          :class="{
            'input--error': touched.confirmPassword && (errors.confirmPassword || (fields.confirmPassword && fields.password !== fields.confirmPassword)),
            'input--success': touched.confirmPassword && !errors.confirmPassword && fields.confirmPassword && fields.password === fields.confirmPassword
          }"
          :placeholder="t('confirm-password')"
          :aria-label="t('confirm-password')"
          @blur="onFieldBlur('confirmPassword')"
        />
        <p v-if="touched.confirmPassword && (errors.confirmPassword || (fields.confirmPassword && fields.password !== fields.confirmPassword))" class="field-error">
          {{ fields.password !== fields.confirmPassword ? t('passwords-mismatch') : errors.confirmPassword }}
        </p>
      </div>

      <!-- Role Selection -->
      <select v-model="role" class="auth-form-input" :aria-label="t('role')">
        <option value="VIEWER">Viewer</option>
        <option value="SUSTAINABILITY_MANAGER">Sustainability manager</option>
        <option value="SUPPLIER">Supplier</option>
      </select>

      <p v-if="message" class="auth-form-msg auth-form-msg--err">{{ message }}</p>

      <button type="button" class="auth-form-link" @click="goLogin">{{ t('back-to-login') }}</button>
      <button type="submit" class="auth-form-submit" :disabled="isSubmitting">
        {{ isSubmitting ? t('signing-in') : t('register') }}
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
