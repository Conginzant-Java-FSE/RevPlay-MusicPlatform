package com.revplay.revplay.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateUserProfile {

    @NotBlank(message = "Full name is required")
    private String fullName;

    private String bio;

    private String profilePictureUrl;
}
