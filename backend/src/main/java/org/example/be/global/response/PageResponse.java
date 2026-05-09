package org.example.be.global.response;

import java.util.List;

import org.springframework.data.domain.Page;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PageResponse<T> {
	private List<T> content;
	private int page;
	private int size;
	private long totalElements;
	private int totalPages;

	public static <T, R> PageResponse<R> from(Page<T> page, List<R> content) {
		return PageResponse.<R>builder()
			.content(content)
			.page(page.getNumber() + 1)
			.size(page.getSize())
			.totalElements(page.getTotalElements())
			.totalPages(page.getTotalPages())
			.build();
	}

	public static <T> PageResponse<T> from(Page<T> page) {
		return PageResponse.<T>builder()
			.content(page.getContent())
			.page(page.getNumber() + 1)
			.size(page.getSize())
			.totalElements(page.getTotalElements())
			.totalPages(page.getTotalPages())
			.build();
	}
}
