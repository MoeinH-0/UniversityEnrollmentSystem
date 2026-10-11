package com.example.universityenrollmentsystem.domain.service.enrollment;

import com.example.universityenrollmentsystem.domain.entity.academic.Semester;
import com.example.universityenrollmentsystem.domain.entity.course.CourseOffering;
import com.example.universityenrollmentsystem.domain.entity.course.CourseStatus;
import com.example.universityenrollmentsystem.domain.entity.student.EnrollmentPlanType;
import com.example.universityenrollmentsystem.domain.entity.student.Student;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class HonorsEnrollmentPlanStrategy extends BaseEnrollmentPlanStrategy {

    private static final int MAX_CREDITS = 28;
    private static final int MAX_OUTSIDE_MAJOR_COURSES = 1;

    @Override
    public EnrollmentPlanType getPlanType() {
        return EnrollmentPlanType.HONORS;
    }

    @Override
    protected int getMaxCredits() {
        return MAX_CREDITS;
    }

    @Override
    protected void checkMajorMatch(Student student, List<CourseOffering> courseOfferings) {
        if (courseOfferings.isEmpty())
            return;

        Semester currentSemester = courseOfferings.get(0).getSemester();

        long existingOutsideCount = student.getEnrollments().stream()
                .filter(record -> record.getStatus() == CourseStatus.IN_PROGRESS)
                .filter(record -> record.getCourseOffering().getSemester().equals(currentSemester))
                .filter(record -> !student.getMajor().getCourses().contains(record.getCourseOffering().getCourse()))
                .count();

        long requestedOutsideCount = courseOfferings.stream()
                .filter(offering -> !student.getMajor().getCourses().contains(offering.getCourse()))
                .count();

        if (existingOutsideCount + requestedOutsideCount > MAX_OUTSIDE_MAJOR_COURSES)
            throw new IllegalStateException("Honors students can pick at most " + MAX_OUTSIDE_MAJOR_COURSES + " course outside their major per semester.");
    }
}
