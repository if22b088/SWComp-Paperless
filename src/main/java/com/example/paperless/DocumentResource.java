package com.example.paperless;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/documents")
public class DocumentResource {

    private List<Document> documents = new ArrayList<>();

    // Get all documents
    @GetMapping
    public ResponseEntity<List<Document>> getDocuments() {

        //returns hardcoded list with 1 entry
        //documents = new ArrayList<>();
        //Document document = new Document();
        //document.setContent("Document Content");
        //documents.add(document);
        return ResponseEntity.ok(documents);
    }
    // Add a new document
    @PostMapping
    public ResponseEntity<Document> addDocument(@RequestBody Document document) {
        document.setDateOfCreation(LocalDateTime.now());
        documents.add(document);
        return ResponseEntity.status(HttpStatus.CREATED).body(document);
    }

    // Update an existing document
    @PutMapping("/{id}")
    public ResponseEntity<Document> updateDocument(@PathVariable("id") long id, @RequestBody Document updatedDocument) {
        Optional<Document> existingDocumentOpt = documents.stream()
                .filter(document -> document.getId() == id)
                .findFirst();

        if (existingDocumentOpt.isPresent()) {
            Document existingDocument = existingDocumentOpt.get();
            existingDocument.setTitle(updatedDocument.getTitle());
            existingDocument.setContent(updatedDocument.getContent());
            existingDocument.setTags(updatedDocument.getTags());
            return ResponseEntity.ok(existingDocument);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // Delete an existing document
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable("id") long id) {
        Optional<Document> existingDocumentOpt = documents.stream()
                .filter(document -> document.getId() == id)
                .findFirst();

        if (existingDocumentOpt.isPresent()) {
            documents.remove(existingDocumentOpt.get());
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
