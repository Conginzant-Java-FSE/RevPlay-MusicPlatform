package com.revplay.musicplatform.audit.enums;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.EnumSet;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class AuditActionTypeTest {

    @Test
    @DisplayName("AuditActionType includes expected action values")
    void hasExpectedValues() {
        EnumSet<AuditActionType> values = EnumSet.allOf(AuditActionType.class);

        assertThat(values).contains(AuditActionType.ROLE_CHANGED, AuditActionType.PLAYLIST_CREATED, AuditActionType.ADMIN_ACTION);
    }
}
