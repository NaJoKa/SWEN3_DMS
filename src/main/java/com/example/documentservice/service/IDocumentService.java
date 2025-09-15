package com.example.documentservice.service;

import com.example.documentservice.entity.Document;
//import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IDocumentService {
//services ausprogrammieren
    //nur Metadaten, ohne Inhalt
    Document upload(Document document);

    List<Document> findAll();

    Document findById(String id);

    Document update(String id, Document document);

    void delete(String id);
}
