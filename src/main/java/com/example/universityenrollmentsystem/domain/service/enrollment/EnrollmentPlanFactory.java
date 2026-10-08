package com.example.universityenrollmentsystem.domain.service.enrollment;

import com.example.universityenrollmentsystem.domain.entity.student.EnrollmentPlanType;
import org.springframework.stereotype.Component;



import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class EnrollmentPlanFactory {
    private final Map<EnrollmentPlanType, EnrollmentPlanStrategy> strategies = new HashMap<>();

    public EnrollmentPlanFactory(List<EnrollmentPlanStrategy> strategyList) {
        for (EnrollmentPlanStrategy strategy : strategyList)
            this.strategies.put(strategy.getPlanType(), strategy);
    }

    public EnrollmentPlanStrategy getStrategy(EnrollmentPlanType planType) {
        if (!strategies.containsKey(planType))
            throw new IllegalArgumentException("Strategy not found for plan type: " + planType);
            
        return strategies.get(planType);
    }
}
