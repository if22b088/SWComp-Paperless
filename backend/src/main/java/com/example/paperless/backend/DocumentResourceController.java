package com.example.paperless.backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/documents")
@CrossOrigin
public class DocumentResourceController {

    //private List<Document> documents = new ArrayList<>();

    @Autowired
    private DocumentRepository documentRepository;


    @GetMapping
    public ResponseEntity<List<Document>> getDocuments() {

        //returns hardcoded list with 1 entry
        //documents = new ArrayList<>();
        //Document document = new Document();
        //document.setContent("Document Content");
        //documents.add(document);

        List<Document> documents = documentRepository.findAll();

        /*//creates a document in case get is first test request
        if (documents.isEmpty()) {
            Document testDocument = new Document();
            testDocument.setId(1);
            testDocument.setTitle("Test Document");
            testDocument.setContent("This is a test document.");
            testDocument.setTags(Arrays.asList("test", "sample"));
            testDocument.setDateOfCreation(LocalDateTime.now());

            documents.add(testDocument);
        }

         */

        return ResponseEntity.ok(documents);
    }
    @GetMapping("/{id}")
    public ResponseEntity<Document> getDocumentById(@PathVariable Long id) {
        Document document = documentRepository.findById(id).orElse(null);
        if (document == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        return ResponseEntity.ok(document);
    }

    @PostMapping
    public ResponseEntity<Document> addDocument(@RequestBody Document document) {
        document.setDateOfCreation(LocalDateTime.now());
        //documents.add(document);
        Document savedDocument = documentRepository.save(document);
        return ResponseEntity.status(HttpStatus.CREATED).body(document);
    }


    @PutMapping("/{id}")
    public ResponseEntity<Document> updateDocument(@PathVariable Long id, @RequestBody Document documentDetails) {
        Optional<Document> tempDocument = documentRepository.findById(id);
        if (tempDocument.isPresent()) {
            Document document = tempDocument.get();
            document.setTitle(documentDetails.getTitle());
            document.setContent(documentDetails.getContent());
            document.setDateOfCreation(LocalDateTime.now());
            Document updatedDocument = documentRepository.save(document);
            return ResponseEntity.ok(updatedDocument);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id) {
        Optional<Document> tempDocument = documentRepository.findById(id);
        if (tempDocument.isPresent()) {
            documentRepository.deleteById(id);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
