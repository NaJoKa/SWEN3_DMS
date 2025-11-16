package com.example.documentservice.service;

import com.example.documentservice.entity.Document;
import com.example.documentservice.exception.KafkaSendException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class OcrMessageProducer {

    private static final Logger logger = LoggerFactory.getLogger(OcrMessageProducer.class);
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final String topicOcr;

    public OcrMessageProducer(KafkaTemplate<String, String> kafkaTemplate,
                              @Value("${kafka.topic.ocr}") String topicOcr) {
        this.kafkaTemplate = kafkaTemplate;
        this.topicOcr = topicOcr;
    }

    public void sendDocumentForOcr(String documentId) {
        try {
            String message = String.format("{\"documentId\": \"%s\"}", documentId);
            logger.info("Sending document '{}' to Kafka topic '{}'", documentId, topicOcr);
            kafkaTemplate.send(topicOcr, message);
            System.out.println("Sent message to OCR topic: " + message);
        } catch (Exception e) {
            logger.error("Failed to send document '{}' to Kafka topic '{}': {}", documentId, topicOcr, e.getMessage());
            throw new KafkaSendException("Error sending document to Kafka", e);
        }
    }
}
