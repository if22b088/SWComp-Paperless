package com.example.paperless.backend.models;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Document(indexName = "documents")
public class ElasticDocument {

    @Id
    private String id;
    private String title;
    private String content;
    private String filePath;
    private List<String> tags = new ArrayList<>();
    private String dateOfCreation;

    public ElasticDocument(String id, String title, String content, String filePath) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.filePath = filePath;
    }

}
