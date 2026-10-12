package com.example.universityenrollmentsystem.domain.service.enrollment;

import com.example.universityenrollmentsystem.domain.entity.student.EnrollmentPlanType;
import com.example.universityenrollmentsystem.domain.exception.validation.DomainValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EnrollmentPlanFactoryTest {

    private EnrollmentPlanFactory factory;
    private RegularEnrollmentPlanStrategy regularStrategy;
    private ProbationEnrollmentPlanStrategy probationStrategy;
    private HonorsEnrollmentPlanStrategy honorsStrategy;
    private GuestEnrollmentPlanStrategy guestStrategy;

    @BeforeEach
    void setUp() {
        regularStrategy = new RegularEnrollmentPlanStrategy();
        probationStrategy = new ProbationEnrollmentPlanStrategy();
        honorsStrategy = new HonorsEnrollmentPlanStrategy();
        guestStrategy = new GuestEnrollmentPlanStrategy();

        factory = new EnrollmentPlanFactory(List.of(
                regularStrategy,
                probationStrategy,
                honorsStrategy,
                guestStrategy
        ));
    }

    @Test
    @DisplayName("Should return RegularEnrollmentPlanStrategy for REGULAR plan type")
    void shouldReturnRegularStrategy() {
        EnrollmentPlanStrategy result = factory.getStrategy(EnrollmentPlanType.REGULAR);
        assertThat(result).isSameAs(regularStrategy);
    }

    @Test
    @DisplayName("Should return ProbationEnrollmentPlanStrategy for PROBATION plan type")
    void shouldReturnProbationStrategy() {
        EnrollmentPlanStrategy result = factory.getStrategy(EnrollmentPlanType.PROBATION);
        assertThat(result).isSameAs(probationStrategy);
    }

    @Test
    @DisplayName("Should return HonorsEnrollmentPlanStrategy for HONORS plan type")
    void shouldReturnHonorsStrategy() {
        EnrollmentPlanStrategy result = factory.getStrategy(EnrollmentPlanType.HONORS);
        assertThat(result).isSameAs(honorsStrategy);
    }

    @Test
    @DisplayName("Should return GuestEnrollmentPlanStrategy for GUEST plan type")
    void shouldReturnGuestStrategy() {
        EnrollmentPlanStrategy result = factory.getStrategy(EnrollmentPlanType.GUEST);
        assertThat(result).isSameAs(guestStrategy);
    }

    @Test
    @DisplayName("Should throw exception when strategy is missing for given plan type")
    void shouldThrowExceptionWhenStrategyMissing() {
        EnrollmentPlanFactory emptyFactory = new EnrollmentPlanFactory(List.of());

        assertThatThrownBy(() -> emptyFactory.getStrategy(EnrollmentPlanType.REGULAR))
                .isInstanceOf(DomainValidationException.class)
                .hasMessageContaining("Strategy not found for plan type: REGULAR");
    }

    @Test
    @DisplayName("Should throw exception when plan type is null")
    void shouldThrowExceptionWhenPlanTypeIsNull() {
        assertThatThrownBy(() -> factory.getStrategy(null))
                .isInstanceOf(DomainValidationException.class);
    }
}
