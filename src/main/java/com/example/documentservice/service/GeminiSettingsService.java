package com.example.documentservice.service;

import com.google.genai.Client;
import com.google.genai.Pager;
import com.google.genai.types.ListModelsConfig;
import com.google.genai.types.Model;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class GeminiSettingsService {

    private static final Logger LOG = LoggerFactory.getLogger(GeminiSettingsService.class);

    private volatile String currentModel;
    private final Path envFilePath;

    public GeminiSettingsService(@Value("${project.env.file:.env}") String envFile) {
        this.envFilePath = Paths.get(envFile);
        // initialize from environment variable first
        String envVal = System.getenv("GOOGLE_GEMINI_MODEL");
        if (envVal != null && !envVal.isBlank()) {
            this.currentModel = envVal;
        } else {
            // try to read from .env file
            this.currentModel = readModelFromEnvFile().orElse(null);
        }
    }

    /**
     * Return a list of available models. Try typed GenAI client first, then fall back to reflection and defaults.
     */
    public List<String> getAvailableModels() {
        List<String> textModels = new ArrayList<>();
        // 1) Try typed Client call (preferred)
        try {
            LOG.debug("Attempting to list models using typed com.google.genai.Client");
            // use the SDK client directly
            Client client = new Client();


            // Holt die Liste aller Modelle
            ListModelsConfig config = ListModelsConfig.builder().pageSize(100).build();
            Pager<Model> modelPager = client.models.list(config);
            java.util.Iterator<Model> modelIterator = modelPager.iterator();
            while (modelIterator.hasNext()) {
                Model model = modelIterator.next();

                //filtern auf "gemini", da diese Modelle Text können.
                String modelId = model.name().orElse("");
                String displayName = model.displayName().orElse("");

                if (modelId.startsWith("models/gemini") || modelId.startsWith("models/gemma")) {
                    // Wir schließen aus, was wir sicher nicht für Text-Gen brauchen
                    if (!modelId.contains("embedding") &&
                            !modelId.contains("image") &&
                            !modelId.contains("video") &&
                            !modelId.contains("audio") &&
                            !modelId.contains("tts")) {

                        textModels.add(model.name().get().replaceFirst("^models/", ""));
                    }
                }
            }

            System.out.println("Verfügbare Gemini Modelle:");
            for (String model : textModels) {
                LOG.debug("- Name: %s \n",
                    model);
            }
            if (!textModels.isEmpty()) {
                return textModels;
            }
        } catch (Exception e) {
            LOG.error("Failed to list models using typed Client: %s. Falling back to reflection and defaults. Error: %s",
                    e.getMessage(), e);
        }


        // fallback defaults
        List<String> defaults = new ArrayList<>();
        defaults.add("gemini-2.5-flash");
        defaults.add("gemini-1.0");
        defaults.add("gemini-1.5-pro");
        defaults.add("chat-bison");
        return defaults;
    }

    private String extractModelName(Object item) {
        if (item == null) return null;
        try {
            // try common method names
            for (String m : new String[]{"getName", "getId", "name", "id", "getModelId", "getModelName"}) {
                try {
                    java.lang.reflect.Method method = item.getClass().getMethod(m);
                    Object val = method.invoke(item);
                    if (val != null) return val.toString();
                } catch (NoSuchMethodException ignored) {
                }
            }
            // fallback to toString
            return item.toString();
        } catch (Exception e) {
            return null;
        }
    }

    public Optional<String> getCurrentModel() {
        return Optional.ofNullable(this.currentModel);
    }

    public synchronized void setCurrentModel(String model) throws IOException {
        if (model == null || model.isBlank()) {
            this.currentModel = null;
        } else {
            this.currentModel = model.trim();
        }
        // try to persist to .env if available
        persistToEnvFile(this.currentModel);
    }

    private Optional<String> readModelFromEnvFile() {
        try {
            if (!Files.exists(envFilePath)) return Optional.empty();
            List<String> lines = Files.readAllLines(envFilePath, StandardCharsets.UTF_8);
            for (String l : lines) {
                String line = l.trim();
                if (line.startsWith("#")) continue;
                if (line.startsWith("GOOGLE_GEMINI_MODEL")) {
                    int idx = line.indexOf('=');
                    if (idx >= 0 && idx < line.length() - 1) {
                        String val = line.substring(idx + 1).trim();
                        if (!val.isBlank()) return Optional.of(val);
                    }
                }
            }
        } catch (Exception e) {
            // ignore, return empty
        }
        return Optional.empty();
    }

    private void persistToEnvFile(String value) throws IOException {
        try {
            if (!Files.exists(envFilePath)) {
                // create new file with the value
                String content = "GOOGLE_GEMINI_MODEL=" + (value == null ? "" : value) + System.lineSeparator();
                Files.write(envFilePath, content.getBytes(StandardCharsets.UTF_8));
                return;
            }

            List<String> lines = Files.readAllLines(envFilePath, StandardCharsets.UTF_8);
            boolean found = false;
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                if (line.trim().startsWith("GOOGLE_GEMINI_MODEL")) {
                    lines.set(i, "GOOGLE_GEMINI_MODEL=" + (value == null ? "" : value));
                    found = true;
                    break;
                }
            }
            if (!found) {
                lines.add("GOOGLE_GEMINI_MODEL=" + (value == null ? "" : value));
            }
            Files.write(envFilePath, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            // bubble up
            throw e;
        }
    }
}
