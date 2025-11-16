package at.fhtw.worker.service;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class OCRService {

    public String processOCR(String imagePath) {
        Tesseract tesseract = new Tesseract();
        // Set the path to the Tesseract executable (if needed, especially on Windows)
        tesseract.setDatapath("./tesseract/tessdata");  // Set the path to tessdata directory

        try {
            // Process the image with Tesseract
            File imageFile = new File(imagePath);
            return tesseract.doOCR(imageFile);
        } catch (TesseractException e) {
            e.printStackTrace();
            return null;
        }
    }
}
