package com.revplay.revplay.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserLikeCreateRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Song ID is required")
    private Long songId;
}