package com.example.documentservice.controller;

import com.example.documentservice.dto.DocumentRequest;
import com.example.documentservice.entity.Document;
import com.example.documentservice.exception.KafkaSendException;
import com.example.documentservice.repository.DocumentRepository;
import com.example.documentservice.service.OcrMessageProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
public class DocumentController implements IDocumentController {
    @Inject
    @Named("documentRepository")
    private DocumentRepository documentRepository;
    private final OcrMessageProducer ocrProducer;

    private static final Logger logger = LoggerFactory.getLogger(DocumentController.class);


    public DocumentController(OcrMessageProducer ocrProducer) {
        this.ocrProducer = ocrProducer;
    }

    @Override
    public Document getDocumentById(@PathVariable Integer id) {
        return this.documentRepository.findById(id).orElse(null);
    }

    @Override
    public ResponseEntity<List<Document>> getAllDocuments() {
        List<Document> documents = this.documentRepository.findAll();
        return ResponseEntity.ok(documents);
    }

    @Override
    public ResponseEntity<Document> uploadDocument(@RequestBody Document document){
        System.out.println(document);
        Document saved = this.documentRepository.save(document);
        return ResponseEntity.ok(saved);
    }

    @Override
    public ResponseEntity<Document> updateDocumentById(@PathVariable Integer id,
                                               @RequestBody Document updatedDocument){
        return this.documentRepository.findById(id).map(existingDocument -> {
                    // Update fields
                    existingDocument.setSummary(updatedDocument.getSummary());
                    existingDocument.setDocumentType(updatedDocument.getDocumentType());
                    existingDocument.setTitle(updatedDocument.getTitle());
                    existingDocument.setOcrText(updatedDocument.getOcrText());
                    existingDocument.setCreated(updatedDocument.getCreated());
                    existingDocument.setCorrespondent(updatedDocument.getCorrespondent());
                    existingDocument.setStoragePath(updatedDocument.getStoragePath());
                    existingDocument.setArchiveSerialNumber(updatedDocument.getArchiveSerialNumber());

                    // Save updated entity
                    Document savedDocument = this.documentRepository.save(existingDocument);
                    return ResponseEntity.ok(savedDocument);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Override
    public ResponseEntity<Void> deleteDocumentById(@PathVariable Integer id) {
        if (documentRepository.existsById(id)) {
            documentRepository.deleteById(id);
            return ResponseEntity.noContent().build(); // 204 No Content
        } else {
            return ResponseEntity.notFound().build(); // 404 Not Found
        }
    }

    @Override
    public String sendForOcr(@RequestBody DocumentRequest request) {
        logger.info(request.toString());
        // send message to Kafka
        ocrProducer.sendDocumentForOcr(request.getDocumentId());
        return "Document sent for OCR: " + request.getDocumentId();
    }

    @PostMapping("/upload")
    public String uploadDocument(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                throw new IllegalArgumentException("Uploaded file is empty");
            }

            //extracting metadata
            String documentId = "doc-" + System.currentTimeMillis();
            logger.info("Received file '{}', size={} bytes", file.getOriginalFilename(), file.getSize());

            //Send message to Kafka
            ocrProducer.sendDocumentForOcr(documentId);

            logger.info("Document '{}' sent for OCR processing", documentId);
            return "Document uploaded successfully and sent for OCR: " + documentId;

        } catch (KafkaSendException e) {
            logger.error("Failed to send document to Kafka: {}", e.getMessage());
            throw e;
        }
    }
}
