package com.example.documentservice.service;

import com.example.documentservice.dto.opensearch.PDFDocument;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;

@Service
public class OpenSearchIndexService {

    private final OpenSearchClient client;
    private final String indexName;

    public OpenSearchIndexService(OpenSearchClient client, @Value("${opensearch.index:documents}") String indexName) {
        this.client = client;
        this.indexName = indexName;
    }

    public SearchResponse<PDFDocument> search(String query, Set<String> allowedIds, Pageable pageable) throws IOException {
        // Build a simple query: multi-match on fields and ids filter
        Query multi = Query.of(q -> q.multiMatch(m -> m.query(query).fields("fileName^2", "summary^1.5", "textContent")));
        List<String> idsList = new ArrayList<>(allowedIds);
        Query ids = Query.of(q -> q.ids(i -> i.values(idsList)));
        Query root = Query.of(q -> q.bool(b -> b.must(multi).filter(ids)));

        SearchRequest sr = SearchRequest.of(s -> s.index(indexName).query(root).from((int) pageable.getOffset()).size(pageable.getPageSize()));
        return client.search(sr, PDFDocument.class);
    }
}
