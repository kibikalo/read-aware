package com.kibikalo.read_aware.upload.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Table(name = "table_of_contents")
@Data
public class TableOfContents {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "tableOfContents", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TocEntry> entries;
}

