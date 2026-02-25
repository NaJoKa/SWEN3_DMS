package at.fhtw.genAIworker.service;

import at.fhtw.genAIworker.dto.PDFDocument;
import at.fhtw.genAIworker.repository.PDFDocumentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Candidate;
import com.google.genai.types.Content;
import com.google.genai.types.Part;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class GeminiService {

    private static final Logger LOG = LoggerFactory.getLogger(GeminiService.class);
    private final Client genaiClient;
    private final PDFDocumentRepository pdfDocumentRepository;

    // Retry/configuration defaults (can be overridden via application.properties)
    //gemini-3-flash-preview
    @Value("${google.gemini.model:gemini-2.5-flash}")
    private String geminiModel;

    @Value("${google.gemini.max-rps:2}")
    private int maxRps;

    @Value("${google.gemini.max-attempts:4}")
    private int maxAttempts;

    private static final long INITIAL_BACKOFF_MS = 1000L; // 1s
    private final AtomicLong lastRequestNano = new AtomicLong(0);
    private final Random jitter = new Random();

    // PDFDocumentRepository is injected by Spring; do NOT create repository instances manually
    @Autowired
    public GeminiService(PDFDocumentRepository pdfDocumentRepository) {
        // The client gets the API key from the environment variable `GOOGLE_API_KEY`.
        this.genaiClient = new Client();
        this.pdfDocumentRepository = pdfDocumentRepository;
    }

    public String summarize(String ocrText) {
        LOG.info("Sending request to Gemini API for summarization...");

        // Professional System Prompt to ensure clean output
        String systemInstruction = """
        You are a document processing assistant.
        Your task is to summarize the following text extracted via OCR.
        Keep it concise (max 3 sentences) and focus on the main topic.
        If the text is gibberish or empty, return 'No readable content found'.

        TEXT TO SUMMARIZE:
        """;

        String prompt = systemInstruction + (ocrText == null ? "" : ocrText);

        long backoff = INITIAL_BACKOFF_MS;
        for (int attempt = 1; attempt <= Math.max(1, maxAttempts); attempt++) {
            try {
                throttleIfNeeded();

                GenerateContentResponse response = genaiClient.models.generateContent(
                        geminiModel,
                        prompt,
                        null);

                return extractSummary(response);

            } catch (Exception e) {
                String msg = e.getMessage() == null ? "" : e.getMessage();

                // If it's a rate limit response, log and retry with backoff
                boolean isRateLimit = msg.contains("429") || msg.toLowerCase().contains("too many requests") || msg.toLowerCase().contains("quota");

                LOG.warn("Attempt {} failed calling Gemini ({}). isRateLimit={}. Exception: {}", attempt, geminiModel, isRateLimit, msg);

                if (attempt >= Math.max(1, maxAttempts)) {
                    LOG.error("All {} attempts to call Gemini failed. Giving up.", maxAttempts);
                    break;
                }

                // Exponential backoff with jitter on transient errors
                long jitterMs = jitter.nextInt(300); // 0-299ms random jitter
                long sleepMs = backoff + jitterMs;
                if (isRateLimit) {
                    // increase wait for rate limits
                    sleepMs *= 4;
                }

                LOG.info("Sleeping {} ms before next attempt (attempt {}).", sleepMs, attempt);
                try {
                    Thread.sleep(sleepMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    LOG.warn("Backoff sleep interrupted");
                    break;
                }

                backoff = Math.min(backoff * 2, 60_000L); // cap backoff at 60s
            }
        }

        return "Summary unavailable due to technical error.";
    }

    /**
     * Simple local throttle to avoid sending more than maxRps requests per second to Gemini from this JVM.
     */
    private void throttleIfNeeded() {
        if (maxRps <= 0) {
            return;
        }
        long minIntervalNs = 1_000_000_000L / maxRps;
        synchronized (lastRequestNano) {
            long now = System.nanoTime();
            long last = lastRequestNano.get();
            long elapsed = now - last;
            if (last != 0 && elapsed < minIntervalNs) {
                long sleepMs = (minIntervalNs - elapsed) / 1_000_000L;
                if (sleepMs > 0) {
                    try {
                        LOG.debug("Throttling Gemini calls, sleeping {} ms to respect maxRps={}", sleepMs, maxRps);
                        Thread.sleep(sleepMs);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
            lastRequestNano.set(System.nanoTime());
        }
    }

    private String extractSummary(GenerateContentResponse response) {
        if (response == null) {
            LOG.warn("Gemini response was null");
            return "No readable content found";
        }

        // candidates() returns Optional<List<Candidate>> according to the SDK
        List<Candidate> candidates = response.candidates().orElse(Collections.emptyList());
        if (candidates.isEmpty()) {
            LOG.warn("No candidates returned from Gemini.");
            return "No readable content found";
        }

        Candidate first = candidates.get(0);
        if (first == null) {
            LOG.warn("First candidate is null");
            return "No readable content found";
        }

        // finishReason() may return Optional<FinishReason> - convert to string safely
        Optional<?> finishReasonOpt = Optional.empty();
        try {
            finishReasonOpt = first.finishReason();
        } catch (Throwable ignored) {
        }
        boolean truncated = finishReasonOpt.map(Object::toString)
                .map("MAX_TOKENS"::equalsIgnoreCase)
                .orElse(false);

        // content() returns Optional<Content>
        Optional<Content> contentOpt = Optional.empty();
        try {
            contentOpt = first.content();
        } catch (Throwable ignored) {
        }

        if (contentOpt.isEmpty()) {
            LOG.warn("Candidate content is empty");
            return truncated ? "Summary truncated; Gemini returned no text (MAX_TOKENS)" : "No readable content found";
        }

        // parts() returns Optional<List<Part>>
        Optional<List<Part>> partsOpt = Optional.empty();
        try {
            partsOpt = contentOpt.flatMap(Content::parts);
        } catch (Throwable ignored) {
        }

        List<Part> parts = partsOpt.orElse(Collections.emptyList());
        if (parts.isEmpty()) {
            LOG.warn("Candidate content parts missing or empty");
            return truncated ? "Summary truncated; Gemini returned no text (MAX_TOKENS)" : "No readable content found";
        }

        // part.text() may return Optional<String>
        Optional<String> textOpt = Optional.empty();
        try {
            textOpt = parts.stream().findFirst().flatMap(Part::text);
        } catch (Throwable ignored) {
        }

        String text = textOpt.filter(StringUtils::hasText).orElse(null);
        if (!StringUtils.hasText(text)) {
            LOG.warn("Extracted text is empty");
            return truncated ? "Summary truncated; Gemini returned no text (MAX_TOKENS)" : "No readable content found";
        }

        String trimmed = text.trim();
        if (truncated) {
            LOG.info("Using truncated Gemini summary");
        }
        return trimmed;
    }

    public void indexDocument(String documentId, String fileName, String textContent, String summary) {
        // Use provided documentId (for dedupe / idempotency)
        //if (documentId == null || documentId.isBlank()) {
        //    documentId = UUID.randomUUID().toString();
        //}
        PDFDocument document = new PDFDocument(documentId, fileName, textContent, summary, Instant.now());
        try {
            // dedupe: if repository already has id skip
            if (pdfDocumentRepository.existsById(documentId)) {
                LOG.info("Document {} already exists in repository, skipping index", documentId);
                return;
            }

            pdfDocumentRepository.save(document);
            LOG.info("Indexed document {} into Opensearch ({} chars)", documentId, textContent == null ? 0 : textContent.length());
        } catch (Exception e) {
            // DON'T rethrow: if indexing fails we should not cause the Kafka listener to re-process and re-call Gemini
            LOG.error("Failed to index document {} into OpenSearch: {}", documentId, e.getMessage(), e);
        }
        return;
    }
}
