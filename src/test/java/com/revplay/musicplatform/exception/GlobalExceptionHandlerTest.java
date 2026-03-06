package com.revplay.musicplatform.exception;

import com.revplay.musicplatform.catalog.exception.DiscoveryNotFoundException;
import com.revplay.musicplatform.catalog.exception.DiscoveryValidationException;
import com.revplay.musicplatform.common.response.ApiResponse;
import com.revplay.musicplatform.playback.exception.PlaybackNotFoundException;
import com.revplay.musicplatform.playback.exception.PlaybackValidationException;
import com.revplay.musicplatform.user.exception.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("unit")
class GlobalExceptionHandlerTest {

    private static final String MESSAGE = "msg";

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("handleNotFound returns 404 with expected body")
    void handleNotFound_returns404() {
        assertError(handler.handleNotFound(new ResourceNotFoundException(MESSAGE)), HttpStatus.NOT_FOUND, MESSAGE);
    }

    @Test
    @DisplayName("handleUnauthorized returns 401")
    void handleUnauthorized_returns401() {
        assertError(handler.handleUnauthorized(new UnauthorizedException(MESSAGE)), HttpStatus.UNAUTHORIZED, MESSAGE);
    }

    @Test
    @DisplayName("handleBadRequest returns 400")
    void handleBadRequest_returns400() {
        assertError(handler.handleBadRequest(new BadRequestException(MESSAGE)), HttpStatus.BAD_REQUEST, MESSAGE);
    }

    @Test
    @DisplayName("handleForbidden supports AccessDeniedException and AuthForbiddenException")
    void handleForbidden_returns403() {
        assertError(handler.handleForbidden(new AccessDeniedException(MESSAGE)), HttpStatus.FORBIDDEN, MESSAGE);
        assertError(handler.handleForbidden(new AuthForbiddenException(MESSAGE)), HttpStatus.FORBIDDEN, MESSAGE);
    }

    @Test
    @DisplayName("handleDuplicate maps duplicate and integrity exceptions to 409")
    void handleDuplicate_returns409() {
        assertError(handler.handleDuplicate(new DuplicateResourceException(MESSAGE)), HttpStatus.CONFLICT, MESSAGE);
        assertError(handler.handleDuplicate(new AuthConflictException(MESSAGE)), HttpStatus.CONFLICT, MESSAGE);
        assertError(handler.handleDuplicate(new DataIntegrityViolationException(MESSAGE)), HttpStatus.CONFLICT, MESSAGE);
    }

    @Test
    @DisplayName("handleDomainNotFound maps domain exceptions to 404")
    void handleDomainNotFound_returns404() {
        assertError(handler.handleDomainNotFound(new PlaybackNotFoundException(MESSAGE)), HttpStatus.NOT_FOUND, MESSAGE);
        assertError(handler.handleDomainNotFound(new DiscoveryNotFoundException(MESSAGE)), HttpStatus.NOT_FOUND, MESSAGE);
        assertError(handler.handleDomainNotFound(new AuthNotFoundException(MESSAGE)), HttpStatus.NOT_FOUND, MESSAGE);
    }

    @Test
    @DisplayName("handleAuthUnauthorized returns 401")
    void handleAuthUnauthorized_returns401() {
        assertError(handler.handleAuthUnauthorized(new AuthUnauthorizedException(MESSAGE)), HttpStatus.UNAUTHORIZED, MESSAGE);
    }

    @Test
    @DisplayName("handleConflict returns 409")
    void handleConflict_returns409() {
        assertError(handler.handleConflict(new ConflictException(MESSAGE)), HttpStatus.CONFLICT, MESSAGE);
    }

