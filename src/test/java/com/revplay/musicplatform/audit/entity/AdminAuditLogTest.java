package com.revplay.musicplatform.audit.entity;

import static org.assertj.core.api.Assertions.assertThat;

import com.revplay.musicplatform.audit.enums.AuditActionType;
import com.revplay.musicplatform.audit.enums.AuditEntityType;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class AdminAuditLogTest {

    @Test
    @DisplayName("builder and getters store audit log fields")
    void builderStoresFields() {
        LocalDateTime now = LocalDateTime.now();
        AdminAuditLog log = AdminAuditLog.builder()
                .id(1L)
                .action(AuditActionType.ROLE_CHANGED)
                .performedBy(2L)
                .entityType(AuditEntityType.USER)
                .entityId(3L)
                .description("desc")
                .timestamp(now)
                .version(0L)
                .build();

        assertThat(log.getId()).isEqualTo(1L);
        assertThat(log.getAction()).isEqualTo(AuditActionType.ROLE_CHANGED);
        assertThat(log.getPerformedBy()).isEqualTo(2L);
        assertThat(log.getEntityType()).isEqualTo(AuditEntityType.USER);
        assertThat(log.getEntityId()).isEqualTo(3L);
        assertThat(log.getDescription()).isEqualTo("desc");
        assertThat(log.getTimestamp()).isEqualTo(now);
        assertThat(log.getVersion()).isZero();
    }
}
