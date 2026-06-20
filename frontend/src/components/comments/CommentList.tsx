import type { TicketComment } from '../../api/ticketsApi'

type CommentListProps = {
  comments: TicketComment[]
}

const dateFormatter = new Intl.DateTimeFormat(undefined, {
  dateStyle: 'medium',
  timeStyle: 'short',
})

function formatDate(value: string) {
  return dateFormatter.format(new Date(value))
}

export function CommentList({ comments }: CommentListProps) {
  if (comments.length === 0) {
    return (
      <div className="empty-state compact">
        <h2>No comments yet</h2>
        <p>Add the first update for this ticket.</p>
      </div>
    )
  }

  return (
    <ul className="comment-list">
      {comments.map((comment) => (
        <li className="comment-item" key={comment.id}>
          <div className="comment-meta">
            <strong>{comment.authorUsername}</strong>
            <span>{formatDate(comment.createdAt)}</span>
          </div>
          <p>{comment.content}</p>
        </li>
      ))}
    </ul>
  )
}
