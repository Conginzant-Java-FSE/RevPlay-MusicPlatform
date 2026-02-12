package com.revplay.revplay.dto.response;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlbumResponse {

    private Long albumId;
    private Long artistId;
    private String title;
    private String description;
    private String coverArtUrl;
    private LocalDate releaseDate;
}
