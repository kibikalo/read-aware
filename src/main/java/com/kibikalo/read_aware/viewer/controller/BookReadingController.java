package com.kibikalo.read_aware.viewer.controller;

import com.kibikalo.read_aware.upload.model.BookMetadata;
import com.kibikalo.read_aware.upload.repo.BookMetadataRepository;
import com.kibikalo.read_aware.viewer.ResourceNotFoundException;
import com.kibikalo.read_aware.viewer.dto.BookMetadataDto;
import com.kibikalo.read_aware.viewer.dto.TableOfContentsDto;
import com.kibikalo.read_aware.viewer.service.BookService;
import com.kibikalo.read_aware.viewer.service.CoverResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/books")
public class BookReadingController {

    private final BookService bookService;
    private final BookMetadataRepository bookMetadataRepository;

    public BookReadingController(BookService bookService, BookMetadataRepository bookMetadataRepository) {
        this.bookService = bookService;
        this.bookMetadataRepository = bookMetadataRepository;
    }

    /**
     * GET /api/books
     * Returns a list of all books as DTOs.
     */
    @GetMapping
    public List<BookMetadataDto> getAllBooks() {
        return bookService.getAllBooksDto();
    }

    /**
     * GET /api/books/{bookId}
     * Returns single BookMetadataDto, or 404 if not found.
     */
    @GetMapping("/{bookId}")
    public ResponseEntity<BookMetadataDto> getBook(@PathVariable Long bookId) {
        return bookService.getBookDto(bookId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /api/books/{bookId}/toc
     * Returns TableOfContentsDto for the given book.
     */
    @GetMapping("/{bookId}/toc")
    public ResponseEntity<TableOfContentsDto> getTableOfContents(@PathVariable Long bookId) {
        TableOfContentsDto tocDto = bookService.getTableOfContentsDto(bookId);
        if (tocDto == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(tocDto);
    }

    /**
     * GET /api/books/{bookId}/chapters/{chapterId}
     * Returns the raw JSON for a given chapter (as a String).
     * (If you like, you can also return a DTO, but let's keep it as-is for now.)
     */
    @GetMapping("/{bookId}/chapters/{chapterId}")
    public ResponseEntity<?> getChapterContent(@PathVariable Long bookId,
                                               @PathVariable Long chapterId) {
        try {
            String chapterJson = bookService.getChapterContent(bookId, chapterId);
            return ResponseEntity.ok(chapterJson);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IOException e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error reading chapter content: " + e.getMessage());
        }
    }

    /**
     * GET /api/books/{bookId}/cover
     * Returns the cover image file as an image/jpeg (or image/png) response.
     */
    @GetMapping("/{bookId}/cover")
    public ResponseEntity<Resource> getBookCover(@PathVariable Long bookId) {
        try {
            // Service call does the heavy lifting
            CoverResource cover = bookService.getBookCover(bookId);

            return ResponseEntity.ok()
                    .contentType(cover.getMediaType())
                    .body(cover.getResource());

        } catch (ResourceNotFoundException e) {
            // If the cover doesn't exist or the book is missing
            return ResponseEntity.notFound().build();
        } catch (IOException e) {
            // If there's an IO error reading the file
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}