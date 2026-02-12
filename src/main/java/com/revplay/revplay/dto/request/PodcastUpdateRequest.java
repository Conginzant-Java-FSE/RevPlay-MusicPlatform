package com.revplay.revplay.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PodcastUpdateRequest {

    @NotNull(message = "categoryId is required")
    private Long categoryId;

    @NotBlank(message = "title is required")
    @Size(max = 200)
    private String title;

    @Size(max = 3000)
    private String description;
}

