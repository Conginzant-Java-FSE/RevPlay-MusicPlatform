package com.revplay.musicplatform.catalog.service.impl;

import com.revplay.musicplatform.catalog.dto.request.SearchRequest;
import com.revplay.musicplatform.catalog.dto.response.SearchResultItemResponse;
import com.revplay.musicplatform.catalog.enums.SearchContentType;
import com.revplay.musicplatform.catalog.exception.DiscoveryValidationException;
import com.revplay.musicplatform.common.dto.PagedResponseDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class SearchServiceImplTest {

    private static final int PAGE_ZERO = 0;
    private static final int PAGE_TWO = 2;
    private static final int SIZE_TEN = 10;
    private static final int SIZE_SEVEN = 7;
    private static final int OFFSET_FOURTEEN = 14;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private SearchServiceImpl searchService;

    @Test
    @DisplayName("search with query returns list combining content")
    @SuppressWarnings("unchecked")
    void search_withQuery_returnsCombinedItems() {
        when(jdbcTemplate.queryForObject(anyString(), eq(Long.class), any(Object[].class))).thenReturn(1L);
        doReturn(List.of(new SearchResultItemResponse(
                        "song",
                        1L,
                        "Faded",
                        10L,
                        "Alan Walker",
                        "MUSIC",
                        LocalDate.now()))).when(jdbcTemplate).query(anyString(), any(RowMapper.class), any(Object[].class));

        PagedResponseDto<SearchResultItemResponse> response = searchService.search(
                new SearchRequest("faded", SearchContentType.ALL, null, null, null, null, PAGE_ZERO, SIZE_TEN, "title", "ASC"));

        assertThat(response.getContent()).isNotEmpty();
        assertThat(response.getTotalElements()).isGreaterThan(0);
    }

    @Test
    @DisplayName("search with type SONG filter returns only song items")
    @SuppressWarnings("unchecked")
    void search_songFilter_returnsOnlySongs() {
        when(jdbcTemplate.queryForObject(anyString(), eq(Long.class), any(Object[].class))).thenReturn(1L);
        doReturn(List.of(new SearchResultItemResponse(
                        "song",
                        2L,
                        "Song A",
                        20L,
                        "Artist A",
                        "MUSIC",
                        LocalDate.now()))).when(jdbcTemplate).query(anyString(), any(RowMapper.class), any(Object[].class));

        PagedResponseDto<SearchResultItemResponse> response = searchService.search(
                new SearchRequest("song", SearchContentType.SONG, null, null, null, null, PAGE_ZERO, SIZE_TEN, "title", "ASC"));

        assertThat(response.getContent()).isNotEmpty();
        assertThat(response.getContent()).allMatch(item -> "song".equals(item.type()));
    }

    @Test
    @DisplayName("empty query throws DiscoveryValidationException")
    void search_emptyQuery_throws() {
        assertThatThrownBy(() -> searchService.search(
                new SearchRequest("", SearchContentType.SONG, null, null, null, null, PAGE_ZERO, SIZE_TEN, "title", "ASC")))
                .isInstanceOf(DiscoveryValidationException.class)
                .hasMessageContaining("q is required");
    }

    @Test
    @DisplayName("pagination is applied to sql query")
    @SuppressWarnings("unchecked")
    void search_paginationApplied() {
        when(jdbcTemplate.queryForObject(anyString(), eq(Long.class), any(Object[].class))).thenReturn(1L);
        doReturn(List.of(new SearchResultItemResponse(
                        "song",
                        3L,
                        "Paged Song",
                        30L,
                        "Artist P",
                        "MUSIC",
                        LocalDate.now()))).when(jdbcTemplate).query(anyString(), any(RowMapper.class), any(Object[].class));

        searchService.search(new SearchRequest(
                "paged",
                SearchContentType.SONG,
                null,
                null,
                null,
                null,
                PAGE_TWO,
                SIZE_SEVEN,
                "title",
                "ASC"));

        ArgumentCaptor<Object[]> argsCaptor = ArgumentCaptor.forClass(Object[].class);
        verify(jdbcTemplate).query(anyString(), any(RowMapper.class), argsCaptor.capture());
        Object[] args = argsCaptor.getValue();
        assertThat(args[args.length - 2]).isEqualTo(SIZE_SEVEN);
        assertThat(args[args.length - 1]).isEqualTo(OFFSET_FOURTEEN);
    }
}
