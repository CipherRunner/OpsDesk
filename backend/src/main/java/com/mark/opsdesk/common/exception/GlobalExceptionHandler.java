package com.mark.opsdesk.common.exception;

import com.mark.opsdesk.common.error.ApiErrorResponse;
import com.mark.opsdesk.common.error.FieldErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

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

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ApiErrorResponse> handleHttpMessageNotReadable(
			HttpMessageNotReadableException exception,
			HttpServletRequest request
	) {
		return badRequest("Invalid request body", request);
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ApiErrorResponse> handleMethodArgumentTypeMismatch(
			MethodArgumentTypeMismatchException exception,
			HttpServletRequest request
	) {
		String message = "Invalid value '" + exception.getValue()
				+ "' for parameter '" + exception.getName() + "'";
		return badRequest(message, request);
	}

	@ExceptionHandler(MissingServletRequestParameterException.class)
	public ResponseEntity<ApiErrorResponse> handleMissingServletRequestParameter(
			MissingServletRequestParameterException exception,
			HttpServletRequest request
	) {
		return badRequest("Missing required parameter '" + exception.getParameterName() + "'", request);
	}

	@ExceptionHandler(ApplicationException.class)
	public ResponseEntity<ApiErrorResponse> handleApplicationException(
			ApplicationException exception,
			HttpServletRequest request
	) {
		HttpStatus status = exception.getStatus();
		ApiErrorResponse response = ApiErrorResponse.of(
				status.value(),
				status.getReasonPhrase(),
				exception.getMessage(),
				request.getRequestURI()
		);

		return ResponseEntity.status(status).body(response);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiErrorResponse> handleException(
			Exception exception,
			HttpServletRequest request
	) {
		if (exception instanceof ErrorResponse errorResponse) {
			return handleSpringErrorResponse(errorResponse, request);
		}

		log.error("Unhandled exception for {} {}", request.getMethod(), request.getRequestURI(), exception);

		HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
		ApiErrorResponse response = ApiErrorResponse.of(
				status.value(),
				status.getReasonPhrase(),
				"An unexpected error occurred",
				request.getRequestURI()
		);

		return ResponseEntity.status(status).body(response);
	}

	/**
	 * Spring MVC reports routing-level problems (unknown resource, unsupported method or media type,
	 * missing path variable, ...) with exceptions that implement {@link ErrorResponse}. Without this
	 * mapping the catch-all handler above would turn every one of them into a 500.
	 */
	private ResponseEntity<ApiErrorResponse> handleSpringErrorResponse(
			ErrorResponse errorResponse,
			HttpServletRequest request
	) {
		HttpStatusCode statusCode = errorResponse.getStatusCode();
		String error = getReasonPhrase(statusCode);
		String detail = errorResponse.getBody().getDetail();
		String message = detail != null && !detail.isBlank() ? detail : error;

		ApiErrorResponse response = ApiErrorResponse.of(
				statusCode.value(),
				error,
				message,
				request.getRequestURI()
		);

		return ResponseEntity.status(statusCode).headers(errorResponse.getHeaders()).body(response);
	}

	private ResponseEntity<ApiErrorResponse> badRequest(String message, HttpServletRequest request) {
		HttpStatus status = HttpStatus.BAD_REQUEST;
		ApiErrorResponse response = ApiErrorResponse.of(
				status.value(),
				status.getReasonPhrase(),
				message,
				request.getRequestURI()
		);

		return ResponseEntity.status(status).body(response);
	}

	private String getReasonPhrase(HttpStatusCode statusCode) {
		HttpStatus status = HttpStatus.resolve(statusCode.value());
		return status != null ? status.getReasonPhrase() : statusCode.toString();
	}
}
