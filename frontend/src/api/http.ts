import axios, { AxiosHeaders } from 'axios'
import { getToken } from '../auth/authStorage'

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
