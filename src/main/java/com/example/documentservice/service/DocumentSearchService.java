package com.example.documentservice.service;

import com.example.documentservice.dto.DocumentDto;
import com.example.documentservice.dto.DocumentSearchResultDto;
import com.example.documentservice.dto.opensearch.PDFDocument;
import com.example.documentservice.entity.Document;
import com.example.documentservice.repository.DocumentRepository;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.example.documentservice.service.mapper.DocumentMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.opensearch.client.opensearch.core.SearchResponse;

@Service
@Transactional(readOnly = true)
public class DocumentSearchService implements IDocumentSearchService {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final OpenSearchIndexService openSearchIndexService; // optional: may be null if not configured
    private final DocumentRepository documentRepository;
    private final DocumentMapper documentMapper;

    public DocumentSearchService(OpenSearchIndexService openSearchIndexService,
                                 DocumentRepository documentRepository,
                                 DocumentMapper documentMapper) {
        this.openSearchIndexService = openSearchIndexService;
        this.documentRepository = documentRepository;
        this.documentMapper = documentMapper;
    }

    @Override
    public Page<DocumentSearchResultDto> search(Long userId, String query, Pageable pageable) {
        if (query == null || query.isBlank()) {
            return Page.empty(pageable);
        }

        if (openSearchIndexService == null) {
            LOG.warn("OpenSearchIndexService not configured - search is disabled. Returning empty page.");
            return Page.empty(pageable);
        }

        List<Document> userDocs = documentRepository.findAllByOwnerIdAndOpensearchIdIsNotNull(userId);
        Map<String, Document> docsByElasticId = userDocs.stream()
                .filter(d -> d.getOpensearchId() != null && !d.getOpensearchId().isBlank())
                .collect(Collectors.toMap(Document::getOpensearchId, Function.identity(), (a, b) -> a));

        if (docsByElasticId.isEmpty()) {
            LOG.debug("User {} has no indexed documents to search", userId);
            return Page.empty(pageable);
        }

        try {
            SearchResponse<PDFDocument> response = openSearchIndexService.search(query, docsByElasticId.keySet(), pageable, true);
            List<DocumentSearchResultDto> results = response.hits().hits().stream()
                    .map(hit -> {
                        // inline mapping to avoid depending on a SearchHit import
                        String hitId = hit.id();
                        Document matched = docsByElasticId.get(hitId);
                        if (matched == null) return null;
                        String snippet = makeSnippet(matched.getOcrText(), 220);

                        DocumentDto dto = documentMapper.toDto(matched);
                        PDFDocument esDoc = hit.source();
                        String summary = esDoc == null ? null : esDoc.summary();
                        Double score = hit.score() == null ? null : hit.score();
                        return new DocumentSearchResultDto(dto.id(), dto.name(), summary, dto.ownerId(), dto.objectKey(), score, snippet);
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            long total = response.hits().total() != null ? response.hits().total().value() : results.size();
            return new PageImpl<>(results, pageable, total);
        } catch (Exception e) {
            LOG.error("Failed to perform search in OpenSearch", e);
            return Page.empty(pageable);
        }
    }


    private String makeSnippet(String text, int maxLen) {
        if (text == null) return "";
        String t = text.replaceAll("\\s+", " ").trim();
        if (t.length() <= maxLen) return t;
        return t.substring(0, maxLen) + "...";
    }
}