package com.kibikalo.read_aware.translation.dto;

public class ContextResponse {
    private String originalContext;
    private String translatedContext;
    private String selectedText;

    public String getOriginalContext() {
        return originalContext;
    }

    public void setOriginalContext(String originalContext) {
        this.originalContext = originalContext;
    }

    public String getTranslatedContext() {
        return translatedContext;
    }

    public void setTranslatedContext(String translatedContext) {
        this.translatedContext = translatedContext;
    }

    public String getSelectedText() {
        return selectedText;
    }

    public void setSelectedText(String selectedText) {
        this.selectedText = selectedText;
    }
}
