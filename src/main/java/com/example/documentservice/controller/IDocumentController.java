package com.example.documentservice.controller;

import com.example.documentservice.dto.DocumentRequest;
import com.example.documentservice.entity.Document;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RequestMapping("/documents")
public interface IDocumentController {
    @GetMapping("/{id}")
    Document getDocumentById(@PathVariable Integer id);

    @GetMapping
    ResponseEntity<List<Document>> getAllDocuments();

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<Document> uploadDocument(@RequestParam("file") MultipartFile file);

    @PutMapping("/{id}")
    ResponseEntity<Document> updateDocumentById(@PathVariable Integer id,
                                                @RequestBody Document updatedDocument);
    @DeleteMapping("/{id}")
    ResponseEntity<Void> deleteDocumentById(@PathVariable Integer id);

    //OCR_QUEUE
    @PostMapping("/send")
    String sendForOcr(@RequestBody DocumentRequest request);

    // --- Ping zum Testen ---
    @GetMapping("/ping")
    String ping();
}
