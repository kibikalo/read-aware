package com.kibikalo.read_aware.viewer.dto;

import com.kibikalo.read_aware.upload.model.BookMetadata;
import com.kibikalo.read_aware.upload.model.Chapter;
import com.kibikalo.read_aware.upload.model.TableOfContents;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class BookMapper {

    public BookMetadataDto toDto(BookMetadata bookMetadata) {
        if (bookMetadata == null) {
            return null;
        }

        BookMetadataDto dto = new BookMetadataDto();
        dto.setId(bookMetadata.getId());
        dto.setTitle(bookMetadata.getTitle());
        dto.setAuthor(bookMetadata.getAuthor());
        dto.setLanguage(bookMetadata.getLanguage());
        dto.setDescription(bookMetadata.getDescription());
        dto.setGenre(bookMetadata.getGenre());
        dto.setUploadDate(bookMetadata.getUploadDate());

        // Map the TableOfContents if it exists
        dto.setTableOfContents(toDto(bookMetadata.getTableOfContents()));

        return dto;
    }

    public TableOfContentsDto toDto(TableOfContents toc) {
        if (toc == null) {
            return null;
        }

        TableOfContentsDto dto = new TableOfContentsDto();
        dto.setId(toc.getId());

        // Convert Chapters to ChapterDto
        List<ChapterDto> chapterDtos = new ArrayList<>();
        if (toc.getChapters() != null) {
            for (Chapter chapter : toc.getChapters()) {
                ChapterDto chapterDto = new ChapterDto();
                chapterDto.setId(chapter.getId());
                chapterDto.setTitle(chapter.getTitle());
                chapterDtos.add(chapterDto);
            }
        }
        dto.setChapters(chapterDtos);

        return dto;
    }
}

