package com.example.universityenrollmentsystem.domain.exception.rule;

import com.example.universityenrollmentsystem.domain.exception.DomainException;

public class MaxCreditsExceededException extends DomainException {

    public MaxCreditsExceededException(int maxCredits, String planType) {
        super(String.format("Max %d credits allowed for %s plan.", maxCredits, planType));
    }
}
