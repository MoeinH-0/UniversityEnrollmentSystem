package com.example.universityenrollmentsystem.service.enrollments.eligible;

import com.example.universityenrollmentsystem.domain.entity.course.Course;
import com.example.universityenrollmentsystem.domain.entity.course.CourseOffering;
import com.example.universityenrollmentsystem.domain.entity.course.CourseStatus;
import com.example.universityenrollmentsystem.domain.entity.student.EnrollmentPlanType;
import com.example.universityenrollmentsystem.domain.entity.student.Student;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class CourseEligibilityChecker {

    public boolean isEligible(Student student, CourseOffering offering) {
        if (isAlreadyPassed(student, offering.getCourse()))
            return false;

        if (isCurrentlyEnrolled(student, offering.getCourse()))
            return false;

        if (!hasPassedPrerequisites(student, offering.getCourse()))
            return false;

        return matchesPlanPolicy(student, offering);
    }

    private boolean isAlreadyPassed(Student student, Course course) {
        return student.getEnrollments().stream()
                .anyMatch(record -> record.getCourseOffering().getCourse().equals(course)
                        && record.getStatus() == CourseStatus.PASSED);
    }

    private boolean isCurrentlyEnrolled(Student student, Course course) {
        return student.getEnrollments().stream()
                .anyMatch(record -> record.getCourseOffering().getCourse().equals(course)
                        && record.getStatus() == CourseStatus.IN_PROGRESS);
    }

    private boolean hasPassedPrerequisites(Student student, Course course) {
        Set<Long> passedCourseIds = student.getEnrollments().stream()
                .filter(record -> record.getStatus() == CourseStatus.PASSED)
                .map(record -> record.getCourseOffering().getCourse().getId())
                .collect(Collectors.toSet());

        return course.getPrerequisites().stream()
                .allMatch(prereq -> passedCourseIds.contains(prereq.getId()));
    }

    private boolean matchesPlanPolicy(Student student, CourseOffering offering) {
        if (student.getPlanType() == EnrollmentPlanType.GUEST)
            return offering.isGuestAllowed();

        if (student.getPlanType() == EnrollmentPlanType.HONORS)
            return true;

        return student.getMajor().getCourses().contains(offering.getCourse());
    }
}
