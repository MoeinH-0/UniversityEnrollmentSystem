package com.example.universityenrollmentsystem.domain.entity.student;

import com.example.universityenrollmentsystem.domain.entity.academic.Major;
import com.example.universityenrollmentsystem.domain.entity.academic.Semester;
import com.example.universityenrollmentsystem.domain.entity.course.Course;
import com.example.universityenrollmentsystem.domain.entity.course.CourseOffering;
import com.example.universityenrollmentsystem.domain.entity.course.CourseStatus;
import com.example.universityenrollmentsystem.domain.exception.rule.DuplicateEnrollmentException;
import com.example.universityenrollmentsystem.domain.exception.state.InvalidEnrollmentStateException;
import com.example.universityenrollmentsystem.domain.exception.validation.DomainValidationException;
import com.example.universityenrollmentsystem.domain.service.enrollment.EnrollmentPlanStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;

class StudentTest {

    private Major computerScience;
    private Major mathematics;
    private Semester semester;
    private Course course1;
    private Course course2;
    private CourseOffering offering1;
    private CourseOffering offering2;
    private EnrollmentPlanStrategy mockStrategy;

    @BeforeEach
    void setUp() {
        computerScience = new Major("Computer Science");
        mathematics = new Major("Mathematics");
        semester = new Semester("Fall 2026", LocalDate.of(2026, 9, 23), LocalDate.of(2027, 1, 20));

        course1 = new Course("Data Structures", "CS201", 3);
        course2 = new Course("Calculus 1", "MATH101", 3);
        computerScience.addCourse(course1);
        mathematics.addCourse(course2);

        offering1 = new CourseOffering(course1, semester, "Dr. A", "01", true, 30, "Mon 10:00", semester.getEndDate().minusWeeks(2));
        offering2 = new CourseOffering(course2, semester, "Dr. B", "01", true, 30, "Tue 10:00", semester.getEndDate().minusWeeks(2));

        mockStrategy = Mockito.mock(EnrollmentPlanStrategy.class);
    }

    @Test
    @DisplayName("Should initialize student with ACTIVE status and empty enrollments")
    void shouldInitializeStudentCorrectly() {
        Student student = new Student("Ali Reza", "401001", computerScience, EnrollmentPlanType.REGULAR);

        assertThat(student.getFullName()).isEqualTo("Ali Reza");
        assertThat(student.getStudentNumber()).isEqualTo("401001");
        assertThat(student.getMajor()).isEqualTo(computerScience);
        assertThat(student.getPlanType()).isEqualTo(EnrollmentPlanType.REGULAR);
        assertThat(student.getStatus()).isEqualTo(StudentStatus.ACTIVE);
        assertThat(student.getEnrollments()).isEmpty();
    }

    @Test
    @DisplayName("Should throw exception when constructor arguments are invalid")
    void shouldThrowExceptionWhenConstructorArgsInvalid() {
        assertThatThrownBy(() -> new Student(null, "401001", computerScience, EnrollmentPlanType.REGULAR))
                .isInstanceOf(DomainValidationException.class);

        assertThatThrownBy(() -> new Student("   ", "401001", computerScience, EnrollmentPlanType.REGULAR))
                .isInstanceOf(DomainValidationException.class);

        assertThatThrownBy(() -> new Student("Ali", null, computerScience, EnrollmentPlanType.REGULAR))
                .isInstanceOf(DomainValidationException.class);

        assertThatThrownBy(() -> new Student("Ali", "401001", null, EnrollmentPlanType.REGULAR))
                .isInstanceOf(DomainValidationException.class);

        assertThatThrownBy(() -> new Student("Ali", "401001", computerScience, null))
                .isInstanceOf(DomainValidationException.class);
    }

    @Test
    @DisplayName("Should update student contact info")
    void shouldUpdateContactInfo() {
        Student student = new Student("Ali Reza", "401001", computerScience, EnrollmentPlanType.REGULAR);

        student.updateContactInfo("ali@example.com", "09123456789");

        assertThat(student.getEmail()).isEqualTo("ali@example.com");
        assertThat(student.getPhoneNumber()).isEqualTo("09123456789");
    }

    @Test
    @DisplayName("Should update student fields successfully")
    void shouldUpdateStudentFields() {
        Student student = new Student("Ali Reza", "401001", computerScience, EnrollmentPlanType.REGULAR);

        student.updateFullName("Ali Rezaei");
        student.updateMajor(mathematics);
        student.updatePlanType(EnrollmentPlanType.HONORS);
        student.updateStatus(StudentStatus.GRADUATED);

        assertThat(student.getFullName()).isEqualTo("Ali Rezaei");
        assertThat(student.getMajor()).isEqualTo(mathematics);
        assertThat(student.getPlanType()).isEqualTo(EnrollmentPlanType.HONORS);
        assertThat(student.getStatus()).isEqualTo(StudentStatus.GRADUATED);
    }

