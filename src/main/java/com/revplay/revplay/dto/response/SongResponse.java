package com.revplay.revplay.dto.response;
import com.revplay.revplay.enums.ContentVisibility;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SongResponse {

    private Long songId;
    private Long artistId;
    private Long albumId;

    private String title;
    private Integer durationSeconds;
    private String fileUrl;

    private LocalDate releaseDate;
    private ContentVisibility visibility;

    private LocalDateTime createdAt;
}
