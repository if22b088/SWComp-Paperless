package com.example.paperless.backend;

import com.example.paperless.backend.models.Document;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.ConstraintViolation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import java.time.LocalDateTime;
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
    public void whenTitleIsExactlyMinLength_thenNoConstraintViolation() {
        document.setTitle("abc"); // Minimum valid title length
        Set<ConstraintViolation<Document>> violations = validator.validate(document);
        Assertions.assertTrue(violations.isEmpty(), "No validation errors should occur for a title with exactly 3 characters.");
    }

    @Test
    public void whenTitleIsExactlyMaxLength_thenNoConstraintViolation() {
        document.setTitle("a".repeat(100)); // Maximum valid title length
        Set<ConstraintViolation<Document>> violations = validator.validate(document);
        Assertions.assertTrue(violations.isEmpty(), "No validation errors should occur for a title with exactly 100 characters.");
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
    public void whenContentIsExactlyMinLength_thenNoConstraintViolation() {
        document.setContent("abcdefghij"); // Minimum valid content length
        Set<ConstraintViolation<Document>> violations = validator.validate(document);
        Assertions.assertTrue(violations.isEmpty(), "No validation errors should occur for content with exactly 10 characters.");
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

    @Test
    public void whenMultipleTagsHaveViolations_thenConstraintViolation() {
        document.setTags(List.of("a", "ThisTagIsWayTooLongThisTagIsWayTooLong"));
        Set<ConstraintViolation<Document>> violations = validator.validate(document);

        Assertions.assertFalse(violations.isEmpty(), "Validation errors should be present for invalid tags.");
        Assertions.assertTrue(violations.stream()
                        .anyMatch(v -> v.getPropertyPath().toString().contains("tags") && v.getMessage().contains("between 2 and 20 characters")),
                "Error message should state each tag must be between 2 and 20 characters.");
    }

    @Test
    public void whenTagsAreEmpty_thenNoConstraintViolation() {
        document.setTags(List.of()); // No tags
        Set<ConstraintViolation<Document>> violations = validator.validate(document);
        Assertions.assertTrue(violations.isEmpty(), "No validation errors should occur for an empty tag list.");
    }

    @Test
    public void whenTagIsExactlyMinLength_thenNoConstraintViolation() {
        document.setTags(List.of("ab")); // Tag with minimum valid length
        Set<ConstraintViolation<Document>> violations = validator.validate(document);
        Assertions.assertTrue(violations.isEmpty(), "No validation errors should occur for a tag with exactly 2 characters.");
    }

    @Test
    public void whenTagIsExactlyMaxLength_thenNoConstraintViolation() {
        document.setTags(List.of("a".repeat(20))); // Tag with maximum valid length
        Set<ConstraintViolation<Document>> violations = validator.validate(document);
        Assertions.assertTrue(violations.isEmpty(), "No validation errors should occur for a tag with exactly 20 characters.");
    }

    @Test
    public void whenMultipleEdgeCaseTags_thenNoConstraintViolation() {
        document.setTags(List.of("ab", "a".repeat(20))); // Tags at min and max length
        Set<ConstraintViolation<Document>> violations = validator.validate(document);
        Assertions.assertTrue(violations.isEmpty(), "No validation errors should occur for tags at valid edge lengths.");
    }

    @Test
    public void whenDateOfCreationIsSet_thenNoConstraintViolation() {
        Assertions.assertNotNull(document.getDateOfCreation(), "dateOfCreation should be initialized.");
    }

    @Test
    public void whenDateOfCreationIsNull_thenConstraintViolation() {
        document.setDateOfCreation(null);
        Set<ConstraintViolation<Document>> violations = validator.validate(document);

        Assertions.assertFalse(violations.isEmpty(), "Validation error should be present when dateOfCreation is null.");
        Assertions.assertTrue(violations.stream()
                        .anyMatch(v -> v.getPropertyPath().toString().equals("dateOfCreation") && v.getMessage().contains("must not be null")),
                "Error message should state dateOfCreation must not be null.");
    }

    @Test
    public void whenDateOfCreationIsInFuture_thenConstraintViolation() {
        document.setDateOfCreation(LocalDateTime.now().plusDays(1));
        Set<ConstraintViolation<Document>> violations = validator.validate(document);

        Assertions.assertFalse(violations.isEmpty(), "Validation error should be present when dateOfCreation is in the future.");
        Assertions.assertTrue(violations.stream()
                        .anyMatch(v -> v.getPropertyPath().toString().equals("dateOfCreation") && v.getMessage().contains("cannot be in the future")),
                "Error message should state dateOfCreation cannot be in the future.");
    }
}
