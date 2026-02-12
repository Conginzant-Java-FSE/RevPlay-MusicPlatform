package com.revplay.revplay.dto.request;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlbumUpdateRequest {

        @Size(max = 150, message = "title must be at most 150 characters")
        private String title;

        @Size(max = 2000, message = "description must be at most 2000 characters")
        private String description;

        @Size(max = 800, message = "coverArtUrl must be at most 800 characters")
        private String coverArtUrl;

        private LocalDate releaseDate;
}
