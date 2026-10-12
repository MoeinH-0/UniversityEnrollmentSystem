package com.example.universityenrollmentsystem.domain.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class BaseEntityTest {

    private static class SampleEntityA extends BaseEntity<Long> {}

    private static class SampleEntityB extends BaseEntity<Long> {}

    @Test
    @DisplayName("Should return false when comparing entities with null id")
    void shouldReturnFalseWhenBothIdsNull() {
        SampleEntityA entity1 = new SampleEntityA();
        SampleEntityA entity2 = new SampleEntityA();

        assertThat(entity1.equals(entity2)).isFalse();
    }

    @Test
    @DisplayName("Should return false when comparing entity to null")
    void shouldReturnFalseWhenComparingToNull() {
        SampleEntityA entity = new SampleEntityA();
        ReflectionTestUtils.setField(entity, "id", 1L);

        assertThat(entity.equals(null)).isFalse();
    }

    @Test
    @DisplayName("Should return true when comparing entity to itself")
    void shouldReturnTrueWhenComparingToItself() {
        SampleEntityA entity = new SampleEntityA();

        assertThat(entity.equals(entity)).isTrue();
    }

    @Test
    @DisplayName("Should return false when comparing different entity types with same id")
    void shouldReturnFalseWhenComparingDifferentTypesWithSameId() {
        SampleEntityA entityA = new SampleEntityA();
        SampleEntityB entityB = new SampleEntityB();
        ReflectionTestUtils.setField(entityA, "id", 1L);
        ReflectionTestUtils.setField(entityB, "id", 1L);

        assertThat(entityA.equals(entityB)).isFalse();
    }

    @Test
    @DisplayName("Should return true when comparing same entity type with same id")
    void shouldReturnTrueWhenSameTypeAndSameId() {
        SampleEntityA entity1 = new SampleEntityA();
        SampleEntityA entity2 = new SampleEntityA();
        ReflectionTestUtils.setField(entity1, "id", 1L);
        ReflectionTestUtils.setField(entity2, "id", 1L);

        assertThat(entity1.equals(entity2)).isTrue();
    }

    @Test
    @DisplayName("Should have identical hashCodes for instances of the same class regardless of id")
    void shouldHaveSameHashCodeForSameClass() {
        SampleEntityA entity1 = new SampleEntityA();
        SampleEntityA entity2 = new SampleEntityA();
        ReflectionTestUtils.setField(entity1, "id", 1L);
        ReflectionTestUtils.setField(entity2, "id", 2L);

        assertThat(entity1.hashCode()).isEqualTo(entity2.hashCode());
    }
}
