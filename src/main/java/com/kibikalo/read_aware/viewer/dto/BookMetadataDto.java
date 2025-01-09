package com.kibikalo.read_aware.viewer.dto;

import java.time.LocalDateTime;

public class BookMetadataDto {
    private Long id;
    private String title;
    private String author;
    private LocalDateTime uploadDate;
    private String language;
    private String description;
    private String genre;
    private TableOfContentsDto tableOfContents;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public LocalDateTime getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(LocalDateTime uploadDate) {
        this.uploadDate = uploadDate;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public TableOfContentsDto getTableOfContents() {
        return tableOfContents;
    }

    public void setTableOfContents(TableOfContentsDto tableOfContents) {
        this.tableOfContents = tableOfContents;
    }
}
