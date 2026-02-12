package com.revplay.revplay.dto.request;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlbumCreateRequest {

        @NotBlank(message = "title is required")
        @Size(max = 150, message = "title must be at most 150 characters")
        private String title;

        @Size(max = 2000, message = "description must be at most 2000 characters")
        private String description;

        @Size(max = 800, message = "coverArtUrl must be at most 800 characters")
        private String coverArtUrl;

        private LocalDate releaseDate;
}
