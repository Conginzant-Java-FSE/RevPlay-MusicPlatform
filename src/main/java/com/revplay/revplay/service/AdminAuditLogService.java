package com.revplay.revplay.service;

import com.revplay.revplay.dto.response.AdminAuditLogResponse;

import java.util.List;

public interface AdminAuditLogService {

    List<AdminAuditLogResponse> getAllLogs();

    List<AdminAuditLogResponse> getLogsByAdmin(Long adminId);
}