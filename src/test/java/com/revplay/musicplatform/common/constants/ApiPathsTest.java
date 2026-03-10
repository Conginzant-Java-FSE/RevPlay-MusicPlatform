package com.revplay.musicplatform.common.constants;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Constructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class ApiPathsTest {

    private static final String API_V1 = "/api/v1";

    @Test
    @DisplayName("api path constants expose expected values")
    void constantsExposeExpectedValues() {
        assertThat(ApiPaths.API_V1).isEqualTo(API_V1);
        assertThat(ApiPaths.ARTISTS).isEqualTo(API_V1 + "/artists");
        assertThat(ApiPaths.ALBUMS).isEqualTo(API_V1 + "/albums");
        assertThat(ApiPaths.SONGS).isEqualTo(API_V1 + "/songs");
        assertThat(ApiPaths.PODCASTS).isEqualTo(API_V1 + "/podcasts");
        assertThat(ApiPaths.PODCAST_CATEGORIES).isEqualTo(API_V1 + "/podcast-categories");
        assertThat(ApiPaths.FILES).isEqualTo(API_V1 + "/files");
    }

    @Test
    @DisplayName("private constructor is invocable via reflection for coverage")
    void privateConstructorCovered() throws Exception {
        Constructor<ApiPaths> constructor = ApiPaths.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        ApiPaths instance = constructor.newInstance();

        assertThat(instance).isNotNull();
    }
}
