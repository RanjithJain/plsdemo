package com.example.plsdemo.service;


import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumer {

    @KafkaListener(topics = "user-updates", groupId = "my-group")
    public void listen(String message) {
        // Process the message and update the service
        System.out.println("Received message: " + message);

        // Perform any service logic, e.g., update user records
    }
}
