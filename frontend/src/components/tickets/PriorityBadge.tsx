import type { TicketPriority } from '../../api/ticketsApi'

const priorityLabels: Record<TicketPriority, string> = {
  LOW: 'Low',
  MEDIUM: 'Medium',
  HIGH: 'High',
  URGENT: 'Urgent',
}

export function PriorityBadge({ priority }: { priority: TicketPriority }) {
  return (
    <span className={`badge priority-${priority.toLowerCase()}`}>
      {priorityLabels[priority]}
    </span>
  )
}
