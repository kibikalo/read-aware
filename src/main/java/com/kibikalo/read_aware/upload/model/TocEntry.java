package com.kibikalo.read_aware.upload.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "toc_entries")
@Data
class TocEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String fileReference; // Path to the JSON file containing the chapter content

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "toc_id")
    private TableOfContents tableOfContents;
}
