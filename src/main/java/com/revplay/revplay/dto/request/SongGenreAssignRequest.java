package com.revplay.revplay.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class SongGenreAssignRequest {

    @NotEmpty(message = "genreIds must not be empty")
    private List<@NotNull(message = "genreId must not be null") Long> genreIds;
}

