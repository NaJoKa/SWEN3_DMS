package at.fhtw.genAIworker.repository;

import at.fhtw.genAIworker.config.OpenSearchConfig;
import at.fhtw.genAIworker.dto.PDFDocument;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.springframework.stereotype.Repository;
import java.io.IOException;

@Repository
public class OpenSearchPDFDocumentRepository implements PDFDocumentRepository {

    private final OpenSearchClient client;
    private final String indexName;

    // Spring injects OpenSearchConfig (which provides the indexName)
    public OpenSearchPDFDocumentRepository(
            OpenSearchClient client,
            OpenSearchConfig openSearchConfig) {
        this.client = client;
        this.indexName = openSearchConfig.getIndexName();
    }

    @Override
    public String save(PDFDocument document) {
        try {
            client.index(i -> i
                    .index(indexName)
                    .id(document.id())
                    .document(document)
            );
            return document.id();
        } catch (IOException e) {
            throw new RuntimeException(
                    String.format("Failed to index document with ID [%s] in OpenSearch index [%s]",
                            document.id(), indexName), e);
        }
    }

    @Override
    public boolean existsById(String id) {
        try {
            var resp = client.exists(e -> e.index(indexName).id(id));
            // resp may be a BooleanResponse-like wrapper with value() method
            try {
                // attempt to call value() reflectively if necessary
                return resp.value();
            } catch (Throwable t) {
                // Fallback: if API differs, try to interpret as boolean via toString
                return Boolean.parseBoolean(String.valueOf(resp));
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to check existence for id " + id + " in index " + indexName, e);
        }
    }
}
