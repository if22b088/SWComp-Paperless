package com.example.paperless.backend;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

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

    @Column(unique=true)
    @NotNull
    private String title;

    @Column
    @NotNull
    private String content;

    @ElementCollection
    private List<String> tags = new ArrayList<>();

    @Column
    private LocalDateTime dateOfCreation;

    public Document() {
        this.dateOfCreation = LocalDateTime.now();
    }
}
