package com.revplay.musicplatform.common;

import static org.assertj.core.api.Assertions.assertThat;

import com.revplay.musicplatform.artist.dto.request.ArtistCreateRequest;
import com.revplay.musicplatform.artist.dto.request.ArtistSocialLinkCreateRequest;
import com.revplay.musicplatform.artist.dto.request.ArtistSocialLinkUpdateRequest;
import com.revplay.musicplatform.artist.dto.request.ArtistUpdateRequest;
import com.revplay.musicplatform.artist.dto.request.ArtistVerifyRequest;
import com.revplay.musicplatform.artist.dto.response.ArtistResponse;
import com.revplay.musicplatform.artist.dto.response.ArtistSocialLinkResponse;
import com.revplay.musicplatform.artist.dto.response.ArtistSummaryResponse;
import com.revplay.musicplatform.catalog.dto.request.AlbumCreateRequest;
import com.revplay.musicplatform.catalog.dto.request.AlbumUpdateRequest;
import com.revplay.musicplatform.catalog.dto.request.GenreUpsertRequest;
import com.revplay.musicplatform.catalog.dto.request.PodcastCategoryCreateRequest;
import com.revplay.musicplatform.catalog.dto.request.PodcastCreateRequest;
import com.revplay.musicplatform.catalog.dto.request.PodcastEpisodeCreateRequest;
import com.revplay.musicplatform.catalog.dto.request.PodcastEpisodeUpdateRequest;
import com.revplay.musicplatform.catalog.dto.request.PodcastUpdateRequest;
import com.revplay.musicplatform.catalog.dto.request.SearchRequest;
import com.revplay.musicplatform.catalog.dto.request.SongCreateRequest;
import com.revplay.musicplatform.catalog.dto.request.SongGenresRequest;
import com.revplay.musicplatform.catalog.dto.request.SongUpdateRequest;
import com.revplay.musicplatform.catalog.dto.request.SongVisibilityRequest;
import com.revplay.musicplatform.catalog.dto.response.AlbumResponse;
import com.revplay.musicplatform.catalog.dto.response.DiscoverWeeklyResponse;
import com.revplay.musicplatform.catalog.dto.response.DiscoveryFeedResponse;
import com.revplay.musicplatform.catalog.dto.response.DiscoveryRecommendationItemResponse;
import com.revplay.musicplatform.catalog.dto.response.GenreResponse;
import com.revplay.musicplatform.catalog.dto.response.ImageUploadResponse;
import com.revplay.musicplatform.catalog.dto.response.NewReleaseItemResponse;
import com.revplay.musicplatform.catalog.dto.response.PodcastCategoryResponse;
import com.revplay.musicplatform.catalog.dto.response.PodcastEpisodeResponse;
import com.revplay.musicplatform.catalog.dto.response.PodcastResponse;
import com.revplay.musicplatform.catalog.dto.response.PopularPodcastItemResponse;
import com.revplay.musicplatform.catalog.dto.response.SearchResultItemResponse;
import com.revplay.musicplatform.catalog.dto.response.SongResponse;
import com.revplay.musicplatform.catalog.dto.response.TopArtistItemResponse;
import com.revplay.musicplatform.common.response.ApiResponse;
import com.revplay.musicplatform.common.response.FieldError;
import com.revplay.musicplatform.config.FileStorageProperties;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

@Tag("unit")
class DtoContractCoverageTest {

    @ParameterizedTest(name = "{0}")
    @MethodSource("dtoTypes")
    @DisplayName("simple data classes honor standard object contracts")
    void simpleDataClassesHonorStandardObjectContracts(Class<?> type) throws Exception {
        Object emptyLeft = createSample(type, 0);
        Object emptyRight = createSample(type, 0);
        Object populatedLeft = createSample(type, 1);
        Object populatedRight = createSample(type, 1);
        Object different = createSample(type, 2);

        assertThat(emptyLeft).isEqualTo(emptyRight);
        assertThat(emptyLeft.hashCode()).isEqualTo(emptyRight.hashCode());

        assertThat(populatedLeft).isEqualTo(populatedLeft);
        assertThat(populatedLeft).isEqualTo(populatedRight);
        assertThat(populatedLeft.hashCode()).isEqualTo(populatedRight.hashCode());
        assertThat(populatedLeft).isNotEqualTo(different);
        assertThat(populatedLeft).isNotEqualTo(null);
        assertThat(populatedLeft).isNotEqualTo("other");
        assertThat(populatedLeft.toString()).contains(type.getSimpleName());

        if (type.isRecord()) {
            assertRecordFieldMismatches(type, populatedLeft);
        } else {
            assertBeanFieldMismatches(type, populatedLeft);
            for (PropertyDescriptor property : Introspector.getBeanInfo(type, Object.class).getPropertyDescriptors()) {
                Method readMethod = property.getReadMethod();
                if (readMethod == null || property.getName().equals("class")) {
                    continue;
                }
                Object value = readMethod.invoke(populatedLeft);
                if (readMethod.getReturnType().isPrimitive()) {
                    assertThat(value).isNotNull();
                }
            }
        }
    }

