package com.kibikalo.read_aware.translation.dto;

public class ContextRequest {
    private String selectedText;
    private String context;

    public String getSelectedText() {
        return selectedText;
    }

    public void setSelectedText(String selectedText) {
        this.selectedText = selectedText;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }
}
