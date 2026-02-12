package com.revplay.revplay.dto.request;
import com.revplay.revplay.enums.ContentVisibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SongUploadMeta {

        @NotBlank(message = "title is required")
        @Size(max = 200, message = "title must be at most 200 characters")
        private String title;

        private Long albumId;

        private LocalDate releaseDate;

        @Builder.Default
        private ContentVisibility visibility = ContentVisibility.PUBLIC;
}


