package com.kibikalo.read_aware.viewer.dto;

import java.util.List;

public class TableOfContentsDto {
    private Long id;
    private List<ChapterDto> chapters;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<ChapterDto> getChapters() {
        return chapters;
    }

    public void setChapters(List<ChapterDto> chapters) {
        this.chapters = chapters;
    }
}
