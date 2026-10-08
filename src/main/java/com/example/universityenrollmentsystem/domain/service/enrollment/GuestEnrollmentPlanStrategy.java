package com.example.universityenrollmentsystem.domain.service.enrollment;

import com.example.universityenrollmentsystem.domain.entity.course.CourseOffering;
import com.example.universityenrollmentsystem.domain.entity.student.EnrollmentPlanType;
import com.example.universityenrollmentsystem.domain.entity.student.Student;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GuestEnrollmentPlanStrategy extends BaseEnrollmentPlanStrategy {

    private static final int MAX_CREDITS = 20;

    @Override
    public EnrollmentPlanType getPlanType() {
        return EnrollmentPlanType.GUEST;
    }

    @Override
    protected int getMaxCredits() {
        return MAX_CREDITS;
    }

    @Override
    protected void applySpecificRules(Student student, List<CourseOffering> courseOfferings) {
        for (CourseOffering offering : courseOfferings) {
            if (!offering.isGuestAllowed())
                throw new IllegalStateException("Guest students can only select guest-allowed courses.");
        }
    }
}
