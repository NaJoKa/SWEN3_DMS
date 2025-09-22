package com.example.documentservice.controller;

import com.example.documentservice.entity.Document;
import com.example.documentservice.repository.DocumentRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.util.List;

@RestController
public class DocumentController implements IDocumentController {
    @Inject
    @Named("documentRepository")
    private DocumentRepository documentRepository;

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
                    // Add other fields as necessary

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

}
