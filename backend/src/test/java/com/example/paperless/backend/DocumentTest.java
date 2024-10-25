package com.example.paperless.backend;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.ConstraintViolation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import java.util.List;
import java.util.Set;

public class DocumentTest {

    private Validator validator;
    private Document document;

    @BeforeEach
    public void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        document = new Document();
        document.setTitle("Valid Title");
        document.setContent("This is valid content with sufficient length.");
        document.setTags(List.of("java", "coding"));
    }

    @Test
    public void whenValidDocument_thenNoConstraintViolations() {
        Set<ConstraintViolation<Document>> violations = validator.validate(document);
        Assertions.assertTrue(violations.isEmpty(), "No validation errors should be present for a valid document.");
    }

    @Test
    public void whenTitleIsNull_thenConstraintViolation() {
        document.setTitle(null);
        Set<ConstraintViolation<Document>> violations = validator.validate(document);

        Assertions.assertFalse(violations.isEmpty(), "Validation error should be present when title is null.");
        Assertions.assertTrue(violations.stream()
                        .anyMatch(v -> v.getPropertyPath().toString().equals("title") && v.getMessage().contains("must not be null")),
                "Error message should state title must not be null.");
    }

    @Test
    public void whenTitleIsTooShort_thenConstraintViolation() {
        document.setTitle("Hi");
        Set<ConstraintViolation<Document>> violations = validator.validate(document);

        Assertions.assertFalse(violations.isEmpty(), "Validation error should be present when title is too short.");
        Assertions.assertTrue(violations.stream()
                        .anyMatch(v -> v.getPropertyPath().toString().equals("title") && v.getMessage().contains("between 3 and 100 characters")),
                "Error message should state title must be between 3 and 100 characters.");
    }

    @Test
    public void whenContentIsTooShort_thenConstraintViolation() {
        document.setContent("Short");
        Set<ConstraintViolation<Document>> violations = validator.validate(document);

        Assertions.assertFalse(violations.isEmpty(), "Validation error should be present when content is too short.");
        Assertions.assertTrue(violations.stream()
                        .anyMatch(v -> v.getPropertyPath().toString().equals("content") && v.getMessage().contains("at least 10 characters")),
                "Error message should state content must be at least 10 characters long.");
    }

    @Test
    public void whenTagIsTooShort_thenConstraintViolation() {
        document.setTags(List.of("a"));
        Set<ConstraintViolation<Document>> violations = validator.validate(document);

        Assertions.assertFalse(violations.isEmpty(), "Validation error should be present when a tag is too short.");
        Assertions.assertTrue(violations.stream()
                        .anyMatch(v -> v.getPropertyPath().toString().contains("tags") && v.getMessage().contains("between 2 and 20 characters")),
                "Error message should state each tag must be between 2 and 20 characters.");
    }

    @Test
    public void whenTagIsTooLong_thenConstraintViolation() {
        document.setTags(List.of("ThisTagIsWayTooLongThisTagIsWayTooLongThisTagIsWayTooLongThisTagIsWayTooLongThisTagIsWayTooLongThisTagIsWayTooLongThisTagIsWayTooLongThisTagIsWayTooLongThisTagIsWayTooLongThisTagIsWayTooLong"));
        Set<ConstraintViolation<Document>> violations = validator.validate(document);

        Assertions.assertFalse(violations.isEmpty(), "Validation error should be present when a tag is too long.");
        Assertions.assertTrue(violations.stream()
                        .anyMatch(v -> v.getPropertyPath().toString().contains("tags") && v.getMessage().contains("between 2 and 20 characters")),
                "Error message should state each tag must be between 2 and 20 characters.");
    }
}
