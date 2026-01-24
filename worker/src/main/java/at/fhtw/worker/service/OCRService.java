package at.fhtw.worker.service;

import at.fhtw.worker.config.WConfig;
import at.fhtw.worker.exception.OcrProcessingException;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.File;

@Service
public class OCRService {

    private static final Logger log = LoggerFactory.getLogger(OCRService.class);
    private WConfig config = new WConfig();

    public String processOCR(String imagePath) {
        Tesseract tesseract = new Tesseract();

        tesseract.setDatapath(config.getTessdataPath());

        try {
            // Process the image with Tesseract
            File imageFile = new File(imagePath);
            return tesseract.doOCR(imageFile);
        } catch (TesseractException e) {
            log.error("Error during OCR processing for image {}: {}", imagePath, e.getMessage(), e);
            return null;
        }
    }

    public String processOCR(BufferedImage image) throws OcrProcessingException {
        Tesseract tesseract = new Tesseract();

        tesseract.setDatapath(config.getTessdataPath());

        try {
            return tesseract.doOCR(image);
        } catch (TesseractException e) {
            log.error("Error during OCR processing (BufferedImage): {}", e.getMessage(), e);
            throw new OcrProcessingException("Tesseract OCR failed", e);
        }
    }
}