    private static Stream<Class<?>> dtoTypes() {
        return Stream.of(
                AlbumCreateRequest.class,
                AlbumUpdateRequest.class,
                GenreUpsertRequest.class,
                PodcastCategoryCreateRequest.class,
                PodcastCreateRequest.class,
                PodcastEpisodeCreateRequest.class,
                PodcastEpisodeUpdateRequest.class,
                PodcastUpdateRequest.class,
                SearchRequest.class,
                SongCreateRequest.class,
                SongGenresRequest.class,
                SongUpdateRequest.class,
                SongVisibilityRequest.class,
                AlbumResponse.class,
                DiscoverWeeklyResponse.class,
                DiscoveryFeedResponse.class,
                DiscoveryRecommendationItemResponse.class,
                GenreResponse.class,
                ImageUploadResponse.class,
                NewReleaseItemResponse.class,
                PodcastCategoryResponse.class,
                PodcastEpisodeResponse.class,
                PodcastResponse.class,
                PopularPodcastItemResponse.class,
                SearchResultItemResponse.class,
                SongResponse.class,
                TopArtistItemResponse.class,
                ArtistCreateRequest.class,
                ArtistSocialLinkCreateRequest.class,
                ArtistSocialLinkUpdateRequest.class,
                ArtistUpdateRequest.class,
                ArtistVerifyRequest.class,
                ArtistResponse.class,
                ArtistSocialLinkResponse.class,
                ArtistSummaryResponse.class,
                ApiResponse.class,
                FieldError.class,
                FileStorageProperties.class
        );
    }

    private static Object createSample(Class<?> type, int variant) throws Exception {
        if (type.isRecord()) {
            RecordComponent[] components = type.getRecordComponents();
            Class<?>[] parameterTypes = new Class<?>[components.length];
            Object[] args = new Object[components.length];
            for (int i = 0; i < components.length; i++) {
                parameterTypes[i] = components[i].getType();
                args[i] = sampleValue(components[i].getType(), components[i].getName(), variant);
            }
            Constructor<?> constructor = type.getDeclaredConstructor(parameterTypes);
            constructor.setAccessible(true);
            return constructor.newInstance(args);
        }

        Constructor<?> constructor = type.getDeclaredConstructor();
        constructor.setAccessible(true);
        Object instance = constructor.newInstance();
        for (PropertyDescriptor property : Introspector.getBeanInfo(instance.getClass(), Object.class).getPropertyDescriptors()) {
            Method writeMethod = property.getWriteMethod();
            if (writeMethod == null || property.getName().equals("class")) {
                continue;
            }
            Object value = sampleValue(property.getPropertyType(), property.getName(), variant);
            writeMethod.invoke(instance, value);
        }
        return instance;
    }

    private static void assertBeanFieldMismatches(Class<?> type, Object baseline) throws Exception {
        for (PropertyDescriptor property : Introspector.getBeanInfo(type, Object.class).getPropertyDescriptors()) {
            Method writeMethod = property.getWriteMethod();
            Method readMethod = property.getReadMethod();
            if (writeMethod == null || readMethod == null || property.getName().equals("class")) {
                continue;
            }
            Object candidate = createSample(type, 1);
            Object currentValue = readMethod.invoke(baseline);
            Object changedValue = differentValue(property.getPropertyType(), property.getName(), currentValue);
            if (java.util.Objects.equals(currentValue, changedValue)) {
                continue;
            }
            writeMethod.invoke(candidate, changedValue);
            assertThat(baseline).isNotEqualTo(candidate);
        }
    }

