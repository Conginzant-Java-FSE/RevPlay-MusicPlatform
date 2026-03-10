package com.revplay.musicplatform.download.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class SongDownloadTest {

    private static final Long ID = 1L;
    private static final Long USER_ID = 10L;
    private static final Long SONG_ID = 20L;

    @Test
    @DisplayName("setters and getters persist values")
    void settersAndGettersWork() {
        SongDownload songDownload = new SongDownload();
        LocalDateTime downloadedAt = LocalDateTime.of(2026, 3, 9, 4, 0, 0);

        songDownload.setId(ID);
        songDownload.setUserId(USER_ID);
        songDownload.setSongId(SONG_ID);
        songDownload.setDownloadedAt(downloadedAt);

        assertThat(songDownload.getId()).isEqualTo(ID);
        assertThat(songDownload.getUserId()).isEqualTo(USER_ID);
        assertThat(songDownload.getSongId()).isEqualTo(SONG_ID);
        assertThat(songDownload.getDownloadedAt()).isEqualTo(downloadedAt);
    }

    @Test
    @DisplayName("prePersist sets downloadedAt when null")
    void prePersistSetsDownloadedAtWhenNull() {
        SongDownload songDownload = new SongDownload();

        songDownload.prePersist();

        assertThat(songDownload.getDownloadedAt()).isNotNull();
    }

    @Test
    @DisplayName("prePersist keeps existing downloadedAt")
    void prePersistKeepsExistingDownloadedAt() {
        SongDownload songDownload = new SongDownload();
        LocalDateTime existing = LocalDateTime.of(2026, 3, 9, 5, 0, 0);
        songDownload.setDownloadedAt(existing);

        songDownload.prePersist();

        assertThat(songDownload.getDownloadedAt()).isEqualTo(existing);
    }
}
