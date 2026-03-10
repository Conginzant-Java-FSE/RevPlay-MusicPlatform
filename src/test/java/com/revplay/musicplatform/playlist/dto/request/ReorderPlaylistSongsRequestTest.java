package com.revplay.musicplatform.playlist.dto.request;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class ReorderPlaylistSongsRequestTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    @DisplayName("valid request passes validation")
    void validRequestPassesValidation() {
        SongPositionRequest songPosition = new SongPositionRequest();
        songPosition.setSongId(10L);
        songPosition.setPosition(1);

        ReorderPlaylistSongsRequest request = new ReorderPlaylistSongsRequest();
        request.setSongs(List.of(songPosition));

        Set<ConstraintViolation<ReorderPlaylistSongsRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("empty songs list fails validation")
    void emptySongsListFailsValidation() {
        ReorderPlaylistSongsRequest request = new ReorderPlaylistSongsRequest();
        request.setSongs(List.of());

        Set<ConstraintViolation<ReorderPlaylistSongsRequest>> violations = validator.validate(request);

        assertThat(violations).isNotEmpty();
        assertThat(violations.stream().map(v -> v.getPropertyPath().toString())).contains("songs");
    }
}
