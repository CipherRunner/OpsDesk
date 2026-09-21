const TOKEN_KEY = 'opsdesk_token'

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
 * for example by dropping the signed-in user. Called on explicit logout and when the API answers
 * 401.
 */
export function clearSession() {
  localStorage.removeItem(TOKEN_KEY)
  sessionClearedListeners.forEach((listener) => listener())
}

export function onSessionCleared(listener: SessionListener) {
  sessionClearedListeners.add(listener)

  return () => {
    sessionClearedListeners.delete(listener)
  }
}
