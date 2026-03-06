package com.revplay.musicplatform.audit.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.revplay.musicplatform.audit.dto.request.AuditLogRequest;
import com.revplay.musicplatform.audit.dto.response.AuditLogResponse;
import com.revplay.musicplatform.audit.entity.AdminAuditLog;
import com.revplay.musicplatform.audit.enums.AuditActionType;
import com.revplay.musicplatform.audit.enums.AuditEntityType;
import com.revplay.musicplatform.audit.repository.AdminAuditLogRepository;
import com.revplay.musicplatform.audit.service.AuditLogService;
import com.revplay.musicplatform.common.MockSecurityContextHelper;
import com.revplay.musicplatform.common.dto.PagedResponseDto;
import com.revplay.musicplatform.user.enums.UserRole;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("integration")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuditTrailIntegrationTest {

    private final AuditLogService auditLogService;
    private final AdminAuditLogRepository adminAuditLogRepository;
    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;

    @Autowired
    AuditTrailIntegrationTest(
            AuditLogService auditLogService,
            AdminAuditLogRepository adminAuditLogRepository,
            MockMvc mockMvc,
            ObjectMapper objectMapper
    ) {
        this.auditLogService = auditLogService;
        this.adminAuditLogRepository = adminAuditLogRepository;
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
    }

    @BeforeEach
    void setUp() {
        adminAuditLogRepository.deleteAll();
        MockSecurityContextHelper.clear();
    }

    @AfterEach
    void tearDown() {
        MockSecurityContextHelper.clear();
    }

    @Test
    @DisplayName("audited actions are queryable by admin")
    void auditedActions_queryable() {
        auditLogService.logInternal(AuditActionType.PLAYLIST_CREATED, 10L, AuditEntityType.PLAYLIST, 100L, "playlist created");
        auditLogService.logInternal(AuditActionType.PASSWORD_CHANGE, 10L, AuditEntityType.USER, 10L, "password changed");

        MockSecurityContextHelper.mockUser(1L, "admin", UserRole.ADMIN);
        PagedResponseDto<AuditLogResponse> page = auditLogService.queryAuditLogs(null, null, null, null, null, 0, 20);

        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getContent().stream().map(AuditLogResponse::getAction))
                .contains(AuditActionType.PLAYLIST_CREATED.name(), AuditActionType.PASSWORD_CHANGE.name());
    }

    @Test
    @DisplayName("internal audit endpoint with valid api key creates entry without jwt")
    void internalEndpoint_validKey_createsAudit() throws Exception {
        long before = adminAuditLogRepository.count();

        mockMvc.perform(post("/api/v1/audit-logs/internal")
                        .header("X-Internal-Api-Key", "test-internal-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request())))
                .andExpect(status().isCreated());

        long after = adminAuditLogRepository.count();
        assertThat(after).isEqualTo(before + 1);

        AdminAuditLog latest = adminAuditLogRepository.findAll().get(0);
        assertThat(latest.getAction()).isEqualTo(AuditActionType.ADMIN_ACTION);
    }

    private AuditLogRequest request() {
        AuditLogRequest request = new AuditLogRequest();
        request.setAction("ADMIN_ACTION");
        request.setPerformedBy(1L);
        request.setEntityType("SYSTEM");
        request.setEntityId(9L);
        request.setDescription("internal");
        return request;
    }
}
