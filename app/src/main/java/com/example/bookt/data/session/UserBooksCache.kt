package com.example.bookt.data.session

import com.example.bookt.data.model.Book

//cache libri utente
object UserBooksCache {
    var readingBooks: List<Book> = emptyList()
        private set
    var favoriteBooks: List<Book> = emptyList()
        private set
    var readBooks: List<Book> = emptyList()
        private set
    var readingLoaded: Boolean = false
        private set
    var favoritesLoaded: Boolean = false
        private set
    var readLoaded: Boolean = false
        private set

    fun setReadingBooks(books: List<Book>) {
        readingBooks = books
        readingLoaded = true
    }

    fun setFavoriteBooks(books: List<Book>) {
        favoriteBooks = books
        favoritesLoaded = true
    }

    fun setReadBooks(books: List<Book>) {
        readBooks = books
        readLoaded = true
    }

    fun addToReading(book: Book) {
        readingBooks = (readingBooks + book).distinctBy { it.id }
        readingLoaded = true

        readBooks = readBooks.filter { it.id != book.id }
        readLoaded = true
    }

    fun removeFromReading(bookId: String) {
        readingBooks = readingBooks.filter { it.id != bookId }
        readingLoaded = true
    }

    fun addToFavorites(book: Book) {
        favoriteBooks = (favoriteBooks + book).distinctBy { it.id }
        favoritesLoaded = true
    }

    fun removeFromFavorites(bookId: String) {
        favoriteBooks = favoriteBooks.filter { it.id != bookId }
        favoritesLoaded = true
    }

    fun addToRead(book: Book) {
        readBooks = (readBooks + book).distinctBy { it.id }
        readLoaded = true

        readingBooks = readingBooks.filter { it.id != book.id }
        readingLoaded = true
    }

    fun removeFromRead(bookId: String) {
        readBooks = readBooks.filter { it.id != bookId }
        readLoaded = true
    }

    fun clear() {
        readingBooks = emptyList()
        favoriteBooks = emptyList()
        readBooks = emptyList()

        readingLoaded = false
        favoritesLoaded = false
        readLoaded = false
    }
}