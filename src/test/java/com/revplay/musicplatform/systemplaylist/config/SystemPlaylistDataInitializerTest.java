package com.revplay.musicplatform.systemplaylist.config;

import com.revplay.musicplatform.systemplaylist.entity.SystemPlaylist;
import com.revplay.musicplatform.systemplaylist.repository.SystemPlaylistRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class SystemPlaylistDataInitializerTest {

    private static final long EMPTY_COUNT = 0L;

    @Mock
    private SystemPlaylistRepository systemPlaylistRepository;

    @InjectMocks
    private SystemPlaylistDataInitializer initializer;

    @Test
    @DisplayName("first run creates default playlists with expected slugs")
    void run_firstTime_seedsDefaultPlaylists() throws Exception {
        when(systemPlaylistRepository.count()).thenReturn(EMPTY_COUNT);
        when(systemPlaylistRepository.findBySlugAndDeletedAtIsNull(any())).thenReturn(Optional.empty());

        initializer.run();

        ArgumentCaptor<SystemPlaylist> captor = ArgumentCaptor.forClass(SystemPlaylist.class);
        verify(systemPlaylistRepository, times(5)).save(captor.capture());

        List<String> slugs = captor.getAllValues().stream().map(SystemPlaylist::getSlug).toList();
        assertThat(slugs).containsExactlyInAnyOrder(
                "telugu-mix",
                "tamil-mix",
                "hindi-mix",
                "english-mix",
                "dj-mix"
        );
    }

    @Test
    @DisplayName("second run with existing rows creates no duplicates")
    void run_secondTime_noDuplicatesCreated() throws Exception {
        when(systemPlaylistRepository.count()).thenReturn(1L);

        initializer.run();

        verify(systemPlaylistRepository, never()).save(any(SystemPlaylist.class));
    }
}
