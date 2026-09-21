package com.mark.opsdesk.common.api;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Stable wire format for paged results. Spring Data's {@code Page} serialises with implementation
 * details (and warns about it); this record is the contract the frontend depends on.
 */
public record PageResponse<T>(
		List<T> content,
		int page,
		int size,
		long totalElements,
		int totalPages
) {
	public static <T> PageResponse<T> from(Page<T> page) {
		return new PageResponse<>(
				page.getContent(),
				page.getNumber(),
				page.getSize(),
				page.getTotalElements(),
				page.getTotalPages()
		);
	}
}
