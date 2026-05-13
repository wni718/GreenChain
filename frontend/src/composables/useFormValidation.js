import { reactive, computed } from 'vue'

/**
 * 表单验证规则定义
 */
export const ValidationRules = {
  required: (message = 'This field is required') => ({
    validate: (value) => {
      if (value === null || value === undefined) return false
      if (typeof value === 'string') return value.trim().length > 0
      if (typeof value === 'number') return true
      if (Array.isArray(value)) return value.length > 0
      return Boolean(value)
    },
    message,
  }),

  email: (message = 'Please enter a valid email address') => ({
    validate: (value) => {
      if (!value || typeof value !== 'string') return true
      if (!value.trim()) return true
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
      return emailRegex.test(value.trim())
    },
    message,
  }),

  minLength: (min, message) => ({
    validate: (value) => {
      if (!value || typeof value !== 'string') return true
      return value.length >= min
    },
    message: message || `Minimum ${min} characters required`,
  }),

  maxLength: (max, message) => ({
    validate: (value) => {
      if (!value || typeof value !== 'string') return true
      return value.length <= max
    },
    message: message || `Maximum ${max} characters allowed`,
  }),

  pattern: (regex, message) => ({
    validate: (value) => {
      if (!value || typeof value !== 'string') return true
      return regex.test(value)
    },
    message,
  }),

  number: (message = 'Please enter a valid number') => ({
    validate: (value) => {
      if (value === null || value === undefined || value === '') return true
      return !isNaN(Number(value))
    },
    message,
  }),

  positiveNumber: (message = 'Please enter a positive number') => ({
    validate: (value) => {
      if (value === null || value === undefined || value === '') return true
      const num = Number(value)
      return !isNaN(num) && num > 0
    },
    message,
  }),

  nonNegativeNumber: (message = 'Please enter a non-negative number') => ({
    validate: (value) => {
      if (value === null || value === undefined || value === '') return true
      const num = Number(value)
      return !isNaN(num) && num >= 0
    },
    message,
  }),

  phone: (message = 'Please enter a valid phone number') => ({
    validate: (value) => {
      if (!value || typeof value !== 'string') return true
      if (!value.trim()) return true
      const phoneRegex = /^[+]?[(]?[0-9]{1,4}[)]?[-\s./0-9]*$/
      return phoneRegex.test(value.trim())
    },
    message,
  }),

  url: (message = 'Please enter a valid URL') => ({
    validate: (value) => {
      if (!value || typeof value !== 'string') return true
      if (!value.trim()) return true
      try {
        new URL(value)
        return true
      } catch {
        return false
      }
    },
    message,
  }),

  date: (message = 'Please enter a valid date') => ({
    validate: (value) => {
      if (!value || typeof value !== 'string') return true
      if (!value.trim()) return true
      const date = new Date(value)
      return !isNaN(date.getTime())
    },
    message,
  }),

  futureDate: (message = 'Date must be in the future') => ({
    validate: (value) => {
      if (!value || typeof value !== 'string') return true
      if (!value.trim()) return true
      const date = new Date(value)
      return !isNaN(date.getTime()) && date >= new Date(new Date().toDateString())
    },
    message,
  }),

  range: (min, max, message) => ({
    validate: (value) => {
      if (value === null || value === undefined || value === '') return true
      const num = Number(value)
      return !isNaN(num) && num >= min && num <= max
    },
    message: message || `Value must be between ${min} and ${max}`,
  }),
}

/**
 * 异步验证器（用于 API 验证等）
 * @param {Function} asyncValidator - 返回 Promise<{ valid: boolean, message?: string }>
 */
export function asyncRule(asyncValidator) {
  return {
    validate: async () => {
      const result = await asyncValidator()
      return result.valid
    },
    message: () => {
      let msg = 'Validation failed'
      asyncValidator().then((r) => {
        if (r.message) msg = r.message
      })
      return msg
    },
    async: true,
  }
}

