package com.example.paperless.backend.rabbitMQ;

import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Service;


// https://www.springcloud.io/post/2022-03/messaging-using-rabbitmq-in-spring-boot-application/#rabbitmqsenderjava
@Service
@Log
public class RabbitMQSenderService {

    private final AmqpTemplate amqpTemplate;

    @Autowired
    public RabbitMQSenderService(AmqpTemplate amqpTemplate) {
        this.amqpTemplate = amqpTemplate;
    }

    public void sendMessage(String message) {

        String routingKey = RabbitMQConfig.DOCUMENT_ROUTING_KEY;//"document.upload";
        String exchange = RabbitMQConfig.EXCHANGE_NAME;
        log.info("Preparing to send message to RabbitMQ with routing key: " + routingKey);
        log.info("Message: " + message); // <-- maybe too much? careful with sensitive information
        try {
            // exchange and routingKey are automatically created through Spring RabbitMQConfig class (alternatively could be created manually via webUI)
            amqpTemplate.convertAndSend(exchange, routingKey, message);
            log.info("Message successfully sent to RabbitMQ exchange '" + exchange + "' with routing key: '" + routingKey + "'");
        } catch (Exception e) {
            log.severe("Failed to send message to RabbitMQ: " + e.getMessage());
        }

    }
}