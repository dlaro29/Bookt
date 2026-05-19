package com.example.bookt.data.remote

import com.example.bookt.data.model.Book
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FirebaseBookStorageManager {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private fun userBooksCollection() =
        auth.currentUser?.uid?.let { uid ->
            db.collection("users")
                .document(uid)
                .collection("books")
        }

    //salva lo stato dei libri dell’utente: da leggere, preferiti o letti
    fun saveBookStatus(
        book: Book,
        inReading: Boolean? = null,
        inFavorites: Boolean? = null,
        inRead: Boolean? = null,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val collection = userBooksCollection()
        if (collection == null) {
            onError("Utente non autenticato")
            return
        }

        val bookData = mutableMapOf<String, Any?>(
            "id" to book.id,
            "title" to book.title,
            "author" to book.author,
            "category" to book.category,
            "thumbnailUrl" to book.thumbnailUrl,
            "description" to book.description,
            "rating" to book.rating,
            "publisher" to book.publisher,
            "publishedDate" to book.publishedDate,
            "pageCount" to book.pageCount
        )

        inReading?.let { bookData["inReading"] = it }
        inFavorites?.let { bookData["inFavorites"] = it }
        inRead?.let { bookData["inRead"] = it }

        collection.document(book.id)
            .set(bookData, com.google.firebase.firestore.SetOptions.merge())
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { exception ->
                onError(exception.message ?: "Errore durante il salvataggio")
            }
    }

    //recupero
    fun getBooksByStatus(
        statusField: String,
        onSuccess: (List<Book>) -> Unit,
        onError: (String) -> Unit
    ) {
        val collection = userBooksCollection()
        if (collection == null) {
            onError("Utente non autenticato")
            return
        }

        collection.whereEqualTo(statusField, true)
            .get()
            .addOnSuccessListener { snapshot ->
                val books = snapshot.documents.mapNotNull { doc ->
                    val id = doc.getString("id") ?: return@mapNotNull null
                    val title = doc.getString("title") ?: return@mapNotNull null
                    val author = doc.getString("author") ?: ""
                    val category = doc.getString("category") ?: ""
                    val thumbnailUrl = doc.getString("thumbnailUrl") ?: ""
                    val description = doc.getString("description") ?: ""
                    val rating = doc.getDouble("rating")
                    val publisher = doc.getString("publisher") ?: ""
                    val publishedDate = doc.getString("publishedDate") ?: ""
                    val pageCount = doc.getLong("pageCount")?.toInt()

                    Book(
                        id = id,
                        title = title,
                        author = author,
                        category = category,
                        thumbnailUrl = thumbnailUrl,
                        description = description,
                        rating = rating,
                        publisher = publisher,
                        publishedDate = publishedDate,
                        pageCount = pageCount
                    )
                }

                onSuccess(books)
            }
            .addOnFailureListener { exception ->
                onError(exception.message ?: "Errore durante il caricamento")
            }
    }

    //controllo
    fun getBookStatus(
        bookId: String,
        onSuccess: (BookStatus) -> Unit,
        onError: (String) -> Unit
    ) {
        val collection = userBooksCollection()
        if (collection == null) {
            onError("Utente non autenticato")
            return
        }

        collection.document(bookId)
            .get()
            .addOnSuccessListener { doc ->
                val status = BookStatus(
                    inReading = doc.getBoolean("inReading") ?: false,
                    inFavorites = doc.getBoolean("inFavorites") ?: false,
                    inRead = doc.getBoolean("inRead") ?: false
                )
                onSuccess(status)
            }
            .addOnFailureListener { exception ->
                onError(exception.message ?: "Errore durante il controllo dello stato")
            }
    }
}

data class BookStatus(
    val inReading: Boolean = false,
    val inFavorites: Boolean = false,
    val inRead: Boolean = false
)
