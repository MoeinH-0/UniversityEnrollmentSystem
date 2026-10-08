package com.example.universityenrollmentsystem.domain.service.enrollment;

import com.example.universityenrollmentsystem.domain.entity.course.CourseOffering;
import com.example.universityenrollmentsystem.domain.entity.student.Student;

import java.util.List;

public abstract class BaseEnrollmentPlanStrategy implements EnrollmentPlanStrategy {

    @Override
    public void validate(Student student, List<CourseOffering> courseOfferings) {
        checkMaxCredits(student, courseOfferings);
        checkDuplicates(student, courseOfferings);
        checkMajorMatch(student, courseOfferings);
        applySpecificRules(student, courseOfferings);
    }

    private void checkMaxCredits(Student student, List<CourseOffering> courseOfferings) {
        int maxCredits = getMaxCredits();
        if (calculateTotalCredits(student, courseOfferings) > maxCredits) {
            throw new IllegalStateException("Max " + maxCredits + " credits allowed for " + getPlanType().name() + " plan.");
        }
    }

    private void checkDuplicates(Student student, List<CourseOffering> courseOfferings) {
        long uniqueRequestedCount = courseOfferings.stream()
                .map(CourseOffering::getCourse)
                .distinct()
                .count();

        if (uniqueRequestedCount != courseOfferings.size())
            throw new IllegalStateException("Requested list contains duplicate courses.");

        for (CourseOffering offering : courseOfferings) {
            boolean alreadyEnrolled = student.getEnrollments().stream()
                    .anyMatch(e -> e.getCourseOffering().getCourse().equals(offering.getCourse()) 
                            && e.getCourseOffering().getSemester().equals(offering.getSemester()));
            
            if (alreadyEnrolled)
                throw new IllegalStateException("Cannot take duplicate course in the same semester.");
        }
    }

    protected void checkMajorMatch(Student student, List<CourseOffering> courseOfferings) {
        for (CourseOffering offering : courseOfferings) {
            if (!student.getMajor().getCourses().contains(offering.getCourse()))
                throw new IllegalStateException("Course must belong to student's major.");
        }
    }

    private int calculateTotalCredits(Student student, List<CourseOffering> courseOfferings) {
        int currentCredits = student.getEnrollments().stream()
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
