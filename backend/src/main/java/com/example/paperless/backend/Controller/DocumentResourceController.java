package com.example.paperless.backend.Controller;

import com.example.paperless.backend.models.Document;
import com.example.paperless.backend.businessLogic.DocumentService;
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
        try {
            List<Document> documents = documentService.getAllDocuments();
            log.info("Successfully fetched all documents.");
            return ResponseEntity.ok(documents);
        } catch (Exception e) {
            log.severe("Error fetching documents: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<Document>> searchDocuments(@RequestParam String query) {
        log.info("Searching documents with query: " + query);
        try {
            List<Document> results = documentService.searchDocuments(query);
            log.info("Search completed successfully for query: " + query);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            log.severe("Error searching documents with query: " + query + ". " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<Document> addDocument(@RequestParam("document") MultipartFile file) {
        log.info("Received file upload request.");
        try {
            Document savedDocument = documentService.addDocument(file);
            log.info("File uploaded and saved successfully with ID: " + savedDocument.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(savedDocument);
        } catch (Exception e) {
            log.severe("Error processing file upload: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id) {
        log.info("Request received to delete document with ID: " + id);
        try {
            documentService.deleteDocument(id);
            log.info("Document deleted successfully with ID: " + id);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (Exception e) {
            log.warning("Error deleting document with ID: " + id + ". " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<byte[]> downloadDocument(@PathVariable Long id) {
        log.info("Downloading document with ID: " + id);
        try {
            byte[] documentBytes = documentService.downloadDocument(id);
            Document document = documentService.getDocumentById(id);
            String fileName = document != null ? document.getTitle() : "document";

            log.info("Document downloaded successfully with ID: " + id);
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\"" + fileName + "\"")
                    .body(documentBytes);
        } catch (Exception e) {
            log.severe("Error downloading document with ID " + id + ": " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
