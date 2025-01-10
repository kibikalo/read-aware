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
 * (We'll rely on reader.js's 'renderChapter' to do the actual rendering.)
 */
function loadChapter(bookId, chapterId) {
    const chapterUrl = `/api/books/${bookId}/chapters/${chapterId}`;

    $.get(chapterUrl)
        .done(function(chapterJsonStr) {
            let chapterData;
            try {
                chapterData = JSON.parse(chapterJsonStr);
            } catch (e) {
                console.error('Error parsing chapter JSON:', e);
                $('#chapter-content').html('<p>Error parsing chapter content</p>');
                return;
            }

            // Use the renderChapter function from reader.js
            renderChapter(chapterData);
        })
        .fail(function(jqXHR, textStatus, errorThrown) {
            console.error('Failed to load chapter content:', textStatus, errorThrown);
            $('#chapter-content').html('<p>Error loading chapter content</p>');
        });
}
