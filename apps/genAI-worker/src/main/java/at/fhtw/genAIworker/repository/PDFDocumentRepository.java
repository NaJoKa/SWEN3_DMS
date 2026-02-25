package at.fhtw.genAIworker.repository;

import at.fhtw.genAIworker.dto.PDFDocument;

public interface PDFDocumentRepository {
    String save(PDFDocument document);
    boolean existsById(String id);
}
