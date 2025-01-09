package com.kibikalo.read_aware.viewer.controller;

import com.kibikalo.read_aware.viewer.dto.BookMetadataDto;
import com.kibikalo.read_aware.viewer.service.BookService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/books")
public class BookListController {

    private final BookService bookService;

    public BookListController(BookService bookService) {
        this.bookService = bookService;
    }

    /**
     * GET /api/books/paged?page=0&size=20
     * Returns a paginated list of BookMetadataDto.
     */
    @GetMapping("/paged")
    public Page<BookMetadataDto> getBooksPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return bookService.getBooksPage(page, size);
    }
}
