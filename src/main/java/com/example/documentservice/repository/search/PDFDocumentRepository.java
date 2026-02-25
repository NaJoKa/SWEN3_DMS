package com.example.documentservice.repository.search;

import com.example.documentservice.dto.opensearch.PDFDocument;
import com.example.documentservice.service.OpenSearchIndexService;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.Set;

@Repository
public class PDFDocumentRepository {

    private final OpenSearchIndexService openSearchIndexService;

    @Autowired
    public PDFDocumentRepository(OpenSearchIndexService openSearchIndexService) {
        this.openSearchIndexService = openSearchIndexService;
    }

    public SearchResponse<PDFDocument> search(String query, Set<String> allowedIds, Pageable pageable, boolean fuzzy) throws IOException {
        return openSearchIndexService.search(query, allowedIds, pageable, fuzzy);
    }
}
