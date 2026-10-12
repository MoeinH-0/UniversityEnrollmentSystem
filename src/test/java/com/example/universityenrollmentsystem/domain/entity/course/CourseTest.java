package com.example.universityenrollmentsystem.domain.entity.course;

import com.example.universityenrollmentsystem.domain.exception.state.InvalidCourseStateException;
import com.example.universityenrollmentsystem.domain.exception.validation.DomainValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CourseTest {

    private static final int MIN_CREDITS = 1;
    private static final int MAX_CREDITS = 4;
    private static final int BELOW_MIN_CREDITS = 0;
    private static final int ABOVE_MAX_CREDITS = 5;

    @Test
    @DisplayName("Should initialize course successfully with valid arguments")
    void shouldInitializeCourseSuccessfully() {
        Course course = new Course("Software Engineering", "SE101", 3);

        assertThat(course.getName()).isEqualTo("Software Engineering");
        assertThat(course.getCourseCode()).isEqualTo("SE101");
        assertThat(course.getCredits()).isEqualTo(3);
        assertThat(course.getPrerequisites()).isEmpty();
        assertThat(course.isDeleted()).isFalse();
    }

    @Test
    @DisplayName("Should throw exception when name or course code is null or blank")
    void shouldThrowExceptionWhenNameOrCodeInvalid() {
        assertThatThrownBy(() -> new Course(null, "SE101", 3))
                .isInstanceOf(DomainValidationException.class);

        assertThatThrownBy(() -> new Course("   ", "SE101", 3))
                .isInstanceOf(DomainValidationException.class);

        assertThatThrownBy(() -> new Course("SE", null, 3))
                .isInstanceOf(DomainValidationException.class);

        assertThatThrownBy(() -> new Course("SE", "  ", 3))
                .isInstanceOf(DomainValidationException.class);
    }

    @Test
    @DisplayName("Should throw exception when credits are outside allowable range of 1 to 4")
    void shouldThrowExceptionWhenCreditsOutOfRange() {
        assertThatThrownBy(() -> new Course("SE", "SE101", BELOW_MIN_CREDITS))
                .isInstanceOf(DomainValidationException.class);

        assertThatThrownBy(() -> new Course("SE", "SE101", ABOVE_MAX_CREDITS))
                .isInstanceOf(DomainValidationException.class);
    }

    @Test
    @DisplayName("Should allow boundary credit values of 1 and 4")
    void shouldAllowBoundaryCreditValues() {
        Course minCourse = new Course("Min Course", "C1", MIN_CREDITS);
        Course maxCourse = new Course("Max Course", "C2", MAX_CREDITS);

        assertThat(minCourse.getCredits()).isEqualTo(MIN_CREDITS);
        assertThat(maxCourse.getCredits()).isEqualTo(MAX_CREDITS);
    }

    @Test
    @DisplayName("Should update course fields and validate new values")
    void shouldUpdateFieldsSuccessfully() {
        Course course = new Course("SE", "SE101", 3);

        course.updateName("Advanced SE");
        course.updateCourseCode("SE201");
        course.updateCredits(MAX_CREDITS);

        assertThat(course.getName()).isEqualTo("Advanced SE");
        assertThat(course.getCourseCode()).isEqualTo("SE201");
        assertThat(course.getCredits()).isEqualTo(MAX_CREDITS);
    }

    @Test
    @DisplayName("Should add prerequisite course successfully")
    void shouldAddPrerequisiteSuccessfully() {
        Course course = new Course("Advanced Programming", "CS102", 3);
        Course prerequisite = new Course("Basic Programming", "CS101", 3);

        course.addPrerequisite(prerequisite);

        assertThat(course.getPrerequisites()).containsExactly(prerequisite);
    }

    @Test
    @DisplayName("Should throw exception when course adds itself as prerequisite")
    void shouldThrowExceptionWhenAddingSelfAsPrerequisite() {
        Course course = new Course("Programming", "CS101", 3);

        assertThatThrownBy(() -> course.addPrerequisite(course))
                .isInstanceOf(InvalidCourseStateException.class);
    }

    @Test
    @DisplayName("Should throw exception when adding duplicate prerequisite")
    void shouldThrowExceptionWhenAddingDuplicatePrerequisite() {
        Course course = new Course("Advanced Programming", "CS102", 3);
        Course prerequisite = new Course("Basic Programming", "CS101", 3);
        course.addPrerequisite(prerequisite);

        assertThatThrownBy(() -> course.addPrerequisite(prerequisite))
                .isInstanceOf(InvalidCourseStateException.class);
    }

    @Test
    @DisplayName("Should remove prerequisite successfully")
    void shouldRemovePrerequisiteSuccessfully() {
        Course course = new Course("Advanced Programming", "CS102", 3);
        Course prerequisite = new Course("Basic Programming", "CS101", 3);
        course.addPrerequisite(prerequisite);

        course.removePrerequisite(prerequisite);

        assertThat(course.getPrerequisites()).isEmpty();
    }

    @Test
    @DisplayName("Should throw exception when removing non-existent prerequisite")
    void shouldThrowExceptionWhenRemovingNonExistentPrerequisite() {
        Course course = new Course("Advanced Programming", "CS102", 3);
        Course nonPrerequisite = new Course("Basic Programming", "CS101", 3);

        assertThatThrownBy(() -> course.removePrerequisite(nonPrerequisite))
                .isInstanceOf(InvalidCourseStateException.class);
    }

    @Test
    @DisplayName("Should clear all prerequisites")
    void shouldClearPrerequisites() {
        Course course = new Course("Course", "C1", 3);
        Course pre1 = new Course("Pre1", "P1", 2);
        Course pre2 = new Course("Pre2", "P2", 2);
        course.addPrerequisite(pre1);
        course.addPrerequisite(pre2);

        course.clearPrerequisites();

        assertThat(course.getPrerequisites()).isEmpty();
    }

    @Test
    @DisplayName("Should mark course as deleted")
    void shouldMarkAsDeleted() {
        Course course = new Course("Course", "C1", 3);

        course.markAsDeleted();

        assertThat(course.isDeleted()).isTrue();
    }

    @Test
    @DisplayName("Should throw UnsupportedOperationException when trying to modify unmodifiable prerequisites or majors lists")
    void shouldThrowExceptionWhenModifyingPrerequisitesOrMajorsListsDirectly() {
        Course course = new Course("Course", "C1", 3);
        Course pre = new Course("Pre", "P1", 2);

        assertThatThrownBy(() -> course.getPrerequisites().add(pre))
                .isInstanceOf(UnsupportedOperationException.class);

        assertThatThrownBy(() -> course.getMajors().add(null))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
