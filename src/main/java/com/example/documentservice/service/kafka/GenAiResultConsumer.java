package com.example.documentservice.service.kafka;

import com.example.documentservice.dto.GenAiResultMessageDto;
import com.example.documentservice.repository.DocumentRepository;
import com.example.documentservice.repository.search.DocumentOSRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class GenAiResultConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(GenAiResultConsumer.class);

    private final ObjectMapper objectMapper;
    private final DocumentRepository documentRepository;

    public GenAiResultConsumer(ObjectMapper objectMapper, DocumentRepository documentRepository) {
        this.objectMapper = objectMapper;
        this.documentRepository = documentRepository;
    }

    @KafkaListener(topics = "${kafka.topic.result}", groupId = "document-service-group")
    public void listen(String message) {
        LOG.info("Message received from topic 'result': {}", message);
        try {
            GenAiResultMessageDto dto = objectMapper.readValue(message, GenAiResultMessageDto.class);
            if (dto == null || dto.objectKey() == null || dto.elasticId().isBlank()
                    || dto.objectKey() == null || dto.objectKey().isBlank()) {
                LOG.warn("Skipping message missing objectKey or elasticId.");
                return;
            }

            // Find the document by objectKey in the JPA repository
            documentRepository.findByObjectKey(dto.objectKey()).ifPresentOrElse(doc -> {
                // Set OpenSearch id on the JPA Document entity
                doc.setOpensearchId(dto.elasticId());
                doc.setTitle(dto.fileName());
                doc.setOcrText(dto.processedMessage());
                doc.setSummary(dto.summary());

                documentRepository.save(doc);
                LOG.info("OpenSearch content ID stored for document with objectKey={}.", dto.objectKey());
            }, () -> LOG.warn("No document found for objectKey={}, cannot store OpenSearch content ID.", dto.objectKey()));
        } catch (Exception e) {
            LOG.error("Failed to process message from topic 'result': {}", message, e);
        }
    }
}
