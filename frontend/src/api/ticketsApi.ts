import { http } from './http'

export type TicketStatus = 'OPEN' | 'IN_PROGRESS' | 'RESOLVED' | 'CLOSED'

export type TicketPriority = 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT'

export type Ticket = {
  id: number
  title: string
  description: string
  status: TicketStatus
  priority: TicketPriority
  createdAt: string
  updatedAt: string
  createdBy: string
  assignedTo: string | null
}

export type TicketComment = {
  id: number
  ticketId: number
  authorId: number
  authorUsername: string
  content: string
  createdAt: string
}

export type TicketFilters = {
  status?: TicketStatus
  priority?: TicketPriority
}

export type CreateTicketRequest = {
  title: string
  description: string
  priority: TicketPriority
  status?: TicketStatus
  assignedTo?: string
}

export type CreateTicketCommentRequest = {
  content: string
}

type PageResponse<T> = {
  content: T[]
  totalElements: number
  totalPages: number
  number: number
  size: number
}

export async function getTickets(filters: TicketFilters = {}) {
  const response = await http.get<PageResponse<Ticket>>('/tickets', {
    params: filters,
  })

  return response.data
}

export async function getTicket(id: number) {
  const response = await http.get<Ticket>(`/tickets/${id}`)

  return response.data
}

export async function createTicket(request: CreateTicketRequest) {
  const response = await http.post<Ticket>('/tickets', request)

  return response.data
}

export async function updateTicketStatus(id: number, status: TicketStatus) {
  const response = await http.patch<Ticket>(`/tickets/${id}/status`, { status })

  return response.data
}

export async function updateTicketPriority(
  id: number,
  priority: TicketPriority,
) {
  const response = await http.patch<Ticket>(`/tickets/${id}/priority`, {
    priority,
  })

  return response.data
}

export async function updateTicketAssignee(id: number, assignedTo: string) {
  const response = await http.patch<Ticket>(`/tickets/${id}/assignee`, {
    assignedTo,
  })

  return response.data
}

export async function getTicketComments(ticketId: number) {
  const response = await http.get<TicketComment[]>(
    `/tickets/${ticketId}/comments`,
  )

  return response.data
}

export async function createTicketComment(
  ticketId: number,
  request: CreateTicketCommentRequest,
) {
  const response = await http.post<TicketComment>(
    `/tickets/${ticketId}/comments`,
    request,
  )

  return response.data
}
