package com.revplay.musicplatform.artist.contoller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.revplay.musicplatform.artist.dto.request.ArtistCreateRequest;
import com.revplay.musicplatform.artist.dto.request.ArtistUpdateRequest;
import com.revplay.musicplatform.artist.dto.request.ArtistVerifyRequest;
import com.revplay.musicplatform.artist.dto.response.ArtistResponse;
import com.revplay.musicplatform.artist.enums.ArtistType;
import com.revplay.musicplatform.artist.service.ArtistService;
import com.revplay.musicplatform.config.FileStorageProperties;
import com.revplay.musicplatform.exception.AccessDeniedException;
import com.revplay.musicplatform.exception.ConflictException;
import com.revplay.musicplatform.exception.ResourceNotFoundException;
import com.revplay.musicplatform.security.AuthenticatedUserPrincipal;
import com.revplay.musicplatform.security.SecurityConfig;
import com.revplay.musicplatform.security.service.JwtService;
import com.revplay.musicplatform.security.service.TokenRevocationService;
import com.revplay.musicplatform.user.enums.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("unit")
@WebMvcTest(ArtistController.class)
@Import(SecurityConfig.class)
class ArtistControllerTest {

    private static final Long ARTIST_ID = 7L;
    private static final String BASE = "/api/v1/artists";

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;

    @MockBean
    private ArtistService artistService;
    @MockBean
    private JwtService jwtService;
    @MockBean
    private TokenRevocationService tokenRevocationService;
    @MockBean
    private FileStorageProperties fileStorageProperties;
    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Autowired
    ArtistControllerTest(MockMvc mockMvc, ObjectMapper objectMapper) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
    }

    @Test
    @DisplayName("POST artists with ARTIST auth returns 201")
    void create_artistAuth_created() throws Exception {
        when(artistService.createArtist(any())).thenReturn(response());

        mockMvc.perform(post(BASE)
                        .with(authentication(auth(UserRole.ARTIST)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.artistId").value(ARTIST_ID));
    }

    @Test
    @DisplayName("POST artists listener forbidden when service rejects")
    void create_listener_forbidden() throws Exception {
        when(artistService.createArtist(any())).thenThrow(new AccessDeniedException("Artists or admins only"));

        mockMvc.perform(post(BASE)
                        .with(authentication(auth(UserRole.LISTENER)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest())))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST artists duplicate maps to 409")
    void create_duplicate_conflict() throws Exception {
        when(artistService.createArtist(any())).thenThrow(new ConflictException("Artist profile already exists"));

        mockMvc.perform(post(BASE)
                        .with(authentication(auth(UserRole.ARTIST)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest())))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("GET artist by id authenticated returns 200")
    void get_byId_ok() throws Exception {
        when(artistService.getArtist(ARTIST_ID)).thenReturn(response());

        mockMvc.perform(get(BASE + "/{id}", ARTIST_ID).with(authentication(auth(UserRole.LISTENER))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.artistId").value(ARTIST_ID));
    }

    @Test
    @DisplayName("GET artist not found returns 404")
    void get_notFound_404() throws Exception {
        when(artistService.getArtist(ARTIST_ID)).thenThrow(new ResourceNotFoundException("Artist not found"));

        mockMvc.perform(get(BASE + "/{id}", ARTIST_ID).with(authentication(auth(UserRole.LISTENER))))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT artist owner update returns 200")
    void update_owner_ok() throws Exception {
        when(artistService.updateArtist(eq(ARTIST_ID), any())).thenReturn(response());

        mockMvc.perform(put(BASE + "/{id}", ARTIST_ID)
                        .with(authentication(auth(UserRole.ARTIST)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.artistId").value(ARTIST_ID));
    }

    @Test
    @DisplayName("PUT artist non owner maps to 404")
    void update_nonOwner_notFound() throws Exception {
        when(artistService.updateArtist(eq(ARTIST_ID), any())).thenThrow(new ResourceNotFoundException("Artist not found"));

        mockMvc.perform(put(BASE + "/{id}", ARTIST_ID)
                        .with(authentication(auth(UserRole.ARTIST)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest())))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PATCH verify with ADMIN returns 200")
    void verify_admin_ok() throws Exception {
        when(artistService.verifyArtist(eq(ARTIST_ID), any())).thenReturn(response());

        ArtistVerifyRequest verifyRequest = new ArtistVerifyRequest();
        verifyRequest.setVerified(true);

        mockMvc.perform(patch(BASE + "/{id}/verify", ARTIST_ID)
                        .with(authentication(auth(UserRole.ADMIN)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.artistId").value(ARTIST_ID));
    }

    @Test
    @DisplayName("PATCH verify with ARTIST maps to 403")
    void verify_artist_forbidden() throws Exception {
        when(artistService.verifyArtist(eq(ARTIST_ID), any())).thenThrow(new AccessDeniedException("Admin only"));
        ArtistVerifyRequest verifyRequest = new ArtistVerifyRequest();
        verifyRequest.setVerified(true);

        mockMvc.perform(patch(BASE + "/{id}/verify", ARTIST_ID)
                        .with(authentication(auth(UserRole.ARTIST)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyRequest)))
                .andExpect(status().isForbidden());
    }

    private ArtistCreateRequest createRequest() {
        ArtistCreateRequest request = new ArtistCreateRequest();
        request.setDisplayName("Rev Artist");
        request.setBio("bio");
        request.setArtistType(ArtistType.MUSIC);
        return request;
    }

    private ArtistUpdateRequest updateRequest() {
        ArtistUpdateRequest request = new ArtistUpdateRequest();
        request.setDisplayName("Rev Artist Updated");
        request.setBio("updated");
        request.setArtistType(ArtistType.MUSIC);
        return request;
    }

    private ArtistResponse response() {
        ArtistResponse response = new ArtistResponse();
        response.setArtistId(ARTIST_ID);
        response.setUserId(1L);
        response.setDisplayName("Rev Artist");
        response.setArtistType(ArtistType.MUSIC);
        response.setVerified(false);
        return response;
    }

    private UsernamePasswordAuthenticationToken auth(UserRole role) {
        AuthenticatedUserPrincipal principal = new AuthenticatedUserPrincipal(1L, "user", role);
        return new UsernamePasswordAuthenticationToken(
                principal,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + role.name()))
        );
    }
}
