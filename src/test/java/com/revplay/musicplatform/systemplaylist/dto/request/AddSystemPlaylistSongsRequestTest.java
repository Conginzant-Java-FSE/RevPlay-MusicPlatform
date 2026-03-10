package com.revplay.musicplatform.systemplaylist.dto.request;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class AddSystemPlaylistSongsRequestTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    @DisplayName("valid request passes validation")
    void validRequestPassesValidation() {
        AddSystemPlaylistSongsRequest request = new AddSystemPlaylistSongsRequest(List.of(1L, 2L));

        Set<ConstraintViolation<AddSystemPlaylistSongsRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
        assertThat(request.songIds()).containsExactly(1L, 2L);
    }

    @Test
    @DisplayName("empty songIds fails validation")
    void emptySongIdsFailsValidation() {
        AddSystemPlaylistSongsRequest request = new AddSystemPlaylistSongsRequest(List.of());

        Set<ConstraintViolation<AddSystemPlaylistSongsRequest>> violations = validator.validate(request);

        assertThat(violations).isNotEmpty();
        assertThat(violations.stream().map(v -> v.getPropertyPath().toString())).contains("songIds");
    }

    @Test
    @DisplayName("null song id entry fails validation")
    void nullSongIdEntryFailsValidation() {
        AddSystemPlaylistSongsRequest request = new AddSystemPlaylistSongsRequest(Arrays.asList(1L, null));

        Set<ConstraintViolation<AddSystemPlaylistSongsRequest>> violations = validator.validate(request);

        assertThat(violations).isNotEmpty();
    }
}
