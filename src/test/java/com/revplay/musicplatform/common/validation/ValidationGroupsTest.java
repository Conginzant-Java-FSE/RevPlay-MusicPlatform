package com.revplay.musicplatform.common.validation;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Constructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class ValidationGroupsTest {

    @Test
    @DisplayName("marker interfaces exist and are distinct")
    void markerInterfacesExist() {
        assertThat(ValidationGroups.Create.class).isNotNull();
        assertThat(ValidationGroups.Update.class).isNotNull();
        assertThat(ValidationGroups.Create.class).isNotEqualTo(ValidationGroups.Update.class);
    }

    @Test
    @DisplayName("private constructor is invocable via reflection for coverage")
    void privateConstructorCovered() throws Exception {
        Constructor<ValidationGroups> constructor = ValidationGroups.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        ValidationGroups instance = constructor.newInstance();

        assertThat(instance).isNotNull();
    }
}
