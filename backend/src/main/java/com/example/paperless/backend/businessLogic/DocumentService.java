package com.example.paperless.backend.businessLogic;

import com.example.paperless.backend.ElasticSearch.ElasticRepository;
import com.example.paperless.backend.models.Document;
import com.example.paperless.backend.models.ElasticDocument;
import com.example.paperless.backend.rabbitMQ.RabbitMQSenderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.minio.*;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
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
    public DocumentService( DocumentRepository documentRepository, RabbitMQSenderService rabbitMQService, MinioClient minioClient, ElasticRepository elasticRepository) {
        this.documentRepository = documentRepository;
        this.rabbitMQService = rabbitMQService;
        this.minioClient = minioClient;
        this.elasticRepository = elasticRepository;
    }

    public List<Document> getAllDocuments() {
        return documentRepository.findAll();
    }


    public Document getDocumentById(Long id) {
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
        //InputStream fileStream = file.getInputStream();

        //save document to minIO bucket
        boolean bucketExists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        if (!bucketExists) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
        }

        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(fileName)
                        .stream(fileStream, fileStream.available(), -1)
                        .contentType(file.getContentType()) // Preserve the file's content type
                        .build()
        );

        //save document to database
        Document document = new Document();
        document.setTitle(fileName);
        document.setContent("File uploaded: " + fileName);
        document.setDateOfCreation(LocalDateTime.now());
        Document savedDocument = documentRepository.save(document);

        // create json message
        Map<String, Object> messagePayload = new HashMap<>();
        messagePayload.put("id", savedDocument.getId());
        messagePayload.put("title", savedDocument.getTitle());
        messagePayload.put("bucketName", bucketName);
        messagePayload.put("fileName", fileName);

        ObjectMapper objectMapper = new ObjectMapper();
        String message = objectMapper.writeValueAsString(messagePayload);

        // send message to RabbitMQ
        rabbitMQService.sendMessage(message);

        return savedDocument;
    }

    public void updateDocument(Long id, String documentContent) {
        Optional<Document> tempDocument = documentRepository.findById(id);
        if (tempDocument.isPresent()) {
            Document document = tempDocument.get();
            document.setContent(documentContent);
            //document.setDateOfCreation(LocalDateTime.now()); <--- now we dont update the upload time
            documentRepository.save(document);
        } else {
            throw new IllegalArgumentException("Document not found");
        }
    }

    public List<Document> searchDocuments(String query) {
       List<ElasticDocument> elasticSearchResults = elasticRepository.findByTitleContainingOrContentContaining(query, query);
       List<Long> ids = new ArrayList<>();

        for (ElasticDocument doc : elasticSearchResults) {
            ids.add(Long.parseLong(doc.getId()));
        }

       return documentRepository.findDocumentsByIds(ids);
    }


    /*
    public void deleteDocument(Long id) {
        if (documentRepository.existsById(id)) {
            documentRepository.deleteById(id);
        } else {
            throw new IllegalArgumentException("Document not found");
        }
    }

     */
    public void deleteDocument(Long id) {
        Document document = getDocumentById(id);
        if (document == null) {
            throw new IllegalArgumentException("Document not found");
        }

        String bucketName = "documents";
        String fileName = document.getTitle();

        try {
            //delete from minIO
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
            //delete from elasticSearch
            elasticRepository.deleteById(String.valueOf(id));
            log.info("Successfully deleted document from Elasticsearch: " + id);
        } catch (Exception e) {
            log.severe("Error deleting document from Elasticsearch: " + e.getMessage());
            throw new RuntimeException("Failed to delete document from Elasticsearch");
        }

        //delete from db
        documentRepository.deleteById(id);
        log.info("Successfully deleted document from the database: " + id);
    }


    public byte[] downloadDocument(Long id) throws Exception {
        Document document = getDocumentById(id);
        if (document == null) {
            throw new IllegalArgumentException("Document not found");
        }

        String bucketName = "documents";
        String fileName = document.getTitle();

        try (InputStream stream = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(fileName)
                        .build())) {

            return stream.readAllBytes();
        } catch (Exception e) {
            log.severe("Error retrieving document from MinIO: " + e.getMessage());
            throw new Exception("Could not download document");
        }
    }
}