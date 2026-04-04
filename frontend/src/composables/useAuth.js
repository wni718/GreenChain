import { ref } from 'vue'

const STORAGE_KEY = 'greenchain_user'

function normalizeStored(raw) {
  if (!raw || typeof raw !== 'object') return null
  return {
    username: raw.username ?? '',
    email: raw.email ?? '',
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
   * @param {{ username: string, email?: string }} user
   */
  function setLoggedIn(user) {
    const payload = {
      username: user.username ?? '',
      email: user.email ?? '',
    }
    currentUser.value = payload
    sessionStorage.setItem(STORAGE_KEY, JSON.stringify(payload))
  }

  function logout() {
    currentUser.value = null
    sessionStorage.removeItem(STORAGE_KEY)
  }

  return {
    currentUser,
    setLoggedIn,
    logout,
  }
}
