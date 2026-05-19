package com.example.bookt.data.local

import android.content.Context
import com.example.bookt.data.model.Book
import org.json.JSONArray
import org.json.JSONObject

class BookStorageManager(context: Context) {
    private val prefs = context.getSharedPreferences("bookt_prefs", Context.MODE_PRIVATE)
    companion object {
        private const val KEY_READING = "reading_books"
        private const val KEY_FAVORITES = "favorite_books"
        private const val KEY_READ = "read_books"
    }

    //aggiunta
    fun addToReading(book: Book) {
        saveBook(KEY_READING, book)
    }

    fun addToFavorites(book: Book) {
        saveBook(KEY_FAVORITES, book)
    }

    fun addToRead(book: Book) {
        saveBook(KEY_READ, book)
    }

    //rimozione
    fun removeFromReading(bookId: String) {
        removeBook(KEY_READING, bookId)
    }

    fun removeFromFavorites(bookId: String) {
        removeBook(KEY_FAVORITES, bookId)
    }

    fun removeFromRead(bookId: String) {
        removeBook(KEY_READ, bookId)
    }

    //selezione
    fun getReadingBooks(): List<Book> = getBooks(KEY_READING)

    fun getFavoriteBooks(): List<Book> = getBooks(KEY_FAVORITES)

    fun getReadBooks(): List<Book> = getBooks(KEY_READ)

    //salvataggio
    private fun saveBook(key: String, book: Book) {
        val currentBooks = getBooks(key).toMutableList()

        val alreadyExists = currentBooks.any { it.id == book.id }
        if (!alreadyExists) {
            currentBooks.add(book)
            saveBooksList(key, currentBooks)
        }
    }

    private fun removeBook(key: String, bookId: String) {
        val updatedBooks = getBooks(key).filter { it.id != bookId }
        saveBooksList(key, updatedBooks)
    }

    private fun saveBooksList(key: String, books: List<Book>) {
        val jsonArray = JSONArray()

        books.forEach { book ->
            val jsonObject = JSONObject().apply {
                put("id", book.id)
                put("title", book.title)
                put("author", book.author)
                put("category", book.category)
                put("thumbnailUrl", book.thumbnailUrl)
                put("description", book.description)
                put("rating", book.rating ?: JSONObject.NULL)
                put("publisher", book.publisher)
                put("publishedDate", book.publishedDate)
                put("pageCount", book.pageCount ?: JSONObject.NULL)
            }
            jsonArray.put(jsonObject)
        }

        prefs.edit().putString(key, jsonArray.toString()).apply()
    }

    private fun getBooks(key: String): List<Book> {
        val jsonString = prefs.getString(key, null) ?: return emptyList()
        val jsonArray = JSONArray(jsonString)
        val books = mutableListOf<Book>()

        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)

            val rating = if (obj.isNull("rating")) null else obj.getDouble("rating")
            val pageCount = if (obj.isNull("pageCount")) null else obj.getInt("pageCount")
            books.add(
                Book(
                    id = obj.getString("id"),
                    title = obj.getString("title"),
                    author = obj.getString("author"),
                    category = obj.getString("category"),
                    thumbnailUrl = obj.getString("thumbnailUrl"),
                    description = obj.getString("description"),
                    rating = rating,
                    publisher =  obj.optString("publisher", ""),
                    publishedDate = obj.optString("publishedDate", ""),
                    pageCount = pageCount
                )
            )
        }

        return books
    }
}