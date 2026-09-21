import axios, { AxiosError, AxiosHeaders, type InternalAxiosRequestConfig } from 'axios'
import { clearSession, getToken } from '../auth/authStorage'

const LOGIN_PATH = '/auth/login'

export const http = axios.create({
  baseURL: '/api',
})

http.interceptors.request.use((config) => {
  const token = getToken()

  if (token) {
    const headers =
      config.headers instanceof AxiosHeaders
        ? config.headers
        : new AxiosHeaders(config.headers)

    headers.set('Authorization', `Bearer ${token}`)
    config.headers = headers
  }

  return config
})

function isLoginRequest(config: InternalAxiosRequestConfig | undefined) {
  return config?.url === LOGIN_PATH
}

/**
 * A 401 on any call except the login itself means the stored token is expired or revoked.
 * Drop it so the app returns to the login page instead of showing errors on every action.
 */
http.interceptors.response.use(
  (response) => response,
  (error: unknown) => {
    if (
      error instanceof AxiosError &&
      error.response?.status === 401 &&
      !isLoginRequest(error.config)
    ) {
      clearSession()
    }

    return Promise.reject(error)
  },
)
