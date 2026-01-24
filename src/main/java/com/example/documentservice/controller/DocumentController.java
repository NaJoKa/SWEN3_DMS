package com.example.documentservice.controller;

import com.example.documentservice.dto.DocumentRequest;
import com.example.documentservice.dto.OcrTopicMessageDto;
import com.example.documentservice.entity.Document;
import com.example.documentservice.exception.KafkaSendException;
import com.example.documentservice.repository.DocumentRepository;
import com.example.documentservice.service.MinioStorageService;
import com.example.documentservice.service.OcrMessageProducer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;

@RestController
public class DocumentController implements IDocumentController {
    @Inject
    @Named("documentRepository")
    private DocumentRepository documentRepository;
    private final OcrMessageProducer ocrProducer;
    private final MinioStorageService minioStorageService;
    private final ObjectMapper objectMapper;

    private static final Logger logger = LoggerFactory.getLogger(DocumentController.class);

    public DocumentController(OcrMessageProducer ocrProducer, MinioStorageService minioStorageService, ObjectMapper objectMapper) {
        this.ocrProducer = ocrProducer;
        this.minioStorageService = minioStorageService;
        this.objectMapper = objectMapper;
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
    public ResponseEntity<Document> uploadDocument(@RequestParam("file") MultipartFile file){
        if (file == null || file.isEmpty()) throw badRequest("File is required");
        System.out.println(file.getName());

        Document doc = new Document();

        //d.setName(meta != null && meta.name() != null ? meta.name() : file.getOriginalFilename());
        doc.setTitle(file.getOriginalFilename());

        //send to MinIO ToDo
        String objectKey = this.minioStorageService.upload(file);
        doc.setObjectKey(objectKey);

        doc = documentRepository.save(doc);

        try {
            //Send message to Kafka
            ocrProducer.sendDocumentForOcr(doc);

            logger.info("Document '{}' sent for OCR processing", objectKey);
            //return "Document uploaded successfully and sent for OCR: " + documentId;

        } catch (KafkaSendException e) {
            logger.error("Failed to send document to Kafka: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Failed to send document to MinIO: {}", e.getMessage());
            throw new RuntimeException(e);
        }

        return ResponseEntity.ok(doc);
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
        //ocrProducer.sendDocumentForOcr(request.getContent());
        return "Document sent for OCR: " + request.getDocumentId();
    }

    @Override
    public String ping() {
        return "pong from document-service";
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}
