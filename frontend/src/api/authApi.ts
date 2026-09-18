import { http } from './http'

export type CurrentUserRole = 'ADMIN' | 'AGENT' | 'REQUESTER'

export type LoginRequest = {
  username: string
  password: string
}

export type CurrentUser = {
  id: number
  username: string
  role: CurrentUserRole
}

export type AuthResponse = {
  token: string
  user: CurrentUser
}

export async function login(request: LoginRequest): Promise<AuthResponse> {
  const response = await http.post<AuthResponse>('/auth/login', request)

  return response.data
}

export async function getCurrentUser(): Promise<CurrentUser> {
  const response = await http.get<CurrentUser>('/me')

  return response.data
}
