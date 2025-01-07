package com.kibikalo.read_aware.upload.service;

import com.kibikalo.read_aware.upload.BookDataRepository;
import com.kibikalo.read_aware.upload.model.BookData;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class UploadService {
    private static final Logger logger = LoggerFactory.getLogger(UploadService.class);

    private final MetadataExtractionService metadataExtractionService;

    private final BookDataRepository bookDataRepository; // Assume this is your JPA repository

    private final Path rootLocation = Paths.get("uploads");

    public UploadService(MetadataExtractionService metadataExtractionService, BookDataRepository bookDataRepository) {
        this.metadataExtractionService = metadataExtractionService;
        this.bookDataRepository = bookDataRepository;
    }

    public void uploadFile(MultipartFile file) {
        try {
            String filename = file.getOriginalFilename();
            Path destinationFile = this.rootLocation.resolve(Paths.get(filename)).normalize().toAbsolutePath();
            if (!destinationFile.getParent().equals(this.rootLocation.toAbsolutePath())) {
                // This is a security check
                throw new IllegalStateException("Cannot store file outside current directory.");
            }
            Files.copy(file.getInputStream(), destinationFile);

            logger.info("File uploaded successfully: {}", filename);

            BookData metadata = metadataExtractionService.extractMetadata(destinationFile.toString());

            logMetadataDetails(metadata);

            bookDataRepository.save(metadata);
            logger.info("Metadata saved successfully for: {}", filename);
        } catch (Exception e) {
            logger.error("Failed to store file " + file.getOriginalFilename(), e);
            throw new RuntimeException("Failed to store file " + file.getOriginalFilename(), e);
        }
    }

    private void logMetadataDetails(BookData metadata) {
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

    // Initialization method to create directory if not exists
    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage location", e);
        }
    }
}


