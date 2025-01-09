package com.kibikalo.read_aware.upload.service;

import com.kibikalo.read_aware.upload.model.TableOfContents;
import com.kibikalo.read_aware.upload.repo.BookMetadataRepository;
import com.kibikalo.read_aware.upload.model.BookMetadata;
import com.kibikalo.read_aware.upload.repo.ChapterRepository;
import com.kibikalo.read_aware.upload.repo.TableOfContentsRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class UploadService {

    private static final Logger logger = LoggerFactory.getLogger(UploadService.class);

    private final MetadataExtractionService metadataExtractionService;
    private final ContentProcessingService contentProcessingService;
    private final BookMetadataRepository bookMetadataRepository;
    private final TableOfContentsRepository tableOfContentsRepository;
    private final ChapterRepository chapterRepository;

    public UploadService(MetadataExtractionService metadataExtractionService, ContentProcessingService contentProcessingService, BookMetadataRepository bookMetadataRepository, TableOfContentsRepository tableOfContentsRepository, ChapterRepository chapterRepository) {
        this.metadataExtractionService = metadataExtractionService;
        this.contentProcessingService = contentProcessingService;
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

        TableOfContents toc = contentProcessingService.processAndSaveBookContents(file, metadata, bookDir);
        metadata.setTableOfContents(toc); // Set the TOC reference
        bookMetadataRepository.save(metadata); // Update the metadata with the TOC reference
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



