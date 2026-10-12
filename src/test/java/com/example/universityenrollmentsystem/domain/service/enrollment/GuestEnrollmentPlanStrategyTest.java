package com.example.universityenrollmentsystem.domain.service.enrollment;

import com.example.universityenrollmentsystem.domain.entity.academic.Major;
import com.example.universityenrollmentsystem.domain.entity.academic.Semester;
import com.example.universityenrollmentsystem.domain.entity.course.Course;
import com.example.universityenrollmentsystem.domain.entity.course.CourseOffering;
import com.example.universityenrollmentsystem.domain.entity.student.EnrollmentPlanType;
import com.example.universityenrollmentsystem.domain.entity.student.Student;
import com.example.universityenrollmentsystem.domain.exception.rule.GuestPolicyViolationException;
import com.example.universityenrollmentsystem.domain.exception.rule.MaxCreditsExceededException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GuestEnrollmentPlanStrategyTest {

    private static final int MAX_CREDITS = 20;

    private GuestEnrollmentPlanStrategy strategy;
    private Major computerScience;
    private Major mechanicalEngineering;
    private Semester fallSemester;
    private Student student;

    @BeforeEach
    void setUp() {
        strategy = new GuestEnrollmentPlanStrategy();
        computerScience = new Major("Computer Science");
        mechanicalEngineering = new Major("Mechanical Engineering");
        fallSemester = new Semester("Fall 2026", LocalDate.of(2026, 9, 23), LocalDate.of(2027, 1, 20));
        student = new Student("Babak Khoramdin", "401555666", computerScience, EnrollmentPlanType.GUEST);
    }

    @Test
    @DisplayName("Should return GUEST plan type")
    void shouldReturnGuestPlanType() {
        assertThat(strategy.getPlanType()).isEqualTo(EnrollmentPlanType.GUEST);
    }

    @Test
    @DisplayName("Should validate successfully when total credits are exactly at maximum limit of 20")
    void shouldValidateSuccessfullyWhenCreditsAreExactlyAtMaxLimit() {
        List<CourseOffering> offerings = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            Course course = createCourse("Course " + i, "CS" + (100 + i), 4, computerScience);
            offerings.add(createOffering(course, fallSemester, true));
        }

        assertThatCode(() -> strategy.validate(student, offerings))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should throw exception when requested credits exceed maximum limit of 20")
    void shouldThrowExceptionWhenCreditsExceedMaxLimit() {
        List<CourseOffering> offerings = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            Course course = createCourse("Course " + i, "CS" + (100 + i), 4, computerScience);
            offerings.add(createOffering(course, fallSemester, true));
        }
        Course extraCourse = createCourse("Extra Course", "CS106", 1, computerScience);
        offerings.add(createOffering(extraCourse, fallSemester, true));

        assertThatThrownBy(() -> strategy.validate(student, offerings))
                .isInstanceOf(MaxCreditsExceededException.class)
                .hasMessageContaining(String.valueOf(MAX_CREDITS));
    }

    @Test
    @DisplayName("Should allow courses from other majors if guest is allowed")
    void shouldAllowCoursesFromAnyMajorWhenGuestAllowed() {
        Course outsideCourse = createCourse("Thermodynamics", "ME101", 3, mechanicalEngineering);
        CourseOffering offering = createOffering(outsideCourse, fallSemester, true);

        assertThatCode(() -> strategy.validate(student, List.of(offering)))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should throw exception when course does not allow guest students")
    void shouldThrowExceptionWhenCourseDoesNotAllowGuests() {
        Course course = createCourse("Algorithms", "CS301", 3, computerScience);
        CourseOffering offering = createOffering(course, fallSemester, false);

        assertThatThrownBy(() -> strategy.validate(student, List.of(offering)))
                .isInstanceOf(GuestPolicyViolationException.class);
    }

    private Course createCourse(String name, String code, int credits, Major major) {
        Course course = new Course(name, code, credits);
        major.addCourse(course);
        return course;
    }

    private CourseOffering createOffering(Course course, Semester semester, boolean guestAllowed) {
        return new CourseOffering(
                course,
                semester,
                "Dr. Tehrani",
                "01",
                guestAllowed,
                30,
                "Tue 14:00",
                semester.getEndDate().minusWeeks(2)
        );
    }
}
