package com.kibikalo.read_aware.translation.service;

import com.kibikalo.read_aware.translation.DeeplConfig;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class DeepLTranslationService {

    private final RestTemplate restTemplate;
    private final DeeplConfig deeplConfig;

    public DeepLTranslationService(DeeplConfig deeplConfig) {
        this.restTemplate = new RestTemplate();
        this.deeplConfig = deeplConfig;
    }

    /**
     * Translates the given text from English to Ukrainian using DeepL.
     */
    public String translateToUkrainian(String text) {
        // Build the request
        String apiUrl = deeplConfig.getApiUrl();
        String apiKey = deeplConfig.getApiKey();

        // According to DeepL docs:
        // POST https://api-free.deepl.com/v2/translate
        // Form-data or x-www-form-urlencoded with:
        //    auth_key=YOUR_API_KEY
        //    text=Your text
        //    source_lang=EN
        //    target_lang=UK

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("auth_key", apiKey);
        body.add("text", text);
        body.add("source_lang", "EN");
        body.add("target_lang", "UK");

        HttpEntity<MultiValueMap<String, String>> requestEntity =
                new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                apiUrl, requestEntity, Map.class);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            // The response structure is something like:
            // {
            //   "translations": [
            //     {
            //       "detected_source_language": "EN",
            //       "text": "Добрий день"
            //     }
            //   ]
            // }
            Map<?, ?> responseBody = response.getBody();
            Object translationsObj = responseBody.get("translations");
            if (translationsObj instanceof List) {
                List<?> translationsList = (List<?>) translationsObj;
                if (!translationsList.isEmpty()) {
                    Object first = translationsList.get(0);
                    if (first instanceof Map) {
                        Object textObj = ((Map<?, ?>) first).get("text");
                        if (textObj instanceof String) {
                            return (String) textObj;
                        }
                    }
                }
            }
        }

        throw new RuntimeException("DeepL translation failed or returned unexpected result.");
    }
}

