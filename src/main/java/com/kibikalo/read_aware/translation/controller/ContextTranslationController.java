package com.kibikalo.read_aware.translation.controller;

import com.kibikalo.read_aware.translation.dto.ContextRequest;
import com.kibikalo.read_aware.translation.dto.ContextResponse;
import com.kibikalo.read_aware.translation.service.DeepLTranslationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/translation")
public class ContextTranslationController {

    private final DeepLTranslationService translationService;

    public ContextTranslationController(DeepLTranslationService translationService) {
        this.translationService = translationService;
    }

    @PostMapping("/context")
    public ResponseEntity<ContextResponse> contextTranslate(@RequestBody ContextRequest req) {
        if (req.getContext() == null || req.getContext().isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        try {
            // Reuse your existing "translate" method to get a full-sentence translation
            String translatedSentence = translationService.translateToUkrainian(req.getContext());

            ContextResponse resp = new ContextResponse();
            resp.setOriginalContext(req.getContext());
            resp.setTranslatedContext(translatedSentence);
            resp.setSelectedText(req.getSelectedText());

            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
        }
    }
}