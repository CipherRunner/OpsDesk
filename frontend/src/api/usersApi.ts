import { http } from './http'

export type UserRole = 'ADMIN' | 'AGENT' | 'REQUESTER'

export type User = {
  id: number
  username: string
  role: UserRole
  createdAt: string
  updatedAt: string
}

export async function getUsers() {
  const response = await http.get<User[]>('/users')

  return response.data
}
