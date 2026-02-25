package com.example.documentservice.service.kafka;

import com.example.documentservice.dto.OcrTopicMessageDto;
import com.example.documentservice.entity.Document;
import com.example.documentservice.exception.KafkaSendException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final ObjectMapper objectMapper;

    public OcrMessageProducer(KafkaTemplate<String, String> kafkaTemplate,
                              @Value("${kafka.topic.ocr}") String topicOcr,
                              ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.topicOcr = topicOcr;
        this.objectMapper = objectMapper;
    }

    public void sendDocumentForOcr(Document document) {
        try {
            String message = objectMapper.writeValueAsString(new OcrTopicMessageDto(
                    document.getObjectKey(),
                    document.getTitle()
            ));
            logger.info("Sending document '{}' to Kafka topic '{}'", message, topicOcr);
            kafkaTemplate.send(topicOcr, message);
            System.out.println("Sent message to OCR topic: " + message);
        } catch (Exception e) {
            logger.error("Failed to send document '{}' to Kafka topic '{}': {}", document.getObjectKey(), topicOcr, e.getMessage());
            throw new KafkaSendException("Error sending document to Kafka", e);
        }
    }
}
