package com.kibikalo.read_aware.translation.service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class DictionaryService {

    private static final String DICTIONARY_API_URL = "https://api.dictionaryapi.dev/api/v2/entries/en/";

    private final RestTemplate restTemplate;

    // (Optional) In real usage, consider caching repeated lookups
    // e.g. a Map<String, List<Map<String, Object>> cache

    public DictionaryService() {
        this.restTemplate = new RestTemplate();
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> lookupWord(String word) {
        // dictionaryapi.dev endpoint
        String url = DICTIONARY_API_URL + word;

        // dictionaryapi.dev returns an array of objects for valid words,
        // or an error object for invalid ones.
        ResponseEntity<Object> response = restTemplate.getForEntity(url, Object.class);

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new RuntimeException("Failed to fetch definition for: " + word);
        }

        // The response could be an array of dictionary entries or an error JSON
        Object responseBody = response.getBody();
        if (responseBody instanceof List) {
            // Return as a list of maps (the typical successful response)
            return (List<Map<String, Object>>) responseBody;
        } else if (responseBody instanceof Map) {
            // Possibly an error from dictionaryapi.dev, like:
            // { "title": "No Definitions Found", "message": ..., "resolution": ... }
            Map<String, Object> errorMap = (Map<String, Object>) responseBody;
            throw new RuntimeException("Dictionary API Error: " + errorMap.get("message"));
        } else {
            throw new RuntimeException("Unknown response format from dictionary API");
        }
    }
}

