package com.example.documentservice.kafka.test;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaTestProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final String topic;

    public KafkaTestProducer(KafkaTemplate<String, String> kafkaTemplate,
                             @Value("${kafka.topic.ocr}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    public void sendTestMessage(String json) {
        kafkaTemplate.send(topic, json);
        System.out.println("📨 Sent message to Kafka topic '" + topic + "': " + json);
    }
}
