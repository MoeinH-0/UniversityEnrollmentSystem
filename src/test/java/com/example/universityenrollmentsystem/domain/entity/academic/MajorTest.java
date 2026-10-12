package com.example.universityenrollmentsystem.domain.entity.academic;

import com.example.universityenrollmentsystem.domain.entity.course.Course;
import com.example.universityenrollmentsystem.domain.exception.state.InvalidMajorStateException;
import com.example.universityenrollmentsystem.domain.exception.validation.DomainValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MajorTest {

    @Test
    @DisplayName("Should initialize major successfully with valid name")
    void shouldInitializeMajorSuccessfully() {
        Major major = new Major("Computer Engineering");

        assertThat(major.getName()).isEqualTo("Computer Engineering");
        assertThat(major.getCourses()).isEmpty();
        assertThat(major.isDeleted()).isFalse();
    }

    @Test
    @DisplayName("Should throw exception when name is null or blank")
    void shouldThrowExceptionWhenNameNullOrBlank() {
        assertThatThrownBy(() -> new Major(null))
                .isInstanceOf(DomainValidationException.class);

        assertThatThrownBy(() -> new Major("  "))
                .isInstanceOf(DomainValidationException.class);
    }

    @Test
    @DisplayName("Should update major name successfully")
    void shouldUpdateNameSuccessfully() {
        Major major = new Major("Computer Engineering");

        major.updateName("Software Engineering");

        assertThat(major.getName()).isEqualTo("Software Engineering");
    }

    @Test
    @DisplayName("Should add course to major successfully")
    void shouldAddCourseToMajor() {
        Major major = new Major("Computer Engineering");
        Course course = new Course("Algorithms", "CS301", 3);

        major.addCourse(course);

        assertThat(major.getCourses()).containsExactly(course);
    }

    @Test
    @DisplayName("Should throw exception when adding null or duplicate course to major")
    void shouldThrowExceptionWhenAddingNullOrDuplicateCourse() {
        Major major = new Major("Computer Engineering");
        Course course = new Course("Algorithms", "CS301", 3);
        major.addCourse(course);

        assertThatThrownBy(() -> major.addCourse(null))
                .isInstanceOf(DomainValidationException.class);

        assertThatThrownBy(() -> major.addCourse(course))
                .isInstanceOf(InvalidMajorStateException.class)
                .hasMessageContaining("already added");
    }

    @Test
    @DisplayName("Should remove course from major successfully")
    void shouldRemoveCourseFromMajor() {
        Major major = new Major("Computer Engineering");
        Course course = new Course("Algorithms", "CS301", 3);
        major.addCourse(course);

        major.removeCourse(course);

        assertThat(major.getCourses()).isEmpty();
    }

    @Test
    @DisplayName("Should throw exception when removing non-existent or null course from major")
    void shouldThrowExceptionWhenRemovingInvalidCourse() {
        Major major = new Major("Computer Engineering");
        Course course = new Course("Algorithms", "CS301", 3);

        assertThatThrownBy(() -> major.removeCourse(null))
                .isInstanceOf(DomainValidationException.class);

        assertThatThrownBy(() -> major.removeCourse(course))
                .isInstanceOf(InvalidMajorStateException.class)
                .hasMessageContaining("not part of this major");
    }

    @Test
    @DisplayName("Should mark major as deleted")
    void shouldMarkAsDeleted() {
        Major major = new Major("Computer Engineering");

        major.markAsDeleted();

        assertThat(major.isDeleted()).isTrue();
    }

    @Test
    @DisplayName("Should throw UnsupportedOperationException when trying to modify unmodifiable courses list")
    void shouldThrowExceptionWhenModifyingCoursesListDirectly() {
        Major major = new Major("Computer Engineering");
        Course course = new Course("Algorithms", "CS301", 3);

        assertThatThrownBy(() -> major.getCourses().add(course))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
