package com.example.paperless.backend;

import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/documents")
@CrossOrigin
@Log //default lombok logger
public class DocumentResourceController {

    @Autowired
    private DocumentRepository documentRepository;

    @GetMapping
    public ResponseEntity<List<Document>> getDocuments() {
        List<Document> documents = documentRepository.findAll();
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

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<Document> addDocument(@RequestParam("document") MultipartFile file) {

            if (file.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }

            // Save file to server
            String fileName = file.getOriginalFilename();
            //gets the file content as byte array;
            //byte[] fileContent = file.getBytes();

            // Create Document object and save to database
            Document document = new Document();
            document.setTitle(fileName);
            document.setContent("File uploaded: " + fileName);
            document.setDateOfCreation(LocalDateTime.now());
            Document savedDocument = documentRepository.save(document);

            return ResponseEntity.status(HttpStatus.CREATED).body(savedDocument);

    }
/*
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<Document> addDocument(@RequestBody Document document) {

        log.info("Received document" + document.getTitle());

        document.setDateOfCreation(LocalDateTime.now());
        //for testing
        document.setContent("pdf uploaded");
        Document savedDocument = documentRepository.save(document);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedDocument);
    }

 */


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
