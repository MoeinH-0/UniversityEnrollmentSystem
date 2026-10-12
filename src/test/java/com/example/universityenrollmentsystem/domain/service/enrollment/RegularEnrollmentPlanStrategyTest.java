package com.example.universityenrollmentsystem.domain.service.enrollment;

import com.example.universityenrollmentsystem.domain.entity.academic.Major;
import com.example.universityenrollmentsystem.domain.entity.academic.Semester;
import com.example.universityenrollmentsystem.domain.entity.course.Course;
import com.example.universityenrollmentsystem.domain.entity.course.CourseOffering;
import com.example.universityenrollmentsystem.domain.entity.student.EnrollmentPlanType;
import com.example.universityenrollmentsystem.domain.entity.student.EnrollmentRecord;
import com.example.universityenrollmentsystem.domain.entity.student.Student;
import com.example.universityenrollmentsystem.domain.exception.rule.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RegularEnrollmentPlanStrategyTest {

    private RegularEnrollmentPlanStrategy strategy;
    private Major computerScience;
    private Major electricalEngineering;
    private Semester fallSemester;
    private Semester springSemester;
    private Student student;

    @BeforeEach
    void setUp() {
        strategy = new RegularEnrollmentPlanStrategy();
        computerScience = new Major("Computer Science");
        electricalEngineering = new Major("Electrical Engineering");
        fallSemester = new Semester("Fall 2026", LocalDate.of(
                2026, 9, 23), LocalDate.of(2027, 1, 20));
        springSemester = new Semester("Spring 2027", LocalDate.of(
                2027, 2, 10), LocalDate.of(2027, 6, 20));
        student = new Student("Ali Rezaei", "401123456",
                computerScience, EnrollmentPlanType.REGULAR);
    }

    @Test
    @DisplayName("Should validate successfully when requested credits are under maximum limit")
    void shouldValidateSuccessfullyWhenCreditsAreUnderLimit() {
        Course course1 = createCourse("Math 1", "MATH101", 3, computerScience);
        Course course2 = createCourse("Physics 1", "PHYS101", 3, computerScience);
        CourseOffering offering1 = createOffering(course1, fallSemester);
        CourseOffering offering2 = createOffering(course2, fallSemester);

        assertThatCode(() -> strategy.validate(student, List.of(offering1, offering2)))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should validate successfully when total credits exactly equal maximum limit of 24")
    void shouldValidateSuccessfullyWhenCreditsAreExactlyAtMaxLimit() {
        List<CourseOffering> offerings = new ArrayList<>();
        for (int i = 1; i <= 6; i++) {
            Course course = createCourse("Course " + i, "CS" + (100 + i), 4, computerScience);
            offerings.add(createOffering(course, fallSemester));
        }

        assertThatCode(() -> strategy.validate(student, offerings))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should throw exception when requested credits exceed maximum limit of 24")
    void shouldThrowExceptionWhenCreditsExceedMaxLimit() {
        List<CourseOffering> offerings = new ArrayList<>();
        for (int i = 1; i <= 6; i++) {
            Course course = createCourse("Course " + i, "CS" + (100 + i), 4, computerScience);
            offerings.add(createOffering(course, fallSemester));
        }
        Course extraCourse = createCourse("Extra Course", "CS107", 2, computerScience);
        offerings.add(createOffering(extraCourse, fallSemester));

        assertThatThrownBy(() -> strategy.validate(student, offerings))
                .isInstanceOf(MaxCreditsExceededException.class)
                .hasMessageContaining("Max 24 credits allowed for REGULAR plan.");
    }

    @Test
    @DisplayName("Should throw exception when course does not belong to student's major")
    void shouldThrowExceptionWhenCourseDoesNotBelongToStudentMajor() {
        Course outsideCourse = createCourse("Circuits", "EE101", 3, electricalEngineering);
        CourseOffering offering = createOffering(outsideCourse, fallSemester);

        assertThatThrownBy(() -> strategy.validate(student, List.of(offering)))
                .isInstanceOf(CourseNotInMajorException.class)
                .hasMessageContaining("Course must belong to student's major.");
    }

    @Test
    @DisplayName("Should throw exception when requested list contains duplicate courses")
    void shouldThrowExceptionWhenRequestedListContainsDuplicateCourses() {
        Course course = createCourse("Math 1", "MATH101", 3, computerScience);
        CourseOffering offering1 = createOffering(course, fallSemester);
        CourseOffering offering2 = createOffering(course, fallSemester);

        assertThatThrownBy(() -> strategy.validate(student, List.of(offering1, offering2)))
                .isInstanceOf(DuplicateEnrollmentException.class)
                .hasMessageContaining("Requested list contains duplicate courses.");
    }

    @Test
    @DisplayName("Should throw exception when student is already enrolled in course in same semester")
    void shouldThrowExceptionWhenStudentAlreadyEnrolledInSameSemester() {
        Course course = createCourse("Math 1", "MATH101", 3, computerScience);
        CourseOffering offering = createOffering(course, fallSemester);

        student.enroll(List.of(offering), strategy);

        assertThatThrownBy(() -> strategy.validate(student, List.of(offering)))
                .isInstanceOf(DuplicateEnrollmentException.class)
                .hasMessageContaining("Cannot take duplicate course in the same semester.");
    }

    @Test
    @DisplayName("Should throw exception when prerequisite course has not been passed")
    void shouldThrowExceptionWhenPrerequisiteNotPassed() {
        Course basicProgramming = createCourse("Basic Programming", "CS101", 3, computerScience);
        Course advancedProgramming = createCourse("Advanced Programming", "CS102", 3, computerScience);
        advancedProgramming.addPrerequisite(basicProgramming);

        CourseOffering advancedOffering = createOffering(advancedProgramming, fallSemester);

        assertThatThrownBy(() -> strategy.validate(student, List.of(advancedOffering)))
                .isInstanceOf(PrerequisiteNotMetException.class)
                .hasMessageContaining("Prerequisite not passed for course: Advanced Programming");
    }

    @Test
    @DisplayName("Should validate successfully when prerequisite course has been passed previously")
    void shouldValidateSuccessfullyWhenPrerequisiteHasBeenPassed() {
        Course basicProgramming = createCourse("Basic Programming", "CS101", 3, computerScience);
        Course advancedProgramming = createCourse("Advanced Programming", "CS102", 3, computerScience);
        advancedProgramming.addPrerequisite(basicProgramming);

        CourseOffering basicOffering = createOffering(basicProgramming, fallSemester);
        student.enroll(List.of(basicOffering), strategy);
        EnrollmentRecord record = student.getEnrollments().get(0);
        record.markAsPassed();

        CourseOffering advancedOffering = createOffering(advancedProgramming, springSemester);

        assertThatCode(() -> strategy.validate(student, List.of(advancedOffering)))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should throw exception when requested courses belong to different semesters")
    void shouldThrowExceptionWhenCoursesBelongToDifferentSemesters() {
        Course course1 = createCourse("Math 1", "MATH101", 3, computerScience);
        Course course2 = createCourse("Math 2", "MATH102", 3, computerScience);

        CourseOffering offeringFall = createOffering(course1, fallSemester);
        CourseOffering offeringSpring = createOffering(course2, springSemester);

        assertThatThrownBy(() -> strategy.validate(student, List.of(offeringFall, offeringSpring)))
                .isInstanceOf(CrossSemesterEnrollmentException.class)
                .hasMessageContaining("All requested course offerings must belong to the same semester.");
    }

    @Test
    @DisplayName("Should not count in-progress credits from different semester toward current semester credit limit")
    void shouldNotCountCreditsFromOtherSemester() {
        List<CourseOffering> fallOfferings = new ArrayList<>();
        for (int i = 1; i <= 6; i++) {
            Course course = createCourse("Course " + i, "CS" + (100 + i), 4, computerScience);
            fallOfferings.add(createOffering(course, fallSemester));
        }
        student.enroll(fallOfferings, strategy);

        Course springCourse = createCourse("Spring Course", "CS201", 3, computerScience);
        CourseOffering springOffering = createOffering(springCourse, springSemester);

        assertThatCode(() -> strategy.validate(student, List.of(springOffering)))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should not count passed or failed courses in same semester toward current credit limit")
    void shouldNotCountPassedOrFailedCoursesTowardCreditLimit() {
        Course course1 = createCourse("Course 1", "CS101", 4, computerScience);
        CourseOffering offering1 = createOffering(course1, fallSemester);
        student.enroll(List.of(offering1), strategy);
        student.getEnrollments().get(0).markAsPassed();

        List<CourseOffering> newOfferings = new ArrayList<>();
        for (int i = 2; i <= 7; i++) {
            Course course = createCourse("Course " + i, "CS" + (100 + i), 4, computerScience);
            newOfferings.add(createOffering(course, fallSemester));
        }

        assertThatCode(() -> strategy.validate(student, newOfferings))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should throw exception when prerequisite status is FAILED")
    void shouldThrowExceptionWhenPrerequisiteStatusIsFailed() {
        Course basic = createCourse("Basic Programming", "CS101", 3, computerScience);
        Course advanced = createCourse("Advanced Programming", "CS102", 3, computerScience);
        advanced.addPrerequisite(basic);

        CourseOffering basicOffering = createOffering(basic, fallSemester);
        student.enroll(List.of(basicOffering), strategy);
        student.getEnrollments().get(0).markAsFailed();

        CourseOffering advancedOffering = createOffering(advanced, springSemester);

        assertThatThrownBy(() -> strategy.validate(student, List.of(advancedOffering)))
                .isInstanceOf(PrerequisiteNotMetException.class);
    }

    @Test
    @DisplayName("Should throw exception when prerequisite status is still IN_PROGRESS")
    void shouldThrowExceptionWhenPrerequisiteStatusIsInProgress() {
        Course basic = createCourse("Basic Programming", "CS101", 3, computerScience);
        Course advanced = createCourse("Advanced Programming", "CS102", 3, computerScience);
        advanced.addPrerequisite(basic);

        CourseOffering basicOffering = createOffering(basic, fallSemester);
        student.enroll(List.of(basicOffering), strategy);

        CourseOffering advancedOffering = createOffering(advanced, springSemester);

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
                "Dr. Ahmadi",
                "01",
                true,
                30,
                "Mon 10:00",
                semester.getEndDate().minusWeeks(2)
        );
    }
}
