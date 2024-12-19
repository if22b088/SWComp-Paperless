package com.example.paperless.backend;

import com.example.paperless.backend.ElasticSearch.ElasticRepository;
import com.example.paperless.backend.models.ElasticDocument;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.elasticsearch.DataElasticsearchTest;

import java.util.List;

@DataElasticsearchTest
public class ElasticRepositoryTest {

    @Autowired
    private ElasticRepository elasticRepository;

    @Test
    public void testFindByTitleContainingOrContentContaining() {
        // Arrange: Create and save documents
        ElasticDocument doc1 = new ElasticDocument("1", "Java Programming", "A guide to Java", "path/to/file1");
        ElasticDocument doc2 = new ElasticDocument("2", "Spring Boot Tutorial", "Learn how to build Spring Boot apps", "path/to/file2");
        ElasticDocument doc3 = new ElasticDocument("3", "Cooking Recipes", "Delicious recipes for everyone", "path/to/file3");

        elasticRepository.save(doc1);
        elasticRepository.save(doc2);
        elasticRepository.save(doc3);

        // Act: Search for documents containing "Java" in title or content
        List<ElasticDocument> result = elasticRepository.findByTitleContainingOrContentContaining("Java", "Java");

        // Assert: Verify that documents containing "Java" are returned
        Assertions.assertEquals(1, result.size());
        Assertions.assertTrue(result.stream().anyMatch(d -> d.getId().equals("1")));

        // Act: Search for documents containing "Spring" in title or content
        result = elasticRepository.findByTitleContainingOrContentContaining("Spring", "Spring");

        // Assert: Verify that documents containing "Spring" are returned
        Assertions.assertEquals(1, result.size());
        Assertions.assertTrue(result.stream().anyMatch(d -> d.getId().equals("2")));

        // Act: Search for documents containing "Recipes"
        result = elasticRepository.findByTitleContainingOrContentContaining("Recipes", "Recipes");

        // Assert: Verify that documents containing "Recipes" are returned
        Assertions.assertEquals(1, result.size());
        Assertions.assertTrue(result.stream().anyMatch(d -> d.getId().equals("3")));
    }
}

