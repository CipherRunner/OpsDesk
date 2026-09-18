import type { TicketPriority } from '../../api/ticketsApi'
import { formatEnumLabel } from '../../domain/ticket'

export function PriorityBadge({ priority }: { priority: TicketPriority }) {
  return (
    <span className={`badge priority-${priority.toLowerCase()}`}>
      {formatEnumLabel(priority)}
    </span>
  )
}