/**
 * 表单验证 Composable
 * @param {Object} schema - 字段验证规则定义
 * @example
 * const { fields, errors, validate, validateAll, resetValidation } = useFormValidation({
 *   username: [ValidationRules.required(), ValidationRules.minLength(3)],
 *   email: [ValidationRules.required(), ValidationRules.email()],
 * })
 */
export function useFormValidation(schema = {}) {
  const fields = reactive({})
  const errors = reactive({})
  const touched = reactive({})
  const validating = reactive({})

  // 初始化字段
  for (const fieldName in schema) {
    fields[fieldName] = ''
    errors[fieldName] = ''
    touched[fieldName] = false
    validating[fieldName] = false
  }

  /**
   * 设置字段值
   */
  function setFieldValue(fieldName, value) {
    if (fieldName in fields) {
      fields[fieldName] = value
      // 实时验证（仅当字段已被访问过）
      if (touched[fieldName]) {
        validateField(fieldName)
      }
    }
  }

  /**
   * 标记字段已访问
   */
  function touchField(fieldName) {
    if (fieldName in touched) {
      touched[fieldName] = true
      validateField(fieldName)
    }
  }

  /**
   * 验证单个字段
   */
  async function validateField(fieldName) {
    if (!(fieldName in schema)) return true

    const rules = schema[fieldName]
    if (!rules || rules.length === 0) {
      errors[fieldName] = ''
      return true
    }

    const value = fields[fieldName]

    for (const rule of rules) {
      if (rule.async) {
        validating[fieldName] = true
        try {
          const isValid = await rule.validate(value)
          if (!isValid) {
            errors[fieldName] = typeof rule.message === 'function' ? rule.message() : rule.message
            return false
          }
        } finally {
          validating[fieldName] = false
        }
      } else {
        const isValid = rule.validate(value)
        if (!isValid) {
          errors[fieldName] = rule.message
          return false
        }
      }
    }

    errors[fieldName] = ''
    return true
  }

  /**
   * 验证所有字段
   */
  async function validateAll() {
    let isValid = true

    for (const fieldName in schema) {
      touched[fieldName] = true
      const fieldValid = await validateField(fieldName)
      if (!fieldValid) {
        isValid = false
      }
    }

    return isValid
  }

  /**
   * 重置验证状态
   */
  function resetValidation() {
    for (const fieldName in schema) {
      errors[fieldName] = ''
      touched[fieldName] = false
      validating[fieldName] = false
    }
  }

  /**
   * 初始化/重置表单
   */
  function initForm(initialValues = {}) {
    resetValidation()
    for (const fieldName in schema) {
      fields[fieldName] = initialValues[fieldName] ?? ''
    }
  }

  /**
   * 检查表单是否有错误
   */
  const hasErrors = computed(() => {
    return Object.values(errors).some((e) => e && e.length > 0)
  })

  /**
   * 获取第一个错误字段名
   */
  const firstErrorField = computed(() => {
    for (const fieldName in errors) {
      if (errors[fieldName] && errors[fieldName].length > 0) {
        return fieldName
      }
    }
    return null
  })

  return {
    fields,
    errors,
    touched,
    validating,
    hasErrors,
    firstErrorField,
    setFieldValue,
    touchField,
    validateField,
    validateAll,
    resetValidation,
    initForm,
  }
}

/**
 * 快速验证单个值
 * @param {any} value - 要验证的值
 * @param {Array} rules - 验证规则数组
 * @returns {{ valid: boolean, message: string }}
 */
export function quickValidate(value, rules) {
  if (!rules || rules.length === 0) {
    return { valid: true, message: '' }
  }

  for (const rule of rules) {
    if (rule.async) continue
    const isValid = rule.validate(value)
    if (!isValid) {
      return { valid: false, message: rule.message }
    }
  }

  return { valid: true, message: '' }
}
