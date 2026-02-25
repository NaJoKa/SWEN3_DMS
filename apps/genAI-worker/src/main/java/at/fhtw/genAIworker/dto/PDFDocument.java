package at.fhtw.genAIworker.dto;

import java.time.Instant;

public record PDFDocument(
        String id,
        String fileName,
        String textContent,
        String summary,
        Instant indexedAt
) {

}
