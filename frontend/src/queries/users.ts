import { useQuery } from '@tanstack/react-query'
import { getAssignableUsers } from '../api/usersApi'

export const userKeys = {
  assignable: ['users', 'assignable'] as const,
}

export function useAssignableUsers(enabled: boolean) {
  return useQuery({
    queryKey: userKeys.assignable,
    queryFn: getAssignableUsers,
    enabled,
    staleTime: 5 * 60 * 1000,
  })
}
