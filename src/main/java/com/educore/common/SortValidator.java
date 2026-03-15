package com.educore.common;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SortValidator {

    public void validate(Pageable pageable, List<String> allowedFields) {

        pageable.getSort().forEach(order -> {
            if (!allowedFields.contains(order.getProperty())) {
                throw new IllegalArgumentException(
                        "Invalid sort field: " + order.getProperty()
                );
            }
        });
    }
}
