package at.fhtw.worker.config;

import java.nio.file.Files;
import java.nio.file.Path;

public class WConfig {
    public final String tessdataPath;

    public WConfig() {
        // Prefer environment variable TESSDATA_PREFIX. Fallback to local repo copy.
        String tessdataPathtmp = System.getenv("TESSDATA_PREFIX");
        if (tessdataPathtmp == null || tessdataPathtmp.isEmpty()) {
            tessdataPathtmp = getDefaultTessdataPath(); // fallback for local dev
        }
        this.tessdataPath = tessdataPathtmp;
    }

    public String getTessdataPath() {
        return tessdataPath;
    }

    private static String getDefaultTessdataPath() {
        String[] candidates = {
                "/usr/share/tesseract-ocr/5/tessdata",
                "/usr/share/tesseract-ocr/4.00/tessdata",
                "/usr/share/tesseract-ocr/tessdata"
        };
        for (String candidate : candidates) {
            try {
                if (Files.isDirectory(Path.of(candidate))) {
                    return candidate;
                }
            } catch (SecurityException ignored) {
                // fall through to next candidate
            }
        }
        return candidates[0];
    }
}
