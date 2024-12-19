package com.example.paperless.backend;

import com.example.paperless.backend.businessLogic.DocumentRepository;
import com.example.paperless.backend.models.Document;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;

@DataJpaTest
public class DocumentRepositoryTest {

    @Autowired
    private DocumentRepository documentRepository;

    @Test
    public void testFindByIdIn() {
        // Arrange: Insert test documents
        Document doc1 = new Document();
        doc1.setTitle("Document One");
        doc1.setContent("Content One");
        doc1.setDateOfCreation(LocalDateTime.now());
        Document savedDoc1 = documentRepository.save(doc1);

        Document doc2 = new Document();
        doc2.setTitle("Document Two");
        doc2.setContent("Content Two");
        doc2.setDateOfCreation(LocalDateTime.now());
        Document savedDoc2 = documentRepository.save(doc2);

        // Act: Find documents by their IDs
        List<Document> foundDocuments = documentRepository.findByIdIn(List.of(savedDoc1.getId(), savedDoc2.getId()));

        // Assert: Verify that both documents are retrieved
        Assertions.assertEquals(2, foundDocuments.size());
        Assertions.assertTrue(foundDocuments.stream().anyMatch(d -> d.getId() == savedDoc1.getId()));
        Assertions.assertTrue(foundDocuments.stream().anyMatch(d -> d.getId() == savedDoc2.getId()));
    }

    @Test
    public void testFindDocumentsByIds() {
        // Arrange: Insert test documents
        Document doc1 = new Document();
        doc1.setTitle("Document One");
        doc1.setContent("Content One");
        doc1.setDateOfCreation(LocalDateTime.now());
        Document savedDoc1 = documentRepository.save(doc1);

        Document doc2 = new Document();
        doc2.setTitle("Document Two");
        doc2.setContent("Content Two");
        doc2.setDateOfCreation(LocalDateTime.now());
        Document savedDoc2 = documentRepository.save(doc2);

        // Act: Use the custom JPQL query to find documents by IDs
        List<Document> foundDocuments = documentRepository.findDocumentsByIds(List.of(savedDoc1.getId(), savedDoc2.getId()));

        // Assert: Verify that both documents are retrieved by JPQL query
        Assertions.assertEquals(2, foundDocuments.size());
        Assertions.assertTrue(foundDocuments.stream().anyMatch(d -> d.getId() == savedDoc1.getId()));
        Assertions.assertTrue(foundDocuments.stream().anyMatch(d -> d.getId() == savedDoc2.getId()));
    }
}
