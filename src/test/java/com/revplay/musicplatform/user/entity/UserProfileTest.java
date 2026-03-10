package com.revplay.musicplatform.user.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class UserProfileTest {

    @Test
    @DisplayName("setters and getters store all profile fields")
    void settersAndGettersStoreFields() {
        UserProfile profile = new UserProfile();
        Instant now = Instant.now();

        profile.setProfileId(1L);
        profile.setUserId(2L);
        profile.setFullName("Full Name");
        profile.setBio("bio");
        profile.setProfilePictureUrl("/img.png");
        profile.setCountry("IN");
        profile.setCreatedAt(now);
        profile.setUpdatedAt(now);
        profile.setVersion(3L);

        assertThat(profile.getProfileId()).isEqualTo(1L);
        assertThat(profile.getUserId()).isEqualTo(2L);
        assertThat(profile.getFullName()).isEqualTo("Full Name");
        assertThat(profile.getCountry()).isEqualTo("IN");
    }
}
