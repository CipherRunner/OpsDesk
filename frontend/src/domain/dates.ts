const dateTimeFormatter = new Intl.DateTimeFormat(undefined, {
  dateStyle: 'medium',
  timeStyle: 'short',
})

export function formatDateTime(isoValue: string): string {
  return dateTimeFormatter.format(new Date(isoValue))
}
