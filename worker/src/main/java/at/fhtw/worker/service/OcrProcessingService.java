package at.fhtw.worker.service;

import at.fhtw.worker.exception.OcrProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class OcrProcessingService {

    private static final Logger log = LoggerFactory.getLogger(OcrProcessingService.class);

    public String process(String documentPath) {
        log.debug("Starting OCR processing for document: {}", documentPath);

        try {
            Thread.sleep(500);

            if (documentPath.contains("error")) {
                throw new OcrProcessingException("Simulated OCR failure for input: " + documentPath);
            }

            String result = "OCR_DONE_" + documentPath.toUpperCase();
            log.debug("Finished OCR processing: {}", result);
            return result;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new OcrProcessingException("Processing interrupted", e);
        }
    }
}
