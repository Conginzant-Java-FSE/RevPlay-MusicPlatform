package com.revplay.revplay.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class PodcastEpisodeUpdateRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private LocalDate releaseDate;
}

