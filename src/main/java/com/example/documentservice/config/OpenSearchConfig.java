package com.example.documentservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.Getter;
import org.apache.http.HttpHost;
import org.opensearch.client.json.jackson.JacksonJsonpMapper;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.transport.OpenSearchTransport;
import org.opensearch.client.transport.rest_client.RestClientTransport;
import org.opensearch.client.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;

import java.net.URI;
import java.net.URISyntaxException;

@Getter
@Configuration
public class OpenSearchConfig {
    private static final Logger LOG = LoggerFactory.getLogger(OpenSearchConfig.class);

    @Value("${opensearch.url}")
    private String opensearchUrl;

    @Value("${opensearch.index}")
    private String indexName;

    @Bean
    public OpenSearchClient openSearchClient(){
        URI uri;
        try {
            uri = new URI(opensearchUrl);
        } catch (URISyntaxException e) {
            LOG.error("Ungültige OpenSearch URL konfiguriert: {}", opensearchUrl, e);
            throw new RuntimeException("Ungültige OpenSearch URL konfiguriert: " + opensearchUrl, e);
        }
        String host = uri.getHost();
        int port = uri.getPort() == -1 ? (uri.getScheme().equals("https") ? 443 : 80) : uri.getPort();
        String scheme = uri.getScheme();

        ObjectMapper mapper = new ObjectMapper();
        // Registriert das Modul für Instant, LocalDateTime, etc.
        mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        // Schreibt Daten als ISO-String (z.B. 2026-02-24T...) statt als Zahlen-Array
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // 1. Den Low-Level REST-Client erstellen
        RestClient restClient = RestClient.builder(
                new HttpHost(host, port, scheme)
        ).build();

        // 2. Den Transport mit dem KONFIGURIERTEN Mapper erstellen
        OpenSearchTransport transport = new RestClientTransport(
                restClient,
                new JacksonJsonpMapper(mapper)
        );

        return new OpenSearchClient(transport);
    }
}