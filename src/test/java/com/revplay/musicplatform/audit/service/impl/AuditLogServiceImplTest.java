package com.revplay.musicplatform.audit.service.impl;

import com.revplay.musicplatform.audit.dto.request.AuditLogRequest;
import com.revplay.musicplatform.audit.dto.response.AuditLogResponse;
import com.revplay.musicplatform.audit.entity.AdminAuditLog;
import com.revplay.musicplatform.audit.enums.AuditActionType;
import com.revplay.musicplatform.audit.enums.AuditEntityType;
import com.revplay.musicplatform.audit.mapper.AuditLogMapper;
import com.revplay.musicplatform.audit.repository.AdminAuditLogRepository;
import com.revplay.musicplatform.common.dto.PagedResponseDto;
import com.revplay.musicplatform.exception.AccessDeniedException;
import com.revplay.musicplatform.security.AuthContextUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class AuditLogServiceImplTest {

    @Mock
    private AdminAuditLogRepository auditLogRepository;
    @Mock
    private AuditLogMapper auditLogMapper;
    @Mock
    private AuthContextUtil authContextUtil;

    @InjectMocks
    private AuditLogServiceImpl service;

    @Test
    @DisplayName("logInternal with all fields saves audit log with expected values")
    void logInternal_allFields_saves() {
        when(auditLogRepository.save(any(AdminAuditLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.logInternal(AuditActionType.PLAYLIST_CREATED, 1L, AuditEntityType.PLAYLIST, 55L, "created");

        ArgumentCaptor<AdminAuditLog> captor = ArgumentCaptor.forClass(AdminAuditLog.class);
        verify(auditLogRepository).save(captor.capture());
        AdminAuditLog saved = captor.getValue();
        assertThat(saved.getAction()).isEqualTo(AuditActionType.PLAYLIST_CREATED);
        assertThat(saved.getPerformedBy()).isEqualTo(1L);
        assertThat(saved.getEntityType()).isEqualTo(AuditEntityType.PLAYLIST);
        assertThat(saved.getEntityId()).isEqualTo(55L);
        assertThat(saved.getDescription()).isEqualTo("created");
    }

    @Test
    @DisplayName("logInternal with null performedBy is accepted and saved")
    void logInternal_nullPerformedBy_saved() {
        when(auditLogRepository.save(any(AdminAuditLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.logInternal(AuditActionType.ADMIN_ACTION, null, AuditEntityType.USER, 2L, "system");

        ArgumentCaptor<AdminAuditLog> captor = ArgumentCaptor.forClass(AdminAuditLog.class);
        verify(auditLogRepository).save(captor.capture());
        assertThat(captor.getValue().getPerformedBy()).isNull();
    }

    @Test
    @DisplayName("queryAuditLogs no filters returns paged result")
    void queryAuditLogs_noFilters_returnsPage() {
        AdminAuditLog log = AdminAuditLog.builder()
                .id(10L)
                .action(AuditActionType.PASSWORD_CHANGE)
                .performedBy(1L)
                .entityType(AuditEntityType.USER)
                .entityId(1L)
                .description("ok")
                .timestamp(LocalDateTime.now())
                .build();
        AuditLogResponse response = AuditLogResponse.builder().id(10L).action("PASSWORD_CHANGE").build();
        Page<AdminAuditLog> page = new PageImpl<>(List.of(log), PageRequest.of(0, 20), 1);

        when(auditLogRepository.findWithFilters(null, null, null, null, null, PageRequest.of(0, 20)))
                .thenReturn(page);
        when(auditLogMapper.toResponse(log)).thenReturn(response);

        PagedResponseDto<AuditLogResponse> result = service.queryAuditLogs(null, null, null, null, null, 0, 20);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getAction()).isEqualTo("PASSWORD_CHANGE");
    }

    @Test
    @DisplayName("queryAuditLogs with action PASSWORD_CHANGE parses values correctly")
    void queryAuditLogs_passwordChange_parsed() {
        Page<AdminAuditLog> empty = Page.empty(PageRequest.of(1, 5));
        when(auditLogRepository.findWithFilters(
                eq(AuditActionType.PASSWORD_CHANGE),
                eq(3L),
                eq(AuditEntityType.USER),
                eq(LocalDateTime.of(2026, 1, 1, 0, 0)),
                eq(LocalDateTime.of(2026, 1, 10, 23, 59, 59, 999999999)),
                eq(PageRequest.of(1, 5))
        )).thenReturn(empty);

        PagedResponseDto<AuditLogResponse> result = service.queryAuditLogs(
                "PASSWORD_CHANGE", 3L, "USER", "2026-01-01", "2026-01-10", 1, 5
        );

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("queryAuditLogs invalid action throws IllegalArgumentException")
    void queryAuditLogs_invalidAction_throws() {
        assertThatThrownBy(() -> service.queryAuditLogs("BAD_ACTION", null, null, null, null, 0, 20))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid audit action filter");
    }

    @Test
    @DisplayName("queryAuditLogs invalid entityType throws IllegalArgumentException")
    void queryAuditLogs_invalidEntityType_throws() {
        assertThatThrownBy(() -> service.queryAuditLogs(null, null, "BAD_ENTITY", null, null, 0, 20))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid audit entity type filter");
    }

    @Test
    @DisplayName("queryAuditLogs non admin caller throws from authContextUtil")
    void queryAuditLogs_nonAdmin_throws() {
        doThrow(new AccessDeniedException("Admin access required")).when(authContextUtil).requireAdmin();

        assertThatThrownBy(() -> service.queryAuditLogs(null, null, null, null, null, 0, 20))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("logAction maps request and returns mapped response")
    void logAction_mapsAndSaves() {
        AuditLogRequest request = new AuditLogRequest();
        request.setAction("PASSWORD_CHANGE");
        request.setPerformedBy(1L);
        request.setEntityType("USER");
        AdminAuditLog entity = AdminAuditLog.builder()
                .action(AuditActionType.PASSWORD_CHANGE)
                .performedBy(1L)
                .entityType(AuditEntityType.USER)
                .build();
        AuditLogResponse response = AuditLogResponse.builder().id(99L).build();

        when(auditLogMapper.toEntity(request)).thenReturn(entity);
        when(auditLogRepository.save(entity)).thenReturn(entity);
        when(auditLogMapper.toResponse(entity)).thenReturn(response);

        AuditLogResponse actual = service.logAction(request);

        assertThat(actual.getId()).isEqualTo(99L);
    }
}
