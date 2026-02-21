package com.revplay.revplay.mapper;

import com.revplay.revplay.dto.response.AdminAuditLogResponse;
import com.revplay.revplay.entity.AdminAuditLog;
import com.revplay.revplay.entity.User;

public class AdminAuditLogMapper {

    private AdminAuditLogMapper() {

    }

    public static AdminAuditLogResponse toResponse(AdminAuditLog log) {

        return AdminAuditLogResponse.builder()
                .id(log.getLogId())
                .adminId(log.getAdminUser().getUserId())
                .action(log.getActionType())
                .targetEntity(log.getTargetEntity())
                .targetId(parseTargetId(log.getTargetEntityId()))
                .actionTime(log.getCreatedAt())
                .build();
    }


    public static AdminAuditLog toEntity(User adminUser,
                                         String actionType,
                                         String targetEntity,
                                         Long targetId,
                                         String details) {

        AdminAuditLog log = new AdminAuditLog();
        log.setAdminUser(adminUser);
        log.setActionType(actionType);
        log.setTargetEntity(targetEntity);
        log.setTargetEntityId(String.valueOf(targetId));
        log.setDetails(details);



        return log;
    }


    private static Long parseTargetId(String targetEntityId) {
        try {
            return Long.parseLong(targetEntityId);
        } catch (NumberFormatException e) {
            return null; // prevents crash if invalid format
        }
    }
}
