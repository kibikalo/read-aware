let currentPage = 0;
const pageSize = 20;

$(document).ready(function() {
    // Load the first page of books
    loadBooks(currentPage);

    // Set up click handlers for pagination
    $('#prev-page').on('click', function() {
        if (currentPage > 0) {
            currentPage--;
            loadBooks(currentPage);
        }
    });

    $('#next-page').on('click', function() {
        // We'll handle boundaries after we know total pages
        currentPage++;
        loadBooks(currentPage);
    });
});

/**
 * Fetch books for the given page from the server and render them.
 */
function loadBooks(page) {
    const url = `/api/books/paged?page=${page}&size=${pageSize}`;

    $.getJSON(url)
        .done(function(pageData) {
            // Clear the container
            $('#books-container').empty();

            const books = pageData.content;
            if (books.length === 0) {
                $('#books-container').append('<p>No books found.</p>');
            } else {
                books.forEach(function(book) {
                    const bookDiv = $('<div>').addClass('book-item');

                    // Title + author
                    const title = $('<h2>').text(book.title);
                    const author = $('<p>').text('Author: ' + (book.author || 'Unknown'));

                    // Description
                    const description = $('<p>').text(book.description || 'No description');

                    // "Read" link
                    const readLink = $('<a>')
                        .attr('href', `/read?bookId=${book.id}`)
                        .text('Read this book');

                    // If the book might have a cover, we can display it
                    // We'll just put the image at the top, or next to the title
                    const coverImg = $('<img>')
                        .addClass('cover-image')
                        .attr('alt', 'Book Cover')
                        .attr('src', `/api/books/${book.id}/cover`);
                    // This will 404 if there's no cover in the backend

                    // Append elements
                    // We'll put the cover at the top
                    bookDiv.append(coverImg, title, author, description, readLink);
                    $('#books-container').append(bookDiv);
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
            $('#books-container').html('<p>Error loading books</p>');
        });
}