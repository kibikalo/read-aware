/** A global cache for each word: { dictionary: ..., translation: ..., context: ... } */
const translationCache = {};

/** The currently clicked word */
let currentWord = null;

/**
 * Called by reader.js when a user clicks on any word.
 */
function onWordClick(word) {
    currentWord = word;

    // Create a new cache entry if we haven't seen this word before
    if (!translationCache[word]) {
        translationCache[word] = {
            dictionary: null,
            translation: null,
            context: null // for future
        };
    }

    // Show dictionary by default
    showTranslationPanel(word, 'dictionary');
}

/**
 * Decides which panel to show (dictionary, translation, or context).
 * If data is missing, fetch from the server; otherwise show cached data.
 */
function showTranslationPanel(word, panelType) {
    // Clear the container
    $('#translation-container').empty();

    // Add the top buttons
    const btnContainer = $('<div>').addClass('translation-buttons');

    const dictBtn = $('<button>').text('Dictionary').on('click', () => {
        showTranslationPanel(word, 'dictionary');
    });
    const transBtn = $('<button>').text('Translation').on('click', () => {
        showTranslationPanel(word, 'translation');
    });
    const ctxBtn = $('<button>').text('Context').on('click', () => {
        showTranslationPanel(word, 'context');
    });

    btnContainer.append(dictBtn, transBtn, ctxBtn);
    $('#translation-container').append(btnContainer);

    // Decide which panel to show
    if (panelType === 'dictionary') {
        if (translationCache[word].dictionary) {
            renderDictionaryResult(translationCache[word].dictionary);
        } else {
            fetchDictionary(word);
        }
    } else if (panelType === 'translation') {
        if (translationCache[word].translation) {
            renderTranslationResult(word, translationCache[word].translation);
        } else {
            fetchTranslation(word);
        }
    } else if (panelType === 'context') {
        // For future usage: context-based translation, etc.
        if (translationCache[word].context) {
            renderContextResult(translationCache[word].context);
        } else {
            $('#translation-container').append('<p>Context feature coming soon...</p>');
        }
    }
}

/**
 * Fetch dictionary data for the given word, store in cache, display.
 */
function fetchDictionary(word) {
    $('#translation-container').append('<p>Loading dictionary...</p>');

    $.ajax({
        url: '/api/dictionary/lookup',
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify({ word: word }),
        success: function(response) {
            translationCache[word].dictionary = response;
            renderDictionaryResult(response);
        },
        error: function(xhr, status, error) {
            console.error('Dictionary lookup error:', error);
            $('#translation-container').append('<p>Error fetching dictionary data.</p>');
        }
    });
}

/**
 * Display dictionary results inside #translation-container (while leaving the top buttons).
 */
function renderDictionaryResult(response) {
    // Remove old "Loading dictionary..." text
    $('#translation-container').find('p:contains("Loading dictionary")').remove();

    if (!response || response.length === 0) {
        $('#translation-container').append('<p>No definition found.</p>');
        return;
    }

    const entry = response[0];
    const wordTitle = $('<h3>').text(entry.word);

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

    $('#translation-container').append(wordTitle, meaningContainer);
}

/**
 * Fetch translation for the given word, store in cache, and display it.
 */
function fetchTranslation(word) {
    $('#translation-container').append('<p>Loading translation...</p>');

    $.ajax({
        url: '/api/translation',  // Updated endpoint
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify({ word: word }),
        success: function(res) {
            translationCache[word].translation = res;
            renderTranslationResult(word, res);
        },
        error: function(xhr, status, error) {
            console.error('Translation error:', error);
            $('#translation-container').append('<p>Error during translation.</p>');
        }
    });
}

/**
 * Display translation (renamed from "advanced translation").
 */
function renderTranslationResult(word, translationResponse) {
    // Remove loading text
    $('#translation-container').find('p:contains("Loading translation")').remove();

    // Example structure: { original: "and", translated: "і" }
    const translationDiv = $('<div>').addClass('translation-result');
    translationDiv.html(`
        <h3>Translation</h3>
        <p><strong>Original:</strong> ${translationResponse.original}</p>
        <p><strong>Ukrainian:</strong> ${translationResponse.translated}</p>
    `);

    $('#translation-container').append(translationDiv);
}

/**
 * Placeholder for context-based feature.
 */
function renderContextResult(contextData) {
    // ...
}