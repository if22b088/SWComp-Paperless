package com.example.paperless.backend.rabbitMQ;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "documentExchange";
    public static final String DOCUMENT_QUEUE_NAME = "documentUploadQueue";
    public static final String DOCUMENT_ROUTING_KEY = "document.upload";

    // queue and routing key for OCR results
    public static final String OCR_RESULT_QUEUE_NAME = "ocrResultQueue";
    public static final String OCR_RESULT_ROUTING_KEY = "document.ocr.result";


    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(EXCHANGE_NAME);
    }


    // queue and binding for document
    @Bean
    public Queue queue() {
        return new Queue(DOCUMENT_QUEUE_NAME, true);
    }

    @Bean
    public Binding binding(Queue queue, DirectExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(DOCUMENT_ROUTING_KEY);
    }


    // queue and binding for ocrResult
    @Bean
    public Queue ocrResultQueue() {
        return new Queue(OCR_RESULT_QUEUE_NAME, true);
    }
    @Bean
    public Binding ocrResultBinding(Queue ocrResultQueue, DirectExchange exchange) {
        return BindingBuilder.bind(ocrResultQueue).to(exchange).with(OCR_RESULT_ROUTING_KEY);
    }
}
