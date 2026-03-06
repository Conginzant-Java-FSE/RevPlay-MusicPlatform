package com.revplay.musicplatform.catalog.service.impl;

import com.revplay.musicplatform.catalog.entity.Album;
import com.revplay.musicplatform.catalog.repository.AlbumRepository;
import com.revplay.musicplatform.catalog.repository.SongRepository;
import com.revplay.musicplatform.exception.BadRequestException;
import com.revplay.musicplatform.exception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class ContentValidationServiceImplTest {

    @Mock
    private AlbumRepository albumRepository;

    @Mock
    private SongRepository songRepository;

    @InjectMocks
    private ContentValidationServiceImpl validationService;

    @Test
    @DisplayName("validateSongDuration: null/0/-1 -> BadRequestException")
    void validateSongDuration_Invalid_ThrowsBadRequest() {
        assertThatThrownBy(() -> validationService.validateSongDuration(null))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Invalid song duration");

        assertThatThrownBy(() -> validationService.validateSongDuration(0))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Invalid song duration");

        assertThatThrownBy(() -> validationService.validateSongDuration(-1))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Invalid song duration");
    }

    @Test
    @DisplayName("validateSongDuration: exceeds limit -> BadRequestException")
    void validateSongDuration_Exceeds_ThrowsBadRequest() {
        assertThatThrownBy(() -> validationService.validateSongDuration(3601)) // 1 hour + 1 second
                .isInstanceOf(BadRequestException.class)
                .hasMessage("song duration exceeds allowed limit");
    }

    @Test
    @DisplayName("validateSongDuration: valid -> no exception")
    void validateSongDuration_Valid_NoException() {
        assertThatCode(() -> validationService.validateSongDuration(180))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validateAlbumBelongsToArtist: null albumId -> no exception")
    void validateAlbumBelongsToArtist_Null_NoException() {
        assertThatCode(() -> validationService.validateAlbumBelongsToArtist(null, 1L))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validateAlbumBelongsToArtist: match -> no exception")
    void validateAlbumBelongsToArtist_Match_NoException() {
        Album album = new Album();
        album.setArtistId(10L);
        when(albumRepository.findById(1L)).thenReturn(Optional.of(album));

        assertThatCode(() -> validationService.validateAlbumBelongsToArtist(1L, 10L))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validateAlbumBelongsToArtist: mismatch -> BadRequestException")
    void validateAlbumBelongsToArtist_Mismatch_ThrowsBadRequest() {
        Album album = new Album();
        album.setArtistId(10L);
        when(albumRepository.findById(1L)).thenReturn(Optional.of(album));

        assertThatThrownBy(() -> validationService.validateAlbumBelongsToArtist(1L, 99L))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Album does not belong to the song artist");
    }

    @Test
    @DisplayName("validateAlbumBelongsToArtist: not found -> ResourceNotFoundException")
    void validateAlbumBelongsToArtist_NotFound_ThrowsResourceNotFound() {
        when(albumRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> validationService.validateAlbumBelongsToArtist(1L, 10L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Album not found");
    }

    @Test
    @DisplayName("validateUniqueSongTitleWithinAlbum: duplicate -> BadRequestException")
    void validateUniqueSongTitleWithinAlbum_Duplicate_ThrowsBadRequest() {
        when(songRepository.existsByAlbumIdAndTitleIgnoreCaseAndIsActiveTrue(anyLong(), eq("Test Title")))
                .thenReturn(true);

        assertThatThrownBy(() -> validationService.validateUniqueSongTitleWithinAlbum(1L, "Test Title"))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Duplicate song title exists in album");
    }

    @Test
    @DisplayName("validateUniqueSongTitleWithinAlbum: unique -> no exception")
    void validateUniqueSongTitleWithinAlbum_Unique_NoException() {
        when(songRepository.existsByAlbumIdAndTitleIgnoreCaseAndIsActiveTrue(anyLong(), anyString()))
                .thenReturn(false);

        assertThatCode(() -> validationService.validateUniqueSongTitleWithinAlbum(1L, "Unique Title"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validateUniqueSongTitleWithinAlbumForUpdate: different song same title -> BadRequestException")
    void validateUniqueSongTitleWithinAlbumForUpdate_Duplicate_ThrowsBadRequest() {
        when(songRepository.existsByAlbumIdAndTitleIgnoreCaseAndIsActiveTrueAndSongIdNot(anyLong(), anyString(),
                anyLong()))
                .thenReturn(true);

        assertThatThrownBy(() -> validationService.validateUniqueSongTitleWithinAlbumForUpdate(1L, "Duplicate", 100L))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Duplicate song title exists in album");
    }

    @Test
    @DisplayName("validateUniqueSongTitleWithinAlbumForUpdate: same song same title -> no exception")
    void validateUniqueSongTitleWithinAlbumForUpdate_Same_NoException() {
        when(songRepository.existsByAlbumIdAndTitleIgnoreCaseAndIsActiveTrueAndSongIdNot(anyLong(), anyString(),
                anyLong()))
                .thenReturn(false);

        assertThatCode(() -> validationService.validateUniqueSongTitleWithinAlbumForUpdate(1L, "Same", 100L))
                .doesNotThrowAnyException();
    }
}
