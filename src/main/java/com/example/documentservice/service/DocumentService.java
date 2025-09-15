package com.example.documentservice.service;

import com.example.documentservice.entity.Document;
import org.springframework.stereotype.Service;

import java.util.List;

//insomnia testen
@Service
public class DocumentService implements IDocumentService {
    @Override
    public Document upload(Document document) {
        System.out.println(document.toString());
        return document;
    }

    @Override
    public List<Document> findAll() {
        return List.of();
    }

    @Override
    public Document findById(String id) {
        return null;
    }

    @Override
    public Document update(String id, Document document) {
        return null;
    }

    @Override
    public void delete(String id) {

    }
}
