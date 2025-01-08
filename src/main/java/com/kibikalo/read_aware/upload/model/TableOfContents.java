package com.kibikalo.read_aware.upload.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Table(name = "table_of_contents")
public class TableOfContents {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "book_id", nullable = false)
    private BookMetadata book;

    @OneToMany(mappedBy = "toc", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Chapter> chapters;

    public Long getId() {
        return id;
    }

    public BookMetadata getBook() {
        return book;
    }

    public List<Chapter> getChapters() {
        return chapters;
    }

    public void setBook(BookMetadata book) {
        this.book = book;
    }

    public void setChapters(List<Chapter> chapters) {
        this.chapters = chapters;
    }
}

