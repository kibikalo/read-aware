package com.kibikalo.read_aware.upload.service;

import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.InputStream;

@Service
public class SentenceDetectionService {

    private SentenceDetectorME sentenceDetector;

    @PostConstruct
    public void init() {
        try {
            // Loading the model from the resources directory
            ClassPathResource resource = new ClassPathResource("opennlp-en-ud-ewt-sentence-1.2-2.5.0.bin");
            try (InputStream modelIn = resource.getInputStream()) {
                SentenceModel model = new SentenceModel(modelIn);
                sentenceDetector = new SentenceDetectorME(model);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load sentence detection model", e);
        }
    }

    public String[] detectSentences(String text) {
        return sentenceDetector.sentDetect(text);
    }
}