package com.kibikalo.read_aware.viewer.controller;

import com.kibikalo.read_aware.viewer.ResourceNotFoundException;
import com.kibikalo.read_aware.viewer.dto.BookMetadataDto;
import com.kibikalo.read_aware.viewer.dto.TableOfContentsDto;
import com.kibikalo.read_aware.viewer.service.BookService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/books")
public class BookReadingController {

    private final BookService bookService;

    public BookReadingController(BookService bookService) {
        this.bookService = bookService;
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
}



