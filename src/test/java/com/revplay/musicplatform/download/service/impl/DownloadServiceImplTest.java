package com.revplay.musicplatform.download.service.impl;

import com.revplay.musicplatform.catalog.entity.Song;
import com.revplay.musicplatform.catalog.repository.SongRepository;
import com.revplay.musicplatform.download.entity.SongDownload;
import com.revplay.musicplatform.download.repository.SongDownloadRepository;
import com.revplay.musicplatform.download.service.SongFileResolver;
import com.revplay.musicplatform.exception.AccessDeniedException;
import com.revplay.musicplatform.exception.BadRequestException;
import com.revplay.musicplatform.exception.ResourceNotFoundException;
import com.revplay.musicplatform.premium.service.SubscriptionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class DownloadServiceImplTest {

    private static final Long USER_ID = 1L;
    private static final Long SONG_ID = 2L;
    private static final String FILE_URL = "/uploads/songs/my-song.mp3";

    @Mock
    private SubscriptionService subscriptionService;
    @Mock
    private SongRepository songRepository;
    @Mock
    private SongFileResolver songFileResolver;
    @Mock
    private SongDownloadRepository songDownloadRepository;

    @InjectMocks
    private DownloadServiceImpl service;

    @Test
    @DisplayName("downloadSong premium user and first download saves SongDownload and returns resource")
    void downloadSong_premiumAndFirstDownload_savesAndReturnsResource() {
        Song song = song("My Song");
        Resource resource = new ByteArrayResource(new byte[]{1, 2, 3});

        when(subscriptionService.isUserPremium(USER_ID)).thenReturn(true);
        when(songRepository.findById(SONG_ID)).thenReturn(Optional.of(song));
        when(songFileResolver.loadSongResource(FILE_URL)).thenReturn(resource);
        when(songDownloadRepository.existsByUserIdAndSongId(USER_ID, SONG_ID)).thenReturn(false);

        Resource actual = service.downloadSong(USER_ID, SONG_ID);

        assertThat(actual).isSameAs(resource);
        ArgumentCaptor<SongDownload> captor = ArgumentCaptor.forClass(SongDownload.class);
        verify(songDownloadRepository).save(captor.capture());
        assertThat(captor.getValue().getUserId()).isEqualTo(USER_ID);
        assertThat(captor.getValue().getSongId()).isEqualTo(SONG_ID);
        assertThat(captor.getValue().getDownloadedAt()).isNotNull();
    }

    @Test
    @DisplayName("downloadSong already downloaded does not save new SongDownload")
    void downloadSong_alreadyDownloaded_noNewRecord() {
        Song song = song("My Song");
        Resource resource = new ByteArrayResource(new byte[]{4, 5});

        when(subscriptionService.isUserPremium(USER_ID)).thenReturn(true);
        when(songRepository.findById(SONG_ID)).thenReturn(Optional.of(song));
        when(songFileResolver.loadSongResource(FILE_URL)).thenReturn(resource);
        when(songDownloadRepository.existsByUserIdAndSongId(USER_ID, SONG_ID)).thenReturn(true);

        Resource actual = service.downloadSong(USER_ID, SONG_ID);

        assertThat(actual).isSameAs(resource);
        verify(songDownloadRepository, never()).save(any(SongDownload.class));
    }

    @Test
    @DisplayName("downloadSong non premium throws AccessDeniedException")
    void downloadSong_nonPremium_throwsAccessDenied() {
        when(subscriptionService.isUserPremium(USER_ID)).thenReturn(false);

        assertThatThrownBy(() -> service.downloadSong(USER_ID, SONG_ID))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Premium subscription required to download songs");
    }

    @Test
    @DisplayName("downloadSong song not found throws ResourceNotFoundException")
    void downloadSong_songMissing_throwsNotFound() {
        when(subscriptionService.isUserPremium(USER_ID)).thenReturn(true);
        when(songRepository.findById(SONG_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.downloadSong(USER_ID, SONG_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Song not found: " + SONG_ID);
    }

    @ParameterizedTest
    @ValueSource(longs = {0L, -1L})
    @DisplayName("downloadSong invalid userId throws BadRequestException")
    void downloadSong_invalidUserId_throwsBadRequest(Long invalidUserId) {
        assertThatThrownBy(() -> service.downloadSong(invalidUserId, SONG_ID))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("userId is required");
    }

    @Test
    @DisplayName("downloadSong null userId throws BadRequestException")
    void downloadSong_nullUserId_throwsBadRequest() {
        assertThatThrownBy(() -> service.downloadSong(null, SONG_ID))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("userId is required");
    }

    @ParameterizedTest
    @ValueSource(longs = {0L, -1L})
    @DisplayName("downloadSong invalid songId throws BadRequestException")
    void downloadSong_invalidSongId_throwsBadRequest(Long invalidSongId) {
        assertThatThrownBy(() -> service.downloadSong(USER_ID, invalidSongId))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("songId is required");
    }

    @Test
    @DisplayName("downloadSong null songId throws BadRequestException")
    void downloadSong_nullSongId_throwsBadRequest() {
        assertThatThrownBy(() -> service.downloadSong(USER_ID, null))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("songId is required");
    }

    @Test
    @DisplayName("isDownloaded returns true when record exists")
    void isDownloaded_exists_returnsTrue() {
        when(songDownloadRepository.existsByUserIdAndSongId(USER_ID, SONG_ID)).thenReturn(true);

        boolean downloaded = service.isDownloaded(USER_ID, SONG_ID);

        assertThat(downloaded).isTrue();
    }

    @Test
    @DisplayName("isDownloaded returns false when record does not exist")
    void isDownloaded_notExists_returnsFalse() {
        when(songDownloadRepository.existsByUserIdAndSongId(USER_ID, SONG_ID)).thenReturn(false);

        boolean downloaded = service.isDownloaded(USER_ID, SONG_ID);

        assertThat(downloaded).isFalse();
    }

    @Test
    @DisplayName("isDownloaded invalid ids throws BadRequestException")
    void isDownloaded_invalidIds_throwsBadRequest() {
        assertThatThrownBy(() -> service.isDownloaded(0L, SONG_ID))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("userId is required");
    }

    @Test
    @DisplayName("getDownloadFileName normal title returns dashed mp3 name")
    void getDownloadFileName_normalTitle_returnsExpectedName() {
        Song song = song("My Song");
        when(songRepository.findById(SONG_ID)).thenReturn(Optional.of(song));

        String name = service.getDownloadFileName(SONG_ID);

        assertThat(name).isEqualTo("My-Song.mp3");
    }

    @Test
    @DisplayName("getDownloadFileName strips special chars")
    void getDownloadFileName_specialChars_stripsChars() {
        Song song = song("A/B:C");
        when(songRepository.findById(SONG_ID)).thenReturn(Optional.of(song));

        String name = service.getDownloadFileName(SONG_ID);

        assertThat(name).isEqualTo("ABC.mp3");
    }

    @Test
    @DisplayName("getDownloadFileName null title falls back to song-id")
    void getDownloadFileName_nullTitle_usesFallback() {
        Song song = song(null);
        when(songRepository.findById(SONG_ID)).thenReturn(Optional.of(song));

        String name = service.getDownloadFileName(SONG_ID);

        assertThat(name).isEqualTo("song-2.mp3");
    }

    @Test
    @DisplayName("getDownloadFileName blank title falls back to song-id")
    void getDownloadFileName_blankTitle_usesFallback() {
        Song song = song("   ");
        when(songRepository.findById(SONG_ID)).thenReturn(Optional.of(song));

        String name = service.getDownloadFileName(SONG_ID);

        assertThat(name).isEqualTo("song-2.mp3");
    }

    @Test
    @DisplayName("getDownloadFileName missing song throws ResourceNotFoundException")
    void getDownloadFileName_songMissing_throwsNotFound() {
        when(songRepository.findById(SONG_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getDownloadFileName(SONG_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Song not found: " + SONG_ID);
    }

    private Song song(String title) {
        Song song = new Song();
        song.setSongId(SONG_ID);
        song.setTitle(title);
        song.setFileUrl(FILE_URL);
        return song;
    }
}
