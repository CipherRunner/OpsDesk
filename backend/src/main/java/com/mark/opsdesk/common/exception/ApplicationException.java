package com.mark.opsdesk.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Base class for errors raised by the service layer. Each subclass carries the HTTP status the
 * API should answer with, so services stay free of Spring MVC types and the mapping lives in one
 * place: {@code GlobalExceptionHandler}.
 */
public abstract class ApplicationException extends RuntimeException {

	private final HttpStatus status;

	protected ApplicationException(HttpStatus status, String message) {
		super(message);
		this.status = status;
	}

	public HttpStatus getStatus() {
		return status;
	}
}
