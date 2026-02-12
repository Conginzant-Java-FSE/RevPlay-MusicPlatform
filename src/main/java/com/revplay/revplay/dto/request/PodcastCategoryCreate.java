package com.revplay.revplay.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PodcastCategoryCreate {

    @NotBlank(message = "name is required")
    @Size(max = 120, message = "name must be <= 120 characters")
    private String name;

    @Size(max = 1000, message = "description must be <= 1000 characters")
    private String description;
}
