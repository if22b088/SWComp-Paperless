package com.example.paperless.backend.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(unique = true)
    @NotNull(message = "Title must not be null")
    @Size(min = 3, max = 100, message = "Title must be between 3 and 100 characters")
    private String title;

    @Column(columnDefinition = "TEXT")
    @NotNull(message = "Content must not be null")
    @Size(min = 10, message = "Content must be at least 10 characters long")
    private String content;

    @ElementCollection
    private List<@Size(min = 2, max = 20, message = "Each tag must be between 2 and 20 characters") String> tags = new ArrayList<>();

    @Column
    @NotNull(message = "Date of creation must not be null")
    @PastOrPresent(message = "Date of creation cannot be in the future")
    private LocalDateTime dateOfCreation;

/*
    @Lob
    @Column
    private byte[] fileData;

 */

    public Document() {
        this.dateOfCreation = LocalDateTime.now();
    }
}
