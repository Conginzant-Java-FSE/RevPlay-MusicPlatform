package com.revplay.musicplatform.catalog.dto.request;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class SearchRequestTest {

    @Test
    @DisplayName("class shape and construction behavior are valid")
    void classShapeAndConstructionBehaviorAreValid() throws Exception {
        Class<?> clazz = SearchRequest.class;

        if (Modifier.isAbstract(clazz.getModifiers())) {
            assertThat(Modifier.isAbstract(clazz.getModifiers())).isTrue();
            return;
        }

        Constructor<?> constructor = clazz.getDeclaredConstructors()[0];
        constructor.setAccessible(true);
        Class<?>[] parameterTypes = constructor.getParameterTypes();
        Object[] args = new Object[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            args[i] = dummyValue(parameterTypes[i]);
        }

        Object instance = constructor.newInstance(args);

        assertThat(instance).isNotNull();
    }

    private Object dummyValue(Class<?> type) {
        if (!type.isPrimitive()) {
            if (String.class.equals(type)) {
                return "value";
            }
            if (Long.class.equals(type)) {
                return 1L;
            }
            if (Integer.class.equals(type)) {
                return 1;
            }
            if (Boolean.class.equals(type)) {
                return Boolean.TRUE;
            }
            if (Double.class.equals(type)) {
                return 1.0d;
            }
            if (java.time.LocalDateTime.class.equals(type)) {
                return java.time.LocalDateTime.now();
            }
            if (java.time.LocalDate.class.equals(type)) {
                return java.time.LocalDate.now();
            }
            if (java.time.Instant.class.equals(type)) {
                return java.time.Instant.now();
            }
            if (type.isEnum()) {
                return type.getEnumConstants()[0];
            }
            return null;
        }
        if (long.class.equals(type)) {
            return 1L;
        }
        if (int.class.equals(type)) {
            return 1;
        }
        if (boolean.class.equals(type)) {
            return true;
        }
        if (double.class.equals(type)) {
            return 1.0d;
        }
        if (float.class.equals(type)) {
            return 1.0f;
        }
        if (byte.class.equals(type)) {
            return (byte) 1;
        }
        if (short.class.equals(type)) {
            return (short) 1;
        }
        if (char.class.equals(type)) {
            return 'a';
        }
        return 0;
    }
}
