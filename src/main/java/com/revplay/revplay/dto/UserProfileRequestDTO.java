package com.revplay.revplay.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserProfileRequestDTO {

    @NotBlank
    private String fullName;

    private String bio;

    private String profilePictureUrl;
}
