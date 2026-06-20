import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { getApiErrorMessage } from '../api/apiError'
import {
  createTicketComment,
  getTicket,
  getTicketComments,
  type Ticket,
  type TicketComment,
  type TicketPriority,
  type TicketStatus,
  updateTicketAssignee,
  updateTicketPriority,
  updateTicketStatus,
} from '../api/ticketsApi'
import { getUsers, type User } from '../api/usersApi'
import { canEditTickets } from '../auth/authStorage'
import { CommentForm } from '../components/comments/CommentForm'
import { CommentList } from '../components/comments/CommentList'
import { TicketDetail } from '../components/tickets/TicketDetail'

export function TicketDetailPage() {
  const { id } = useParams()
  const ticketId = Number(id)
  const hasValidTicketId = Number.isInteger(ticketId) && ticketId > 0
  const [ticket, setTicket] = useState<Ticket | null>(null)
  const [comments, setComments] = useState<TicketComment[]>([])
  const [assigneeOptions, setAssigneeOptions] = useState<User[]>([])
  const [usersMessage, setUsersMessage] = useState('')
  const [loadError, setLoadError] = useState('')
  const [actionError, setActionError] = useState('')
  const [commentError, setCommentError] = useState('')
  const [isLoading, setIsLoading] = useState(true)
  const [isUpdatingStatus, setIsUpdatingStatus] = useState(false)
  const [isUpdatingPriority, setIsUpdatingPriority] = useState(false)
  const [isUpdatingAssignee, setIsUpdatingAssignee] = useState(false)
  const [isAddingComment, setIsAddingComment] = useState(false)
  const canEditTicketActions = canEditTickets()

  useEffect(() => {
    let ignore = false

    async function loadTicket() {
      if (!hasValidTicketId) {
        setLoadError('Invalid ticket id.')
        setIsLoading(false)
        return
      }

      setIsLoading(true)
      setLoadError('')
      setActionError('')
      setCommentError('')

      try {
        const [ticketResponse, commentResponse] = await Promise.all([
          getTicket(ticketId),
          getTicketComments(ticketId),
        ])

        if (!ignore) {
          setTicket(ticketResponse)
          setComments(commentResponse)
        }
      } catch (requestError) {
        if (!ignore) {
          setLoadError(
            getApiErrorMessage(requestError, 'Failed to load ticket.'),
          )
          setTicket(null)
          setComments([])
        }
      } finally {
        if (!ignore) {
          setIsLoading(false)
        }
      }

      if (!canEditTicketActions) {
        setAssigneeOptions([])
        setUsersMessage('')
        return
      }

      try {
        const users = await getUsers()
        const assignableUsers = users.filter(
          (user) => user.role === 'ADMIN' || user.role === 'AGENT',
        )

        if (!ignore) {
          setAssigneeOptions(assignableUsers)
          setUsersMessage('')
        }
      } catch (requestError) {
        if (!ignore) {
          setAssigneeOptions([])
          setUsersMessage(
            `Assignee list unavailable: ${getApiErrorMessage(
              requestError,
              'Failed to load users.',
            )}`,
          )
        }
      }
    }

    loadTicket()

    return () => {
      ignore = true
    }
  }, [canEditTicketActions, hasValidTicketId, ticketId])

  async function handleStatusChange(status: TicketStatus) {
    if (!canEditTicketActions || !ticket || ticket.status === status) {
      return
    }

    setActionError('')
    setIsUpdatingStatus(true)

    try {
      const updatedTicket = await updateTicketStatus(ticket.id, status)
      setTicket(updatedTicket)
    } catch (requestError) {
      setActionError(
        getApiErrorMessage(requestError, 'Failed to update status.'),
      )
    } finally {
      setIsUpdatingStatus(false)
    }
  }

  async function handlePriorityChange(priority: TicketPriority) {
    if (!canEditTicketActions || !ticket || ticket.priority === priority) {
      return
    }

    setActionError('')
    setIsUpdatingPriority(true)

    try {
      const updatedTicket = await updateTicketPriority(ticket.id, priority)
      setTicket(updatedTicket)
    } catch (requestError) {
      setActionError(
        getApiErrorMessage(requestError, 'Failed to update priority.'),
      )
    } finally {
      setIsUpdatingPriority(false)
    }
  }

  async function handleAssigneeChange(assignedTo: string) {
    if (
      !canEditTicketActions ||
      !ticket ||
      !assignedTo ||
      ticket.assignedTo === assignedTo
    ) {
      return
    }

    setActionError('')
    setIsUpdatingAssignee(true)

    try {
      const updatedTicket = await updateTicketAssignee(ticket.id, assignedTo)
      setTicket(updatedTicket)
    } catch (requestError) {
      setActionError(
        getApiErrorMessage(requestError, 'Failed to update assignee.'),
      )
    } finally {
      setIsUpdatingAssignee(false)
    }
  }

  async function handleAddComment(content: string) {
    if (!ticket) {
      return false
    }

    setCommentError('')
    setIsAddingComment(true)

    try {
      const newComment = await createTicketComment(ticket.id, { content })
      setComments((currentComments) => [...currentComments, newComment])
      return true
    } catch (requestError) {
      setCommentError(
        getApiErrorMessage(requestError, 'Failed to add comment.'),
      )
      return false
    } finally {
      setIsAddingComment(false)
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
            assigneeOptions={assigneeOptions}
            canEditTicketActions={canEditTicketActions}
            isUpdatingAssignee={isUpdatingAssignee}
            isUpdatingPriority={isUpdatingPriority}
            isUpdatingStatus={isUpdatingStatus}
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

            <CommentList comments={comments} />
            <CommentForm
              error={commentError}
              isSubmitting={isAddingComment}
              onSubmit={handleAddComment}
            />
          </div>
        </>
      ) : null}
    </section>
  )
}
