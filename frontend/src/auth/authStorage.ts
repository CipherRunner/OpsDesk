const TOKEN_KEY = 'opsdesk_token'
const ROLE_KEY = 'opsdesk_user_role'

export type CurrentUserRole = 'ADMIN' | 'AGENT' | 'REQUESTER'

type SessionListener = () => void

const sessionClearedListeners = new Set<SessionListener>()

export function getToken(): string | null {
  return localStorage.getItem(TOKEN_KEY)
}

export function setToken(token: string) {
  localStorage.setItem(TOKEN_KEY, token)
}

/**
 * Forgets the current session and tells subscribers (see `onSessionCleared`) so they can react,
 * for example by leaving a protected page. Called on explicit logout and when the API answers 401.
 */
export function clearSession() {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(ROLE_KEY)
  sessionClearedListeners.forEach((listener) => listener())
}

export function onSessionCleared(listener: SessionListener) {
  sessionClearedListeners.add(listener)

  return () => {
    sessionClearedListeners.delete(listener)
  }
}

export function getCurrentUserRole(): CurrentUserRole | null {
  const role = localStorage.getItem(ROLE_KEY)

  if (role === 'ADMIN' || role === 'AGENT' || role === 'REQUESTER') {
    return role
  }

  return null
}

export function setCurrentUserRole(role: CurrentUserRole) {
  localStorage.setItem(ROLE_KEY, role)
}

export function canEditTickets() {
  const role = getCurrentUserRole()

  return role === 'ADMIN' || role === 'AGENT'
}

export function isAuthenticated() {
  return Boolean(getToken())
}
