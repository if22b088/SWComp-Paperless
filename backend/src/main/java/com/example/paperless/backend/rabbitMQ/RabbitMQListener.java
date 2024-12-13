package com.example.paperless.backend.rabbitMQ;
import com.example.paperless.backend.businessLogic.DocumentService;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@RabbitListener(queues = RabbitMQConfig.OCR_RESULT_QUEUE_NAME)
public class RabbitMQListener {
    private final DocumentService documentService;

    @Autowired
    public RabbitMQListener(DocumentService documentService) {
        this.documentService = documentService;
    }
    @RabbitHandler
    //todo: is it correct to perform the update to the db here?
    public void receiveMessage(String documentContent) {
            String[] parts = documentContent.split("/", 2); // Split into two parts on the first /
            String id = parts[0]; // Parse the first part as a long
            String ocrResult = parts[1];
            long newID =Long.parseLong(id);
            documentService.updateDocument(newID,ocrResult);
    }
}


