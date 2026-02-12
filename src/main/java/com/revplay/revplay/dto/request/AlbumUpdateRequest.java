package com.revplay.revplay.dto.request;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record AlbumUpdateRequestDTO(
        @NotBlank
        @Size(max = 150)
        String title,

        @Size(max = 2000)
        String description,

        @Size(max = 800)
        String coverArtUrl,

        LocalDate releaseDate
) {}
