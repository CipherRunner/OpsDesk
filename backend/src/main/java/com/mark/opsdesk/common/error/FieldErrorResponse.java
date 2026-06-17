package com.mark.opsdesk.common.error;

public record FieldErrorResponse(
		String field,
		String message
) {
}
