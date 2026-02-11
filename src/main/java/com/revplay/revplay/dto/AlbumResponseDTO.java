package com.revplay.revplay.dto;
import java.time.LocalDate;

public record AlbumResponseDTO(
        Long albumId,
        Long artistId,
        String title,
        String description,
        String coverArtUrl,
        LocalDate releaseDate
) {}
