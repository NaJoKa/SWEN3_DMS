package at.fhtw.worker.config;

import net.sourceforge.tess4j.util.LoadLibs;
import java.io.File;

public class WConfig {
    public final String tessdataPath;

    public WConfig() {
        // Prefer environment variable TESSDATA_PREFIX. Fallback to local repo copy.
        String tessdataPathtmp = System.getenv("TESSDATA_PREFIX");
        if (tessdataPathtmp == null || tessdataPathtmp.isEmpty()) {
            File tessDataFolder = LoadLibs.extractTessResources("tessdata");
            tessdataPathtmp = tessDataFolder.getAbsolutePath();
        }
        this.tessdataPath = tessdataPathtmp;
    }

    public String getTessdataPath() {
        return tessdataPath;
    }
}
