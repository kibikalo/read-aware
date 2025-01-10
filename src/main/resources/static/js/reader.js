$(document).ready(function() {
    // 1) Extract bookId from query param
    const urlParams = new URLSearchParams(window.location.search);
    const bookIdParam = urlParams.get('bookId');

    if (!bookIdParam) {
        $('#chapter-content').html('<p>Error: No bookId provided in URL.</p>');
        return;
    }

    const bookId = parseInt(bookIdParam, 10);
    if (isNaN(bookId)) {
        $('#chapter-content').html('<p>Error: Invalid bookId in URL.</p>');
        return;
    }

    // 2) Load the TOC for the specified book (function from toc.js)
    loadTOC(bookId);
});

/**
 * Render a chapter JSON structure in #chapter-content
 */
function renderChapter(chapterData) {
    const container = $('#chapter-content');
    container.empty();

    if (!chapterData || !Array.isArray(chapterData.content)) {
        container.html('<p>Invalid chapter format.</p>');
        return;
    }

    chapterData.content.forEach(function(block) {
        let element;
        switch (block.type) {
            case 'h1':
            case 'h2':
            case 'h3':
            case 'h4':
            case 'h5':
            case 'h6':
                element = renderHeading(block.type, block.content);
                break;
            case 'p':
                element = renderParagraph(block.content);
                break;
            default:
                // fallback
                element = renderParagraph(block.content);
                break;
        }
        container.append(element);
    });
}

/**
 * Create heading elements with word-level spans
 */
function renderHeading(blockType, blockContent) {
    const heading = document.createElement(blockType);

    blockContent.forEach(function(sentence) {
        const sentenceSpan = document.createElement('span');
        sentenceSpan.classList.add('sentence');

        // For each word, create a span
        sentence.forEach(function(word, wIndex) {
            const wordSpan = document.createElement('span');
            wordSpan.classList.add('word');
            wordSpan.textContent = word;

            // On click, call our dictionary logic
            wordSpan.addEventListener('click', function(evt) {
                evt.stopPropagation();
                // Now we pass the entire sentenceSpan to onWordClick
                onWordClick(word, sentenceSpan);
            });

            sentenceSpan.appendChild(wordSpan);

            // add space if needed
            if (wIndex < sentence.length - 1) {
                sentenceSpan.appendChild(document.createTextNode(' '));
            }
        });

        heading.appendChild(sentenceSpan);
        heading.appendChild(document.createTextNode(' '));
    });

    return heading;
}

/**
 * Create paragraph with sentence and word spans
 */
function renderParagraph(blockContent) {
    const p = document.createElement('p');

    blockContent.forEach(function(sentence, sIndex) {
        const sentenceSpan = document.createElement('span');
        sentenceSpan.classList.add('sentence');

        sentence.forEach(function(word, wIndex) {
            const wordSpan = document.createElement('span');
            wordSpan.classList.add('word');
            wordSpan.textContent = word;

            wordSpan.addEventListener('click', function(evt) {
                evt.stopPropagation();
                // Now we pass the entire sentenceSpan to onWordClick
                onWordClick(word, sentenceSpan);
            });

            sentenceSpan.appendChild(wordSpan);

            if (wIndex < sentence.length - 1) {
                sentenceSpan.appendChild(document.createTextNode(' '));
            }
        });

        p.appendChild(sentenceSpan);

        // add space between sentences
        if (sIndex < blockContent.length - 1) {
            p.appendChild(document.createTextNode(' '));
        }
    });

    return p;
}