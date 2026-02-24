package com.revplay.revplay.dto.response;

import lombok.Builder;
import lombok.Data;
@Builder
@Data
public class SongGenreResponse {
    private Long songGenreId;
    private Long songId;
    private Long genreId;
}


