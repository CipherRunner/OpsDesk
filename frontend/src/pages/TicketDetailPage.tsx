import { Link, useParams } from 'react-router-dom'
import { getApiErrorMessage } from '../api/apiError'
import type { TicketPriority, TicketStatus } from '../api/ticketsApi'
import { useAuth } from '../auth/useAuth'
import { CommentForm } from '../components/comments/CommentForm'
import { CommentList } from '../components/comments/CommentList'
import { AuditList } from '../components/tickets/AuditList'
import { TicketDetail } from '../components/tickets/TicketDetail'
import {
  useAddComment,
  useTicket,
  useTicketAudit,
  useTicketComments,
  useUpdateTicket,
} from '../queries/tickets'
import { useAssignableUsers } from '../queries/users'

function parseTicketId(value: string | undefined): number | null {
  const id = Number(value)

  return Number.isInteger(id) && id > 0 ? id : null
}

export function TicketDetailPage() {
  const { id } = useParams()
  const ticketId = parseTicketId(id)
  const { canEditTickets } = useAuth()

  const ticketQuery = useTicket(ticketId)
  const commentsQuery = useTicketComments(ticketId)
  const auditQuery = useTicketAudit(ticketId)
  const usersQuery = useAssignableUsers(canEditTickets && ticketId !== null)
  const update = useUpdateTicket(ticketId ?? 0)
  const addComment = useAddComment(ticketId ?? 0)

  const ticket = ticketQuery.data
  const isLoading =
    ticketId !== null && (ticketQuery.isPending || commentsQuery.isPending)

  const loadError =
    ticketId === null
      ? 'Invalid ticket id.'
      : ticketQuery.error
        ? getApiErrorMessage(ticketQuery.error, 'Failed to load ticket.')
        : commentsQuery.error
          ? getApiErrorMessage(commentsQuery.error, 'Failed to load comments.')
          : ''

  const actionError =
    update.status.error
      ? getApiErrorMessage(update.status.error, 'Failed to update status.')
      : update.priority.error
        ? getApiErrorMessage(update.priority.error, 'Failed to update priority.')
        : update.assignee.error
          ? getApiErrorMessage(update.assignee.error, 'Failed to update assignee.')
          : ''

  const usersMessage = usersQuery.error
    ? `Assignee list unavailable: ${getApiErrorMessage(usersQuery.error, 'Failed to load users.')}`
    : ''

  function handleStatusChange(status: TicketStatus) {
    if (canEditTickets && ticket && ticket.status !== status) {
      update.status.mutate(status)
    }
  }

  function handlePriorityChange(priority: TicketPriority) {
    if (canEditTickets && ticket && ticket.priority !== priority) {
      update.priority.mutate(priority)
    }
  }

  function handleAssigneeChange(assignedTo: string) {
    if (canEditTickets && ticket && assignedTo && ticket.assignedTo !== assignedTo) {
      update.assignee.mutate(assignedTo)
    }
  }

  async function handleAddComment(content: string) {
    try {
      await addComment.mutateAsync({ content })
      return true
    } catch {
      return false
    }
  }

  return (
    <section className="page">
      <div className="page-header page-header-row">
        <div>
          <p className="eyebrow">Ticket</p>
          <h1>{ticket ? ticket.title : `Ticket ${id ?? ''}`}</h1>
        </div>

        <Link className="text-link" to="/tickets">
          Back to tickets
        </Link>
      </div>

      {isLoading ? <div className="panel loading-panel">Loading ticket...</div> : null}

      {loadError ? <p className="form-error">{loadError}</p> : null}

      {!isLoading && ticket ? (
        <>
          {actionError ? <p className="form-error">{actionError}</p> : null}

          <TicketDetail
            assigneeOptions={usersQuery.data ?? []}
            canEditTicketActions={canEditTickets}
            isUpdatingAssignee={update.assignee.isPending}
            isUpdatingPriority={update.priority.isPending}
            isUpdatingStatus={update.status.isPending}
            ticket={ticket}
            usersUnavailableMessage={usersMessage}
            onAssigneeChange={handleAssigneeChange}
            onPriorityChange={handlePriorityChange}
            onStatusChange={handleStatusChange}
          />

          <div className="panel comments-panel">
            <div className="panel-heading">
              <h2>Comments</h2>
            </div>

            <CommentList comments={commentsQuery.data ?? []} />
            <CommentForm
              error={
                addComment.error
                  ? getApiErrorMessage(addComment.error, 'Failed to add comment.')
                  : ''
              }
              isSubmitting={addComment.isPending}
              onSubmit={handleAddComment}
            />
          </div>

          <div className="panel comments-panel">
            <div className="panel-heading">
              <h2>History</h2>
            </div>

            {auditQuery.error ? (
              <p className="form-error">
                {getApiErrorMessage(auditQuery.error, 'Failed to load history.')}
              </p>
            ) : (
              <AuditList entries={auditQuery.data ?? []} />
            )}
          </div>
        </>
      ) : null}
    </section>
  )
}
