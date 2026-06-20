import type { FormEvent } from 'react'
import { useState } from 'react'

type CommentFormProps = {
  error?: string
  isSubmitting?: boolean
  onSubmit: (content: string) => Promise<boolean | void> | boolean | void
}

export function CommentForm({
  error,
  isSubmitting = false,
  onSubmit,
}: CommentFormProps) {
  const [content, setContent] = useState('')
  const [validationError, setValidationError] = useState('')

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setValidationError('')

    if (!content.trim()) {
      setValidationError('Comment content is required.')
      return
    }

    const shouldClear = await onSubmit(content.trim())

    if (shouldClear !== false) {
      setContent('')
    }
  }

  return (
    <form className="comment-form" onSubmit={handleSubmit}>
      <label className="field">
        Add comment
        <textarea
          maxLength={2000}
          onChange={(event) => setContent(event.target.value)}
          rows={4}
          value={content}
        />
      </label>

      {validationError ? <p className="form-error">{validationError}</p> : null}
      {error ? <p className="form-error">{error}</p> : null}

      <button className="primary-button comment-button" disabled={isSubmitting}>
        {isSubmitting ? 'Adding...' : 'Add comment'}
      </button>
    </form>
  )
}
