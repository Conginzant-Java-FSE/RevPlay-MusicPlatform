package com.revplay.musicplatform.audit.dto.response;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class AuditLogResponseTest {

    @Test
    @DisplayName("builder and accessors expose response fields")
    void builderAndAccessorsExposeFields() {
        LocalDateTime now = LocalDateTime.now();
        AuditLogResponse response = AuditLogResponse.builder()
                .id(1L)
                .action("CREATE")
                .performedBy(2L)
                .entityType("SONG")
                .entityId(3L)
                .description("desc")
                .timestamp(now)
                .build();

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getAction()).isEqualTo("CREATE");
        assertThat(response.getTimestamp()).isEqualTo(now);
    }
}
