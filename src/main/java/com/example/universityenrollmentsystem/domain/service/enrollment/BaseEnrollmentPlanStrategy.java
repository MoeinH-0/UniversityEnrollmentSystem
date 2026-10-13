package com.example.universityenrollmentsystem.domain.service.enrollment;

import com.example.universityenrollmentsystem.domain.entity.academic.Semester;
import com.example.universityenrollmentsystem.domain.entity.course.CourseOffering;
import com.example.universityenrollmentsystem.domain.entity.course.Course;
import com.example.universityenrollmentsystem.domain.entity.course.CourseStatus;
import com.example.universityenrollmentsystem.domain.entity.student.Student;
import com.example.universityenrollmentsystem.domain.exception.rule.CourseNotInMajorException;
import com.example.universityenrollmentsystem.domain.exception.rule.CrossSemesterEnrollmentException;
import com.example.universityenrollmentsystem.domain.exception.rule.DuplicateEnrollmentException;
import com.example.universityenrollmentsystem.domain.exception.rule.MaxCreditsExceededException;
import com.example.universityenrollmentsystem.domain.exception.rule.PrerequisiteNotMetException;

import java.util.List;

public abstract class BaseEnrollmentPlanStrategy implements EnrollmentPlanStrategy {

    @Override
    public void validate(Student student, List<CourseOffering> courseOfferings) {
        checkSameSemester(courseOfferings);
        checkMaxCredits(student, courseOfferings);
        checkDuplicates(student, courseOfferings);
        checkMajorMatch(student, courseOfferings);
        checkPrerequisites(student, courseOfferings);
        applySpecificRules(student, courseOfferings);
    }

    private void checkSameSemester(List<CourseOffering> courseOfferings) {
        long semesterCount = courseOfferings.stream()
                .map(CourseOffering::getSemester)
                .distinct()
                .count();

        if (semesterCount > 1)
            throw new CrossSemesterEnrollmentException();
    }

    private void checkMaxCredits(Student student, List<CourseOffering> courseOfferings) {
        int maxCredits = getMaxCredits();

        if (calculateTotalCredits(student, courseOfferings) > maxCredits)
            throw new MaxCreditsExceededException(maxCredits, getPlanType().name());
    }

    private void checkDuplicates(Student student, List<CourseOffering> courseOfferings) {
        long uniqueRequestedCount = courseOfferings.stream()
                .map(CourseOffering::getCourse)
                .distinct()
                .count();

        if (uniqueRequestedCount != courseOfferings.size())
            throw new DuplicateEnrollmentException("Requested list contains duplicate courses.");

        for (CourseOffering offering : courseOfferings) {
            boolean alreadyEnrolled = student.getEnrollments().stream()
                    .anyMatch(e -> e.getCourseOffering()
                            .getCourse().equals(offering.getCourse())
                            && e.getCourseOffering().getSemester().equals(offering.getSemester())
                            && e.getStatus() == CourseStatus.IN_PROGRESS);

            if (alreadyEnrolled)
                throw new DuplicateEnrollmentException("Cannot take duplicate course in the same semester.");
        }
    }

    protected void checkMajorMatch(Student student, List<CourseOffering> courseOfferings) {
        for (CourseOffering offering : courseOfferings)
            if (!student.getMajor().getCourses().contains(offering.getCourse()))
                throw new CourseNotInMajorException();
    }

    private void checkPrerequisites(Student student, List<CourseOffering> courseOfferings) {
        for (CourseOffering offering : courseOfferings) {
            for (Course prerequisite : offering.getCourse().getPrerequisites()) {
                boolean hasPassed = student.getEnrollments().stream()
                        .anyMatch(e -> e.getCourseOffering()
                                .getCourse().equals(prerequisite)
                                && e.getStatus() == CourseStatus.PASSED);

                if (!hasPassed)
                    throw new PrerequisiteNotMetException(offering.getCourse().getName());
            }
        }
    }

    private int calculateTotalCredits(Student student, List<CourseOffering> courseOfferings) {
        if (courseOfferings.isEmpty())
            return 0;

        Semester currentSemester = courseOfferings.get(0).getSemester();

        int currentCredits = student.getEnrollments().stream()
                .filter(e -> e.getStatus() == CourseStatus.IN_PROGRESS)
                .filter(e -> e.getCourseOffering().getSemester().equals(currentSemester))
                .mapToInt(e -> e.getCourseOffering().getCourse().getCredits())
                .sum();

        int newCredits = courseOfferings.stream()
                .mapToInt(c -> c.getCourse().getCredits())
                .sum();

        return currentCredits + newCredits;
    }

    protected abstract int getMaxCredits();

    protected void applySpecificRules(Student student, List<CourseOffering> courseOfferings) {
    }
}
