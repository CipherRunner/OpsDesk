import { AxiosError, type InternalAxiosRequestConfig } from 'axios'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { http } from '../api/http'
import { onSessionCleared, setToken } from '../auth/authStorage'

function failWith(status: number) {
  http.defaults.adapter = async (config: InternalAxiosRequestConfig) => {
    const response = {
      config,
      data: { message: 'nope' },
      headers: {},
      status,
      statusText: String(status),
    }

    throw new AxiosError('Request failed', 'ERR_BAD_REQUEST', config, null, response)
  }
}

describe('http client', () => {
  beforeEach(() => {
    localStorage.clear()
    setToken('stale-token')
  })

  it('clears the session and notifies listeners on 401', async () => {
    const listener = vi.fn()
    const unsubscribe = onSessionCleared(listener)
    failWith(401)

    await expect(http.get('/tickets')).rejects.toBeInstanceOf(AxiosError)

    expect(localStorage.getItem('opsdesk_token')).toBeNull()
    expect(listener).toHaveBeenCalledTimes(1)
    unsubscribe()
  })

  it('keeps the session when the login call itself is rejected', async () => {
    const listener = vi.fn()
    const unsubscribe = onSessionCleared(listener)
    failWith(401)

    await expect(http.post('/auth/login', {})).rejects.toBeInstanceOf(AxiosError)

    expect(localStorage.getItem('opsdesk_token')).toBe('stale-token')
    expect(listener).not.toHaveBeenCalled()
    unsubscribe()
  })

  it('keeps the session on other errors', async () => {
    failWith(500)

    await expect(http.get('/tickets')).rejects.toBeInstanceOf(AxiosError)

    expect(localStorage.getItem('opsdesk_token')).toBe('stale-token')
  })

  it('sends the stored token as a bearer header', async () => {
    let seenAuthorization: string | undefined
    http.defaults.adapter = async (config: InternalAxiosRequestConfig) => {
      seenAuthorization = config.headers.get('Authorization') as string | undefined

      return { config, data: [], headers: {}, status: 200, statusText: 'OK' }
    }

    await http.get('/tickets')

    expect(seenAuthorization).toBe('Bearer stale-token')
  })
})
