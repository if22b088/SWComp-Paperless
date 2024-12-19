package com.example.paperless.backend;

import com.example.paperless.backend.rabbitMQ.RabbitMQConfig;
import com.example.paperless.backend.rabbitMQ.RabbitMQSenderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.amqp.core.AmqpTemplate;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class RabbitMQSenderServiceTest {

    @Mock
    private AmqpTemplate amqpTemplate;

    @InjectMocks
    private RabbitMQSenderService rabbitMQSenderService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testSendMessage() {
        String message = "Test message";

        rabbitMQSenderService.sendMessage(message);

        // Verify that convertAndSend was called with the correct exchange, routing key, and message
        verify(amqpTemplate, times(1)).convertAndSend(
                eq(RabbitMQConfig.EXCHANGE_NAME),
                eq(RabbitMQConfig.DOCUMENT_ROUTING_KEY),
                eq(message)
        );
    }

    @Test
    public void testSendMessageException() {
        String message = "Test message";

        // Simulate an exception being thrown by amqpTemplate
        doThrow(new RuntimeException("Simulated failure")).when(amqpTemplate).convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                RabbitMQConfig.DOCUMENT_ROUTING_KEY,
                message
        );

        rabbitMQSenderService.sendMessage(message);

        // Even if an exception occurs, we don't expect the method to rethrow it (based on the current code)
        // Just verify that convertAndSend was called and the log was produced
        verify(amqpTemplate, times(1)).convertAndSend(
                eq(RabbitMQConfig.EXCHANGE_NAME),
                eq(RabbitMQConfig.DOCUMENT_ROUTING_KEY),
                eq(message)
        );
    }
}

