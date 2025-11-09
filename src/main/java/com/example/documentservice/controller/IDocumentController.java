package com.example.documentservice.controller;

import com.example.documentservice.dto.DocumentRequest;
import com.example.documentservice.entity.Document;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/documents")
public interface IDocumentController {
    @GetMapping("/{id}")
    Document getDocumentById(@PathVariable Integer id);

    @GetMapping
    ResponseEntity<List<Document>> getAllDocuments();

    @PutMapping("/{id}")
    ResponseEntity<Document> updateDocumentById(@PathVariable Integer id,
                                                @RequestBody Document updatedDocument);
    @DeleteMapping("/{id}")
    ResponseEntity<Void> deleteDocumentById(@PathVariable Integer id);

    @PostMapping("/send")
    String sendForOcr(@RequestBody DocumentRequest request);
}
