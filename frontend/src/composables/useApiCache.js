import { ref } from 'vue'

const CACHE_PREFIX = 'gc_cache_'
const DEFAULT_TTL = 5 * 60 * 1000 // 5 minutes

function getCacheKey(endpoint) {
  return `${CACHE_PREFIX}${endpoint}`
}

function isValid(entry) {
  if (!entry || typeof entry !== 'object') return false
  return Date.now() - entry.timestamp < entry.ttl
}

export function useApiCache() {
  /**
   * Get cached data if still valid.
   * @param {string} endpoint - API endpoint (e.g. '/api/dashboard/summary')
   * @returns {{ data: any, fromCache: boolean } | null}
   */
  function get(endpoint) {
    try {
      const raw = sessionStorage.getItem(getCacheKey(endpoint))
      if (!raw) return null
      const entry = JSON.parse(raw)
      if (!isValid(entry)) {
        sessionStorage.removeItem(getCacheKey(endpoint))
        return null
      }
      return { data: entry.data, fromCache: true }
    } catch {
      return null
    }
  }

  /**
   * Store data in cache.
   * @param {string} endpoint - API endpoint
   * @param {any} data - Data to cache
   * @param {number} [ttl] - Time to live in ms (default 5 min)
   */
  function set(endpoint, data, ttl = DEFAULT_TTL) {
    try {
      const entry = {
        data,
        timestamp: Date.now(),
        ttl,
      }
      sessionStorage.setItem(getCacheKey(endpoint), JSON.stringify(entry))
    } catch {
      // Storage quota exceeded — ignore
    }
  }

  /**
   * Remove a specific cached entry.
   * @param {string} endpoint
   */
  function invalidate(endpoint) {
    sessionStorage.removeItem(getCacheKey(endpoint))
  }

  /**
   * Clear all cached entries.
   */
  function clearAll() {
    const prefix = CACHE_PREFIX
    const keys = Object.keys(sessionStorage).filter((k) => k.startsWith(prefix))
    keys.forEach((k) => sessionStorage.removeItem(k))
  }

  return { get, set, invalidate, clearAll }
}
