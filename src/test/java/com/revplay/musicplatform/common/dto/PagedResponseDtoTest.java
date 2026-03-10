package com.revplay.musicplatform.common.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@Tag("unit")
class PagedResponseDtoTest {

    private static final int PAGE_ZERO = 0;
    private static final int PAGE_ONE = 1;
    private static final int PAGE_TWO = 2;
    private static final int SIZE_TEN = 10;
    private static final long TOTAL_ELEMENTS = 25L;
    private static final int TOTAL_PAGES_THREE = 3;
    private static final int TOTAL_PAGES_ZERO = 0;
    private static final String SORT_BY = "createdAt";
    private static final String SORT_DIR = "DESC";

    @Test
    @DisplayName("constructor marks last true when totalPages is zero")
    void constructorSetsLastForEmptyPages() {
        PagedResponseDto<String> response = new PagedResponseDto<>(
                List.of(),
                PAGE_ZERO,
                SIZE_TEN,
                0L,
                TOTAL_PAGES_ZERO,
                SORT_BY,
                SORT_DIR
        );

        assertThat(response.isLast()).isTrue();
    }

    @Test
    @DisplayName("constructor marks last false when page is not final page")
    void constructorSetsLastFalseForNonFinalPage() {
        PagedResponseDto<String> response = new PagedResponseDto<>(
                List.of("one"),
                PAGE_ONE,
                SIZE_TEN,
                TOTAL_ELEMENTS,
                TOTAL_PAGES_THREE,
                SORT_BY,
                SORT_DIR
        );

        assertThat(response.isLast()).isFalse();
    }

    @Test
    @DisplayName("constructor marks last true when page is final page")
    void constructorSetsLastTrueForFinalPage() {
        PagedResponseDto<String> response = new PagedResponseDto<>(
                List.of("one"),
                PAGE_TWO,
                SIZE_TEN,
                TOTAL_ELEMENTS,
                TOTAL_PAGES_THREE,
                SORT_BY,
                SORT_DIR
        );

        assertThat(response.isLast()).isTrue();
    }

    @Test
    @DisplayName("of maps page fields to paged response")
    void ofMapsPageValues() {
        PageRequest pageRequest = PageRequest.of(PAGE_ONE, SIZE_TEN);
        Page<String> page = new PageImpl<>(List.of("a", "b"), pageRequest, TOTAL_ELEMENTS);

        PagedResponseDto<String> response = PagedResponseDto.of(page);

        assertThat(response.getContent()).containsExactly("a", "b");
        assertThat(response.getPage()).isEqualTo(PAGE_ONE);
        assertThat(response.getSize()).isEqualTo(SIZE_TEN);
        assertThat(response.getTotalElements()).isEqualTo(TOTAL_ELEMENTS);
        assertThat(response.getTotalPages()).isEqualTo(TOTAL_PAGES_THREE);
        assertThat(response.isLast()).isFalse();
    }
}
