package com.kibikalo.read_aware.upload.service;

import com.kibikalo.read_aware.upload.model.Chapter;
import com.kibikalo.read_aware.upload.model.TableOfContents;
import com.kibikalo.read_aware.upload.repo.BookMetadataRepository;
import com.kibikalo.read_aware.upload.model.BookMetadata;
import com.kibikalo.read_aware.upload.repo.ChapterRepository;
import com.kibikalo.read_aware.upload.repo.TableOfContentsRepository;
import jakarta.annotation.PostConstruct;
import nl.siegmann.epublib.domain.TOCReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.annotation.Transactional;
import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.epub.EpubReader;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class UploadService {

    private static final Logger logger = LoggerFactory.getLogger(UploadService.class);

    private final MetadataExtractionService metadataExtractionService;
    private final BookMetadataRepository bookMetadataRepository;
    private final TableOfContentsRepository tableOfContentsRepository;
    private final ChapterRepository chapterRepository;

    public UploadService(MetadataExtractionService metadataExtractionService, BookMetadataRepository bookMetadataRepository, TableOfContentsRepository tableOfContentsRepository, ChapterRepository chapterRepository) {
        this.metadataExtractionService = metadataExtractionService;
        this.bookMetadataRepository = bookMetadataRepository;
        this.tableOfContentsRepository = tableOfContentsRepository;
        this.chapterRepository = chapterRepository;
    }

    private final Path rootLocation = Paths.get("uploads");

    @Transactional
    public void uploadFile(MultipartFile file) throws IOException {
        String filename = file.getOriginalFilename();
        Path tempFile = this.rootLocation.resolve(filename).normalize().toAbsolutePath();
        Files.copy(file.getInputStream(), tempFile);

        BookMetadata metadata = metadataExtractionService.extractMetadata(tempFile.toString());
        metadata = bookMetadataRepository.save(metadata);

        Path bookDir = this.rootLocation.resolve(metadata.getId().toString());
        Files.createDirectories(bookDir);

        Path finalFile = bookDir.resolve(filename);
        Files.move(tempFile, finalFile);  // Move the file to the book-specific directory
        metadata.setFilePath(finalFile.toString());

        TableOfContents toc = processAndSaveBookContents(file, metadata, bookDir);
        metadata.setTableOfContents(toc); // Set the TOC reference
        bookMetadataRepository.save(metadata); // Update the metadata with the TOC reference
    }

    public TableOfContents processAndSaveBookContents(MultipartFile file, BookMetadata metadata, Path bookDir) throws IOException {
        Book book = new EpubReader().readEpub(file.getInputStream());
        List<TOCReference> tocReferences = book.getTableOfContents().getTocReferences();
        List<Chapter> chapters = new ArrayList<>();

        TableOfContents toc = new TableOfContents(); // Create TOC first
        toc.setBook(metadata);

        for (TOCReference tocReference : tocReferences) {
            Chapter chapter = new Chapter();
            chapter.setBook(metadata);
            chapter.setToc(toc);
            chapter.setTitle(tocReference.getTitle());
            String chapterContent = extractChapterContent(tocReference);
            Path chapterPath = saveChapterContent(chapterContent, bookDir, tocReference.getTitle());
            chapter.setFilePath(chapterPath.toString());
            chapters.add(chapter);
        }

        toc.setChapters(chapters); // Set chapters for TOC
        toc = tableOfContentsRepository.save(toc); // Save TOC with chapters
        chapterRepository.saveAll(chapters); // Save chapters after TOC is saved

        return toc;
    }

    private String extractChapterContent(TOCReference tocReference) throws IOException {
        // Get the input stream from the resource linked to the TOCReference
        InputStream inputStream = tocReference.getResource().getInputStream();
        try {
            byte[] contentBytes = inputStream.readAllBytes();
            return new String(contentBytes, StandardCharsets.UTF_8);
        } finally {
            inputStream.close();
        }
    }

    private Path saveChapterContent(String content, Path bookDir, String chapterTitle) throws IOException {
        Document document = Jsoup.parse(content);
        String textContent = document.text();
        String jsonContent = "{\"content\": \"" + textContent.replace("\"", "\\\"") + "\"}"; // Basic JSON structure

        Path chapterFile = bookDir.resolve(chapterTitle.replaceAll("[^a-zA-Z0-9\\.\\-]", "_") + ".json");
        Files.writeString(chapterFile, jsonContent, StandardCharsets.UTF_8);
        return chapterFile;
    }


    private void logMetadataDetails(BookMetadata metadata) {
        if (metadata != null) {
            logger.info("Extracted Metadata:");
            logger.info("Title: {}, Length: {}", metadata.getTitle(), metadata.getTitle().length());
            logger.info("Author: {}, Length: {}", metadata.getAuthor(), metadata.getAuthor().length());
            logger.info("Language: {}, Length: {}", metadata.getLanguage(), metadata.getLanguage().length());
            logger.info("Publish Date: {}", metadata.getPublishDate());
            logger.info("Upload Date: {}", metadata.getUploadDate());
            logger.info("Description: {}, Length: {}", metadata.getDescription(), metadata.getDescription().length());
            logger.info("Genre: {}, Length: {}", metadata.getGenre(), metadata.getGenre() != null ? metadata.getGenre().length() : 0);
            logger.info("Cover Image Path: {}", metadata.getCoverImagePath());
        } else {
            logger.info("No metadata extracted.");
        }
    }

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(rootLocation);
        } catch (Exception e) {
            throw new RuntimeException("Could not initialize storage location", e);
        }
    }
}



