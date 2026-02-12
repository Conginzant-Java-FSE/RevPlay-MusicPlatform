package com.revplay.revplay.dto.request;
import com.revplay.revplay.enums.ContentVisibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record SongUploadMetaDTO(
        @NotNull Long artistId,

        Long albumId,

        @NotBlank
        @Size(max = 200)
        String title,

        LocalDate releaseDate,

        ContentVisibility visibility,

        // fallback only (mp3 duration read fail ayite use cheyyadaniki)
        @Positive
        Integer durationSeconds
) {}