    private static void assertRecordFieldMismatches(Class<?> type, Object baseline) throws Exception {
        RecordComponent[] components = type.getRecordComponents();
        for (int changedIndex = 0; changedIndex < components.length; changedIndex++) {
            Class<?>[] parameterTypes = new Class<?>[components.length];
            Object[] args = new Object[components.length];
            for (int i = 0; i < components.length; i++) {
                parameterTypes[i] = components[i].getType();
                args[i] = sampleValue(components[i].getType(), components[i].getName(), 1);
            }
            Object currentValue = components[changedIndex].getAccessor().invoke(baseline);
            Object changedValue = differentValue(components[changedIndex].getType(), components[changedIndex].getName(), currentValue);
            if (java.util.Objects.equals(currentValue, changedValue)) {
                continue;
            }
            args[changedIndex] = changedValue;
            Constructor<?> constructor = type.getDeclaredConstructor(parameterTypes);
            constructor.setAccessible(true);
            Object candidate = constructor.newInstance(args);
            assertThat(baseline).isNotEqualTo(candidate);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static Object differentValue(Class<?> type, String name, Object currentValue) {
        if (type.equals(Boolean.class) || type.equals(boolean.class)) {
            return !(Boolean) currentValue;
        }
        if (type.equals(String.class)) {
            return name + "-different";
        }
        if (type.equals(Long.class) || type.equals(long.class)) {
            return ((Number) currentValue).longValue() + 1L;
        }
        if (type.equals(Integer.class) || type.equals(int.class)) {
            return ((Number) currentValue).intValue() + 1;
        }
        if (type.equals(Double.class) || type.equals(double.class)) {
            return ((Number) currentValue).doubleValue() + 1.0d;
        }
        if (type.equals(Float.class) || type.equals(float.class)) {
            return ((Number) currentValue).floatValue() + 1.0f;
        }
        if (type.equals(BigDecimal.class)) {
            return ((BigDecimal) currentValue).add(BigDecimal.ONE);
        }
        if (type.equals(LocalDate.class)) {
            return ((LocalDate) currentValue).plusDays(1);
        }
        if (type.equals(LocalDateTime.class)) {
            return ((LocalDateTime) currentValue).plusDays(1);
        }
        if (type.equals(Instant.class)) {
            return ((Instant) currentValue).plusSeconds(1);
        }
        if (List.class.isAssignableFrom(type)) {
            return List.of("different");
        }
        if (Set.class.isAssignableFrom(type)) {
            return Set.of("different");
        }
        if (type.isEnum()) {
            Object[] constants = type.getEnumConstants();
            for (Object constant : constants) {
                if (!constant.equals(currentValue)) {
                    return constant;
                }
            }
            return currentValue;
        }
        return sampleValue(type, name, 2);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static Object sampleValue(Class<?> type, String name, int variant) {
        if (variant == 0) {
            if (type.isPrimitive()) {
                if (type.equals(boolean.class)) {
                    return false;
                }
                if (type.equals(char.class)) {
                    return '\0';
                }
                if (type.equals(float.class)) {
                    return 0.0f;
                }
                if (type.equals(double.class)) {
                    return 0.0d;
                }
                if (type.equals(long.class)) {
                    return 0L;
                }
                return 0;
            }
            return null;
        }
        if (type.equals(String.class)) {
            return name + "-" + variant;
        }
        if (type.equals(Long.class) || type.equals(long.class)) {
            return 100L + variant;
        }
        if (type.equals(Integer.class) || type.equals(int.class)) {
            return 10 + variant;
        }
        if (type.equals(Boolean.class) || type.equals(boolean.class)) {
            return variant % 2 == 0;
        }
        if (type.equals(Double.class) || type.equals(double.class)) {
            return 1.5d + variant;
        }
        if (type.equals(Float.class) || type.equals(float.class)) {
            return 2.5f + variant;
        }
        if (type.equals(BigDecimal.class)) {
            return BigDecimal.valueOf(100 + variant);
        }
        if (type.equals(LocalDate.class)) {
            return LocalDate.of(2026, 1, 1).plusDays(variant);
        }
        if (type.equals(LocalDateTime.class)) {
            return LocalDateTime.of(2026, 1, 1, 10, 0).plusDays(variant);
        }
        if (type.equals(Instant.class)) {
            return Instant.parse("2026-01-01T00:00:00Z").plusSeconds(variant);
        }
        if (List.class.isAssignableFrom(type)) {
            return List.of("item-" + variant);
        }
        if (Set.class.isAssignableFrom(type)) {
            return Set.of("item-" + variant);
        }
        if (type.isEnum()) {
            Object[] constants = type.getEnumConstants();
            return constants[Math.min(variant - 1, constants.length - 1)];
        }
        return null;
    }
}
