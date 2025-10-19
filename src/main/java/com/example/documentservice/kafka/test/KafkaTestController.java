package com.example.documentservice.kafka.test;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/test")
public class KafkaTestController {

    private final KafkaTestProducer producer;

    public KafkaTestController(KafkaTestProducer producer) {
        this.producer = producer;
    }

    @PostMapping("/kafka")
    public String sendToKafka(@RequestBody String json) {
        producer.sendTestMessage(json);
        return "✅ Sent JSON to Kafka: " + json;
    }
}
