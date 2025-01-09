package com.kibikalo.read_aware.upload.service;

import com.kibikalo.read_aware.upload.model.BookMetadata;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.epub.EpubReader;

import java.io.FileInputStream;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Service
public class MetadataExtractionService {

    public BookMetadata extractMetadata(String filePath) {
        try (FileInputStream fis = new FileInputStream(filePath)) {
            Book book = (new EpubReader()).readEpub(fis);

            BookMetadata metadata = new BookMetadata();
            metadata.setTitle(book.getTitle());
            metadata.setAuthor(book.getMetadata().getAuthors().stream()
                    .map(author -> author.getFirstname() + " " + author.getLastname())
                    .collect(Collectors.joining(", ")));
            metadata.setLanguage(book.getMetadata().getLanguage());
//            if (!book.getMetadata().getDates().isEmpty()) {
//                metadata.setPublishDate(LocalDateTime.parse(book.getMetadata().getDates().get(0).getValue()));
//            }
            metadata.setUploadDate(LocalDateTime.now());

            // Extract and clean description (until implement styling in description too)
            String rawDescription = book.getMetadata().getDescriptions().isEmpty()
                    ? null
                    : book.getMetadata().getDescriptions().get(0);
            String cleanDescription = rawDescription != null ? Jsoup.parse(rawDescription).text() : null;
            metadata.setDescription(cleanDescription);

            // Add later
            // metadata.setCoverImagePath(coverImagePath);
            // metadata.setGenre(genre);

            return metadata;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}


