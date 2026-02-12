package com.revplay.revplay.dto.response;
import com.revplay.revplay.enums.ContentVisibility;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record SongResponseDTO(
        Long songId,
        Long artistId,
        Long albumId,
        String title,
        Integer durationSeconds,
        String fileUrl,
        LocalDate releaseDate,
        ContentVisibility visibility,
        LocalDateTime createdAt
) {}

