import type { TicketStatus } from '../../api/ticketsApi'
import { formatEnumLabel } from '../../domain/ticket'

export function TicketStatusBadge({ status }: { status: TicketStatus }) {
  return (
    <span className={`badge status-${status.toLowerCase().replace('_', '-')}`}>
      {formatEnumLabel(status)}
    </span>
  )
}
