package com.revplay.musicplatform.ads.service.impl;

import com.revplay.musicplatform.ads.entity.Ad;
import com.revplay.musicplatform.ads.entity.AdImpression;
import com.revplay.musicplatform.ads.entity.UserAdPlaybackState;
import com.revplay.musicplatform.ads.repository.AdImpressionRepository;
import com.revplay.musicplatform.ads.repository.AdRepository;
import com.revplay.musicplatform.ads.repository.UserAdPlaybackStateRepository;
import com.revplay.musicplatform.exception.BadRequestException;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class AdServiceImplTest {

    private static final Long USER_ID = 10L;
    private static final Long SONG_ID = 100L;

    @Mock
    private AdRepository adRepository;
    @Mock
    private AdImpressionRepository adImpressionRepository;
    @Mock
    private UserAdPlaybackStateRepository userAdPlaybackStateRepository;

    @InjectMocks
    private AdServiceImpl service;

    @Test
    @DisplayName("getNextAd null userId throws BadRequestException")
    void getNextAd_nullUser_throws() {
        assertThatThrownBy(() -> service.getNextAd(null, SONG_ID))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("userId and songId are required");
    }

    @Test
    @DisplayName("getNextAd null songId throws BadRequestException")
    void getNextAd_nullSong_throws() {
        assertThatThrownBy(() -> service.getNextAd(USER_ID, null))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("first song creates state count one and no ad")
    void getNextAd_firstSong_noAd() {
        when(userAdPlaybackStateRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());

        Ad ad = service.getNextAd(USER_ID, SONG_ID);

        assertThat(ad).isNull();
        ArgumentCaptor<UserAdPlaybackState> stateCaptor = ArgumentCaptor.forClass(UserAdPlaybackState.class);
        verify(userAdPlaybackStateRepository).save(stateCaptor.capture());
        assertThat(stateCaptor.getValue().getSongsPlayedCount()).isEqualTo(1);
        verify(adImpressionRepository, never()).save(any(AdImpression.class));
    }

    @Test
    @DisplayName("second song no ad")
    void getNextAd_secondSong_noAd() {
        UserAdPlaybackState state = new UserAdPlaybackState();
        state.setUserId(USER_ID);
        state.setSongsPlayedCount(1);
        when(userAdPlaybackStateRepository.findByUserId(USER_ID)).thenReturn(Optional.of(state));

        Ad ad = service.getNextAd(USER_ID, SONG_ID);

        assertThat(ad).isNull();
        assertThat(state.getSongsPlayedCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("third song returns ad and saves impression")
    void getNextAd_thirdSong_returnsAd() {
        UserAdPlaybackState state = new UserAdPlaybackState();
        state.setUserId(USER_ID);
        state.setSongsPlayedCount(2);
        Ad active = activeAd(1L);
        when(userAdPlaybackStateRepository.findByUserId(USER_ID)).thenReturn(Optional.of(state));
        when(adRepository.findByIsActiveTrueAndStartDateBeforeAndEndDateAfter(any(), any())).thenReturn(List.of(active));

        Ad selected = service.getNextAd(USER_ID, SONG_ID);

        assertThat(selected).isNotNull();
        assertThat(selected.getId()).isEqualTo(1L);
        verify(adImpressionRepository).save(any(AdImpression.class));
    }

    @ParameterizedTest
    @ValueSource(ints = {2, 5, 8, 11})
    @DisplayName("counts leading to 3rd multiple return ad and save impression")
    void getNextAd_everyThirdCall_returnsAd(int existingCount) {
        UserAdPlaybackState state = new UserAdPlaybackState();
        state.setUserId(USER_ID);
        state.setSongsPlayedCount(existingCount);
        Ad active = activeAd(5L);
        when(userAdPlaybackStateRepository.findByUserId(USER_ID)).thenReturn(Optional.of(state));
        when(adRepository.findByIsActiveTrueAndStartDateBeforeAndEndDateAfter(any(), any())).thenReturn(List.of(active));

        Ad selected = service.getNextAd(USER_ID, SONG_ID);

        assertThat(selected).isNotNull();
        verify(adImpressionRepository).save(any(AdImpression.class));
    }

    @Test
    @DisplayName("count three with no active ads returns null and no impression")
    void getNextAd_thirdSong_noActiveAds_returnsNull() {
        UserAdPlaybackState state = new UserAdPlaybackState();
        state.setUserId(USER_ID);
        state.setSongsPlayedCount(2);
        when(userAdPlaybackStateRepository.findByUserId(USER_ID)).thenReturn(Optional.of(state));
        when(adRepository.findByIsActiveTrueAndStartDateBeforeAndEndDateAfter(any(), any())).thenReturn(List.of());

        Ad selected = service.getNextAd(USER_ID, SONG_ID);

        assertThat(selected).isNull();
        verify(adImpressionRepository, never()).save(any(AdImpression.class));
    }

    @Test
    @DisplayName("null songsPlayedCount treated as zero")
    void getNextAd_nullCount_treatedAsZero() {
        UserAdPlaybackState state = new UserAdPlaybackState();
        state.setUserId(USER_ID);
        state.setSongsPlayedCount(null);
        when(userAdPlaybackStateRepository.findByUserId(USER_ID)).thenReturn(Optional.of(state));

        Ad selected = service.getNextAd(USER_ID, SONG_ID);

        assertThat(selected).isNull();
        assertThat(state.getSongsPlayedCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("impression fields are populated correctly")
    void getNextAd_impressionFields_verified() {
        UserAdPlaybackState state = new UserAdPlaybackState();
        state.setUserId(USER_ID);
        state.setSongsPlayedCount(2);
        Ad active = activeAd(7L);
        when(userAdPlaybackStateRepository.findByUserId(USER_ID)).thenReturn(Optional.of(state));
        when(adRepository.findByIsActiveTrueAndStartDateBeforeAndEndDateAfter(any(), any())).thenReturn(List.of(active));

        service.getNextAd(USER_ID, SONG_ID);

        ArgumentCaptor<AdImpression> captor = ArgumentCaptor.forClass(AdImpression.class);
        verify(adImpressionRepository).save(captor.capture());
        AdImpression impression = captor.getValue();
        assertThat(impression.getAdId()).isEqualTo(7L);
        assertThat(impression.getUserId()).isEqualTo(USER_ID);
        assertThat(impression.getSongId()).isEqualTo(SONG_ID);
        assertThat(impression.getPlayedAt()).isNotNull();
    }

    @Test
    @DisplayName("existing count two increments to three and shows ad")
    void getNextAd_countTwo_toThree_showsAd() {
        UserAdPlaybackState state = new UserAdPlaybackState();
        state.setUserId(USER_ID);
        state.setSongsPlayedCount(2);
        when(userAdPlaybackStateRepository.findByUserId(USER_ID)).thenReturn(Optional.of(state));
        when(adRepository.findByIsActiveTrueAndStartDateBeforeAndEndDateAfter(any(), any())).thenReturn(List.of(activeAd(9L)));

        Ad selected = service.getNextAd(USER_ID, SONG_ID);

        assertThat(state.getSongsPlayedCount()).isEqualTo(3);
        assertThat(selected).isNotNull();
    }

    private Ad activeAd(Long id) {
        Ad ad = new Ad();
        ad.setId(id);
        ad.setTitle("Ad-" + id);
        ad.setIsActive(true);
        ad.setStartDate(LocalDateTime.now().minusDays(1));
        ad.setEndDate(LocalDateTime.now().plusDays(1));
        ad.setDurationSeconds(15);
        ad.setMediaUrl("/ads/a.mp3");
        return ad;
    }
}
