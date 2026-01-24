package at.fhtw.worker.listener;


import at.fhtw.worker.dto.OcrTopicMessageDto;
import at.fhtw.worker.dto.ResultTopicMessageDto;
import at.fhtw.worker.exception.OcrProcessingException;
import at.fhtw.worker.service.OcrProcessingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.minio.messages.JsonOutputSerialization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class OcrListener {

    private static final Logger log = LoggerFactory.getLogger(OcrListener.class);

    private final OcrProcessingService ocrProcessingService;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final String inputTopic;
    private final String outputTopic;
    private final String dlqTopic;

    public OcrListener(OcrProcessingService ocrProcessingService, KafkaTemplate<String, String> kafkaTemplate,
                       @Value("${kafka.topic.input:doc.ocr}") String inputTopic,
                       @Value("${kafka.topic.output:doc.ocr.result}") String outputTopic,
                       @Value("${kafka.topic.dlq:doc.ocr.dlq}") String dlqTopic) {
        this.ocrProcessingService = ocrProcessingService;
        this.kafkaTemplate = kafkaTemplate;
        this.inputTopic = inputTopic;
        this.outputTopic = outputTopic;
        this.dlqTopic = dlqTopic;
    }

    @KafkaListener(topics = "doc.ocr", groupId = "ocr-worker")
    public void consume(String message) {
        log.info("Received message from Kafka topic '{}': {}", inputTopic, message);

        try {
            // Deserialize incoming message to DTO
            OcrTopicMessageDto dto = objectMapper.readValue(message, OcrTopicMessageDto.class);

            // Process and get result DTO
            ResultTopicMessageDto resultDto = ocrProcessingService.process(dto);

            // Serialize result and send
            String resultJson = objectMapper.writeValueAsString(resultDto);
            kafkaTemplate.send(outputTopic, resultJson);
            log.info("Sent result to topic '{}', documentId={}", outputTopic, resultDto.getObjectKey());

        } catch (OcrProcessingException ex) {
            log.error("OCR processing failed: {}", ex.getMessage(), ex);
            kafkaTemplate.send(dlqTopic, message);
            log.warn("Message sent to DLQ ({})", dlqTopic);
        } catch (Exception e) {
            log.error("Unexpected error in OCR worker: {}", e.getMessage(), e);
            kafkaTemplate.send(dlqTopic, message);
        }
    }
}
