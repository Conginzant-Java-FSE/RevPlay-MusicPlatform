package com.revplay.revplay.service.impl;

import com.revplay.revplay.dto.response.AdminAuditLogResponse;
import com.revplay.revplay.entity.User;
import com.revplay.revplay.mapper.AdminAuditLogMapper;
import com.revplay.revplay.repository.AdminAuditLogRepository;
import com.revplay.revplay.repository.UserRepository;
import com.revplay.revplay.service.AdminAuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminAuditLogServiceImpl implements AdminAuditLogService {

    private final AdminAuditLogRepository logRepository;
    private final UserRepository userRepository;

    @Override
    public List<AdminAuditLogResponse> getAllLogs() {
        return logRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(AdminAuditLogMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<AdminAuditLogResponse> getLogsByAdmin(Long adminId) {

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        return logRepository.findByAdminUser(admin)
                .stream()
                .map(AdminAuditLogMapper::toResponse)
                .collect(Collectors.toList());
    }
}