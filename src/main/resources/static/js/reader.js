// reader.js

// Adjust this if you have multiple books or pass the bookId differently
const DEFAULT_BOOK_ID = 5;

$(document).ready(function() {
    // Load the Table of Contents for the default book when the page is ready
    loadTOC(DEFAULT_BOOK_ID);
});

/**
 * Fetch the TOC from the server for the given bookId and render it in #toc-container.
 */
function loadTOC(bookId) {
    const tocUrl = `/api/books/${bookId}/toc`;

    $.getJSON(tocUrl)
        .done(function(tocData) {
            if (!tocData || !tocData.chapters) {
                $('#toc-container').append('<p>No chapters found for this book.</p>');
                return;
            }

            // Clear any existing TOC items
            $('#toc-container').find('.chapter-link').remove();

            // Iterate over the chapters and create clickable links
            tocData.chapters.forEach(function(chapter) {
                const link = $('<a>')
                    .attr('href', '#')
                    .addClass('chapter-link')
                    .text(chapter.title || `Chapter ${chapter.id}`)
                    .on('click', function(e) {
                        e.preventDefault();
                        loadChapter(bookId, chapter.id);
                    });
                $('#toc-container').append(link);
            });
        })
        .fail(function(jqXHR, textStatus, errorThrown) {
            console.error('Failed to load TOC:', textStatus, errorThrown);
            $('#toc-container').append('<p>Error loading TOC</p>');
        });
}

/**
 * Fetch a chapter's JSON content from the server and render it into #chapter-content.
 */
function loadChapter(bookId, chapterId) {
    const chapterUrl = `/api/books/${bookId}/chapters/${chapterId}`;

    $.get(chapterUrl)
        .done(function(chapterJsonStr) {
            // chapterJsonStr is the raw JSON string from your backend
            let chapterData;
            try {
                chapterData = JSON.parse(chapterJsonStr);
            } catch (e) {
                console.error('Error parsing chapter JSON:', e);
                $('#chapter-content').html('<p>Error parsing chapter content</p>');
                return;
            }

            renderChapter(chapterData);
        })
        .fail(function(jqXHR, textStatus, errorThrown) {
            console.error('Failed to load chapter content:', textStatus, errorThrown);
            $('#chapter-content').html('<p>Error loading chapter content</p>');
        });
}

/**
 * Given the parsed JSON for a chapter, build and insert the HTML into #chapter-content.
 * Example JSON structure:
 * {
 *   "content": [
 *     {
 *       "type": "h1",
 *       "content": [["CHAPTER", "EIGHT"]]
 *     },
 *     {
 *       "type": "p",
 *       "content": [
 *         ["The", "wind", "blew", "from", "the", "sea,", ...],
 *         ["Another", "sentence", ...]
 *       ]
 *     },
 *     ...
 *   ]
 * }
 */
function renderChapter(chapterData) {
    const container = $('#chapter-content');
    container.empty(); // Clear any existing content

    // Safety check
    if (!chapterData || !Array.isArray(chapterData.content)) {
        container.html('<p>Invalid chapter format.</p>');
        return;
    }

    // Iterate over each block (heading, paragraph, etc.)
    chapterData.content.forEach(function(block) {
        let element;

        switch (block.type) {
            case 'h1':
            case 'h2':
            case 'h3':
            case 'h4':
                element = document.createElement(block.type);
                // block.content is an array of arrays of words, but typically headings might just have 1 array
                element.textContent = block.content.flat().join(' ');
                break;
            case 'p':
                element = renderParagraph(block.content);
                break;
            default:
                // fallback - treat unknown type as a paragraph
                element = renderParagraph(block.content);
                break;
        }

        container.append(element);
    });
}

/**
 * Create a <p> element from an array of arrays of words.
 * blockContent = [
 *   ["The", "wind", "blew", ...],
 *   ["Another", "sentence", ...]
 * ]
 */
function renderParagraph(blockContent) {
    const p = document.createElement('p');

    // Each item in blockContent is a sentence array
    blockContent.forEach(function(sentence, sentenceIndex) {
        sentence.forEach(function(word, wordIndex) {
            // Create a span for each word so we can attach events
            const span = document.createElement('span');
            span.classList.add('word');
            span.textContent = word;

            // Optionally, attach click/hover listeners for translations, etc.
            span.addEventListener('click', function(e) {
                // For now, just log the clicked word to console
                console.log('Clicked word:', word);
                // Future: Make an API call to get translation/definition
            });

            p.appendChild(span);

            // Add a space after each word except maybe the last in the sentence
            if (wordIndex < sentence.length - 1) {
                p.appendChild(document.createTextNode(' '));
            }
        });

        // Optionally add extra spacing or a line break after each sentence
        if (sentenceIndex < blockContent.length - 1) {
            p.appendChild(document.createTextNode(' '));
        }
    });

    return p;
}
