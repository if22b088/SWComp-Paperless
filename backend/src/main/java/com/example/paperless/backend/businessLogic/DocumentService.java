package com.example.paperless.backend.businessLogic;

import com.example.paperless.backend.ElasticSearch.ElasticRepository;
import com.example.paperless.backend.models.Document;
import com.example.paperless.backend.models.ElasticDocument;
import com.example.paperless.backend.rabbitMQ.RabbitMQSenderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.minio.*;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Log
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final RabbitMQSenderService rabbitMQService;
    private final MinioClient minioClient;
    private final ElasticRepository elasticRepository;

    @Autowired
    public DocumentService(DocumentRepository documentRepository, RabbitMQSenderService rabbitMQService, MinioClient minioClient, ElasticRepository elasticRepository) {
        this.documentRepository = documentRepository;
        this.rabbitMQService = rabbitMQService;
        this.minioClient = minioClient;
        this.elasticRepository = elasticRepository;
    }

    public List<Document> getAllDocuments() {
        log.info("Fetching all documents...");
        return documentRepository.findAll();
    }

    public Document getDocumentById(Long id) {
        log.info("Fetching document with ID: " + id);
        return documentRepository.findById(id).orElse(null);
    }

    public Document addDocument(MultipartFile file) throws Exception {
        if (file.isEmpty()) {
            log.warning("Received empty file upload request.");
            throw new IllegalArgumentException("File is empty");
        }

        String fileName = file.getOriginalFilename();
        log.info("Processing file upload: " + fileName);

        String bucketName = "documents";
        InputStream fileStream = new ByteArrayInputStream(file.getBytes());

        try {
            boolean bucketExists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!bucketExists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                log.info("Created new bucket: " + bucketName);
            }

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .stream(fileStream, fileStream.available(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
            log.info("Successfully uploaded file to MinIO: " + fileName);
        } catch (Exception e) {
            log.severe("Error uploading file to MinIO: " + e.getMessage());
            throw new RuntimeException("Failed to upload file to MinIO");
        }

        Document document = new Document();
        document.setTitle(fileName);
        document.setContent("File uploaded: " + fileName);
        document.setDateOfCreation(LocalDateTime.now());
        Document savedDocument = documentRepository.save(document);
        log.info("Document saved in database with ID: " + savedDocument.getId());

        Map<String, Object> messagePayload = new HashMap<>();
        messagePayload.put("id", savedDocument.getId());
        messagePayload.put("title", savedDocument.getTitle());
        messagePayload.put("bucketName", bucketName);
        messagePayload.put("fileName", fileName);

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String message = objectMapper.writeValueAsString(messagePayload);
            rabbitMQService.sendMessage(message);
            log.info("Message sent to RabbitMQ: " + message);
        } catch (Exception e) {
            log.severe("Error sending message to RabbitMQ: " + e.getMessage());
            throw new RuntimeException("Failed to send message to RabbitMQ");
        }

        return savedDocument;
    }

    public void updateDocument(Long id, String documentContent) {
        log.info("Updating document with ID: " + id);
        Optional<Document> tempDocument = documentRepository.findById(id);
        if (tempDocument.isPresent()) {
            Document document = tempDocument.get();
            document.setContent(documentContent);
            documentRepository.save(document);
            log.info("Document updated successfully with ID: " + id);
        } else {
            log.warning("Document not found for ID: " + id);
            throw new IllegalArgumentException("Document not found");
        }
    }

    public List<Document> searchDocuments(String query) {
        log.info("Searching documents with query: " + query);
        List<ElasticDocument> elasticSearchResults = elasticRepository.findByTitleContainingOrContentContaining(query, query);
        List<Long> ids = new ArrayList<>();

        for (ElasticDocument doc : elasticSearchResults) {
            ids.add(Long.parseLong(doc.getId()));
            log.info("Found documents matching query: " + query);
        }

        return documentRepository.findDocumentsByIds(ids);
    }

    public void deleteDocument(Long id) {
        log.info("Deleting document with ID: " + id);
        Document document = getDocumentById(id);
        if (document == null) {
            log.warning("Document not found for ID: " + id);
            throw new IllegalArgumentException("Document not found");
        }

        String bucketName = "documents";
        String fileName = document.getTitle();

        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .build()
            );
            log.info("Successfully deleted document from MinIO: " + fileName);
        } catch (Exception e) {
            log.severe("Error deleting document from MinIO: " + e.getMessage());
            throw new RuntimeException("Failed to delete document from MinIO");
        }

        try {
            elasticRepository.deleteById(String.valueOf(id));
            log.info("Successfully deleted document from Elasticsearch: " + id);
        } catch (Exception e) {
            log.severe("Error deleting document from Elasticsearch: " + e.getMessage());
            throw new RuntimeException("Failed to delete document from Elasticsearch");
        }

        documentRepository.deleteById(id);
        log.info("Successfully deleted document from the database: " + id);
    }

    public byte[] downloadDocument(Long id) throws Exception {
        log.info("Downloading document with ID: " + id);
        Document document = getDocumentById(id);
        if (document == null) {
            log.warning("Document not found for ID: " + id);
            throw new IllegalArgumentException("Document not found");
        }

        String bucketName = "documents";
        String fileName = document.getTitle();

        try (InputStream stream = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(fileName)
                        .build())) {

            log.info("Successfully retrieved document from MinIO: " + fileName);
            return stream.readAllBytes();
        } catch (Exception e) {
            log.severe("Error retrieving document from MinIO: " + e.getMessage());
            throw new Exception("Could not download document");
        }
    }
}