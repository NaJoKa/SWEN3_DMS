package at.fhtw.genAIworker.listener;

import at.fhtw.genAIworker.dto.GenAiResultMessage;
import at.fhtw.genAIworker.dto.OcrResultMessage;
import at.fhtw.genAIworker.service.GeminiService;
import at.fhtw.genAIworker.repository.PDFDocumentRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.UUID;

@Component
public class GenAIConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(GenAIConsumer.class);

    private final GeminiService geminiService;
    private final PDFDocumentRepository pdfDocumentRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${kafka.topic.output.genai}")
    private String outputTopic;

    public GenAIConsumer(GeminiService geminiService,
                         PDFDocumentRepository pdfDocumentRepository,
                         KafkaTemplate<String, String> kafkaTemplate) {
        this.geminiService = geminiService;
        this.pdfDocumentRepository = pdfDocumentRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = "${kafka.topic.input.genai}", groupId = "${spring.kafka.consumer.group-id}")
    public void listen(String message) {
        LOG.info("Received message for GenAI summarization: {}", message);

        if (message == null || message.isBlank()) {
            LOG.warn("Received empty message, skipping");
            return;
        }

        try {
            OcrResultMessage payload = objectMapper.readValue(message, OcrResultMessage.class);
            String opensearchId = UUID.randomUUID().toString();

            // Deduplicate: if document already indexed, skip to avoid repeated Gemini calls
            try {
                if (pdfDocumentRepository.existsById(opensearchId)) {
                    LOG.info("Document {} already indexed, skipping processing.", opensearchId);
                    return;
                }
            } catch (Exception e) {
                LOG.warn("Could not check existing document id {} due to: {}. Proceeding with processing.", opensearchId, e.getMessage());
            }

            String text = payload.processedMessage();
            String fileName = payload.fileName();
            String objectKey = payload.objectKey();

            //String summary = geminiService.summarize(text);
            String summary = "This is a placeholder summary. Replace with actual Gemini API call.";
            LOG.info("Generated summary: {}", summary);

            // Index document using provided or generated opensearchId
            geminiService.indexDocument(opensearchId, fileName, text, summary);
            GenAiResultMessage output = new GenAiResultMessage(text, summary, objectKey, opensearchId, fileName);
            String outputJson = serialize(output);


            if (outputTopic != null && !outputTopic.isBlank()) {
                try {
                    kafkaTemplate.send(outputTopic, opensearchId, outputJson);
                    LOG.info("Published summary to topic {} for opensearchId {}", outputTopic, opensearchId);
                } catch (Exception e) {
                    LOG.error("Failed to publish summary to Kafka: {}", e.getMessage());
                }
            }

        } catch (Exception e) {
            LOG.error("Failed processing incoming message: {}", e.getMessage(), e);
            // swallow exception to prevent Kafka retry storms; the consumer already logs and handles errors
        }
    }
    private String serialize(GenAiResultMessage output) {
        try {
            return objectMapper.writeValueAsString(output);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize GenAI result", e);
        }
    }
}