    @Test
    @DisplayName("Should throw exception when updating fields with null or invalid values")
    void shouldThrowExceptionWhenUpdatingWithInvalidValues() {
        Student student = new Student("Ali Reza", "401001", computerScience, EnrollmentPlanType.REGULAR);

        assertThatThrownBy(() -> student.updateFullName("  "))
                .isInstanceOf(DomainValidationException.class);

        assertThatThrownBy(() -> student.updateMajor(null))
                .isInstanceOf(DomainValidationException.class);

        assertThatThrownBy(() -> student.updatePlanType(null))
                .isInstanceOf(DomainValidationException.class);

        assertThatThrownBy(() -> student.updateStatus(null))
                .isInstanceOf(DomainValidationException.class);
    }

    @Test
    @DisplayName("Should enroll student in course offerings and invoke strategy validation")
    void shouldEnrollStudentSuccessfully() {
        Student student = new Student("Ali Reza", "401001", computerScience, EnrollmentPlanType.REGULAR);

        student.enroll(List.of(offering1), mockStrategy);

        verify(mockStrategy).validate(student, List.of(offering1));
        assertThat(student.getEnrollments()).hasSize(1);
        assertThat(student.getEnrollments().get(0).getCourseOffering()).isEqualTo(offering1);
        assertThat(student.getEnrollments().get(0).getStatus()).isEqualTo(CourseStatus.IN_PROGRESS);
    }

    @Test
    @DisplayName("Should throw exception when enrolling in empty course list or null strategy")
    void shouldThrowExceptionWhenOfferingsOrStrategyInvalid() {
        Student student = new Student("Ali Reza", "401001", computerScience, EnrollmentPlanType.REGULAR);

        assertThatThrownBy(() -> student.enroll(List.of(), mockStrategy))
                .isInstanceOf(DomainValidationException.class);

        assertThatThrownBy(() -> student.enroll(List.of(offering1), null))
                .isInstanceOf(DomainValidationException.class);
    }

    @Test
    @DisplayName("Should throw exception when enrolling in course already in progress or passed")
    void shouldThrowExceptionWhenEnrollingInDuplicateCourse() {
        Student student = new Student("Ali Reza", "401001", computerScience, EnrollmentPlanType.REGULAR);
        student.enroll(List.of(offering1), mockStrategy);

        assertThatThrownBy(() -> student.enroll(List.of(offering1), mockStrategy))
                .isInstanceOf(DuplicateEnrollmentException.class);

        student.getEnrollments().get(0).markAsPassed();

        assertThatThrownBy(() -> student.enroll(List.of(offering1), mockStrategy))
                .isInstanceOf(DuplicateEnrollmentException.class);
    }

    @Test
    @DisplayName("Should drop in-progress course offering successfully")
    void shouldDropCourseSuccessfully() {
        Student student = new Student("Ali Reza", "401001", computerScience, EnrollmentPlanType.REGULAR);
        student.enroll(List.of(offering1), mockStrategy);

        student.drop(offering1);

        assertThat(student.getEnrollments()).isEmpty();
    }

    @Test
    @DisplayName("Should throw exception when dropping course not in progress or not enrolled")
    void shouldThrowExceptionWhenDroppingInvalidCourse() {
        Student student = new Student("Ali Reza", "401001", computerScience, EnrollmentPlanType.REGULAR);
        student.enroll(List.of(offering1), mockStrategy);

        assertThatThrownBy(() -> student.drop(offering2))
                .isInstanceOf(InvalidEnrollmentStateException.class);

        student.getEnrollments().get(0).markAsPassed();

        assertThatThrownBy(() -> student.drop(offering1))
                .isInstanceOf(InvalidEnrollmentStateException.class);
    }

    @Test
    @DisplayName("Should successfully enroll in course offering previously marked as FAILED")
    void shouldAllowEnrollingInPreviouslyFailedCourse() {
        Student student = new Student("Ali Reza", "401001", computerScience, EnrollmentPlanType.REGULAR);
        student.enroll(List.of(offering1), mockStrategy);
        student.getEnrollments().get(0).markAsFailed();

        student.enroll(List.of(offering1), mockStrategy);

        assertThat(student.getEnrollments()).hasSize(2);
    }

    @Test
    @DisplayName("Should enroll in multiple distinct course offerings simultaneously")
    void shouldEnrollInMultipleCourseOfferingsSimultaneously() {
        Student student = new Student("Ali Reza", "401001", computerScience, EnrollmentPlanType.REGULAR);

        student.enroll(List.of(offering1, offering2), mockStrategy);

        assertThat(student.getEnrollments()).hasSize(2);
    }

    @Test
    @DisplayName("Should throw UnsupportedOperationException when trying to modify unmodifiable enrollments list directly")
    void shouldThrowExceptionWhenModifyingEnrollmentsListDirectly() {
        Student student = new Student("Ali Reza", "401001", computerScience, EnrollmentPlanType.REGULAR);

        assertThatThrownBy(() -> student.getEnrollments().add(null))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
