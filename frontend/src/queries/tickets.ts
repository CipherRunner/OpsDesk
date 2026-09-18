import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {
  createTicket,
  createTicketComment,
  getTicket,
  getTicketAudit,
  getTicketComments,
  getTickets,
  updateTicketAssignee,
  updateTicketPriority,
  updateTicketStatus,
  type CreateTicketCommentRequest,
  type CreateTicketRequest,
  type Ticket,
  type TicketComment,
  type TicketFilters,
  type TicketPriority,
  type TicketStatus,
} from '../api/ticketsApi'

/**
 * Query keys, nested so that invalidating `lists()` refreshes every filtered list and
 * `detail(id)` covers the ticket plus everything under it.
 */
export const ticketKeys = {
  all: ['tickets'] as const,
  lists: () => [...ticketKeys.all, 'list'] as const,
  list: (filters: TicketFilters) => [...ticketKeys.lists(), filters] as const,
  detail: (id: number) => [...ticketKeys.all, 'detail', id] as const,
  comments: (id: number) => [...ticketKeys.detail(id), 'comments'] as const,
  audit: (id: number) => [...ticketKeys.detail(id), 'audit'] as const,
}

export function useTickets(filters: TicketFilters) {
  return useQuery({
    queryKey: ticketKeys.list(filters),
    queryFn: () => getTickets(filters),
  })
}

export function useTicket(id: number | null) {
  return useQuery({
    queryKey: ticketKeys.detail(id ?? 0),
    queryFn: () => getTicket(id as number),
    enabled: id !== null,
  })
}

export function useTicketComments(id: number | null) {
  return useQuery({
    queryKey: ticketKeys.comments(id ?? 0),
    queryFn: () => getTicketComments(id as number),
    enabled: id !== null,
  })
}

export function useTicketAudit(id: number | null) {
  return useQuery({
    queryKey: ticketKeys.audit(id ?? 0),
    queryFn: () => getTicketAudit(id as number),
    enabled: id !== null,
  })
}

export function useCreateTicket() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (request: CreateTicketRequest) => createTicket(request),
    onSuccess: (ticket) => {
      queryClient.setQueryData(ticketKeys.detail(ticket.id), ticket)
      void queryClient.invalidateQueries({ queryKey: ticketKeys.lists() })
    },
  })
}

/**
 * The three field-level updates on a ticket. Each puts the returned ticket straight into the
 * detail cache and marks lists stale, so the page never shows a value the server rejected.
 */
export function useUpdateTicket(id: number) {
  const queryClient = useQueryClient()

  function onSuccess(ticket: Ticket) {
    queryClient.setQueryData(ticketKeys.detail(id), ticket)
    void queryClient.invalidateQueries({ queryKey: ticketKeys.lists() })
    void queryClient.invalidateQueries({ queryKey: ticketKeys.audit(id) })
  }

  const status = useMutation({
    mutationFn: (status: TicketStatus) => updateTicketStatus(id, status),
    onSuccess,
  })
  const priority = useMutation({
    mutationFn: (priority: TicketPriority) => updateTicketPriority(id, priority),
    onSuccess,
  })
  const assignee = useMutation({
    mutationFn: (assignedTo: string) => updateTicketAssignee(id, assignedTo),
    onSuccess,
  })

  return { status, priority, assignee }
}

export function useAddComment(id: number) {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (request: CreateTicketCommentRequest) => createTicketComment(id, request),
    onSuccess: (comment) => {
      queryClient.setQueryData<TicketComment[]>(ticketKeys.comments(id), (comments = []) => [
        ...comments,
        comment,
      ])
      void queryClient.invalidateQueries({ queryKey: ticketKeys.audit(id) })
    },
  })
}
