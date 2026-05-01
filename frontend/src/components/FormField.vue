<script setup>
import { computed } from 'vue'

const props = defineProps({
  /** 字段名称（用于 label 和 aria） */
  name: {
    type: String,
    required: true,
  },
  /** 标签文本 */
  label: {
    type: String,
    default: '',
  },
  /** 错误信息 */
  error: {
    type: String,
    default: '',
  },
  /** 是否正在异步验证 */
  validating: {
    type: Boolean,
    default: false,
  },
  /** 是否必填（显示星号） */
  required: {
    type: Boolean,
    default: false,
  },
  /** 帮助文本 */
  hint: {
    type: String,
    default: '',
  },
  /** 输入类型: text, email, password, number, tel, url, date */
  type: {
    type: String,
    default: 'text',
  },
  /** 占位符文本 */
  placeholder: {
    type: String,
    default: '',
  },
  /** 是否禁用 */
  disabled: {
    type: Boolean,
    default: false,
  },
  /** 是否显示成功状态 */
  showSuccess: {
    type: Boolean,
    default: false,
  },
})

defineEmits(['update:modelValue', 'blur', 'focus'])

const modelValue = defineModel({ type: [String, Number] })

const hasError = computed(() => Boolean(props.error && props.error.length > 0))
const hasSuccess = computed(
  () => props.showSuccess && !hasError.value && Boolean(modelValue.value),
)
</script>

<template>
  <div class="form-field" :class="{ 'form-field--error': hasError, 'form-field--success': hasSuccess }">
    <label v-if="label || required" :for="`field-${name}`" class="form-field__label">
      <span v-if="label" class="form-field__label-text">{{ label }}</span>
      <span v-if="required" class="form-field__required" aria-label="required">*</span>
    </label>

    <div class="form-field__input-wrapper">
      <slot :hasError="hasError" :hasSuccess="hasSuccess">
        <input
          :id="`field-${name}`"
          v-model="modelValue"
          :type="type"
          :placeholder="placeholder"
          :disabled="disabled"
          :aria-invalid="hasError"
          :aria-describedby="hasError || hint ? `field-${name}-desc` : undefined"
          class="form-field__input"
          :class="{
            'form-field__input--error': hasError,
            'form-field__input--success': hasSuccess,
            'form-field__input--loading': validating,
          }"
          @input="$emit('update:modelValue', $event.target.value)"
          @blur="$emit('blur', $event)"
          @focus="$emit('focus', $event)"
        />
      </slot>

      <!-- 加载状态 -->
      <span v-if="validating" class="form-field__icon form-field__icon--loading" aria-hidden="true">
        <svg class="spin" width="16" height="16" viewBox="0 0 24 24" fill="none">
          <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="3" opacity="0.25" />
          <path
            d="M12 2a10 10 0 0 1 10 10"
            stroke="currentColor"
            stroke-width="3"
            stroke-linecap="round"
          />
        </svg>
      </span>

      <!-- 成功状态 -->
      <span v-else-if="hasSuccess" class="form-field__icon form-field__icon--success" aria-hidden="true">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
          <path
            d="M20 6L9 17l-5-5"
            stroke="currentColor"
            stroke-width="2.5"
            stroke-linecap="round"
            stroke-linejoin="round"
          />
        </svg>
      </span>

      <!-- 错误状态 -->
      <span v-else-if="hasError" class="form-field__icon form-field__icon--error" aria-hidden="true">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
          <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2" />
          <path d="M12 8v4" stroke="currentColor" stroke-width="2" stroke-linecap="round" />
          <circle cx="12" cy="16" r="1" fill="currentColor" />
        </svg>
      </span>
    </div>

    <!-- 错误/帮助文本 -->
    <p
      v-if="hasError"
      :id="`field-${name}-desc`"
      class="form-field__message form-field__message--error"
      role="alert"
    >
      {{ error }}
    </p>
    <p v-else-if="hint" :id="`field-${name}-desc`" class="form-field__message form-field__message--hint">
      {{ hint }}
    </p>
  </div>
</template>

<style scoped>
.form-field {
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
}

.form-field__label {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  font-size: 0.85rem;
  font-weight: 600;
  color: #4a5c4a;
  cursor: pointer;
}

.form-field__label-text {
  color: inherit;
}

.form-field__required {
  color: #dc2626;
  font-weight: 700;
  font-size: 0.9em;
}

.form-field__input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.form-field__input {
  width: 100%;
  padding: 0.5rem 0.6rem;
  padding-right: 2.25rem;
  border: 1.5px solid #b5c4b5;
  border-radius: 6px;
  font-size: 0.95rem;
  font-family: inherit;
  color: #1a2e1a;
  background: #fff;
  transition: border-color 0.15s ease, box-shadow 0.15s ease, background-color 0.15s ease;
  outline: none;
}

.form-field__input::placeholder {
  color: #9ca89c;
}

.form-field__input:hover:not(:disabled) {
  border-color: #8aa88a;
}

.form-field__input:focus {
  border-color: #528951;
  box-shadow: 0 0 0 3px rgba(82, 137, 81, 0.15);
}

.form-field__input:disabled {
  background: #f5f8f5;
  color: #8a9a8a;
  cursor: not-allowed;
}

/* Error state */
.form-field__input--error {
  border-color: #dc2626;
  background: #fef2f2;
}

.form-field__input--error:hover:not(:disabled) {
  border-color: #b91c1c;
}

.form-field__input--error:focus {
  border-color: #dc2626;
  box-shadow: 0 0 0 3px rgba(220, 38, 38, 0.12);
}

/* Success state */
.form-field__input--success {
  border-color: #16a34a;
  background: #f0fdf4;
}

.form-field__input--success:focus {
  border-color: #16a34a;
  box-shadow: 0 0 0 3px rgba(22, 163, 74, 0.12);
}

/* Loading state */
.form-field__input--loading {
  padding-right: 2.25rem;
}

.form-field__icon {
  position: absolute;
  right: 0.6rem;
  display: flex;
  align-items: center;
  justify-content: center;
  pointer-events: none;
}

.form-field__icon--success {
  color: #16a34a;
}

.form-field__icon--error {
  color: #dc2626;
}

.form-field__icon--loading {
  color: #528951;
}

/* Spin animation for loading */
.spin {
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

/* Messages */
.form-field__message {
  margin: 0;
  font-size: 0.8rem;
  line-height: 1.4;
}

.form-field__message--error {
  color: #dc2626;
  display: flex;
  align-items: center;
  gap: 0.25rem;
}

.form-field__message--hint {
  color: #6b7d6b;
}

/* Shake animation on error */
.form-field--error .form-field__input {
  animation: shake 0.4s ease-in-out;
}

@keyframes shake {
  0%,
  100% {
    transform: translateX(0);
  }
  20%,
  60% {
    transform: translateX(-4px);
  }
  40%,
  80% {
    transform: translateX(4px);
  }
}
</style>
