package com.example.universityenrollmentsystem.domain.service.enrollment;

import com.example.universityenrollmentsystem.domain.entity.student.EnrollmentPlanType;
import org.springframework.stereotype.Component;

@Component
public class RegularEnrollmentPlanStrategy extends BaseEnrollmentPlanStrategy {

    private static final int MAX_CREDITS = 24;

    @Override
    public EnrollmentPlanType getPlanType() {
        return EnrollmentPlanType.REGULAR;
    }

    @Override
    protected int getMaxCredits() {
        return MAX_CREDITS;
    }
}
