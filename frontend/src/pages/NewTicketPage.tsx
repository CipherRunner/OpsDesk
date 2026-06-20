import { Link, useNavigate } from 'react-router-dom'
import { useState } from 'react'
import { getApiErrorMessage } from '../api/apiError'
import {
  createTicket,
  type CreateTicketRequest,
} from '../api/ticketsApi'
import { TicketForm } from '../components/tickets/TicketForm'

export function NewTicketPage() {
  const [error, setError] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)
  const navigate = useNavigate()

  async function handleCreateTicket(request: CreateTicketRequest) {
    setError('')
    setIsSubmitting(true)

    try {
      const ticket = await createTicket(request)
      navigate(`/tickets/${ticket.id}`)
    } catch (requestError) {
      setError(
        getApiErrorMessage(requestError, 'Failed to create ticket.'),
      )
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <section className="page narrow-page">
      <div className="page-header page-header-row">
        <div>
          <p className="eyebrow">Create</p>
          <h1>New ticket</h1>
        </div>

        <Link className="text-link" to="/tickets">
          Back to tickets
        </Link>
      </div>

      <TicketForm
        error={error}
        isSubmitting={isSubmitting}
        onSubmit={handleCreateTicket}
      />
    </section>
  )
}
