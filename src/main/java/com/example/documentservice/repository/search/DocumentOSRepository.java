package com.example.documentservice.repository.search;

import com.example.documentservice.dto.opensearch.PDFDocument;
import com.example.documentservice.entity.Document;
import com.example.documentservice.repository.DocumentRepository;
import com.example.documentservice.service.OpenSearchIndexService;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;

@Repository
public class DocumentOSRepository {

    private final OpenSearchIndexService openSearchIndexService;
    private final DocumentRepository documentRepository; // JPA repository for finding Document entities

    @Autowired
    public DocumentOSRepository(OpenSearchIndexService openSearchIndexService, DocumentRepository documentRepository) {
        this.openSearchIndexService = openSearchIndexService;
        this.documentRepository = documentRepository;
    }

    public Optional<Document> findByObjectKey(String objectKey) {
        return documentRepository.findByObjectKey(objectKey);
    }

    public SearchResponse<PDFDocument> search(String query, Set<String> allowedIds, Pageable pageable) throws IOException {
        return openSearchIndexService.search(query, allowedIds, pageable);
    }
}
