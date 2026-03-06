package com.revplay.musicplatform.playlist.service.impl;

import com.revplay.musicplatform.audit.service.AuditLogService;
import com.revplay.musicplatform.common.dto.PagedResponseDto;
import com.revplay.musicplatform.exception.AccessDeniedException;
import com.revplay.musicplatform.exception.DuplicateResourceException;
import com.revplay.musicplatform.playlist.dto.request.LikeRequest;
import com.revplay.musicplatform.playlist.dto.response.UserLikeResponse;
import com.revplay.musicplatform.playlist.entity.UserLike;
import com.revplay.musicplatform.playlist.mapper.UserLikeMapper;
import com.revplay.musicplatform.playlist.repository.UserLikeRepository;
import com.revplay.musicplatform.playlist.service.ContentReferenceValidationService;
import com.revplay.musicplatform.security.AuthContextUtil;
import com.revplay.musicplatform.user.enums.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class UserLikeServiceImplTest {

    private static final Long USER_ID = 10L;
    private static final Long OTHER_USER_ID = 20L;
    private static final Long LIKE_ID = 1L;
    private static final Long LIKEABLE_ID = 101L;
    private static final String SONG_TYPE = "SONG";

    @Mock
    private UserLikeRepository userLikeRepository;
    @Mock
    private UserLikeMapper userLikeMapper;
    @Mock
    private AuditLogService auditLogService;
    @Mock
    private AuthContextUtil authContextUtil;
    @Mock
    private ContentReferenceValidationService contentReferenceValidationService;

    @InjectMocks
    private UserLikeServiceImpl service;

    @Test
    @DisplayName("likeContent: not liked yet creates like and returns response")
    void likeContent_notLikedYet_creates() {
        LikeRequest request = new LikeRequest();
        request.setLikeableId(LIKEABLE_ID);
        request.setLikeableType(SONG_TYPE);

        UserLike toSave = UserLike.builder().userId(USER_ID).likeableId(LIKEABLE_ID).likeableType(SONG_TYPE).build();
        UserLike saved = UserLike.builder().id(LIKE_ID).userId(USER_ID).likeableId(LIKEABLE_ID).likeableType(SONG_TYPE).build();
        UserLikeResponse response = UserLikeResponse.builder().id(LIKE_ID).userId(USER_ID).likeableId(LIKEABLE_ID).likeableType(SONG_TYPE).build();

        when(authContextUtil.requireCurrentUserId()).thenReturn(USER_ID);
        when(userLikeRepository.existsByUserIdAndLikeableIdAndLikeableType(USER_ID, LIKEABLE_ID, SONG_TYPE)).thenReturn(false);
        when(userLikeMapper.toEntity(request, USER_ID)).thenReturn(toSave);
        when(userLikeRepository.save(toSave)).thenReturn(saved);
        when(userLikeMapper.toResponse(saved)).thenReturn(response);

        UserLikeResponse actual = service.likeContent(request);

        assertThat(actual.getId()).isEqualTo(LIKE_ID);
        verify(contentReferenceValidationService).validateLikeTargetExists(SONG_TYPE, LIKEABLE_ID);
        verify(auditLogService).logInternal(any(), eq(USER_ID), any(), eq(LIKEABLE_ID), any());
    }

    @Test
    @DisplayName("likeContent: already liked throws DuplicateResourceException")
    void likeContent_alreadyLiked_throws() {
        LikeRequest request = new LikeRequest();
        request.setLikeableId(LIKEABLE_ID);
        request.setLikeableType(SONG_TYPE);

        when(authContextUtil.requireCurrentUserId()).thenReturn(USER_ID);
        when(userLikeRepository.existsByUserIdAndLikeableIdAndLikeableType(USER_ID, LIKEABLE_ID, SONG_TYPE)).thenReturn(true);

        assertThatThrownBy(() -> service.likeContent(request))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    @DisplayName("unlikeContent: existing own like deletes and audits")
    void unlikeContent_ownLike_deletes() {
        UserLike existing = UserLike.builder()
                .id(LIKE_ID)
                .userId(USER_ID)
                .likeableId(LIKEABLE_ID)
                .likeableType(SONG_TYPE)
                .build();

        when(authContextUtil.requireCurrentUserId()).thenReturn(USER_ID);
        when(userLikeRepository.findById(LIKE_ID)).thenReturn(Optional.of(existing));

        service.unlikeContent(LIKE_ID);

        verify(userLikeRepository).delete(existing);
        verify(auditLogService).logInternal(any(), eq(USER_ID), any(), eq(LIKEABLE_ID), any());
    }

    @Test
    @DisplayName("unlikeContent: other user like throws AccessDeniedException")
    void unlikeContent_otherUserLike_denied() {
        UserLike existing = UserLike.builder()
                .id(LIKE_ID)
                .userId(OTHER_USER_ID)
                .likeableId(LIKEABLE_ID)
                .likeableType(SONG_TYPE)
                .build();

        when(authContextUtil.requireCurrentUserId()).thenReturn(USER_ID);
        when(userLikeRepository.findById(LIKE_ID)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.unlikeContent(LIKE_ID))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("getUserLikes: self user gets paginated likes")
    void getUserLikes_self_returnsPage() {
        UserLike like = UserLike.builder().id(LIKE_ID).userId(USER_ID).likeableId(LIKEABLE_ID).likeableType(SONG_TYPE).build();
        UserLikeResponse response = UserLikeResponse.builder().id(LIKE_ID).userId(USER_ID).build();
        Page<UserLike> page = new PageImpl<>(List.of(like), PageRequest.of(0, 10), 1);

        when(authContextUtil.requireCurrentUserId()).thenReturn(USER_ID);
        when(userLikeRepository.findByUserId(USER_ID, PageRequest.of(0, 10))).thenReturn(page);
        when(userLikeMapper.toResponse(like)).thenReturn(response);

        PagedResponseDto<UserLikeResponse> actual = service.getUserLikes(USER_ID, null, 0, 10);

        assertThat(actual.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("getUserLikes: different user without admin role denied")
    void getUserLikes_otherWithoutAdmin_denied() {
        when(authContextUtil.requireCurrentUserId()).thenReturn(USER_ID);
        when(authContextUtil.hasRole(UserRole.ADMIN.name())).thenReturn(false);

        assertThatThrownBy(() -> service.getUserLikes(OTHER_USER_ID, null, 0, 10))
                .isInstanceOf(AccessDeniedException.class);
    }
}
