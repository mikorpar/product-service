package com.mkorpar.productservice.data.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import org.springframework.data.domain.Page;

import java.util.List;

@Builder
@Schema(description = "Paginated response wrapper.")
public record PageResDTO<T>(
        @Schema(description = "List of items for the current page.")
        List<T> content,
        @Schema(description = "Current page number.", example = "0")
        int page,
        @Schema(description = "Total number of pages.", example = "10", name = "total_pages")
        int totalPages,
        @Schema(description = "Size of the page.", example = "20")
        int size,
        @Schema(description = "Number of elements in the current page.", example = "20", name = "number_of_elements")
        int numberOfElements,
        @Schema(description = "Total number of elements across all pages.", example = "200", name = "total_elements")
        long totalElements,
        @Schema(description = "Indicates if this is the first page.", example = "true")
        boolean first,
        @Schema(description = "Indicates if this is the last page.", example = "false")
        boolean last
) {
    public static <T> PageResDTO<T> from(Page<?> page, List<T> content) {
        return new PageResDTO<>(
                content,
                page.getNumber(),
                page.getTotalPages(),
                page.getSize(),
                page.getNumberOfElements(),
                page.getTotalElements(),
                page.isFirst(),
                page.isLast()
        );
    }
}
