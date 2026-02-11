package com.revplay.revplay.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReorderQueueRequest {

    @NotNull(message = "Queue item ID is required")
    private Long queueId;

    @NotNull(message = "New position is required")
    @Min(value = 1, message = "Position must be at least 1")
    private Integer newPosition;
}

