package at.fhtw.worker.listener;


import at.fhtw.worker.exception.OcrProcessingException;
import at.fhtw.worker.service.OcrProcessingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class OcrListener {

    private static final Logger log = LoggerFactory.getLogger(OcrListener.class);

    private final OcrProcessingService ocrProcessingService;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public OcrListener(OcrProcessingService ocrProcessingService, KafkaTemplate<String, String> kafkaTemplate) {
        this.ocrProcessingService = ocrProcessingService;
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = "doc.ocr", groupId = "ocr-worker")
    public void consume(String message) {
        log.info("Received message from Kafka topic 'doc.ocr': {}", message);

        try {
            String result = ocrProcessingService.process(message);
            log.info("Successfully processed message: {}", result);

            kafkaTemplate.send("doc.ocr.result", result);
            log.info("Sent result to topic 'doc.ocr.result'");

        } catch (OcrProcessingException ex) {
            log.error("OCR processing failed: {}", ex.getMessage(), ex);
            kafkaTemplate.send("doc.ocr.dlq", message);
            log.warn("Message sent to DLQ (doc.ocr.dlq)");
        } catch (Exception e) {
            log.error("Unexpected error in OCR worker: {}", e.getMessage(), e);
            kafkaTemplate.send("doc.ocr.dlq", message);
        }
    }
}
