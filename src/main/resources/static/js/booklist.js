let currentPage = 0;
const pageSize = 20;

$(document).ready(function() {
    loadBooks(currentPage);

    $('#prev-page').on('click', function() {
        if (currentPage > 0) {
            currentPage--;
            loadBooks(currentPage);
        }
    });

    $('#next-page').on('click', function() {
        currentPage++;
        loadBooks(currentPage);
    });
});

function loadBooks(page) {
    const url = `/api/books/paged?page=${page}&size=${pageSize}`;

    $.getJSON(url)
        .done(function(pageData) {
            // Clear the grid
            $('#books-grid').empty();

            const books = pageData.content;
            if (books.length === 0) {
                $('#books-grid').append('<p>No books found.</p>');
            } else {
                books.forEach(function(book) {
                    // Create the card
                    const bookCard = $('<div>').addClass('book-item');

                    // Cover image
                    const coverImg = $('<img>')
                        .addClass('cover-image')
                        .attr('alt', 'Book Cover')
                        .attr('src', `/api/books/${book.id}/cover`)
                        .on('error', function() {
                            // Optional fallback if cover not found
                            $(this).attr('src', '/images/no-cover.png');
                        });

                    // Title
                    const title = $('<div>')
                        .addClass('book-title')
                        .text(book.title);

                    // Author
                    const author = $('<div>')
                        .addClass('book-author')
                        .text(book.author ? `by ${book.author}` : 'Author unknown');

                    // Description (truncate if too large)
                    const truncatedDesc = book.description || 'No description available';
                    const description = $('<div>')
                        .addClass('book-description')
                        .text(truncatedDesc);

                    // "Read" link
                    const readLink = $('<a>')
                        .addClass('read-link')
                        .attr('href', `/read?bookId=${book.id}`)
                        .text('Read this book');

                    // Append them in order
                    bookCard.append(coverImg, title, author, description, readLink);
                    $('#books-grid').append(bookCard);
                });
            }

            // Update pagination
            const totalPages = pageData.totalPages;
            const currentPageNum = pageData.number;
            $('#page-info').text(`Page ${currentPageNum + 1} of ${totalPages}`);

            // Prev/Next button states
            $('#prev-page').prop('disabled', currentPageNum <= 0);
            $('#next-page').prop('disabled', currentPageNum >= totalPages - 1);
        })
        .fail(function(jqXHR, textStatus, errorThrown) {
            console.error('Failed to load books:', textStatus, errorThrown);
            $('#books-grid').html('<p>Error loading books</p>');
        });
}