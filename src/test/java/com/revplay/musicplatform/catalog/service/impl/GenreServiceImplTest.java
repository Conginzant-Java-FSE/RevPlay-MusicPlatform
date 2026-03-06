package com.revplay.musicplatform.catalog.service.impl;

import com.revplay.musicplatform.catalog.dto.request.GenreUpsertRequest;
import com.revplay.musicplatform.catalog.dto.response.GenreResponse;
import com.revplay.musicplatform.catalog.entity.Genre;
import com.revplay.musicplatform.catalog.exception.DiscoveryNotFoundException;
import com.revplay.musicplatform.catalog.exception.DiscoveryValidationException;
import com.revplay.musicplatform.catalog.repository.GenreRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class GenreServiceImplTest {

    @Mock
    private GenreRepository genreRepository;

    @InjectMocks
    private GenreServiceImpl genreService;

    @Test
    @DisplayName("getAll: returns list of active genres")
    void getAll_ReturnsList() {
        Genre genre = new Genre();
        genre.setGenreId(1L);
        genre.setName("Rock");
        genre.setIsActive(true);

        when(genreRepository.findByIsActiveTrueOrderByNameAscGenreIdAsc()).thenReturn(List.of(genre));

        List<GenreResponse> result = genreService.getAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Rock");
    }

    @Test
    @DisplayName("getById: found -> returns response")
    void getById_Found_ReturnsResponse() {
        Genre genre = new Genre();
        genre.setGenreId(1L);
        genre.setName("Rock");
        genre.setIsActive(true);

        when(genreRepository.findByGenreIdAndIsActiveTrue(1L)).thenReturn(Optional.of(genre));

        GenreResponse result = genreService.getById(1L);

        assertThat(result.name()).isEqualTo("Rock");
    }

    @Test
    @DisplayName("getById: not found -> DiscoveryNotFoundException")
    void getById_NotFound_ThrowsException() {
        when(genreRepository.findByGenreIdAndIsActiveTrue(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> genreService.getById(1L))
                .isInstanceOf(DiscoveryNotFoundException.class)
                .hasMessageContaining("not found");
    }

    @Test
    @DisplayName("create: new genre -> success")
    void create_New_Success() {
        GenreUpsertRequest request = new GenreUpsertRequest("Jazz", "Jazz music");
        when(genreRepository.existsByNameIgnoreCaseAndIsActiveTrue("Jazz")).thenReturn(false);
        when(genreRepository.findByNameIgnoreCaseAndIsActiveFalse("Jazz")).thenReturn(Optional.empty());

        Genre savedGenre = new Genre();
        savedGenre.setGenreId(1L);
        savedGenre.setName("Jazz");
        savedGenre.setIsActive(true);
        when(genreRepository.save(any(Genre.class))).thenReturn(savedGenre);

        GenreResponse response = genreService.create(request);

        assertThat(response.name()).isEqualTo("Jazz");
        verify(genreRepository).save(any(Genre.class));
    }

    @Test
    @DisplayName("create: duplicate name -> DiscoveryValidationException")
    void create_Duplicate_ThrowsException() {
        GenreUpsertRequest request = new GenreUpsertRequest("Rock", null);
        when(genreRepository.existsByNameIgnoreCaseAndIsActiveTrue("Rock")).thenReturn(true);

        assertThatThrownBy(() -> genreService.create(request))
                .isInstanceOf(DiscoveryValidationException.class)
                .hasMessage("Genre name already exists");
    }

    @Test
    @DisplayName("create: exists as inactive -> reactivates")
    void create_InactiveExists_Reactivates() {
        GenreUpsertRequest request = new GenreUpsertRequest("Pop", "New description");
        Genre inactive = new Genre();
        inactive.setGenreId(5L);
        inactive.setName("Pop");
        inactive.setIsActive(false);

        when(genreRepository.existsByNameIgnoreCaseAndIsActiveTrue("Pop")).thenReturn(false);
        when(genreRepository.findByNameIgnoreCaseAndIsActiveFalse("Pop")).thenReturn(Optional.of(inactive));
        when(genreRepository.save(inactive)).thenReturn(inactive);

        GenreResponse response = genreService.create(request);

        assertThat(inactive.getIsActive()).isTrue();
        assertThat(inactive.getDescription()).isEqualTo("New description");
        assertThat(response.genreId()).isEqualTo(5L);
    }

    @Test
    @DisplayName("update: success")
    void update_Success() {
        GenreUpsertRequest request = new GenreUpsertRequest("Updated Rock", "Desc");
        Genre genre = new Genre();
        genre.setGenreId(1L);
        genre.setName("Rock");
        genre.setIsActive(true);

        when(genreRepository.findByGenreIdAndIsActiveTrue(1L)).thenReturn(Optional.of(genre));
        when(genreRepository.existsByNameIgnoreCaseAndIsActiveTrueAndGenreIdNot("Updated Rock", 1L)).thenReturn(false);
        when(genreRepository.save(genre)).thenReturn(genre);

        GenreResponse response = genreService.update(1L, request);

        assertThat(genre.getName()).isEqualTo("Updated Rock");
        assertThat(response.name()).isEqualTo("Updated Rock");
    }

    @Test
    @DisplayName("delete: soft deletes")
    void delete_SoftDeletes() {
        Genre genre = new Genre();
        genre.setGenreId(1L);
        genre.setIsActive(true);

        when(genreRepository.findByGenreIdAndIsActiveTrue(1L)).thenReturn(Optional.of(genre));

        genreService.delete(1L);

        assertThat(genre.getIsActive()).isFalse();
        verify(genreRepository).save(genre);
    }
}
