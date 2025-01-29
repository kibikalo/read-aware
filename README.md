# Web-based EPUB Reader with translation features

## Overview
This project is a web-based EPUB reader application designed to enhance language learning and reading comprehension. Users can upload EPUB files, view their contents, and access definitions or translations for words and sentences in near real-time. The application is built using Spring Boot, PostgreSQL, Thymleaf + AJAX, and integrates external dictionary and translation services.

## Features
- **EPUB Upload and Parsing**: Upload EPUB files, extract metadata (title, author, language), and process content into sentences and words.
- **Interactive Dictionary and Translation**: Fetch definitions, synonyms, and translations for selected words and sentences using integrated APIs.
- **Dynamic Frontend**: Uses AJAX for seamless updates, allowing a responsive user experience without full page reloads.
- **REST API**: Exposes endpoints for managing books, chapters, and metadata.

---

## Technologies
### Backend
- **Spring Boot**: Framework for REST API development and backend logic.
- **Spring Data JPA**: Simplifies database operations for managing book metadata and content.
- **PostgreSQL**: Stores book metadata and table of contents.
- **OpenNLP**: Detects sentence boundaries for text processing.
- **EpubReader**: Extracts metadata and content from EPUB files.
- **JSoup**: Parses and sanitizes HTML from EPUB content.
- **DictionaryAPI.dev**: Provides word definitions and synonyms.
- **DeepL API**: Handles translations for words and sentences.

### Frontend
- **Thymeleaf**: Server-side rendering of HTML templates.
- **jQuery / AJAX**: Enables asynchronous requests and DOM manipulation.
- **HTML/CSS**: Provides layout and styling.

---

## Backend Architecture
The backend uses a layered architecture:

1. **Controller Layer**: Handles HTTP requests and routes them to appropriate services.
2. **Service Layer**: Implements business logic, such as metadata extraction, content parsing, and sentence detection.
3. **Repository Layer**: Manages database interactions via Spring Data JPA.

### Workflow
#### File Upload
1. The user uploads an EPUB file.
2. **UploadService** handles:
    - Metadata extraction (title, author, language).
    - Saving the file to a structured directory.
    - Parsing content into JSON chapters with sentences and words.
3. Metadata are stored in PostgreSQL, and processed content as JSON files.

#### Translation and Definitions
1. Frontend sends requests to endpoints such as `/api/dictionary/lookup` or `/api/translation`.
2. Responses include definitions or translations fetched from external services.

### Key API Endpoints
- **`GET /api/books`**: Fetch all books.
- **`GET /api/books/{bookId}`**: Fetch metadata for a specific book.
- **`GET /api/books/{bookId}/chapters/{chapterId}`**: Retrieve chapter content in JSON format.
- **`GET /api/books/{bookId}/cover`**: Fetch the book cover image.

---

## Frontend Architecture
The frontend uses a hybrid approach combining:
1. **Thymeleaf for Initial Rendering**: Generates static HTML for pages like book lists and readers.
2. **AJAX for Interactivity**: Updates content dynamically, such as loading chapter data and fetching translations.

### Key Modules
- **booklist.js**: Manages paginated book lists.
- **reader.js**: Loads chapter content and renders JSON into structured HTML.
- **dictionary.js**: Handles word lookups and translation requests.
- **upload.js**: Manages EPUB file uploads.

### UI Layout
- **Book List Page**: Displays books with pagination.
- **Reader Page**: Three-column layout with:
    - Table of Contents (TOC).
    - Main reading area.
    - Translation and dictionary panel.
- **Upload Page**: Drag-and-drop interface for uploading EPUBs.

---

## Potential Enhancements
- **Contextual Translations**: Integrate AI-based solutions for broader context analysis.
- **Additional Formats**: Support for PDF etc.
- **User Personalization**: Allow saving favorite words, notes, or custom dictionaries. Bookmarks features. 
- **Scalability**: Move files to cloud storage and adopt distributed databases.

---

## Summary
This project demonstrates the integration of modern backend and frontend technologies to create a user-friendly EPUB reader application with advanced language tools. Its modular design and RESTful architecture make it scalable and extensible for future enhancements.
