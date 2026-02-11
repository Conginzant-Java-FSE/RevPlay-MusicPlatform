package com.revplay.revplay.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PlaylistUpdateRequest {

    @Size(max = 100)
    private String name;

    @Size(max = 500)
    private String description;

    private Boolean isPublic;
}