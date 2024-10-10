package com.example.paperless.backend;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class Document {

    private long id;
    private String title;
    private String content;
    private List<String> tags;
    private LocalDateTime dateOfCreation;

    public LocalDateTime getDateOfCreation() {
        return dateOfCreation;
    }
}
