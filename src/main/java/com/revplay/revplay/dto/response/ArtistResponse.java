package com.revplay.revplay.dto.response;
import com.revplay.revplay.enums.ArtistType;

import java.time.LocalDateTime;

public record ArtistResponseDTO(
        Long artistId,
        Long userId,
        String displayName,
        String bio,
        ArtistType artistType,
        Boolean verified,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
