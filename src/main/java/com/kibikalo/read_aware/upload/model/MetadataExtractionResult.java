package com.kibikalo.read_aware.upload.model;

public class MetadataExtractionResult {
    private BookMetadata metadata;
    private byte[] coverImageBytes;      // could be null if no cover
    private String coverMediaType;       // e.g. "image/jpeg" or "image/png"

    public BookMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(BookMetadata metadata) {
        this.metadata = metadata;
    }

    public byte[] getCoverImageBytes() {
        return coverImageBytes;
    }

    public void setCoverImageBytes(byte[] coverImageBytes) {
        this.coverImageBytes = coverImageBytes;
    }

    public String getCoverMediaType() {
        return coverMediaType;
    }

    public void setCoverMediaType(String coverMediaType) {
        this.coverMediaType = coverMediaType;
    }
}

