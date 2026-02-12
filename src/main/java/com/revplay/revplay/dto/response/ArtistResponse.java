package com.revplay.revplay.dto.response;
import com.revplay.revplay.enums.ArtistType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ArtistResponse {

    private Long artistId;
    private Long userId;
    private String displayName;
    private String bio;
    private ArtistType artistType;
    private Boolean verified;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
