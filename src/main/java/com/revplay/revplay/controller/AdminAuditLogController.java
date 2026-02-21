package com.revplay.revplay.controller;

import com.revplay.revplay.dto.response.AdminAuditLogResponse;
import com.revplay.revplay.service.AdminAuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin-audit-logs")
@RequiredArgsConstructor
public class AdminAuditLogController {

    private final AdminAuditLogService service;

    @GetMapping
    public List<AdminAuditLogResponse> getAllLogs() {
        return service.getAllLogs();
    }

    @GetMapping("/admin/{adminId}")
    public List<AdminAuditLogResponse> getLogsByAdmin(@PathVariable Long adminId) {
        return service.getLogsByAdmin(adminId);
    }
}