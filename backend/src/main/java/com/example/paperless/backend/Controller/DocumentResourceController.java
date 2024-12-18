package com.example.paperless.backend.Controller;

import com.example.paperless.backend.models.Document;
import com.example.paperless.backend.businessLogic.DocumentService;
import com.example.paperless.backend.models.ElasticDocument;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/documents")
@CrossOrigin
@Log
public class DocumentResourceController {

    private final DocumentService documentService;

    @Autowired
    public DocumentResourceController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @GetMapping
    public ResponseEntity<List<Document>> getDocuments() {
        log.info("Fetching all documents...");
        return ResponseEntity.ok(documentService.getAllDocuments());
    }

    @GetMapping("/search")
    public ResponseEntity<List<Document>> searchDocuments(@RequestParam String query) {
        log.info("Searching documents with query: " + query);
        List<Document> results=documentService.searchDocuments(query);;
        return ResponseEntity.ok(results);
    }

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<Document> addDocument(@RequestParam("document") MultipartFile file) {
        try {
            Document savedDocument = documentService.addDocument(file);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedDocument);
        } catch (Exception e) {
            log.severe("Error processing file upload: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id) {
        try {
            documentService.deleteDocument(id);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<byte[]> downloadDocument(@PathVariable Long id) {
        try {
            log.info("Downloading document with ID: " + id);
            byte[] documentBytes = documentService.downloadDocument(id);

            Document document = documentService.getDocumentById(id);
            String fileName = document != null ? document.getTitle() : "document";

            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\"" + fileName + "\"")

                    .body(documentBytes);
        } catch (Exception e) {
            log.severe("Error downloading document with ID " + id + ": " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
