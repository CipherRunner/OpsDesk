import type { FormEvent } from 'react'
import { useState } from 'react'
import type {
  CreateTicketRequest,
  TicketPriority,
} from '../../api/ticketsApi'

type TicketFormProps = {
  error?: string
  isSubmitting?: boolean
  onSubmit: (request: CreateTicketRequest) => Promise<void> | void
}

const priorities: TicketPriority[] = ['LOW', 'MEDIUM', 'HIGH', 'URGENT']

function formatOption(value: string) {
  return value
    .toLowerCase()
    .split('_')
    .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
    .join(' ')
}

export function TicketForm({
  error,
  isSubmitting = false,
  onSubmit,
}: TicketFormProps) {
  const [title, setTitle] = useState('')
  const [description, setDescription] = useState('')
  const [priority, setPriority] = useState<TicketPriority>('MEDIUM')
  const [validationError, setValidationError] = useState('')

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setValidationError('')

    if (!title.trim()) {
      setValidationError('Ticket title is required.')
      return
    }

    await onSubmit({
      title: title.trim(),
      description: description.trim(),
      priority,
    })
  }

  return (
    <form className="panel form-panel" onSubmit={handleSubmit}>
      <label className="field">
        Title
        <input
          maxLength={255}
          name="title"
          onChange={(event) => setTitle(event.target.value)}
          type="text"
          value={title}
        />
      </label>

      <label className="field">
        Description
        <textarea
          name="description"
          onChange={(event) => setDescription(event.target.value)}
          rows={6}
          value={description}
        />
      </label>

      <label className="field">
        Priority
        <select
          name="priority"
          onChange={(event) => setPriority(event.target.value as TicketPriority)}
          value={priority}
        >
          {priorities.map((option) => (
            <option key={option} value={option}>
              {formatOption(option)}
            </option>
          ))}
        </select>
      </label>

      {validationError ? <p className="form-error">{validationError}</p> : null}
      {error ? <p className="form-error">{error}</p> : null}

      <button className="primary-button form-action" disabled={isSubmitting}>
        {isSubmitting ? 'Creating...' : 'Create ticket'}
      </button>
    </form>
  )
}
