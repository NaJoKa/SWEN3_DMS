package at.fhtw.worker.service;

import at.fhtw.worker.dto.OcrTopicMessageDto;
import at.fhtw.worker.dto.ResultTopicMessageDto;
import at.fhtw.worker.exception.OcrProcessingException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

@Service
public class OcrProcessingService {

    private static final Logger log = LoggerFactory.getLogger(OcrProcessingService.class);

    private static final long MAX_PDF_BYTES = 25L * 1024L * 1024L; // 25 MB
    private static final float RENDER_DPI = 300f;

    @Autowired
    private MinioService minioService;

    @Autowired
    private OCRService ocrService;

    /**
     * Process an OcrTopicMessageDto, download PDF, perform OCR, and return a ResultTopicMessageDto.
     */
    public ResultTopicMessageDto process(OcrTopicMessageDto messageDto) throws OcrProcessingException {
        log.debug("OcrProcessingService.process() - received message: {}", messageDto);

        try {
            String objectKey = messageDto.getObjectKey();
            String objectBucket = messageDto.getBucket();
            if (objectKey == null || objectKey.isBlank()) {
                throw new OcrProcessingException("Missing objectKey");
            }
            if (objectBucket == null || objectBucket.isBlank()) {
                objectBucket = objectKey;
            }

            long size = minioService.getObjectSize(objectKey);
            if (size > MAX_PDF_BYTES) {
                throw new OcrProcessingException("PDF too large: " + size + " bytes (max " + MAX_PDF_BYTES + ")");
            }

            try (InputStream data = minioService.downloadFileStream(objectKey);
                 PDDocument document = PDDocument.load(data)) {

                PDFRenderer renderer = new PDFRenderer(document);

                StringBuilder resultText = new StringBuilder();
                int pages = document.getNumberOfPages();
                for (int page = 0; page < pages; page++) {
                    try {
                        BufferedImage pageImage = renderer.renderImageWithDPI(page, RENDER_DPI, ImageType.RGB);
                        String pageId = objectKey + "#page=" + (page + 1);
                        String pageText = runOcrOnImage(pageImage, pageId);
                        if (pageText != null && !pageText.isBlank()) {
                            if (!resultText.isEmpty()) {
                                resultText.append(System.lineSeparator()).append(System.lineSeparator());
                            }
                            resultText.append(pageText);
                        }
                    } catch (Exception pex) {
                        log.error("Error OCRing page {} of {}: {}", page, objectKey, pex.getMessage(), pex);
                        throw new OcrProcessingException("Error OCRing page " + page + " for " + objectKey, pex);
                    }
                }

                String finalOcrText = resultText.toString().trim();
                if (finalOcrText.isEmpty()) {
                    log.warn("No text extracted from PDF {}", objectKey);
                }

                ResultTopicMessageDto resultDto = new ResultTopicMessageDto();
                resultDto.setObjectKey(objectKey);
                resultDto.setBucket(objectBucket);
                resultDto.setText(finalOcrText);
                log.debug("Finished OCR for {}, extracted text length: {}", objectKey, finalOcrText.length());
                log.debug("Extracted text preview: {}", finalOcrText.length() > 200 ? finalOcrText.substring(0, 200) + "..." : finalOcrText);
                return resultDto;
            }

        } catch (IOException e) {
            throw new OcrProcessingException("Failed to process message/document: " + e.getMessage(), e);
        }
    }

    private String runOcrOnImage(BufferedImage image, String pageIdentifier) throws OcrProcessingException {
        log.debug("Running OCR for {}", pageIdentifier);
        String result = ocrService.processOCR(image);
        return result != null ? result.trim() : "";
    }
}
