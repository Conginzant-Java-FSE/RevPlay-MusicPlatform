package com.revplay.musicplatform.playlist.dto.request;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class AddSongToPlaylistRequestTest {

    private static final Long SONG_ID = 123L;

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    @DisplayName("valid request passes validation")
    void validRequestPassesValidation() {
        AddSongToPlaylistRequest request = new AddSongToPlaylistRequest();
        request.setSongId(SONG_ID);
        request.setPosition(1);

        Set<ConstraintViolation<AddSongToPlaylistRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("non positive song id fails validation")
    void nonPositiveSongIdFailsValidation() {
        AddSongToPlaylistRequest request = new AddSongToPlaylistRequest();
        request.setSongId(0L);

        Set<ConstraintViolation<AddSongToPlaylistRequest>> violations = validator.validate(request);

        assertThat(violations).isNotEmpty();
        assertThat(violations.stream().map(v -> v.getPropertyPath().toString())).contains("songId");
    }
}
