package com.kibikalo.read_aware.translation.controller;

import com.kibikalo.read_aware.translation.service.DeepLTranslationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/translation")
public class TranslationController {

    private final DeepLTranslationService translationService;

    public TranslationController(DeepLTranslationService translationService) {
        this.translationService = translationService;
    }

    /**
     * POST /api/translation/advanced
     * JSON: { "word": "Hello" }
     */
    @PostMapping()
    public ResponseEntity<?> advancedTranslation(@RequestBody Map<String, String> request) {
        String text = request.get("word");
        if (text == null || text.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Missing 'word' parameter");
        }

        try {
            String translated = translationService.translateToUkrainian(text);
            // Return a simple JSON object
            return ResponseEntity.ok(Map.of("original", text, "translated", translated));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Translation error: " + e.getMessage());
        }
    }
}
