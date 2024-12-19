package com.example.paperless.backend;

import com.example.paperless.backend.models.ElasticDocument;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

public class ElasticDocumentTest {

    private ElasticDocument elasticDocument;

    @BeforeEach
    public void setUp() {
        elasticDocument = new ElasticDocument("1", "Test Title", "Test Content", "path/to/file");
    }

    @Test
    public void whenConstructed_thenFieldsAreInitialized() {
        Assertions.assertEquals("1", elasticDocument.getId(), "ID should be initialized by the constructor.");
        Assertions.assertEquals("Test Title", elasticDocument.getTitle(), "Title should be initialized by the constructor.");
        Assertions.assertEquals("Test Content", elasticDocument.getContent(), "Content should be initialized by the constructor.");
        Assertions.assertEquals("path/to/file", elasticDocument.getFilePath(), "FilePath should be initialized by the constructor.");
        Assertions.assertTrue(elasticDocument.getTags().isEmpty(), "Tags should be empty by default.");
        Assertions.assertNull(elasticDocument.getDateOfCreation(), "Date of creation should be null unless explicitly set.");
    }

    @Test
    public void whenSettingFields_thenValuesAreUpdated() {
        elasticDocument.setId("2");
        elasticDocument.setTitle("Updated Title");
        elasticDocument.setContent("Updated Content");
        elasticDocument.setFilePath("path/to/updatedFile");
        elasticDocument.setTags(List.of("tag1", "tag2"));
        elasticDocument.setDateOfCreation("2024-12-19T10:00:00");

        Assertions.assertEquals("2", elasticDocument.getId(), "ID should be updated.");
        Assertions.assertEquals("Updated Title", elasticDocument.getTitle(), "Title should be updated.");
        Assertions.assertEquals("Updated Content", elasticDocument.getContent(), "Content should be updated.");
        Assertions.assertEquals("path/to/updatedFile", elasticDocument.getFilePath(), "FilePath should be updated.");
        Assertions.assertEquals(2, elasticDocument.getTags().size(), "Tags should contain the newly set values.");
        Assertions.assertEquals("2024-12-19T10:00:00", elasticDocument.getDateOfCreation(), "DateOfCreation should be updated.");
    }

    @Test
    public void whenSettingEmptyTags_thenListIsEmpty() {
        elasticDocument.setTags(List.of());
        Assertions.assertTrue(elasticDocument.getTags().isEmpty(), "Tags should be an empty list.");
    }

    @Test
    public void whenSettingTags_thenTheyAreStoredCorrectly() {
        elasticDocument.setTags(List.of("elasticsearch", "search"));
        Assertions.assertTrue(elasticDocument.getTags().contains("elasticsearch"), "Tags should contain 'elasticsearch'.");
        Assertions.assertTrue(elasticDocument.getTags().contains("search"), "Tags should contain 'search'.");
    }
}

