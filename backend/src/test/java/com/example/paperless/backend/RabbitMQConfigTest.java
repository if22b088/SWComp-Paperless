package com.example.paperless.backend;

import com.example.paperless.backend.rabbitMQ.RabbitMQConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class RabbitMQConfigTest {

    @Autowired
    private DirectExchange exchange;

    @Autowired
    private Queue queue; // This should correspond to DOCUMENT_QUEUE_NAME

    @Autowired
    private Binding binding; // Binding for document queue

    @Autowired
    private Queue ocrResultQueue; // OCR_RESULT_QUEUE_NAME

    @Autowired
    private Binding ocrResultBinding; // Binding for OCR result queue

    @Test
    public void testExchangeBean() {
        Assertions.assertNotNull(exchange, "Exchange bean should be created.");
        Assertions.assertEquals(RabbitMQConfig.EXCHANGE_NAME, exchange.getName(), "Exchange name should match the configured value.");
    }

    @Test
    public void testDocumentQueueBean() {
        Assertions.assertNotNull(queue, "Queue bean should be created.");
        Assertions.assertEquals(RabbitMQConfig.DOCUMENT_QUEUE_NAME, queue.getName(), "Queue name should match the configured value.");
        Assertions.assertTrue(queue.isDurable(), "Document queue should be durable.");
    }

    @Test
    public void testDocumentBindingBean() {
        Assertions.assertNotNull(binding, "Binding bean should be created.");
        Assertions.assertEquals(RabbitMQConfig.DOCUMENT_ROUTING_KEY, binding.getRoutingKey(), "Document binding routing key should match the configured value.");
        Assertions.assertEquals(RabbitMQConfig.EXCHANGE_NAME, binding.getExchange(), "Document binding should be associated with the correct exchange.");
        Assertions.assertEquals(RabbitMQConfig.DOCUMENT_QUEUE_NAME, binding.getDestination(), "Document binding should be associated with the correct queue.");
    }

    @Test
    public void testOcrResultQueueBean() {
        Assertions.assertNotNull(ocrResultQueue, "OCR Result queue bean should be created.");
        Assertions.assertEquals(RabbitMQConfig.OCR_RESULT_QUEUE_NAME, ocrResultQueue.getName(), "OCR result queue name should match the configured value.");
        Assertions.assertTrue(ocrResultQueue.isDurable(), "OCR result queue should be durable.");
    }

    @Test
    public void testOcrResultBindingBean() {
        Assertions.assertNotNull(ocrResultBinding, "OCR result binding bean should be created.");
        Assertions.assertEquals(RabbitMQConfig.OCR_RESULT_ROUTING_KEY, ocrResultBinding.getRoutingKey(), "OCR result binding routing key should match the configured value.");
        Assertions.assertEquals(RabbitMQConfig.EXCHANGE_NAME, ocrResultBinding.getExchange(), "OCR result binding should be associated with the correct exchange.");
        Assertions.assertEquals(RabbitMQConfig.OCR_RESULT_QUEUE_NAME, ocrResultBinding.getDestination(), "OCR result binding should be associated with the correct queue.");
    }
}

