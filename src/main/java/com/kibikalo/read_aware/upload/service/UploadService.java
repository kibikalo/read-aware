package com.kibikalo.read_aware.upload.service;

import com.kibikalo.read_aware.upload.model.MetadataExtractionResult;
import com.kibikalo.read_aware.upload.model.TableOfContents;
import com.kibikalo.read_aware.upload.repo.BookMetadataRepository;
import com.kibikalo.read_aware.upload.model.BookMetadata;
import com.kibikalo.read_aware.upload.repo.ChapterRepository;
import com.kibikalo.read_aware.upload.repo.TableOfContentsRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class UploadService {

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
        // 1) Copy the uploaded file to a temp location
        String filename = file.getOriginalFilename();
        Path tempFile = this.rootLocation.resolve(filename).normalize().toAbsolutePath();
        Files.copy(file.getInputStream(), tempFile, StandardCopyOption.REPLACE_EXISTING);

        // 2) Extract metadata (including cover bytes)
        MetadataExtractionResult result = metadataExtractionService.extractMetadata(tempFile.toString());
        BookMetadata metadata = result.getMetadata();

        // 3) Persist BookMetadata initially (no file paths yet)
        metadata = bookMetadataRepository.save(metadata);

        // 4) Create a directory for this specific book
        Path bookDir = this.rootLocation.resolve(metadata.getId().toString());
        Files.createDirectories(bookDir);

        // 5) Move the EPUB file into bookDir
        Path finalFile = bookDir.resolve(filename);
        Files.move(tempFile, finalFile, StandardCopyOption.REPLACE_EXISTING);
        metadata.setFilePath(finalFile.toString());

        // 6) If we have coverImageBytes, store it (e.g. "cover.jpg")
        if (result.getCoverImageBytes() != null) {
            String ext = ".jpg"; // default
            if (result.getCoverMediaType() != null && result.getCoverMediaType().contains("png")) {
                ext = ".png";
            }
            Path coverFile = bookDir.resolve("cover" + ext);
            Files.write(coverFile, result.getCoverImageBytes());
            metadata.setCoverImagePath(coverFile.toString());
        }

        // 7) Process the book contents (TOC, chapters)
        TableOfContents toc = contentProcessingService.processAndSaveBookContents(file, metadata, bookDir);
        metadata.setTableOfContents(toc);

        // 8) Update the metadata with final info (cover path, etc.)
        bookMetadataRepository.save(metadata);
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