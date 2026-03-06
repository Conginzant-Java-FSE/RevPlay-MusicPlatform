package com.revplay.musicplatform.ads.service.impl;



import com.revplay.musicplatform.ads.entity.Ad;
import com.revplay.musicplatform.ads.repository.AdRepository;
import com.revplay.musicplatform.config.FileStorageProperties;
import com.revplay.musicplatform.exception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class AdminAdServiceImplTest {

    @Mock
    private AdRepository adRepository;
    @Mock
    private FileStorageProperties fileStorageProperties;

    @InjectMocks
    private AdminAdServiceImpl service;

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("uploadAd stores file and saves active ad")
    void uploadAd_success() throws IOException {
        when(fileStorageProperties.getBaseDir()).thenReturn(tempDir.toString());
        when(fileStorageProperties.getAdsDir()).thenReturn("ads");
        MultipartFile file = new MockMultipartFile("file", "ad.mp3", "audio/mpeg", "abc".getBytes());
        when(adRepository.save(any(Ad.class))).thenAnswer(inv -> inv.getArgument(0));

        Ad saved = service.uploadAd("Title", file, 15);

        assertThat(saved.getTitle()).isEqualTo("Title");
        assertThat(saved.getIsActive()).isTrue();
        assertThat(saved.getMediaUrl()).contains("/ads/");
        Path adsPath = tempDir.resolve("ads");
        assertThat(Files.exists(adsPath)).isTrue();
    }

    @Test
    @DisplayName("deactivateAd toggles isActive false")
    void deactivateAd_success() {
        Ad ad = ad(1L, true);
        when(adRepository.findById(1L)).thenReturn(Optional.of(ad));
        when(adRepository.save(ad)).thenReturn(ad);

        Ad updated = service.deactivateAd(1L);

        assertThat(updated.getIsActive()).isFalse();
        verify(adRepository).save(ad);
    }

    @Test
    @DisplayName("activateAd toggles isActive true")
    void activateAd_success() {
        Ad ad = ad(1L, false);
        when(adRepository.findById(1L)).thenReturn(Optional.of(ad));
        when(adRepository.save(ad)).thenReturn(ad);

        Ad updated = service.activateAd(1L);

        assertThat(updated.getIsActive()).isTrue();
        verify(adRepository).save(ad);
    }

    @Test
    @DisplayName("activateAd not found throws ResourceNotFoundException")
    void activateAd_notFound_throws() {
        when(adRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.activateAd(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private Ad ad(Long id, boolean active) {
        Ad ad = new Ad();
        ad.setId(id);
        ad.setTitle("Ad");
        ad.setMediaUrl("/x.mp3");
        ad.setDurationSeconds(10);
        ad.setStartDate(LocalDateTime.now().minusDays(1));
        ad.setEndDate(LocalDateTime.now().plusDays(1));
        ad.setIsActive(active);
        return ad;
    }
}

