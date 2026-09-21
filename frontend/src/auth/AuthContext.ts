import { createContext } from 'react'
import type { CurrentUser, LoginRequest } from '../api/authApi'

export type AuthContextValue = {
  /** True until the stored token has been checked against the API on startup. */
  isLoading: boolean
  user: CurrentUser | null
  canEditTickets: boolean
  login: (request: LoginRequest) => Promise<void>
  logout: () => void
}

export const AuthContext = createContext<AuthContextValue | null>(null)
