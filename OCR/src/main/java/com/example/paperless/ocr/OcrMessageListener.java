package com.example.paperless.ocr;

import com.example.paperless.ocr.config.RabbitMQConfig;
import com.example.paperless.ocr.model.ElasticDocument;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.errors.MinioException;
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
        try {
            // parse JSON message
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> messagePayload = objectMapper.readValue(message, new TypeReference<Map<String, Object>>() {});

            Long id = ((Number) messagePayload.get("id")).longValue();
            String title = (String) messagePayload.get("title");
            String bucketName = (String) messagePayload.get("bucketName");
            String fileName = (String) messagePayload.get("fileName");

            System.out.println("ID: " + id);
            System.out.println("Title: " + title);
            System.out.println("Bucket: " + bucketName);
            System.out.println("File: " + fileName);

            // process file from MinIO
            String filePath = bucketName + "/" + fileName;
            File file = downloadFile(filePath);
            String ocrResult = ocrWorkerService.performOCR(file);
            System.out.println("OCR Result: " + ocrResult);
            sendResultBack(id,ocrResult);

            //index documentText in Elasticsearch
            ElasticDocument elasticDocument = new ElasticDocument(String.valueOf(id), title, ocrResult,filePath);
            indexService.indexDocument(elasticDocument);

            if (file.exists()) {
                file.delete();
            }

        } catch (TesseractException | IOException e) {
            e.printStackTrace();
        } catch (MinioException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    private File downloadFile(String filePath) throws IOException, MinioException, NoSuchAlgorithmException, InvalidKeyException {
        // extract bucket name and object name from the filePath
        // assuming the filePath contains "bucketName/objectName"
        String[] parts = filePath.split("/");
        String bucketName = parts[0];
        String objectName = parts[1];


        // create a file on the local file system where the file will be stored
        Path tempFilePath = Paths.get(System.getProperty("java.io.tmpdir"), objectName);
        File tempFile = tempFilePath.toFile();

        // get file from minIO
        try (InputStream inputStream = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .build())) {

            // copy the input stream to a local file
            Files.copy(inputStream, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }

        return tempFile;
    }

    private void sendResultBack(Long id, String ocrResult) {
        String message = id +"/"+ ocrResult;
        //OcrResultPayload ocrResultPayload = new OcrResultPayload(ID, ocrResult);
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.OCR_RESULT_ROUTING_KEY, message);
    }

}
