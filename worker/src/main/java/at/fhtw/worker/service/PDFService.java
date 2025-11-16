package at.fhtw.worker.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

@Service
public class PDFService {

    public int convertPDFToImages(String pdfFilePath, String outputDirectory) throws IOException {
        File pdfFile = new File(pdfFilePath);
        PDDocument document = PDDocument.load(pdfFile);
        PDFRenderer pdfRenderer = new PDFRenderer(document);
        int pageNum = 0;
        for (; pageNum < document.getNumberOfPages(); pageNum++) {
            BufferedImage image = pdfRenderer.renderImage(pageNum, 2.0f);  // 2.0f scales the image resolution
            File imageFile = new File(outputDirectory + "/page_" + pageNum + ".png");
            ImageIO.write(image, "PNG", imageFile);
        }

        document.close();
        return pageNum;
    }
}
