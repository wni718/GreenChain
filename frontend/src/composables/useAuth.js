import { ref } from 'vue'

const STORAGE_KEY = 'greenchain_user'

function normalizeStored(raw) {
  if (!raw || typeof raw !== 'object') return null
  return {
    username: raw.username ?? '',
    email: raw.email ?? '',
    role: raw.role ?? '',
    /** Used for HTTP Basic on protected APIs when enabled; cleared on logout */
    password: raw.password != null ? String(raw.password) : '',
  }
}

function readStored() {
  try {
    const raw = sessionStorage.getItem(STORAGE_KEY)
    if (!raw) return null
    return normalizeStored(JSON.parse(raw))
  } catch {
    return null
  }
}

const currentUser = ref(readStored())

export function useAuth() {
  /**
   * @param {{ username: string, email?: string, role?: string, password?: string }} user
   */
  function setLoggedIn(user) {
    const prev = readStored()
    const payload = {
      username: user.username ?? '',
      email: user.email ?? '',
      role: user.role ?? prev?.role ?? '',
      password:
        user.password !== undefined ? String(user.password ?? '') : (prev?.password ?? ''),
    }
    currentUser.value = payload
    sessionStorage.setItem(STORAGE_KEY, JSON.stringify(payload))
  }

  function logout() {
    currentUser.value = null
    sessionStorage.removeItem(STORAGE_KEY)
  }

  /** @returns {Record<string, string>} */
  function apiAuthHeader() {
    const u = currentUser.value
    if (!u?.username || !u.password) return {}
    try {
      const token = btoa(`${u.username}:${u.password}`)
      return { Authorization: `Basic ${token}` }
    } catch {
      return {}
    }
  }

  return {
    currentUser,
    setLoggedIn,
    logout,
    apiAuthHeader,
  }
}
