package com.revplay.musicplatform.audit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.revplay.musicplatform.audit.dto.request.AuditLogRequest;
import com.revplay.musicplatform.audit.dto.response.AuditLogResponse;
import com.revplay.musicplatform.audit.service.AuditLogService;
import com.revplay.musicplatform.common.dto.PagedResponseDto;
import com.revplay.musicplatform.config.FileStorageProperties;
import com.revplay.musicplatform.exception.AccessDeniedException;
import com.revplay.musicplatform.security.AuthenticatedUserPrincipal;
import com.revplay.musicplatform.security.SecurityConfig;
import com.revplay.musicplatform.security.service.JwtService;
import com.revplay.musicplatform.security.service.TokenRevocationService;
import com.revplay.musicplatform.user.enums.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("unit")
@WebMvcTest(AuditLogController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = "app.audit.internal-api-key=test-internal-key")
class AuditLogControllerTest {

    private static final String BASE = "/api/v1/audit-logs";
    private static final String INTERNAL = "/api/v1/audit-logs/internal";

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;

    @MockBean
    private AuditLogService auditLogService;
    @MockBean
    private JwtService jwtService;
    @MockBean
    private TokenRevocationService tokenRevocationService;
    @MockBean
    private FileStorageProperties fileStorageProperties;
    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Autowired
    AuditLogControllerTest(MockMvc mockMvc, ObjectMapper objectMapper) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
    }

    @Test
    @DisplayName("GET audit logs admin returns 200")
    void query_admin_ok() throws Exception {
        PagedResponseDto<AuditLogResponse> page = PagedResponseDto.<AuditLogResponse>builder()
                .content(List.of(response()))
                .page(0)
                .size(20)
                .totalElements(1)
                .totalPages(1)
                .last(true)
                .build();
        when(auditLogService.queryAuditLogs(any(), any(), any(), any(), any(), any(Integer.class), any(Integer.class)))
                .thenReturn(page);

        mockMvc.perform(get(BASE).with(authentication(auth(UserRole.ADMIN))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].action").value("PASSWORD_CHANGED"));
    }

    @Test
    @DisplayName("GET audit logs listener maps to 403 when service rejects")
    void query_listener_forbidden() throws Exception {
        when(auditLogService.queryAuditLogs(any(), any(), any(), any(), any(), any(Integer.class), any(Integer.class)))
                .thenThrow(new AccessDeniedException("Admin access required"));

        mockMvc.perform(get(BASE).with(authentication(auth(UserRole.LISTENER))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET audit logs no auth returns 403")
    void query_noAuth_forbidden() throws Exception {
        mockMvc.perform(get(BASE)).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST internal with correct api key returns 201")
    void internal_correctKey_created() throws Exception {
        when(auditLogService.logAction(any())).thenReturn(response());

        mockMvc.perform(post(INTERNAL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Internal-Api-Key", "test-internal-key")
                        .content(objectMapper.writeValueAsString(request())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("POST internal with wrong api key returns 403")
    void internal_wrongKey_forbidden() throws Exception {
        mockMvc.perform(post(INTERNAL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Internal-Api-Key", "wrong")
                        .content(objectMapper.writeValueAsString(request())))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET audit logs with action filter returns 200")
    void query_withActionFilter_ok() throws Exception {
        PagedResponseDto<AuditLogResponse> page = PagedResponseDto.<AuditLogResponse>builder()
                .content(List.of(response()))
                .page(0)
                .size(20)
                .totalElements(1)
                .totalPages(1)
                .last(true)
                .build();
        when(auditLogService.queryAuditLogs(any(), any(), any(), any(), any(), any(Integer.class), any(Integer.class)))
                .thenReturn(page);

        mockMvc.perform(get(BASE)
                        .with(authentication(auth(UserRole.ADMIN)))
                        .param("action", "PASSWORD_CHANGED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].action").value("PASSWORD_CHANGED"));
    }

    private AuditLogRequest request() {
        AuditLogRequest request = new AuditLogRequest();
        request.setAction("PASSWORD_CHANGED");
        request.setPerformedBy(1L);
        request.setEntityType("USER");
        request.setEntityId(9L);
        request.setDescription("changed");
        return request;
    }

    private AuditLogResponse response() {
        return AuditLogResponse.builder()
                .id(1L)
                .action("PASSWORD_CHANGED")
                .performedBy(1L)
                .entityType("USER")
                .entityId(9L)
                .description("changed")
                .timestamp(LocalDateTime.now())
                .build();
    }

    private UsernamePasswordAuthenticationToken auth(UserRole role) {
        AuthenticatedUserPrincipal principal = new AuthenticatedUserPrincipal(1L, "u1", role);
        return new UsernamePasswordAuthenticationToken(
                principal,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + role.name()))
        );
    }
}
