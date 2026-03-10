package com.revplay.musicplatform.playback.dto.request;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class QueueAddRequestTest {

    private static final Long USER_ID = 12L;
    private static final Long SONG_ID = 101L;
    private static final Long EPISODE_ID = 202L;
    private static final String EXACTLY_ONE_MESSAGE = "Exactly one of songId or episodeId must be provided";

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    @DisplayName("valid request with songId passes validation")
    void validSongRequestPassesValidation() {
        QueueAddRequest request = new QueueAddRequest(USER_ID, SONG_ID, null);

        Set<ConstraintViolation<QueueAddRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
        assertThat(request.userId()).isEqualTo(USER_ID);
        assertThat(request.songId()).isEqualTo(SONG_ID);
        assertThat(request.episodeId()).isNull();
    }

    @Test
    @DisplayName("valid request with episodeId passes validation")
    void validEpisodeRequestPassesValidation() {
        QueueAddRequest request = new QueueAddRequest(USER_ID, null, EPISODE_ID);

        Set<ConstraintViolation<QueueAddRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("invalid request with both songId and episodeId null fails class validation")
    void bothContentIdsNullFailsValidation() {
        QueueAddRequest request = new QueueAddRequest(USER_ID, null, null);

        Set<ConstraintViolation<QueueAddRequest>> violations = validator.validate(request);

        assertThat(violations).isNotEmpty();
        assertThat(violations.stream().map(ConstraintViolation::getMessage)).contains(EXACTLY_ONE_MESSAGE);
    }

    @Test
    @DisplayName("invalid request with both songId and episodeId set fails class validation")
    void bothContentIdsSetFailsValidation() {
        QueueAddRequest request = new QueueAddRequest(USER_ID, SONG_ID, EPISODE_ID);

        Set<ConstraintViolation<QueueAddRequest>> violations = validator.validate(request);

        assertThat(violations).isNotEmpty();
        assertThat(violations.stream().map(ConstraintViolation::getMessage)).contains(EXACTLY_ONE_MESSAGE);
    }

    @Test
    @DisplayName("invalid request with null userId fails field validation")
    void nullUserIdFailsValidation() {
        QueueAddRequest request = new QueueAddRequest(null, SONG_ID, null);

        Set<ConstraintViolation<QueueAddRequest>> violations = validator.validate(request);

        assertThat(violations).isNotEmpty();
        assertThat(violations.stream().map(ConstraintViolation::getPropertyPath).map(Object::toString))
                .contains("userId");
    }
}
