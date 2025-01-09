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

            // Render each book
            const books = pageData.content;
            if (books.length === 0) {
                $('#books-container').append('<p>No books found.</p>');
                // Possibly disable next button
            } else {
                books.forEach(function(book) {
                    // Create a container or list item for each book
                    const bookDiv = $('<div>').addClass('book-item');

                    // Title + author
                    const title = $('<h2>').text(book.title);
                    const author = $('<p>').text('Author: ' + (book.author || 'Unknown'));

                    // Description (truncated or full)
                    const description = $('<p>').text(book.description || 'No description');

                    // "Read" link or button
                    // If your reading page is /read?bookId=, use that format:
                    const readLink = $('<a>')
                        .attr('href', `/read?bookId=${book.id}`)
                        .text('Read this book');

                    // Append elements to the bookDiv
                    bookDiv.append(title, author, description, readLink);

                    // Add it to the container
                    $('#books-container').append(bookDiv);
                });
            }

            // Update pagination display
            const totalPages = pageData.totalPages;
            const currentPageNum = pageData.number;
            $('#page-info').text(`Page ${currentPageNum + 1} of ${totalPages}`);

            // Handle boundary conditions for prev/next
            if (currentPageNum <= 0) {
                $('#prev-page').prop('disabled', true);
            } else {
                $('#prev-page').prop('disabled', false);
            }

            if (currentPageNum >= totalPages - 1) {
                $('#next-page').prop('disabled', true);
            } else {
                $('#next-page').prop('disabled', false);
            }
        })
        .fail(function(jqXHR, textStatus, errorThrown) {
            console.error('Failed to load books:', textStatus, errorThrown);
            $('#books-container').html('<p>Error loading books</p>');
        });
}