import type { TicketStatus } from '../../api/ticketsApi'

const statusLabels: Record<TicketStatus, string> = {
  OPEN: 'Open',
  IN_PROGRESS: 'In progress',
  RESOLVED: 'Resolved',
  CLOSED: 'Closed',
}

export function TicketStatusBadge({ status }: { status: TicketStatus }) {
  return (
    <span className={`badge status-${status.toLowerCase().replace('_', '-')}`}>
      {statusLabels[status]}
    </span>
  )
}
