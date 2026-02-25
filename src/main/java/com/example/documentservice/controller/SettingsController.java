package com.example.documentservice.controller;

import com.example.documentservice.service.GeminiSettingsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/settings")
public class SettingsController {

    private final GeminiSettingsService geminiSettingsService;

    public SettingsController(GeminiSettingsService geminiSettingsService) {
        this.geminiSettingsService = geminiSettingsService;
    }

    @GetMapping("/models")
    public ResponseEntity<List<String>> models() {
        List<String> models = geminiSettingsService.getAvailableModels();
        return ResponseEntity.ok(models);
    }

    @GetMapping("/model")
    public ResponseEntity<Map<String, String>> currentModel() {
        return ResponseEntity.ok(Map.of("model", geminiSettingsService.getCurrentModel().orElse("")));
    }

    @PostMapping("/model")
    public ResponseEntity<?> setModel(@RequestBody Map<String, String> payload) {
        String model = payload.get("model");
        try {
            geminiSettingsService.setCurrentModel(model);
            return ResponseEntity.ok(Map.of("model", model == null ? "" : model));
        } catch (IOException e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to persist model"));
        }
    }
}
