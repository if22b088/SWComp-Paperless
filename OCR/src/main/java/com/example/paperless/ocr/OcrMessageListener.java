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
import java.util.HashMap;
import java.util.Map;


@Component
@RabbitListener(queues = RabbitMQConfig.DOCUMENT_QUEUE_NAME)
public class OcrMessageListener {

    private RabbitTemplate rabbitTemplate;


    private OcrService ocrService;


    private MinioClient minioClient;

    private IndexService indexService;



    @Autowired
    public OcrMessageListener(RabbitTemplate rabbitTemplate, OcrService ocrService,MinioClient minioClient, IndexService indexService) {
        this.rabbitTemplate = rabbitTemplate;
        this.ocrService = ocrService;
        this.minioClient = minioClient;
        this.indexService = indexService;
    }

    @RabbitHandler
    public void receiveMessage(String filePath) {
        try {
            System.out.println("filepath: " + filePath);
            // extract ID from filePath (everything up to the first '/')
            String[] parts = filePath.split("/", 2); // Split into two parts
            long id = Long.parseLong(parts[0]); // Parse the first part as a long
            System.out.println("Extracted ID: " + id);

            filePath = parts[1];

            System.out.println("OCRService: Should be Bucket FilePath: "+ filePath);
            File file = downloadFile(filePath);
            String ocrResult = ocrService.performOCR(file);
            System.out.println("ocrResult: "+ocrResult);
            sendResultBack(id,ocrResult);


            // Index documentText in Elasticsearch
            Map<String, Object> document = new HashMap<>();
            document.put("id", id);
            document.put("text", ocrResult);
            document.put("filePath", filePath);

            String indexName = "documents";
            indexService.indexDocument(indexName, String.valueOf(id), document);

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

    //todo check if ocrResultPayload class is needed (how does the restapi retrieve the file from queue)
    private void sendResultBack(Long id, String ocrResult) {

        String message = id +"/"+ ocrResult;
        //OcrResultPayload ocrResultPayload = new OcrResultPayload(ID, ocrResult);
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.OCR_RESULT_ROUTING_KEY, message);
    }
/* does not work because rabbitMQ uses the full qualified class name of which does not exist on the rest api
    @Getter
    @Setter
    static class OcrResultPayload implements Serializable {
        private Long id;
        private String ocrResult;

        public OcrResultPayload(long id, String ocrResult) {
            this.id = id;
            this.ocrResult = ocrResult;
        }
    }

 */

}
