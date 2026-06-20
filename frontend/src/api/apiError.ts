import { AxiosError } from 'axios'

type ApiErrorBody = {
  message?: string
  error?: string
  fieldErrors?: Array<{
    field: string
    message: string
  }>
}

function isApiErrorBody(value: unknown): value is ApiErrorBody {
  if (!value || typeof value !== 'object') {
    return false
  }

  return 'message' in value || 'error' in value || 'fieldErrors' in value
}

export function getApiErrorMessage(error: unknown, fallback: string) {
  if (error instanceof AxiosError && isApiErrorBody(error.response?.data)) {
    const data = error.response.data
    const fieldMessage = data.fieldErrors?.[0]

    if (fieldMessage) {
      return `${fieldMessage.field}: ${fieldMessage.message}`
    }

    return data.message ?? data.error ?? fallback
  }

  if (error instanceof Error) {
    return error.message
  }

  return fallback
}
