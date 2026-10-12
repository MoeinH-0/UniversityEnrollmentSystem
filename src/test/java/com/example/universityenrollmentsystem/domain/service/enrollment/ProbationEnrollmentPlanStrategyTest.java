package com.example.universityenrollmentsystem.domain.service.enrollment;

import com.example.universityenrollmentsystem.domain.entity.academic.Major;
import com.example.universityenrollmentsystem.domain.entity.academic.Semester;
import com.example.universityenrollmentsystem.domain.entity.course.Course;
import com.example.universityenrollmentsystem.domain.entity.course.CourseOffering;
import com.example.universityenrollmentsystem.domain.entity.student.EnrollmentPlanType;
import com.example.universityenrollmentsystem.domain.entity.student.Student;
import com.example.universityenrollmentsystem.domain.exception.rule.CourseNotInMajorException;
import com.example.universityenrollmentsystem.domain.exception.rule.MaxCreditsExceededException;
import com.example.universityenrollmentsystem.domain.exception.rule.PrerequisiteNotMetException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProbationEnrollmentPlanStrategyTest {

    private static final int MAX_CREDITS = 14;

    private ProbationEnrollmentPlanStrategy strategy;
    private Major computerScience;
    private Major electricalEngineering;
    private Semester fallSemester;
    private Student student;

    @BeforeEach
    void setUp() {
        strategy = new ProbationEnrollmentPlanStrategy();
        computerScience = new Major("Computer Science");
        electricalEngineering = new Major("Electrical Engineering");
        fallSemester = new Semester("Fall 2026", LocalDate.of(2026, 9, 23), LocalDate.of(2027, 1, 20));
        student = new Student("Sara Mohammadi", "401987654", computerScience, EnrollmentPlanType.PROBATION);
    }

    @Test
    @DisplayName("Should return PROBATION plan type")
    void shouldReturnProbationPlanType() {
        assertThat(strategy.getPlanType()).isEqualTo(EnrollmentPlanType.PROBATION);
    }

    @Test
    @DisplayName("Should validate successfully when requested credits are under 14 credits")
    void shouldValidateSuccessfullyWhenCreditsAreUnderLimit() {
        Course course1 = createCourse("Math 1", "MATH101", 3, computerScience);
        Course course2 = createCourse("Physics 1", "PHYS101", 3, computerScience);
        CourseOffering offering1 = createOffering(course1, fallSemester);
        CourseOffering offering2 = createOffering(course2, fallSemester);

        assertThatCode(() -> strategy.validate(student, List.of(offering1, offering2)))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should validate successfully when total credits exactly equal maximum limit of 14")
    void shouldValidateSuccessfullyWhenCreditsAreExactlyAtMaxLimit() {
        List<CourseOffering> offerings = new ArrayList<>();
        offerings.add(createOffering(createCourse("Course 1", "CS101", 4, computerScience), fallSemester));
        offerings.add(createOffering(createCourse("Course 2", "CS102", 4, computerScience), fallSemester));
        offerings.add(createOffering(createCourse("Course 3", "CS103", 3, computerScience), fallSemester));
        offerings.add(createOffering(createCourse("Course 4", "CS104", 3, computerScience), fallSemester));

        assertThatCode(() -> strategy.validate(student, offerings))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should throw exception when requested credits exceed maximum limit of 14")
    void shouldThrowExceptionWhenCreditsExceedMaxLimit() {
        List<CourseOffering> offerings = new ArrayList<>();
        offerings.add(createOffering(createCourse("Course 1", "CS101", 4, computerScience), fallSemester));
        offerings.add(createOffering(createCourse("Course 2", "CS102", 4, computerScience), fallSemester));
        offerings.add(createOffering(createCourse("Course 3", "CS103", 4, computerScience), fallSemester));
        offerings.add(createOffering(createCourse("Course 4", "CS104", 3, computerScience), fallSemester));

        assertThatThrownBy(() -> strategy.validate(student, offerings))
                .isInstanceOf(MaxCreditsExceededException.class)
                .hasMessageContaining(String.valueOf(MAX_CREDITS));
    }

    @Test
    @DisplayName("Should throw exception when course does not belong to student's major")
    void shouldThrowExceptionWhenCourseDoesNotBelongToStudentMajor() {
        Course outsideCourse = createCourse("Power Systems", "EE201", 3, electricalEngineering);
        CourseOffering offering = createOffering(outsideCourse, fallSemester);

        assertThatThrownBy(() -> strategy.validate(student, List.of(offering)))
                .isInstanceOf(CourseNotInMajorException.class);
    }

    @Test
    @DisplayName("Should throw exception when prerequisite has not been passed")
    void shouldThrowExceptionWhenPrerequisiteNotPassed() {
        Course basic = createCourse("Basics", "CS100", 3, computerScience);
        Course advanced = createCourse("Advanced", "CS200", 3, computerScience);
        advanced.addPrerequisite(basic);

        CourseOffering advancedOffering = createOffering(advanced, fallSemester);

        assertThatThrownBy(() -> strategy.validate(student, List.of(advancedOffering)))
                .isInstanceOf(PrerequisiteNotMetException.class);
    }

    private Course createCourse(String name, String code, int credits, Major major) {
        Course course = new Course(name, code, credits);
        major.addCourse(course);
        return course;
    }

    private CourseOffering createOffering(Course course, Semester semester) {
        return new CourseOffering(
                course,
                semester,
                "Dr. Alavi",
                "01",
                true,
                30,
                "Sat 08:00",
                semester.getEndDate().minusWeeks(2)
        );
    }
}
