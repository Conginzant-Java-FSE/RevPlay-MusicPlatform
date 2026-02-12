package com.revplay.revplay.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class PodcastEpisodeUploadMeta {

    @NotBlank(message = "Title is required")
    private String title;


    @Positive(message = "Duration must be positive")
    private Integer durationSeconds;

    private LocalDate releaseDate;
}
