package com.kibikalo.read_aware.upload.model;

import jakarta.persistence.*;

@Entity
@Table(name = "chapters")
public class Chapter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "book_id", nullable = false)
    private BookMetadata book;

    @ManyToOne
    @JoinColumn(name = "toc_id")
    private TableOfContents toc;

    private String title;
    private String filePath; // Path to the chapter's JSON file

    public Chapter() {}

    public Long getId() {
        return id;
    }

    public BookMetadata getBook() {
        return book;
    }

    public void setBook(BookMetadata book) {
        this.book = book;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public TableOfContents getToc() {
        return toc;
    }

    public void setToc(TableOfContents toc) {
        this.toc = toc;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}

