package com.revplay.revplay.mapper;

import com.revplay.revplay.dto.response.AdminAuditLogResponse;
import com.revplay.revplay.entity.AdminAuditLog;
import org.springframework.stereotype.Component;

@Component
public class AdminAuditLogMapper {


    public AdminAuditLogResponse toResponse(AdminAuditLog log) {

        Long targetId = null;

        if (log.getTargetEntityId() != null) {
            try {
                targetId = Long.parseLong(log.getTargetEntityId());
            } catch (NumberFormatException e) {
                targetId = null;
            }
        }

        return AdminAuditLogResponse.builder()
                .id(log.getLogId())
                .adminId(log.getAdminUser().getUserId())
                .action(log.getActionType())
                .targetEntity(log.getTargetEntity())
                .targetId(targetId)
                .actionTime(log.getCreatedAt())
                .build();
    }
}
