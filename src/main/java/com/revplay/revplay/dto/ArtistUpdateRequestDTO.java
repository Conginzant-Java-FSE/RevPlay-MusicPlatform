package com.revplay.revplay.dto;

import com.revplay.revplay.enums.ArtistType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ArtistUpdateRequestDTO(
        @NotBlank
        @Size(max = 120)
        String displayName,

        @Size(max = 2000)
        String bio,

        @NotNull
        ArtistType artistType
) {}

