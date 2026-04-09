/**
 * Turn fetch response bodies into readable error messages (Spring Boot often returns JSON or plain text).
 */
export function formatErrorBody(status, text) {
  const trimmed = (text || '').trim()
  if (!trimmed) {
    if (status === 401 || status === 403) {
      return (
        'Forbidden or not signed in (HTTP ' +
        status +
        '). Confirm the API is reachable and you are using npm run dev so /api is proxied.'
      )
    }
    if (status === 404) {
      return 'Not found (HTTP 404). Use npm run dev / npm run preview (with proxy) and ensure the backend is running.'
    }
    if (status === 502 || status === 503) {
      return (
        'Cannot reach the backend (HTTP ' +
        status +
        '). Check: run GreenChainBackendApplication (Tomcat on port 8080); open http://127.0.0.1:8080/test; run npm run dev in frontend.'
      )
    }
    return status ? 'Request failed (HTTP ' + status + ')' : 'Request failed'
  }
  try {
    const j = JSON.parse(trimmed)
    if (typeof j.message === 'string' && j.message) return j.message
    if (typeof j.error === 'string' && j.error) return j.error
  } catch {
    /* plain text */
  }
  return trimmed.length > 280 ? trimmed.slice(0, 280) + '…' : trimmed
}
