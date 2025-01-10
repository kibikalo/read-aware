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
 * Render the dictionary result in the right navbar (#translation-container).
 * The structure of 'response' depends on how we parse the dictionaryapi.dev result on the backend.
 */
function renderDictionaryResult(response) {
    // Clear old data
    $('#translation-container').empty();

    if (!response || response.length === 0) {
        $('#translation-container').html('<p>No definition found.</p>');
        return;
    }

    // response might be an array of dictionary entries.
    // Let's assume we do some minimal parsing.
    // dictionaryapi.dev returns something like:
    // [
    //   {
    //     "word": "wind",
    //     "phonetics": [...],
    //     "meanings": [
    //       {
    //         "partOfSpeech": "noun",
    //         "definitions": [ { "definition": "...", "synonyms": [...], "antonyms": [...] } ],
    //         ...
    //       }
    //     ],
    //     ...
    //   }
    // ]

    // We'll render the first entry's data, for example.
    const entry = response[0];
    const wordTitle = $('<h3>').text(entry.word);

    // A button to request a translation
    const translationBtn = $('<button>')
        .text('Translation')
        .on('click', function() {
            requestTranslation(entry.word);
        });

    // Show each meaning
    const meaningContainer = $('<div>');
    if (entry.meanings && entry.meanings.length > 0) {
        entry.meanings.forEach((meaning) => {
            const pos = meaning.partOfSpeech;
            const definitionsList = $('<ul>');

            if (meaning.definitions) {
                meaning.definitions.forEach((defObj) => {
                    const defItem = $('<li>').text(defObj.definition);
                    definitionsList.append(defItem);
                });
            }

            meaningContainer.append($('<h4>').text(pos));
            meaningContainer.append(definitionsList);
        });
    }

    // Append the translationBtn (along with wordTitle and meaningContainer)
    $('#translation-container').append(wordTitle, translationBtn, meaningContainer);
}

/**
 * Calls /api/translation/advanced to translate the word to Ukrainian
 */
function requestTranslation(word) {
    $.ajax({
        url: '/api/translation',
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify({ word: word }),
        success: function(res) {
            // e.g. { original: "Hello", translated: "Привіт" }
            const translatedText = res.translated;
            showTranslation(word, translatedText);
        },
        error: function(xhr, status, error) {
            console.error('Translation error:', error);
            $('#translation-container').append('<p>Error during advanced translation.</p>');
        }
    });
}

function showTranslation(originalWord, translatedText) {
    // You can append or replace content in #translation-container
    const translationDiv = $('<div>').addClass('translation');
    translationDiv.html(`
        <h4>Translation</h4>
        <p><strong>Original:</strong> ${originalWord}</p>
        <p><strong>Ukrainian:</strong> ${translatedText}</p>
    `);

    $('#translation-container').append(translationDiv);
}

/**
 * Enhance word click handler to call dictionary lookup.
 */
function onWordClick(word) {
    // For now, log the clicked word
    console.log('Clicked word:', word);

    // Make a POST request to our Spring Boot backend
    $.ajax({
        url: '/api/dictionary/lookup',
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify({ word: word }),
        success: function(response) {
            // response might have definitions, synonyms, etc.
            renderDictionaryResult(response);
        },
        error: function(xhr, status, error) {
            console.error('Dictionary lookup error:', error);
            $('#translation-container').html('<p>Error fetching dictionary data.</p>');
        }
    });
}

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
                onWordClick(word);
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
                onWordClick(word);
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
