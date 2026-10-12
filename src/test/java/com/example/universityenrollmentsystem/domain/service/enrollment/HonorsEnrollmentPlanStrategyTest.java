package com.example.universityenrollmentsystem.domain.service.enrollment;

import com.example.universityenrollmentsystem.domain.entity.academic.Major;
import com.example.universityenrollmentsystem.domain.entity.academic.Semester;
import com.example.universityenrollmentsystem.domain.entity.course.Course;
import com.example.universityenrollmentsystem.domain.entity.course.CourseOffering;
import com.example.universityenrollmentsystem.domain.entity.student.EnrollmentPlanType;
import com.example.universityenrollmentsystem.domain.entity.student.Student;
import com.example.universityenrollmentsystem.domain.exception.rule.MaxCreditsExceededException;
import com.example.universityenrollmentsystem.domain.exception.rule.OutsideMajorLimitExceededException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HonorsEnrollmentPlanStrategyTest {

    private static final int MAX_CREDITS = 28;

    private HonorsEnrollmentPlanStrategy strategy;
    private Major computerScience;
    private Major electricalEngineering;
    private Semester fallSemester;
    private Semester springSemester;
    private Student student;

    @BeforeEach
    void setUp() {
        strategy = new HonorsEnrollmentPlanStrategy();
        computerScience = new Major("Computer Science");
        electricalEngineering = new Major("Electrical Engineering");
        fallSemester = new Semester("Fall 2026", LocalDate.of(2026, 9, 23), LocalDate.of(2027, 1, 20));
        springSemester = new Semester("Spring 2027", LocalDate.of(2027, 2, 10), LocalDate.of(2027, 6, 20));
        student = new Student("Maryam Mirzakhani", "401111222", computerScience, EnrollmentPlanType.HONORS);
    }

    @Test
    @DisplayName("Should return HONORS plan type")
    void shouldReturnHonorsPlanType() {
        assertThat(strategy.getPlanType()).isEqualTo(EnrollmentPlanType.HONORS);
    }

    @Test
    @DisplayName("Should validate successfully when total credits are exactly at maximum limit of 28")
    void shouldValidateSuccessfullyWhenCreditsAreExactlyAtMaxLimit() {
        List<CourseOffering> offerings = new ArrayList<>();
        for (int i = 1; i <= 7; i++) {
            Course course = createCourse("Course " + i, "CS" + (100 + i), 4, computerScience);
            offerings.add(createOffering(course, fallSemester));
        }

        assertThatCode(() -> strategy.validate(student, offerings))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should throw exception when requested credits exceed maximum limit of 28")
    void shouldThrowExceptionWhenCreditsExceedMaxLimit() {
        List<CourseOffering> offerings = new ArrayList<>();
        for (int i = 1; i <= 7; i++) {
            Course course = createCourse("Course " + i, "CS" + (100 + i), 4, computerScience);
            offerings.add(createOffering(course, fallSemester));
        }
        Course extraCourse = createCourse("Extra Course", "CS108", 2, computerScience);
        offerings.add(createOffering(extraCourse, fallSemester));

        assertThatThrownBy(() -> strategy.validate(student, offerings))
                .isInstanceOf(MaxCreditsExceededException.class)
                .hasMessageContaining(String.valueOf(MAX_CREDITS));
    }

    @Test
    @DisplayName("Should allow selecting one course outside student's major")
    void shouldAllowOneCourseOutsideMajor() {
        Course majorCourse = createCourse("Data Structures", "CS201", 3, computerScience);
        Course outsideCourse = createCourse("Circuits", "EE101", 3, electricalEngineering);

        CourseOffering offering1 = createOffering(majorCourse, fallSemester);
        CourseOffering offering2 = createOffering(outsideCourse, fallSemester);

        assertThatCode(() -> strategy.validate(student, List.of(offering1, offering2)))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should throw exception when requesting more than one course outside major in same request")
    void shouldThrowExceptionWhenRequestingMoreThanOneCourseOutsideMajor() {
        Course outsideCourse1 = createCourse("Circuits", "EE101", 3, electricalEngineering);
        Course outsideCourse2 = createCourse("Electronics", "EE102", 3, electricalEngineering);

        CourseOffering offering1 = createOffering(outsideCourse1, fallSemester);
        CourseOffering offering2 = createOffering(outsideCourse2, fallSemester);

        assertThatThrownBy(() -> strategy.validate(student, List.of(offering1, offering2)))
                .isInstanceOf(OutsideMajorLimitExceededException.class);
    }

    @Test
    @DisplayName("Should throw exception when student already enrolled in outside major course and requests another in same semester")
    void shouldThrowExceptionWhenStudentAlreadyEnrolledInOutsideCourse() {
        Course outsideCourse1 = createCourse("Circuits", "EE101", 3, electricalEngineering);
        Course outsideCourse2 = createCourse("Electronics", "EE102", 3, electricalEngineering);

        CourseOffering offering1 = createOffering(outsideCourse1, fallSemester);
        CourseOffering offering2 = createOffering(outsideCourse2, fallSemester);

        student.enroll(List.of(offering1), strategy);

        assertThatThrownBy(() -> strategy.validate(student, List.of(offering2)))
                .isInstanceOf(OutsideMajorLimitExceededException.class);
    }

    @Test
    @DisplayName("Should return early without exception when courseOfferings list is empty")
    void shouldReturnEarlyWhenCourseOfferingsEmpty() {
        assertThatCode(() -> strategy.validate(student, List.of()))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should allow outside course when previous outside course in same semester was already PASSED")
    void shouldAllowOutsideCourseWhenPreviousOutsideCoursePassed() {
        Course outsideCourse1 = createCourse("Circuits", "EE101", 3, electricalEngineering);
        Course outsideCourse2 = createCourse("Electronics", "EE102", 3, electricalEngineering);

        CourseOffering offering1 = createOffering(outsideCourse1, fallSemester);
        CourseOffering offering2 = createOffering(outsideCourse2, fallSemester);

        student.enroll(List.of(offering1), strategy);
        student.getEnrollments().get(0).markAsPassed();

        assertThatCode(() -> strategy.validate(student, List.of(offering2)))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should allow outside course when student has in-progress outside course in another semester")
    void shouldAllowOutsideCourseWhenOtherOutsideCourseInDifferentSemester() {
        Course outsideCourse1 = createCourse("Circuits", "EE101", 3, electricalEngineering);
        Course outsideCourse2 = createCourse("Electronics", "EE102", 3, electricalEngineering);

        CourseOffering offeringFall = createOffering(outsideCourse1, fallSemester);
        CourseOffering offeringSpring = createOffering(outsideCourse2, springSemester);

        student.enroll(List.of(offeringFall), strategy);

        assertThatCode(() -> strategy.validate(student, List.of(offeringSpring)))
                .doesNotThrowAnyException();
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
                "Dr. Rad",
                "01",
                true,
                30,
                "Sun 10:00",
                semester.getEndDate().minusWeeks(2)
        );
    }
}
