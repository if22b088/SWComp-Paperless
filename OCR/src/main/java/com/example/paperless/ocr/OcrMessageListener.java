package com.example.paperless.ocr;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.errors.MinioException;
import lombok.Getter;
import lombok.Setter;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.amqp.rabbit.core.RabbitTemplate;


import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.io.InputStream;
import java.nio.file.Files;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;


@Component
@RabbitListener(queues = RabbitMQConfig.DOCUMENT_QUEUE_NAME)
public class OcrMessageListener {

    private RabbitTemplate rabbitTemplate;


    private OcrService ocrService;


    private MinioClient minioClient;


    @Autowired
    public OcrMessageListener(RabbitTemplate rabbitTemplate, OcrService ocrService,MinioClient minioClient) {
        this.rabbitTemplate = rabbitTemplate;
        this.ocrService = ocrService;
        this.minioClient = minioClient;
    }

    @RabbitHandler
    public void receiveMessage(String filePath) {
        try {
            System.out.println("OCRService: Should be Bucket FilePath: "+ filePath);
            File file = downloadFile(filePath);
            String ocrResult = ocrService.performOCR(file);
            System.out.println("ocrResult: "+ocrResult);
            sendResultBack(filePath,ocrResult);
            //TODO index documentText in elastic search
            //TODO remove local tmpFile created in downloadFile

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
        // Assuming the filePath contains "bucketName/objectName"
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

            // Copy the input stream to a local file
            Files.copy(inputStream, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }

        return tempFile;
    }

    //todo check if ocrResultPayload class is needed (how does the restapi retrieve the file from queue)
    private void sendResultBack(String filePath, String ocrResult) {
        //OcrResultPayload payload = new OcrResultPayload(filePath, ocrResult);
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.OCR_RESULT_ROUTING_KEY, ocrResult);
    }
/*
    @Getter
    @Setter
    static class OcrResultPayload implements Serializable {
        private String filePath;
        private String ocrResult;

        public OcrResultPayload(String filePath, String ocrResult) {
            this.filePath = filePath;
            this.ocrResult = ocrResult;
        }
    }

 */
}
