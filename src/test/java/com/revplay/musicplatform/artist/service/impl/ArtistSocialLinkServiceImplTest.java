package com.revplay.musicplatform.artist.service.impl;

import com.revplay.musicplatform.artist.dto.request.ArtistSocialLinkCreateRequest;
import com.revplay.musicplatform.artist.dto.request.ArtistSocialLinkUpdateRequest;
import com.revplay.musicplatform.artist.dto.response.ArtistSocialLinkResponse;
import com.revplay.musicplatform.artist.entity.Artist;
import com.revplay.musicplatform.artist.entity.ArtistSocialLink;
import com.revplay.musicplatform.artist.mapper.ArtistSocialLinkMapper;
import com.revplay.musicplatform.artist.repository.ArtistRepository;
import com.revplay.musicplatform.artist.repository.ArtistSocialLinkRepository;
import com.revplay.musicplatform.catalog.enums.SocialPlatform;
import com.revplay.musicplatform.catalog.util.AccessValidator;
import com.revplay.musicplatform.catalog.util.SecurityUtil;
import com.revplay.musicplatform.exception.ConflictException;
import com.revplay.musicplatform.exception.ResourceNotFoundException;
import com.revplay.musicplatform.user.enums.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class ArtistSocialLinkServiceImplTest {

    private static final Long ARTIST_ID = 15L;
    private static final Long LINK_ID = 77L;
    private static final Long USER_ID = 44L;

    @Mock
    private ArtistSocialLinkRepository repository;
    @Mock
    private ArtistRepository artistRepository;
    @Mock
    private ArtistSocialLinkMapper mapper;
    @Mock
    private SecurityUtil securityUtil;
    @Mock
    private AccessValidator accessValidator;

    @InjectMocks
    private ArtistSocialLinkServiceImpl service;

    @Test
    @DisplayName("create social link saves platform and url")
    void create_saves() {
        ArtistSocialLinkCreateRequest request = new ArtistSocialLinkCreateRequest();
        request.setPlatform(SocialPlatform.INSTAGRAM);
        request.setUrl("https://instagram.com/rev");
        ArtistSocialLink entity = link(LINK_ID, ARTIST_ID, SocialPlatform.INSTAGRAM, request.getUrl());
        ArtistSocialLinkResponse response = response(LINK_ID, SocialPlatform.INSTAGRAM, request.getUrl());

        mockOwnerAccess();
        when(repository.existsByArtistIdAndPlatform(ARTIST_ID, SocialPlatform.INSTAGRAM)).thenReturn(false);
        when(mapper.toEntity(request, ARTIST_ID)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toResponse(entity)).thenReturn(response);

        ArtistSocialLinkResponse actual = service.create(ARTIST_ID, request);

        assertThat(actual.getLinkId()).isEqualTo(LINK_ID);
        assertThat(actual.getPlatform()).isEqualTo(SocialPlatform.INSTAGRAM);
    }

    @Test
    @DisplayName("create duplicate platform throws ConflictException")
    void create_duplicatePlatform_throws() {
        ArtistSocialLinkCreateRequest request = new ArtistSocialLinkCreateRequest();
        request.setPlatform(SocialPlatform.TWITTER);
        request.setUrl("https://x.com/rev");

        mockOwnerAccess();
        when(repository.existsByArtistIdAndPlatform(ARTIST_ID, SocialPlatform.TWITTER)).thenReturn(true);

        assertThatThrownBy(() -> service.create(ARTIST_ID, request))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    @DisplayName("update social link updates url")
    void update_updatesUrl() {
        ArtistSocialLinkUpdateRequest request = new ArtistSocialLinkUpdateRequest();
        request.setPlatform(SocialPlatform.YOUTUBE);
        request.setUrl("https://youtube.com/new");
        ArtistSocialLink entity = link(LINK_ID, ARTIST_ID, SocialPlatform.YOUTUBE, "old");
        ArtistSocialLinkResponse response = response(LINK_ID, SocialPlatform.YOUTUBE, request.getUrl());

        mockOwnerAccess();
        when(repository.findById(LINK_ID)).thenReturn(Optional.of(entity));
        when(repository.existsByArtistIdAndPlatformAndLinkIdNot(ARTIST_ID, SocialPlatform.YOUTUBE, LINK_ID)).thenReturn(false);
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toResponse(entity)).thenReturn(response);

        ArtistSocialLinkResponse actual = service.update(ARTIST_ID, LINK_ID, request);

        assertThat(actual.getUrl()).isEqualTo(request.getUrl());
        verify(mapper).updateEntity(entity, request);
    }

    @Test
    @DisplayName("delete social link removes entity")
    void delete_removes() {
        ArtistSocialLink entity = link(LINK_ID, ARTIST_ID, SocialPlatform.SPOTIFY, "https://s");

        mockOwnerAccess();
        when(repository.findById(LINK_ID)).thenReturn(Optional.of(entity));

        service.delete(ARTIST_ID, LINK_ID);

        verify(repository).delete(entity);
    }

    @Test
    @DisplayName("list returns all links for artist")
    void list_returnsLinks() {
        ArtistSocialLink first = link(1L, ARTIST_ID, SocialPlatform.INSTAGRAM, "a");
        ArtistSocialLink second = link(2L, ARTIST_ID, SocialPlatform.YOUTUBE, "b");
        when(repository.findByArtistId(ARTIST_ID)).thenReturn(List.of(first, second));
        when(mapper.toResponse(first)).thenReturn(response(1L, SocialPlatform.INSTAGRAM, "a"));
        when(mapper.toResponse(second)).thenReturn(response(2L, SocialPlatform.YOUTUBE, "b"));

        List<ArtistSocialLinkResponse> responses = service.list(ARTIST_ID);

        assertThat(responses).hasSize(2);
    }

    @Test
    @DisplayName("operations on missing artist throw ResourceNotFoundException")
    void operations_missingArtist_throw() {
        when(securityUtil.getUserRole()).thenReturn(UserRole.ARTIST.name());
        when(artistRepository.findById(ARTIST_ID)).thenReturn(Optional.empty());

        ArtistSocialLinkCreateRequest createRequest = new ArtistSocialLinkCreateRequest();
        createRequest.setPlatform(SocialPlatform.WEBSITE);
        createRequest.setUrl("https://site");

        assertThatThrownBy(() -> service.create(ARTIST_ID, createRequest))
                .isInstanceOf(ResourceNotFoundException.class);
        assertThatThrownBy(() -> service.update(ARTIST_ID, LINK_ID, new ArtistSocialLinkUpdateRequest()))
                .isInstanceOf(ResourceNotFoundException.class);
        assertThatThrownBy(() -> service.delete(ARTIST_ID, LINK_ID))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private void mockOwnerAccess() {
        Artist artist = new Artist();
        artist.setArtistId(ARTIST_ID);
        artist.setUserId(USER_ID);
        when(securityUtil.getUserRole()).thenReturn(UserRole.ARTIST.name());
        when(securityUtil.getUserId()).thenReturn(USER_ID);
        when(artistRepository.findById(ARTIST_ID)).thenReturn(Optional.of(artist));
    }

    private ArtistSocialLink link(Long linkId, Long artistId, SocialPlatform platform, String url) {
        ArtistSocialLink link = new ArtistSocialLink();
        link.setLinkId(linkId);
        link.setArtistId(artistId);
        link.setPlatform(platform);
        link.setUrl(url);
        return link;
    }

    private ArtistSocialLinkResponse response(Long linkId, SocialPlatform platform, String url) {
        ArtistSocialLinkResponse response = new ArtistSocialLinkResponse();
        response.setLinkId(linkId);
        response.setPlatform(platform);
        response.setUrl(url);
        return response;
    }
}
