package com.kibikalo.read_aware.translation;

import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;

@Configuration
public class DeeplConfig {

    @Value("${deepl.api.key}")
    private String apiKey;

    @Value("${deepl.api.url:https://api-free.deepl.com/v2/translate}")
    private String apiUrl;

    public String getApiKey() {
        return apiKey;
    }

    public String getApiUrl() {
        return apiUrl;
    }
}

