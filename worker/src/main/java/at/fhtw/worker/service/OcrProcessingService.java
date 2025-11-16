package at.fhtw.worker.service;

import at.fhtw.worker.exception.OcrProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

@Service
public class OcrProcessingService {
    @Autowired
    private MinioService minioService;

    @Autowired
    private PDFService pdfService;

    @Autowired
    private OCRService ocrService;

    private static final Logger log = LoggerFactory.getLogger(OcrProcessingService.class);

    public String process(String documentPath) {
        log.debug("Starting OCR processing for document: {}", documentPath);

        try {
            // Create an ObjectMapper to parse the JSON
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(documentPath);

            // Extract the value of documentId
            String documentId = jsonNode.get("documentId").asText();
            System.out.println(documentId);  // Output: name.pdf

            String pdfDestinationPath = "./tmp/" + documentId;
            String imagesPath = "./tmp/images";

            // Step 1: Download PDF from MinIO
            minioService.downloadFile("documents", documentId, pdfDestinationPath);

            // Step 2: Convert PDF to Images
            int pageNum = pdfService.convertPDFToImages(pdfDestinationPath, imagesPath);

            // Step 3: Process OCR for each image
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < pageNum; i++) {  // Loop through 5 pages as an example
                String imageFilePath = imagesPath + "/page_" + i + ".png";
                String ocrResult = ocrService.processOCR(imageFilePath);
                result.append("Page ").append(i).append(" OCR Result: ").append(ocrResult).append("\n");

                if (new File(imageFilePath).delete()) {
                    System.out.println("Image deleted successfully");
                } else {
                    System.out.println("Failed to delete the image");
                }
            }

            //delete pdf file
            if (new File(pdfDestinationPath).delete()) {
                System.out.println("PDF file deleted successfully");
            } else {
                System.out.println("Failed to delete the PDF file");
            }

            if (documentPath.contains("error")) {
                throw new OcrProcessingException("Simulated OCR failure for input: " + documentPath);
            }

            //String result = "OCR_DONE_" + documentPath.toUpperCase();
            log.debug("Finished OCR processing: {}", result);
            return result.toString();

        } catch (IOException e) {
            return "Error processing PDF and OCR: " + e.getMessage();
        }
    }
}
