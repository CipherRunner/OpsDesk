type PaginationProps = {
  /** Zero-based page index, as the API counts. */
  page: number
  totalPages: number
  totalElements: number
  onPageChange: (page: number) => void
}

export function Pagination({
  page,
  totalPages,
  totalElements,
  onPageChange,
}: PaginationProps) {
  if (totalPages <= 1) {
    return null
  }

  const hasPrevious = page > 0
  const hasNext = page < totalPages - 1

  return (
    <nav className="pagination" aria-label="Pagination">
      <button
        className="pagination-button"
        disabled={!hasPrevious}
        type="button"
        onClick={() => onPageChange(page - 1)}
      >
        Previous
      </button>

      <span className="helper-text">
        Page {page + 1} of {totalPages} · {totalElements} tickets
      </span>

      <button
        className="pagination-button"
        disabled={!hasNext}
        type="button"
        onClick={() => onPageChange(page + 1)}
      >
        Next
      </button>
    </nav>
  )
}
