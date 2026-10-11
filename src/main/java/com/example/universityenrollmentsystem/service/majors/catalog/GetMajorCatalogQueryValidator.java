package com.example.universityenrollmentsystem.service.majors.catalog;

import com.example.universityenrollmentsystem.service.exceptions.validation.CommandValidationException;
import org.springframework.stereotype.Component;

@Component
public class GetMajorCatalogQueryValidator {
    public void validate(GetMajorCatalogQuery query) {
        if (query == null)
            throw new CommandValidationException("Query cannot be null");

        if (query.majorId() == null)
            throw new CommandValidationException("MajorId cannot be null");
    }
}
