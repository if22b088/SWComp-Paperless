package com.example.paperless.backend.businessLogic;

import com.example.paperless.backend.models.Document;
import com.example.paperless.backend.DocumentRepository;
import com.example.paperless.backend.rabbitMQ.RabbitMQSenderService;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Log
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final RabbitMQSenderService rabbitMQService;
    private final MinioClient minioClient;

    @Autowired
    public DocumentService(DocumentRepository documentRepository, RabbitMQSenderService rabbitMQService, MinioClient minioClient) {
        this.documentRepository = documentRepository;
        this.rabbitMQService = rabbitMQService;
        this.minioClient = minioClient;
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

        boolean bucketExists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        if (!bucketExists) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
        }

        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(fileName)
                        .stream(fileStream, fileStream.available(), -1)
                        .build()
        );

        Document document = new Document();
        document.setTitle(fileName);
        document.setContent("File uploaded: " + fileName);
        document.setDateOfCreation(LocalDateTime.now());
        Document savedDocument = documentRepository.save(document);

        rabbitMQService.sendMessage(savedDocument.getId()+"/documents/" + fileName);
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

    public void deleteDocument(Long id) {
        if (documentRepository.existsById(id)) {
            documentRepository.deleteById(id);
        } else {
            throw new IllegalArgumentException("Document not found");
        }
    }
}
