const uploadContainer = document.getElementById('upload-container');
const fileInput = document.getElementById('fileInput');
const uploadStatus = document.getElementById('upload-status');

// When clicking on the container, trigger file input
uploadContainer.addEventListener('click', () => {
    fileInput.click();
});

// File input change -> handle file
fileInput.addEventListener('change', (event) => {
    const file = event.target.files[0];
    if (file) {
        uploadFile(file);
    }
});

// Drag & drop events
uploadContainer.addEventListener('dragover', (e) => {
    e.preventDefault();
    e.stopPropagation();
    uploadContainer.classList.add('dragover');
});

uploadContainer.addEventListener('dragleave', (e) => {
    e.preventDefault();
    e.stopPropagation();
    uploadContainer.classList.remove('dragover');
});

uploadContainer.addEventListener('drop', (e) => {
    e.preventDefault();
    e.stopPropagation();
    uploadContainer.classList.remove('dragover');

    const file = e.dataTransfer.files[0];
    if (file) {
        uploadFile(file);
    }
});

function uploadFile(file) {
    // Basic check
    if (!file.name.endsWith('.epub')) {
        uploadStatus.innerText = 'Please upload an EPUB file.';
        return;
    }

    // Prepare FormData
    const formData = new FormData();
    formData.append('file', file);

    // AJAX request
    $.ajax({
        url: '/api/epub/upload',
        type: 'POST',
        data: formData,
        contentType: false,
        processData: false,  // jQuery not to transform data
        success: function(response) {
            uploadStatus.innerText = 'File uploaded and processed successfully.';
        },
        error: function(xhr, status, error) {
            uploadStatus.innerText = 'Error uploading file: ' + xhr.responseText;
            console.error('Upload error:', error);
        }
    });
}