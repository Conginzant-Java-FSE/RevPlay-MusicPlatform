package com.revplay.musicplatform.audit.enums;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.EnumSet;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class AuditEntityTypeTest {

    @Test
    @DisplayName("AuditEntityType includes expected entity values")
    void hasExpectedValues() {
        EnumSet<AuditEntityType> values = EnumSet.allOf(AuditEntityType.class);

        assertThat(values).contains(AuditEntityType.USER, AuditEntityType.PLAYLIST, AuditEntityType.SYSTEM);
    }
}
