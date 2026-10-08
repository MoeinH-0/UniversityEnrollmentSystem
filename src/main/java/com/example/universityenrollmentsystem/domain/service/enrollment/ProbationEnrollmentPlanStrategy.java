package com.example.universityenrollmentsystem.domain.service.enrollment;

import com.example.universityenrollmentsystem.domain.entity.student.EnrollmentPlanType;
import org.springframework.stereotype.Component;

@Component
public class ProbationEnrollmentPlanStrategy extends BaseEnrollmentPlanStrategy {

    private static final int MAX_CREDITS = 14;

    @Override
    public EnrollmentPlanType getPlanType() {
        return EnrollmentPlanType.PROBATION;
    }

    @Override
    protected int getMaxCredits() {
        return MAX_CREDITS;
    }
}
