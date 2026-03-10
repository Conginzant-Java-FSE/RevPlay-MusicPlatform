package com.revplay.musicplatform.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class FileStoragePropertiesTest {

    private static final String BASE_DIR = "/tmp/revplay";
    private static final String SONGS_DIR = "songs";
    private static final String PODCASTS_DIR = "podcasts";
    private static final String IMAGES_DIR = "images";
    private static final String ADS_DIR = "ads";

    @Test
    @DisplayName("setters and getters store configured upload directories")
    void propertiesStoreConfiguredValues() {
        FileStorageProperties properties = new FileStorageProperties();

        properties.setBaseDir(BASE_DIR);
        properties.setSongsDir(SONGS_DIR);
        properties.setPodcastsDir(PODCASTS_DIR);
        properties.setImagesDir(IMAGES_DIR);
        properties.setAdsDir(ADS_DIR);

        assertThat(properties.getBaseDir()).isEqualTo(BASE_DIR);
        assertThat(properties.getSongsDir()).isEqualTo(SONGS_DIR);
        assertThat(properties.getPodcastsDir()).isEqualTo(PODCASTS_DIR);
        assertThat(properties.getImagesDir()).isEqualTo(IMAGES_DIR);
        assertThat(properties.getAdsDir()).isEqualTo(ADS_DIR);
    }
}
