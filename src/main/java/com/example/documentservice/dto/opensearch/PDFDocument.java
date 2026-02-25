package com.example.documentservice.dto.opensearch;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;


@JsonInclude(JsonInclude.Include.NON_NULL)
public record PDFDocument(
        @JsonProperty("id") String id,
        @JsonProperty("fileName") String fileName,
        @JsonProperty("textContent") String textContent,
        @JsonProperty("summary") String summary,
        @JsonProperty("indexedAt") Instant indexedAt
) {
}
