package com.mark.opsdesk.common.error;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

public record ApiErrorResponse(
		Instant timestamp,
		int status,
		String error,
		String message,
		String path,
		@JsonInclude(JsonInclude.Include.NON_EMPTY)
		List<FieldErrorResponse> fieldErrors
) {
	public static ApiErrorResponse of(
			int status,
			String error,
			String message,
			String path
	) {
		return new ApiErrorResponse(Instant.now(), status, error, message, path, null);
	}

	public static ApiErrorResponse withFieldErrors(
			int status,
			String error,
			String message,
			String path,
			List<FieldErrorResponse> fieldErrors
	) {
		return new ApiErrorResponse(Instant.now(), status, error, message, path, fieldErrors);
	}
}
