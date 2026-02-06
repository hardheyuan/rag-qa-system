import { describe, expect, it } from 'vitest'
import { isJwtExpiringSoon, parseJwtExpirationMs } from './jwt'

function createJwtWithExp(expSeconds) {
  const header = Buffer.from(JSON.stringify({ alg: 'HS256', typ: 'JWT' })).toString('base64url')
  const payload = Buffer.from(JSON.stringify({ exp: expSeconds })).toString('base64url')
  return `${header}.${payload}.signature`
}

describe('jwt helpers', () => {
  it('parses expiration timestamp from jwt payload', () => {
    const token = createJwtWithExp(1893456000)
    expect(parseJwtExpirationMs(token)).toBe(1893456000 * 1000)
  })

  it('returns null for invalid jwt', () => {
    expect(parseJwtExpirationMs('not-a-jwt')).toBeNull()
    expect(parseJwtExpirationMs('a.b.c')).toBeNull()
  })

  it('detects token expiring soon', () => {
    const now = Date.now()
    const token = createJwtWithExp(Math.floor((now + 30_000) / 1000))
    expect(isJwtExpiringSoon(token, 60_000, now)).toBe(true)
  })

  it('detects token still valid beyond buffer', () => {
    const now = Date.now()
    const token = createJwtWithExp(Math.floor((now + 5 * 60_000) / 1000))
    expect(isJwtExpiringSoon(token, 60_000, now)).toBe(false)
  })
})