    @Test
    @DisplayName("handleRequestValidation maps listed exceptions to 400")
    void handleRequestValidation_returns400() throws Exception {
        assertError(handler.handleRequestValidation(new PlaybackValidationException(MESSAGE)), HttpStatus.BAD_REQUEST, MESSAGE);
        assertError(handler.handleRequestValidation(new DiscoveryValidationException(MESSAGE)), HttpStatus.BAD_REQUEST, MESSAGE);
        assertError(handler.handleRequestValidation(new AuthValidationException(MESSAGE)), HttpStatus.BAD_REQUEST, MESSAGE);
        assertError(handler.handleRequestValidation(new IllegalArgumentException(MESSAGE)), HttpStatus.BAD_REQUEST, MESSAGE);
        assertError(handler.handleRequestValidation(new MissingServletRequestPartException("file")), HttpStatus.BAD_REQUEST, "Required part 'file' is not present.");
        assertError(handler.handleRequestValidation(new MissingServletRequestParameterException("q", "String")), HttpStatus.BAD_REQUEST, "Required request parameter 'q' for method parameter type String is not present");
        assertError(handler.handleRequestValidation(new HttpMediaTypeNotSupportedException("application/xml")), HttpStatus.BAD_REQUEST, "application/xml");
    }

    @Test
    @DisplayName("handleNoResource returns 404")
    void handleNoResource_returns404() {
        NoResourceFoundException ex = new NoResourceFoundException(HttpMethod.GET, "/missing");

        ResponseEntity<ApiResponse<Void>> response = handler.handleNoResource(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getMessage()).contains("No static resource");
        assertThat(response.getBody().getTimestamp()).isNotNull();
        assertThat(response.getBody().getData()).isNull();
    }

    @Test
    @DisplayName("handleValidation with MethodArgumentNotValidException returns field errors")
    void handleValidation_methodArgumentNotValid_returnsErrors() throws Exception {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new DummyRequest(), "dummyRequest");
        bindingResult.addError(new FieldError("dummyRequest", "field", "must not be blank"));
        Method method = DummyController.class.getDeclaredMethod("accept", DummyRequest.class);
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(
                new org.springframework.core.MethodParameter(method, 0),
                bindingResult
        );

        ResponseEntity<ApiResponse<Void>> response = handler.handleValidation(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Validation failed");
        assertThat(response.getBody().getErrors()).isNotEmpty();
        assertThat(response.getBody().getErrors().get(0).getField()).isEqualTo("field");
        assertThat(response.getBody().getErrors().get(0).getReason()).isEqualTo("must not be blank");
    }

    @Test
    @DisplayName("handleValidation with BindException returns field errors")
    void handleValidation_bindException_returnsErrors() {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new DummyRequest(), "dummyRequest");
        bindingResult.addError(new FieldError("dummyRequest", "field", "invalid"));
        BindException bindException = new BindException(bindingResult);

        ResponseEntity<ApiResponse<Void>> response = handler.handleValidation(bindException);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrors()).isNotEmpty();
        assertThat(response.getBody().getErrors().get(0).getField()).isEqualTo("field");
    }

    @Test
    @DisplayName("handleGeneric returns 500 with unexpected error message")
    void handleGeneric_returns500() {
        assertError(handler.handleGeneric(new RuntimeException("boom")), HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error");
    }

    @Test
    @DisplayName("handleNotFound with null message does not throw and returns 404")
    void handleNotFound_nullMessage_safe() {
        ResponseEntity<ApiResponse<Void>> response = handler.handleNotFound(new ResourceNotFoundException((String) null));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getTimestamp()).isNotNull();
    }

    private void assertError(ResponseEntity<ApiResponse<Void>> response, HttpStatus status, String message) {
        assertThat(response.getStatusCode()).isEqualTo(status);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getMessage()).isEqualTo(message);
        assertThat(response.getBody().getTimestamp()).isNotNull();
        assertThat(response.getBody().getData()).isNull();
    }

    private static final class DummyRequest {
    }

    private static final class DummyController {
        @SuppressWarnings("unused")
        private void accept(DummyRequest request) {
        }
    }
}
