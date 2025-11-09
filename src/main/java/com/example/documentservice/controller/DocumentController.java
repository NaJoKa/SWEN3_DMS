package com.example.documentservice.controller;

import com.example.documentservice.dto.DocumentRequest;
import com.example.documentservice.entity.Document;
import com.example.documentservice.repository.DocumentRepository;
import com.example.documentservice.service.MinioStorageService;
import com.example.documentservice.service.OcrMessageProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;

import java.util.List;

@RestController
public class DocumentController {

    private static final Logger logger = LoggerFactory.getLogger(DocumentController.class);

    private final DocumentRepository documentRepository;
    private final OcrMessageProducer ocrMessageProducer;
    private final MinioStorageService minioStorageService;

    public DocumentController(DocumentRepository documentRepository,
                              OcrMessageProducer ocrMessageProducer,
                              MinioStorageService minioStorageService) {
        this.documentRepository = documentRepository;
        this.ocrMessageProducer = ocrMessageProducer;
        this.minioStorageService = minioStorageService;
    }

    // --- Ping zum Testen ---
    @GetMapping("/documents/ping")
    public String ping() {
        return "pong from document-service";
    }

    // --- CRUD GET ---

    // Tests erwarten: Rückgabe = Document
    @GetMapping("/documents/{id}")
    public Document getDocumentById(@PathVariable Integer id) {
        return this.documentRepository.findById(id).orElse(null);
    }

    @GetMapping("/documents")
    public ResponseEntity<List<Document>> getAllDocuments() {
        List<Document> documents = this.documentRepository.findAll();
        return ResponseEntity.ok(documents);
    }

    // --- JSON-Upload (für Tests) ---

    // Tests erwarten: uploadDocument(Document) -> ResponseEntity<Document>
    @PostMapping(path = "/documents", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Document> uploadDocument(@RequestBody Document document) {
        Document saved = this.documentRepository.save(document);
        return ResponseEntity.ok(saved);
    }

    // --- Multipart-Upload (Datei + Titel) ---

    @PostMapping(
            path = "/documents/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<String> uploadDocumentMultipart(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "title", required = false) String title
    ) {


        String info = "got file=" + file.getOriginalFilename()
                + ", title=" + (title != null ? title : "<null>");

        return ResponseEntity.ok(info);
    }

    // --- Update / Delete ---

    @PutMapping("/documents/{id}")
    public ResponseEntity<Document> updateDocumentById(
            @PathVariable Integer id,
            @RequestBody Document updatedDocument
    ) {
        return this.documentRepository.findById(id)
                .map(existingDocument -> {
                    existingDocument.setSummary(updatedDocument.getSummary());
                    existingDocument.setDocumentType(updatedDocument.getDocumentType());
                    existingDocument.setTitle(updatedDocument.getTitle());
                    existingDocument.setOcrText(updatedDocument.getOcrText());
                    existingDocument.setCreated(updatedDocument.getCreated());
                    existingDocument.setCorrespondent(updatedDocument.getCorrespondent());
                    existingDocument.setStoragePath(updatedDocument.getStoragePath());
                    existingDocument.setArchiveSerialNumber(updatedDocument.getArchiveSerialNumber());

                    Document savedDocument = this.documentRepository.save(existingDocument);
                    return ResponseEntity.ok(savedDocument);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/documents/{id}")
    public ResponseEntity<Void> deleteDocumentById(@PathVariable Integer id) {
        if (documentRepository.existsById(id)) {
            documentRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // --- OCR-Endpoint ---

    @PostMapping("/documents/send-for-ocr")
    public String sendForOcr(@RequestBody DocumentRequest request) {
        logger.info("sendForOcr request: {}", request);

        int id;
        try {
            id = Integer.parseInt(request.getDocumentId());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(
                    "documentId muss eine Zahl sein, erhalten: " + request.getDocumentId(),
                    ex
            );
        }

        Document doc = documentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Document not found: " + id));

        ocrMessageProducer.sendOcrMessage(doc);

        return "Document sent for OCR: " + id;
    }
}
