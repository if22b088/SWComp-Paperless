package com.example.paperless.backend;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
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

    private final MinioClient minioClient;

    @Autowired
    public DocumentResourceController(RabbitMQSenderService rabbitMQService, DocumentRepository documentRepository,MinioClient minioClient) {
        this.rabbitMQService = rabbitMQService;
        this.documentRepository = documentRepository;
        this.minioClient = minioClient;
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


            // save/upload document file to MinIO
            String bucketName = "documents";  // minIO bucket to store the file
            //String fileKey = "uploads/" + fileName;  // file path within the bucket

            InputStream fileStream = new ByteArrayInputStream(file.getBytes());

            // check if the bucket exists
            boolean bucketExists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());

            if (!bucketExists) {
                log.info("Bucket does not exist. Creating bucket: " + bucketName);
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            } else {
                log.info("Bucket already exists: " + bucketName);
            }

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)  // object name (file name)
                            //baeldung syntax did not work here
                            .stream(fileStream, fileStream.available(), -1)  // file content, file size, -1 for unlimited size
                            .build()
            );

            //minioClient.putObject(bucketName, fileKey, fileStream, file.getContentType(), file.getSize());

            // Save document to database
            Document document = new Document();
            document.setTitle(fileName);
            document.setContent("File uploaded: " + fileName);
            //document.setFileData(fileContent); <-- not needed because we store in minIO
            document.setDateOfCreation(LocalDateTime.now());
            Document savedDocument = documentRepository.save(document);
            log.info("Document saved with ID: " + savedDocument.getId());

            // send a message to RabbitMQ after the document is stored

            rabbitMQService.sendMessage("documents/" + fileName);
            log.info("Message sent to RabbitMQ: " + fileName);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedDocument);

        } catch (Exception e) {
            log.severe("Error occurred while processing the document upload: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
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
