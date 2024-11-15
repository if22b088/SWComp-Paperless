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


    private final RabbitMQSenderService rabbitMQService;

    private final DocumentRepository documentRepository;

    @Autowired
    public DocumentResourceController(RabbitMQSenderService rabbitMQService, DocumentRepository documentRepository) {
        this.rabbitMQService = rabbitMQService;
        this.documentRepository = documentRepository;
    }

    @GetMapping
    public ResponseEntity<List<Document>> getDocuments() {
        log.info("Fetching all documents...");
        List<Document> documents = documentRepository.findAll();
        log.info("Found " + documents.size() + " documents.");
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
        try {
            if (file.isEmpty()) {
                log.warning("Received empty file upload request.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }

            String fileName = file.getOriginalFilename();
            log.info("Received file upload with filename: " + fileName);
            //gets the file content as byte array;
            byte[] fileContent = file.getBytes();

            // Save document to database
            Document document = new Document();
            document.setTitle(fileName);
            document.setContent("File uploaded: " + fileName);
            document.setFileData(fileContent);
            document.setDateOfCreation(LocalDateTime.now());
            Document savedDocument = documentRepository.save(document);
            log.info("Document saved with ID: " + savedDocument.getId());

            // Send a message to RabbitMQ after the document is stored
            String message = "New document uploaded: " + fileName;
            rabbitMQService.sendMessage(message);
            log.info("Message sent to RabbitMQ: " + message);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedDocument);

        } catch (Exception e) {
            log.severe("Error occurred while processing the document upload: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
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
        log.info("Deleting document with ID: " + id);
        Optional<Document> tempDocument = documentRepository.findById(id);
        if (tempDocument.isPresent()) {
            log.info("Document with ID " + id + " deleted successfully.");
            documentRepository.deleteById(id);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } else {
            log.warning("Document with ID " + id + " not found for deletion.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
