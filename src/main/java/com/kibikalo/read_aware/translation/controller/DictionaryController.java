package com.kibikalo.read_aware.translation.controller;

import com.kibikalo.read_aware.translation.service.DictionaryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dictionary")
public class DictionaryController {

    private final DictionaryService dictionaryService;

    public DictionaryController(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    /**
     * POST /api/dictionary/lookup
     * Expects JSON: { "word": "wind" }
     * Returns the dictionaryapi.dev response as JSON
     */
    @PostMapping("/lookup")
    public ResponseEntity<?> lookupWord(@RequestBody Map<String, String> request) {
        String word = request.get("word");
        if (word == null || word.isBlank()) {
            return ResponseEntity.badRequest().body("Missing 'word' parameter.");
        }

        try {
            // Call service
            List<Map<String, Object>> lookupResult = dictionaryService.lookupWord(word);
            return ResponseEntity.ok(lookupResult); // Return JSON
        } catch (Exception e) {
            // If dictionaryapi.dev returns 404 or something, we handle it
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }
}