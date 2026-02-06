function decodeBase64Url(value) {
  if (!value) return null

  const normalized = value.replace(/-/g, '+').replace(/_/g, '/')
  const padded = normalized + '='.repeat((4 - normalized.length % 4) % 4)

  try {
    return atob(padded)
  } catch {
    return null
  }
}

export function parseJwtExpirationMs(token) {
  if (!token || typeof token !== 'string') return null

  const parts = token.split('.')
  if (parts.length < 2) return null

  const payload = decodeBase64Url(parts[1])
  if (!payload) return null

  try {
    const parsed = JSON.parse(payload)
    if (typeof parsed.exp !== 'number') return null
    return parsed.exp * 1000
  } catch {
    return null
  }
}

export function isJwtExpiringSoon(token, bufferMs = 60_000, nowMs = Date.now()) {
  const expirationMs = parseJwtExpirationMs(token)
  if (!expirationMs) return true
  return expirationMs <= nowMs + bufferMs
}
