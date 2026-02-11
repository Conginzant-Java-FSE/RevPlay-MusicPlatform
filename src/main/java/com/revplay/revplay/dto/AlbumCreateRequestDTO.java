package com.revplay.revplay.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record AlbumCreateRequestDTO(
        @NotNull Long artistId,

        @NotBlank
        @Size(max = 150)
        String title,

        @Size(max = 2000)
        String description,

        @Size(max = 800)
        String coverArtUrl,

        LocalDate releaseDate
) {}

