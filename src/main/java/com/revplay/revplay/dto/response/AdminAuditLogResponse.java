package com.revplay.revplay.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AdminAuditLogResponse {

    private Long id;
    private Long adminId;
    private String action;
    private String targetEntity;
    private Long targetId;
    private LocalDateTime actionTime;
}