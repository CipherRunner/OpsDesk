package com.mark.opsdesk.common.exception;

import com.mark.opsdesk.common.error.ApiErrorResponse;
import com.mark.opsdesk.common.error.FieldErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValid(
			MethodArgumentNotValidException exception,
			HttpServletRequest request
	) {
		HttpStatus status = HttpStatus.BAD_REQUEST;
		List<FieldErrorResponse> fieldErrors = exception.getBindingResult()
				.getFieldErrors()
				.stream()
				.map(fieldError -> new FieldErrorResponse(
						fieldError.getField(),
						fieldError.getDefaultMessage()
				))
				.toList();

		ApiErrorResponse response = ApiErrorResponse.withFieldErrors(
				status.value(),
				status.getReasonPhrase(),
				"Validation failed",
				request.getRequestURI(),
				fieldErrors
		);

		return ResponseEntity.status(status).body(response);
	}

	@ExceptionHandler(ResponseStatusException.class)
	public ResponseEntity<ApiErrorResponse> handleResponseStatus(
			ResponseStatusException exception,
			HttpServletRequest request
	) {
		HttpStatusCode statusCode = exception.getStatusCode();
		String error = getReasonPhrase(statusCode);
		String message = exception.getReason() != null ? exception.getReason() : error;

		ApiErrorResponse response = ApiErrorResponse.of(
				statusCode.value(),
				error,
				message,
				request.getRequestURI()
		);

		return ResponseEntity.status(statusCode).body(response);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiErrorResponse> handleException(
			Exception exception,
			HttpServletRequest request
	) {
		HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
		ApiErrorResponse response = ApiErrorResponse.of(
				status.value(),
				status.getReasonPhrase(),
				"An unexpected error occurred",
				request.getRequestURI()
		);

		return ResponseEntity.status(status).body(response);
	}

	private String getReasonPhrase(HttpStatusCode statusCode) {
		HttpStatus status = HttpStatus.resolve(statusCode.value());
		return status != null ? status.getReasonPhrase() : statusCode.toString();
	}
}
