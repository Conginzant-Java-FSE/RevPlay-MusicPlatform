package com.revplay.musicplatform.systemplaylist.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class BaseAuditEntityTest {

    @Test
    @DisplayName("onCreate sets createdAt and updatedAt")
    void onCreateSetsTimestamps() {
        TestAuditEntity entity = new TestAuditEntity();

        entity.onCreate();

        assertThat(entity.getCreatedAt()).isNotNull();
        assertThat(entity.getUpdatedAt()).isNotNull();
        assertThat(entity.getCreatedAt()).isEqualTo(entity.getUpdatedAt());
    }

    @Test
    @DisplayName("onUpdate refreshes updatedAt")
    void onUpdateRefreshesUpdatedAt() {
        TestAuditEntity entity = new TestAuditEntity();
        entity.setUpdatedAt(LocalDateTime.now().minusHours(1));
        LocalDateTime previous = entity.getUpdatedAt();

        entity.onUpdate();

        assertThat(entity.getUpdatedAt()).isAfter(previous);
    }

    private static final class TestAuditEntity extends BaseAuditEntity {
    }
}
