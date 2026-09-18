import { useCallback, useEffect, useMemo, useState, type ReactNode } from 'react'
import {
  getCurrentUser,
  login as loginRequest,
  type CurrentUser,
  type LoginRequest,
} from '../api/authApi'
import { AuthContext, type AuthContextValue } from './AuthContext'
import { clearSession, getToken, onSessionCleared, setToken } from './authStorage'

/**
 * Owns the signed-in user. Only the token is persisted; who the token belongs to is asked from
 * the API on startup, so a role change on the server is picked up on the next reload instead of
 * being trusted from browser storage.
 */
export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<CurrentUser | null>(null)
  const [isLoading, setIsLoading] = useState(() => Boolean(getToken()))

  useEffect(() => {
    if (!getToken()) {
      return
    }

    let ignore = false

    getCurrentUser()
      .then((currentUser) => {
        if (!ignore) {
          setUser(currentUser)
        }
      })
      .catch(() => {
        // A 401 has already cleared the session through the http interceptor; any other failure
        // (network, 5xx) also means we cannot trust the token for this page load.
        if (!ignore) {
          clearSession()
        }
      })
      .finally(() => {
        if (!ignore) {
          setIsLoading(false)
        }
      })

    return () => {
      ignore = true
    }
  }, [])

  useEffect(() => onSessionCleared(() => setUser(null)), [])

  const login = useCallback(async (request: LoginRequest) => {
    const response = await loginRequest(request)

    setToken(response.token)
    setUser(response.user)
  }, [])

  const logout = useCallback(() => {
    clearSession()
  }, [])

  const value = useMemo<AuthContextValue>(
    () => ({
      isLoading,
      user,
      canEditTickets: user?.role === 'ADMIN' || user?.role === 'AGENT',
      login,
      logout,
    }),
    [isLoading, login, logout, user],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
