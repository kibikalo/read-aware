$(document).ready(function() {
    // 1) Extract bookId from query param
    const urlParams = new URLSearchParams(window.location.search);
    const bookIdParam = urlParams.get('bookId');

    // 2) Convert to number (optional) or handle missing param
    if (!bookIdParam) {
        // If no bookId is provided, you could show an error or a default book
        $('#chapter-content').html('<p>Error: No bookId provided in URL.</p>');
        return;
    }

    const bookId = parseInt(bookIdParam, 10);
    /* OR */
    /*const defaultBookId = 5;
    const bookId = bookIdParam ? parseInt(bookIdParam, 10) : defaultBookId;*/

    if (isNaN(bookId)) {
        // Not a valid number
        $('#chapter-content').html('<p>Error: Invalid bookId in URL.</p>');
        return;
    }

    // 3) Load the TOC for the specified book
    loadTOC(bookId);
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
            case 'h5':
            case 'h6':
                // Use our new helper to render the heading with word spans
                element = renderHeading(block.type, block.content);
                break;
            case 'p':
                // Render paragraph with sentence + word spans
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
 * Create a heading element (h1, h2, etc.) from an array of arrays of words.
 * Example heading content:
 * blockContent = [
 *   ["CHAPTER", "EIGHT"]
 * ]
 * Sometimes headings might only have one array, but we'll handle multiple.
 */
function renderHeading(blockType, blockContent) {
    // Create the heading element (e.g., <h1>, <h2>, etc.)
    const heading = document.createElement(blockType);

    // Each item in blockContent is a sentence array
    blockContent.forEach(function(sentence, sentenceIndex) {
        // Create a span to wrap the entire sentence
        const sentenceSpan = document.createElement('span');
        sentenceSpan.classList.add('sentence');

        // Optional: attach click/hover listeners for translations, etc.
        sentenceSpan.addEventListener('click', function() {
            console.log('Clicked sentence in heading:', sentence);
            // Future: Make an API call to get translation/definition
        });

        // For each word in the sentence, create a span
        sentence.forEach(function(word, wordIndex) {
            const wordSpan = document.createElement('span');
            wordSpan.classList.add('word');
            wordSpan.textContent = word;

            // Optional: attach click/hover listeners for translations, etc.
            wordSpan.addEventListener('click', function() {
                console.log('Clicked word in heading:', word);
                // Future: Make an API call to get translation/definition
            });

            sentenceSpan.appendChild(wordSpan);

            // Add a space after each word except the last in the sentence
            if (wordIndex < sentence.length - 1) {
                sentenceSpan.appendChild(document.createTextNode(' '));
            }
        });

        // Append the sentence span to the heading element
        heading.appendChild(sentenceSpan);

        // Optionally add extra spacing between sentences
        if (sentenceIndex < blockContent.length - 1) {
            // Add space or line break between sentences
            heading.appendChild(document.createTextNode(' '));
        }
    });

    return heading;
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
        // Create a span to wrap the entire sentence
        const sentenceSpan = document.createElement('span');
        sentenceSpan.classList.add('sentence');

        // Optional: attach click/hover listeners for translations, etc.
        sentenceSpan.addEventListener('click', function() {
            console.log('Clicked sentence:', sentence);
            // Future: Make an API call to get translation/definition
        });

        // For each word in the sentence, create a span
        sentence.forEach(function(word, wordIndex) {
            const wordSpan = document.createElement('span');
            wordSpan.classList.add('word');
            wordSpan.textContent = word;

            // Optional: attach click/hover listeners for translations, etc.
            wordSpan.addEventListener('click', function() {
                console.log('Clicked word:', word);
                // Future: Make an API call to get translation/definition
            });

            sentenceSpan.appendChild(wordSpan);

            // Add a space after each word except the last in the sentence
            if (wordIndex < sentence.length - 1) {
                sentenceSpan.appendChild(document.createTextNode(' '));
            }
        });

        // Append the sentence span to the <p> element
        p.appendChild(sentenceSpan);

        // Optionally add extra spacing or a line break between sentences
        if (sentenceIndex < blockContent.length - 1) {
            // Here, we add a space or you might add a period, line break, etc.
            p.appendChild(document.createTextNode(' '));
        }
    });

    return p;
}
