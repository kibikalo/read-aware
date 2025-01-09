package com.kibikalo.read_aware.viewer.service;

import com.kibikalo.read_aware.upload.model.BookMetadata;
import com.kibikalo.read_aware.upload.model.Chapter;
import com.kibikalo.read_aware.upload.repo.BookMetadataRepository;
import com.kibikalo.read_aware.upload.repo.ChapterRepository;
import com.kibikalo.read_aware.upload.repo.TableOfContentsRepository;
import com.kibikalo.read_aware.viewer.ResourceNotFoundException;
import com.kibikalo.read_aware.viewer.dto.BookMapper;
import com.kibikalo.read_aware.viewer.dto.BookMetadataDto;
import com.kibikalo.read_aware.viewer.dto.TableOfContentsDto;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BookService {

    private final BookMetadataRepository bookMetadataRepository;
    private final ChapterRepository chapterRepository;
    private final TableOfContentsRepository tocRepository;
    private final BookMapper bookMapper;

    public BookService(BookMetadataRepository bookMetadataRepository,
                       ChapterRepository chapterRepository,
                       TableOfContentsRepository tocRepository,
                       BookMapper bookMapper) {
        this.bookMetadataRepository = bookMetadataRepository;
        this.chapterRepository = chapterRepository;
        this.tocRepository = tocRepository;
        this.bookMapper = bookMapper;
    }

    /**
     * Returns DTOs for all books in the repository.
     */
    public List<BookMetadataDto> getAllBooksDto() {
        return bookMetadataRepository.findAll().stream()
                .map(bookMapper::toDto)  // Convert each BookMetadata to BookMetadataDto
                .collect(Collectors.toList());
    }

    /**
     * Returns a single book as a DTO, if found.
     */
    public Optional<BookMetadataDto> getBookDto(Long bookId) {
        return bookMetadataRepository.findById(bookId)
                .map(bookMapper::toDto);
    }

    /**
     * Returns TableOfContentsDto for the given book, if it exists.
     */
    public TableOfContentsDto getTableOfContentsDto(Long bookId) {
        return bookMetadataRepository.findById(bookId)
                .map(BookMetadata::getTableOfContents)
                .map(bookMapper::toDto)  // convert TableOfContents to TableOfContentsDto
                .orElse(null);
    }

    public String getChapterContent(Long bookId, Long chapterId) throws IOException {
        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Chapter not found with id " + chapterId));

        // Make sure the chapter belongs to the requested book
        if (!chapter.getBook().getId().equals(bookId)) {
            throw new ResourceNotFoundException(
                    "Chapter id " + chapterId + " does not belong to book id " + bookId);
        }

        // The filePath field presumably points to the JSON file containing chapter text
        Path path = Paths.get(chapter.getFilePath());
        if (!Files.exists(path)) {
            throw new FileNotFoundException("File not found at path: " + path.toString());
        }

        // Read the entire file content as a string (UTF-8)
        return Files.readString(path, StandardCharsets.UTF_8);
    }

    /**
     * Reads the chapter file (JSON) from disk and returns it as a raw String.
     * Throws ResourceNotFoundException if the chapter or book is not found.
     */
//    public String getChapterContent(Long bookId, Long chapterId) throws IOException {
//        Chapter chapter = chapterRepository.findById(chapterId)
//                .orElseThrow(() -> new ResourceNotFoundException(
//                        "Chapter not found with id " + chapterId));
//
//        // Make sure the chapter belongs to the requested book
//        if (!chapter.getBook().getId().equals(bookId)) {
//            throw new ResourceNotFoundException(
//                    "Chapter id " + chapterId + " does not belong to book id " + bookId);
//        }
//
//        // The filePath field presumably points to the JSON file containing chapter text
//        Path path = Paths.get(chapter.getFilePath());
//        if (!Files.exists(path)) {
//            throw new FileNotFoundException("File not found at path: " + path.toString());
//        }
//
//        // Read the entire file content as a string (UTF-8)
//        return Files.readString(path, StandardCharsets.UTF_8);
//    }
}