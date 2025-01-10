package com.kibikalo.read_aware.upload.service;

import com.kibikalo.read_aware.upload.model.BookMetadata;
import com.kibikalo.read_aware.upload.model.MetadataExtractionResult;
import nl.siegmann.epublib.domain.Resource;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.epub.EpubReader;

import java.io.FileInputStream;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Service
public class MetadataExtractionService {

    public MetadataExtractionResult extractMetadata(String filePath) {
        MetadataExtractionResult result = new MetadataExtractionResult();

        try (FileInputStream fis = new FileInputStream(filePath)) {
            Book book = (new EpubReader()).readEpub(fis);

            BookMetadata metadata = new BookMetadata();
            metadata.setTitle(book.getTitle());
            metadata.setAuthor(
                    book.getMetadata().getAuthors().stream()
                            .map(author -> author.getFirstname() + " " + author.getLastname())
                            .collect(Collectors.joining(", "))
            );
            metadata.setLanguage(book.getMetadata().getLanguage());
            metadata.setUploadDate(LocalDateTime.now());

            // Description (cleaned)
            String rawDescription = book.getMetadata().getDescriptions().isEmpty()
                    ? null
                    : book.getMetadata().getDescriptions().get(0);
            String cleanDescription = rawDescription != null
                    ? Jsoup.parse(rawDescription).text()
                    : null;
            metadata.setDescription(cleanDescription);

            // Extract the cover image
            Resource coverResource = book.getCoverImage();
            if (coverResource != null) {
                byte[] coverBytes = coverResource.getData();
                String mediaType = coverResource.getMediaType() != null
                        ? coverResource.getMediaType().toString()
                        : "image/jpeg"; // fallback

                result.setCoverImageBytes(coverBytes);
                result.setCoverMediaType(mediaType);
            }

            // Put BookMetadata inside the result
            result.setMetadata(metadata);

        } catch (Exception e) {
            e.printStackTrace();
            result.setMetadata(null);
            result.setCoverImageBytes(null);
        }

        return result;
    }
}