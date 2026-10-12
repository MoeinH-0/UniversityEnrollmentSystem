package com.example.universityenrollmentsystem.domain.exception.rule;

import com.example.universityenrollmentsystem.domain.exception.DomainException;

public class OutsideMajorLimitExceededException extends DomainException {

    public OutsideMajorLimitExceededException(int maxLimit) {
        super(String.format("Honors students can pick at most %d course outside their major per semester.", maxLimit));
    }
}
