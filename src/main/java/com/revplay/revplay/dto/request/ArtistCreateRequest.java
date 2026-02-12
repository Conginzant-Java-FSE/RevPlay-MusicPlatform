package com.revplay.revplay.dto.request;

import com.revplay.revplay.enums.ArtistType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ArtistCreateRequest {

        @NotBlank
        @Size(max = 120)
        private String displayName;

        @Size(max = 2000)
        private String bio;

        @NotNull
        private ArtistType artistType;
}
