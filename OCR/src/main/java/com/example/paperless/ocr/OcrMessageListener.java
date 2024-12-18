package com.example.paperless.ocr;

import com.example.paperless.ocr.config.RabbitMQConfig;
import com.example.paperless.ocr.model.ElasticDocument;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.errors.MinioException;
import lombok.extern.java.Log;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.io.InputStream;
import java.nio.file.Files;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

@Component
@RabbitListener(queues = RabbitMQConfig.DOCUMENT_QUEUE_NAME)
@Log
public class OcrMessageListener {

    private final RabbitTemplate rabbitTemplate;
    private final OcrWorkerService ocrWorkerService;
    private final MinioClient minioClient;
    private final IndexService indexService;

    @Autowired
    public OcrMessageListener(RabbitTemplate rabbitTemplate, OcrWorkerService ocrWorkerService, MinioClient minioClient, IndexService indexService) {
        this.rabbitTemplate = rabbitTemplate;
        this.ocrWorkerService = ocrWorkerService;
        this.minioClient = minioClient;
        this.indexService = indexService;
    }

    @RabbitHandler
    public void receiveMessage(String message) {
        log.info("Received message: " + message);
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> messagePayload = objectMapper.readValue(message, new TypeReference<Map<String, Object>>() {});

            Long id = ((Number) messagePayload.get("id")).longValue();
            String title = (String) messagePayload.get("title");
            String bucketName = (String) messagePayload.get("bucketName");
            String fileName = (String) messagePayload.get("fileName");

            log.info("Message details - ID: " + id + ", Title: " + title + ", Bucket: " + bucketName + ", File: " + fileName);

            File file = downloadFile(bucketName, fileName);

            String ocrResult = ocrWorkerService.performOCR(file);
            log.info("OCR completed for file: " + fileName);

            sendResultBack(id, ocrResult);

            ElasticDocument elasticDocument = new ElasticDocument(String.valueOf(id), title, ocrResult, bucketName + "/" + fileName);
            indexService.indexDocument(elasticDocument);

            if (file.exists() && !file.delete()) {
                log.warning("Temporary file deletion failed for file: " + file.getAbsolutePath());
            }

            log.info("Message processing completed for ID: " + id);
        } catch (TesseractException | IOException e) {
            log.severe("Error during OCR or file processing: " + e.getMessage());
            throw new RuntimeException("Error processing OCR", e);
        } catch (MinioException | NoSuchAlgorithmException | InvalidKeyException e) {
            log.severe("Error interacting with MinIO: " + e.getMessage());
            throw new RuntimeException("Error accessing MinIO", e);
        } catch (Exception e) {
            log.severe("Unexpected error: " + e.getMessage());
            throw new RuntimeException("Unexpected error occurred", e);
        }
    }

    private File downloadFile(String bucketName, String fileName) throws IOException, MinioException, NoSuchAlgorithmException, InvalidKeyException {
        log.info("Downloading file from MinIO - Bucket: " + bucketName + ", File: " + fileName);

        Path tempFilePath = Paths.get(System.getProperty("java.io.tmpdir"), fileName);
        File tempFile = tempFilePath.toFile();

        try (InputStream inputStream = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(fileName)
                        .build())) {

            Files.copy(inputStream, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            log.info("File downloaded successfully to: " + tempFile.getAbsolutePath());
        } catch (Exception e) {
            log.severe("Error downloading file from MinIO: " + e.getMessage());
            throw e;
        }

        return tempFile;
    }

    private void sendResultBack(Long id, String ocrResult) {
        log.info("Sending OCR result back for ID: " + id);
        try {
            String message = id + "/" + ocrResult;
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.OCR_RESULT_ROUTING_KEY, message);
            log.info("OCR result sent successfully for ID: " + id);
        } catch (Exception e) {
            log.severe("Error sending OCR result: " + e.getMessage());
            throw new RuntimeException("Failed to send OCR result", e);
        }
    }
}