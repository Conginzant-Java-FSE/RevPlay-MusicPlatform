package com.revplay.revplay.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AddToQueueRequest {

    private Long songId;

    private Long episodeId;

    public void validate() {
        if ((songId == null && episodeId == null) || (songId != null && episodeId != null)) {
            throw new IllegalArgumentException("Exactly one of songId or episodeId must be provided");
        }
    }
}

