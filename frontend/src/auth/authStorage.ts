const TOKEN_KEY = 'opsdesk_token'
const ROLE_KEY = 'opsdesk_user_role'

export type CurrentUserRole = 'ADMIN' | 'AGENT' | 'REQUESTER'

export function getToken(): string | null {
  return localStorage.getItem(TOKEN_KEY)
}

export function setToken(token: string) {
  localStorage.setItem(TOKEN_KEY, token)
}

export function removeToken() {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(ROLE_KEY)
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
