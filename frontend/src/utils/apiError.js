/**
 * 将 fetch 响应体转成可读错误文案（Spring Boot 常为 JSON 或空 body）。
 */
export function formatErrorBody(status, text) {
  const trimmed = (text || '').trim()
  if (!trimmed) {
    if (status === 401 || status === 403) {
      return '无权限或未登录（HTTP ' + status + '）。请确认接口已放行且使用 npm run dev 走代理。'
    }
    if (status === 404) {
      return '接口不存在（HTTP 404）。请使用 npm run dev / npm run preview（已配置代理），并确认后端已启动。'
    }
    if (status === 502 || status === 503) {
      return (
        '无法连接后端（HTTP ' +
        status +
        '）。请依次检查：① 在 IDEA 中运行 GreenChainBackendApplication，控制台出现 Tomcat started on port 8080；② 浏览器单独打开 http://127.0.0.1:8080/test 能显示 GreenChain Backend is running；③ 在 frontend 目录执行 npm run dev（改 vite 配置或 .env 后需关掉再重新执行）。'
      )
    }
    return status ? '请求失败（HTTP ' + status + '）' : '请求失败'
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
