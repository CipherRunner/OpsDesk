import type { TicketPriority, TicketStatus } from '../api/ticketsApi'

export const TICKET_STATUSES: readonly TicketStatus[] = [
  'OPEN',
  'IN_PROGRESS',
  'RESOLVED',
  'CLOSED',
]

export const TICKET_PRIORITIES: readonly TicketPriority[] = [
  'LOW',
  'MEDIUM',
  'HIGH',
  'URGENT',
]

/**
 * Turns an API enum constant into a label: `IN_PROGRESS` becomes `In progress`.
 */
export function formatEnumLabel(value: string): string {
  const words = value.toLowerCase().split('_').join(' ')

  return words.charAt(0).toUpperCase() + words.slice(1)
}
