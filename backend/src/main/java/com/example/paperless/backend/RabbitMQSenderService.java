package com.example.paperless.backend;

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

        //routing key used for queue (queue has to exist (was created manually via webUI)
        String routingKey = "document.upload";
        log.info("Preparing to send message to RabbitMQ with routing key: " + routingKey);
        log.info("Message: " + message);
        try {
            // "documentExchange" is the exchange name, has to exist on rabbitMQ server (was created manually via webUI)
            amqpTemplate.convertAndSend("documentExchange", routingKey, message);
            log.info("Message successfully sent to RabbitMQ exchange 'documentExchange' with routing key: " + routingKey);
        } catch (Exception e) {
            log.severe("Failed to send message to RabbitMQ: " + e.getMessage());
        }

    }
}