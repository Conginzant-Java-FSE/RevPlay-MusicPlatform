package com.revplay.musicplatform.artist.integration;

import com.revplay.musicplatform.artist.dto.request.ArtistCreateRequest;
import com.revplay.musicplatform.artist.dto.request.ArtistUpdateRequest;
import com.revplay.musicplatform.artist.dto.request.ArtistVerifyRequest;
import com.revplay.musicplatform.artist.dto.response.ArtistResponse;
import com.revplay.musicplatform.artist.enums.ArtistType;
import com.revplay.musicplatform.artist.repository.ArtistRepository;
import com.revplay.musicplatform.artist.service.ArtistService;
import com.revplay.musicplatform.common.MockSecurityContextHelper;
import com.revplay.musicplatform.exception.ConflictException;
import com.revplay.musicplatform.exception.UnauthorizedException;
import com.revplay.musicplatform.user.enums.UserRole;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Tag("integration")
@SpringBootTest
@ActiveProfiles("test")
class ArtistLifecycleIntegrationTest {

    private static final Long ARTIST_USER_ID = 1001L;
    private static final Long LISTENER_ID = 1002L;
    private static final Long ADMIN_ID = 1L;

    private final ArtistService artistService;
    private final ArtistRepository artistRepository;

    @Autowired
    ArtistLifecycleIntegrationTest(ArtistService artistService, ArtistRepository artistRepository) {
        this.artistService = artistService;
        this.artistRepository = artistRepository;
    }

    @BeforeEach
    void setUp() {
        artistRepository.deleteAll();
        MockSecurityContextHelper.clear();
    }

    @AfterEach
    void tearDown() {
        MockSecurityContextHelper.clear();
    }

    @Test
    @DisplayName("artist create verify and update lifecycle succeeds")
    void lifecycle_createVerifyUpdate() {
        MockSecurityContextHelper.mockUser(ARTIST_USER_ID, "artist", UserRole.ARTIST);
        ArtistResponse created = artistService.createArtist(createRequest("Artist One"));
        assertThat(created.getArtistId()).isNotNull();
        assertThat(created.getVerified()).isFalse();

        MockSecurityContextHelper.mockUser(ADMIN_ID, "admin", UserRole.ADMIN);
        ArtistVerifyRequest verifyRequest = new ArtistVerifyRequest();
        verifyRequest.setVerified(true);
        ArtistResponse verified = artistService.verifyArtist(created.getArtistId(), verifyRequest);
        assertThat(verified.getVerified()).isTrue();

        MockSecurityContextHelper.mockUser(ARTIST_USER_ID, "artist", UserRole.ARTIST);
        ArtistUpdateRequest updateRequest = updateRequest("Artist One Updated");
        ArtistResponse updated = artistService.updateArtist(created.getArtistId(), updateRequest);
        assertThat(updated.getDisplayName()).isEqualTo("Artist One Updated");
    }

    @Test
    @DisplayName("creating second profile for same artist user throws conflict")
    void createSecondProfile_conflict() {
        MockSecurityContextHelper.mockUser(ARTIST_USER_ID, "artist", UserRole.ARTIST);
        artistService.createArtist(createRequest("First"));

        assertThatThrownBy(() -> artistService.createArtist(createRequest("Second")))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    @DisplayName("listener cannot verify artist")
    void listenerCannotVerify_forbidden() {
        MockSecurityContextHelper.mockUser(ARTIST_USER_ID, "artist", UserRole.ARTIST);
        ArtistResponse created = artistService.createArtist(createRequest("Verify Target"));

        MockSecurityContextHelper.mockUser(LISTENER_ID, "listener", UserRole.LISTENER);
        ArtistVerifyRequest verifyRequest = new ArtistVerifyRequest();
        verifyRequest.setVerified(true);

        assertThatThrownBy(() -> artistService.verifyArtist(created.getArtistId(), verifyRequest))
                .isInstanceOf(UnauthorizedException.class);
    }

    private ArtistCreateRequest createRequest(String name) {
        ArtistCreateRequest request = new ArtistCreateRequest();
        request.setDisplayName(name);
        request.setBio("bio");
        request.setArtistType(ArtistType.MUSIC);
        return request;
    }

    private ArtistUpdateRequest updateRequest(String name) {
        ArtistUpdateRequest request = new ArtistUpdateRequest();
        request.setDisplayName(name);
        request.setBio("updated");
        request.setArtistType(ArtistType.MUSIC);
        return request;
    }
}
