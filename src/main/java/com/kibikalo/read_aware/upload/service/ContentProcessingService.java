package com.kibikalo.read_aware.upload.service;

import com.kibikalo.read_aware.upload.model.BookMetadata;
import com.kibikalo.read_aware.upload.model.Chapter;
import com.kibikalo.read_aware.upload.model.TableOfContents;
import com.kibikalo.read_aware.upload.repo.ChapterRepository;
import com.kibikalo.read_aware.upload.repo.TableOfContentsRepository;
import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.TOCReference;
import nl.siegmann.epublib.epub.EpubReader;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.TextNode;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.json.JSONArray;
import org.json.JSONObject;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class ContentProcessingService {

    private final ChapterRepository chapterRepository;
    private final TableOfContentsRepository tableOfContentsRepository;
    private final SentenceDetectionService sentenceDetectionService;

    @Autowired
    public ContentProcessingService(ChapterRepository chapterRepository, TableOfContentsRepository tableOfContentsRepository, SentenceDetectionService sentenceDetectionService) {
        this.chapterRepository = chapterRepository;
        this.tableOfContentsRepository = tableOfContentsRepository;
        this.sentenceDetectionService = sentenceDetectionService;
    }

    public TableOfContents processAndSaveBookContents(MultipartFile file, BookMetadata metadata, Path bookDir) throws IOException {
        Book book = new EpubReader().readEpub(file.getInputStream());
        List<TOCReference> tocReferences = book.getTableOfContents().getTocReferences();
        List<Chapter> chapters = new ArrayList<>();

        TableOfContents toc = new TableOfContents();
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

        toc.setChapters(chapters);
        toc = tableOfContentsRepository.save(toc);
        chapterRepository.saveAll(chapters);

        return toc;
    }

    private String extractChapterContent(TOCReference tocReference) throws IOException {
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

        // Remove all <a> tags selectively and preserve text
        document.select("a").forEach(a -> {
            if (!a.text().isEmpty()) {  // Check if the <a> tag contains text
                a.replaceWith(new TextNode(a.text()));  // Replace <a> with just its text
            } else {
                a.remove();  // Remove the <a> tag if it's empty or not needed
            }
        });

        // Iterate over all elements and clear their attributes
        for (Element element : document.getAllElements()) {
            element.clearAttributes();
        }

        Elements relevantElements = document.select("h1, h2, h3, h4, h5, h6, p");

        JSONArray jsonContentArray = new JSONArray();
        for (Element element : relevantElements) {
            String[] sentences = sentenceDetectionService.detectSentences(element.text());

            JSONArray sentencesArray = new JSONArray();
            for (String sentence : sentences) {
                List<String> words = splitIntoWords(sentence);  // Split the sentence into words
                JSONArray wordsArray = new JSONArray();
                for (String word : words) {
                    wordsArray.put(word);  // Add each word to an array
                }
                sentencesArray.put(wordsArray);  // Add the array of words to the sentences array
            }

            JSONObject jsonElement = new JSONObject();
            jsonElement.put("type", element.tagName());
            jsonElement.put("content", sentencesArray);
            jsonContentArray.put(jsonElement);
        }

        JSONObject chapterJson = new JSONObject();
        chapterJson.put("content", jsonContentArray);

        Path chapterFile = bookDir.resolve(chapterTitle.replaceAll("[^a-zA-Z0-9\\.\\-]", "_") + ".json");
        Files.writeString(chapterFile, chapterJson.toString(), StandardCharsets.UTF_8);
        return chapterFile;
    }

    public List<String> splitIntoWords(String sentence) {
        return Arrays.asList(sentence.split("\\s+")); // Splits by whitespace
    }
}
