package com.example.bookt.ui.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.bookt.data.auth.AuthManager
import com.example.bookt.data.local.BookStorageManager
import com.example.bookt.data.model.Book
import com.example.bookt.data.remote.FirebaseBookStorageManager
import com.example.bookt.ui.theme.BooktTheme
class BookDetailFragment : Fragment() {

    private lateinit var book: Book
    private lateinit var storageManager: BookStorageManager
    private lateinit var authManager: AuthManager
    private lateinit var firebaseStorageManager: FirebaseBookStorageManager

    private var inReading by mutableStateOf(false)
    private var inFavorites by mutableStateOf(false)
    private var inRead by mutableStateOf(false)
    private var isLoadingStatus by mutableStateOf(false)

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val selectedBook = arguments?.getSerializable("book") as? Book

        if (selectedBook == null) {
            Toast.makeText(requireContext(), "Libro non disponibile", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
            return
        }

        book = selectedBook
        storageManager = BookStorageManager(requireContext())
        authManager = AuthManager()
        firebaseStorageManager = FirebaseBookStorageManager()
        inReading = arguments?.getBoolean("opened_from_reading") ?: false
        inFavorites = arguments?.getBoolean("opened_from_favorites") ?: false
        inRead = arguments?.getBoolean("opened_from_read") ?: false
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
            )

            setContent {
                BooktTheme {
                    BookDetailScreen(
                        book = book,
                        inReading = inReading,
                        inFavorites = inFavorites,
                        inRead = inRead,
                        isLoadingStatus = isLoadingStatus,
                        onReadingClick = {
                            toggleReading()
                        },
                        onFavoriteClick = {
                            toggleFavorite()
                        },
                        onReadClick = {
                            toggleRead()
                        }
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadBookStatus()
    }

    private fun loadBookStatus() {
        isLoadingStatus = false

        if (authManager.isUserLoggedIn()) {
            firebaseStorageManager.getBookStatus(
                bookId = book.id,
                onSuccess = { status ->
                    if (!isAdded) return@getBookStatus

                    inReading = status.inReading
                    inFavorites = status.inFavorites
                    inRead = status.inRead
                },
                onError = { message ->
                    if (!isAdded) return@getBookStatus

                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                }
            )
        } else {
            inReading = storageManager.getReadingBooks().any { it.id == book.id }
            inFavorites = storageManager.getFavoriteBooks().any { it.id == book.id }
            inRead = storageManager.getReadBooks().any { it.id == book.id }
        }
    }

    private fun toggleReading() {
        val oldReading = inReading
        val oldRead = inRead

        val newReadingStatus = !inReading

        // Aggiornamento immediato della UI
        inReading = newReadingStatus

        if (newReadingStatus) {
            inRead = false
        }

        if (authManager.isUserLoggedIn()) {
            firebaseStorageManager.saveBookStatus(
                book = book,
                inReading = newReadingStatus,
                inRead = if (newReadingStatus) false else null,
                onSuccess = {
                    if (!isAdded) return@saveBookStatus

                    val message = if (newReadingStatus) {
                        "Aggiunto a Da leggere"
                    } else {
                        "Rimosso da Da leggere"
                    }

                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                },
                onError = { message ->
                    if (!isAdded) return@saveBookStatus

                    // Rollback se Firebase fallisce
                    inReading = oldReading
                    inRead = oldRead

                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                }
            )
        } else {
            if (newReadingStatus) {
                storageManager.addToReading(book)
                storageManager.removeFromRead(book.id)
                Toast.makeText(requireContext(), "Aggiunto a Da leggere", Toast.LENGTH_SHORT).show()
            } else {
                storageManager.removeFromReading(book.id)
                Toast.makeText(requireContext(), "Rimosso da Da leggere", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun toggleFavorite() {
        val oldFavorite = inFavorites

        val newFavoriteStatus = !inFavorites

        // Aggiornamento immediato della UI
        inFavorites = newFavoriteStatus

        if (authManager.isUserLoggedIn()) {
            firebaseStorageManager.saveBookStatus(
                book = book,
                inFavorites = newFavoriteStatus,
                onSuccess = {
                    if (!isAdded) return@saveBookStatus

                    val message = if (newFavoriteStatus) {
                        "Aggiunto ai Preferiti"
                    } else {
                        "Rimosso dai Preferiti"
                    }

                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                },
                onError = { message ->
                    if (!isAdded) return@saveBookStatus

                    // Rollback se Firebase fallisce
                    inFavorites = oldFavorite

                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                }
            )
        } else {
            if (newFavoriteStatus) {
                storageManager.addToFavorites(book)
                Toast.makeText(requireContext(), "Aggiunto ai Preferiti", Toast.LENGTH_SHORT).show()
            } else {
                storageManager.removeFromFavorites(book.id)
                Toast.makeText(requireContext(), "Rimosso dai Preferiti", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun toggleRead() {
        val oldRead = inRead
        val oldReading = inReading

        val newReadStatus = !inRead

        // Aggiornamento immediato della UI
        inRead = newReadStatus

        if (newReadStatus) {
            inReading = false
        }

        if (authManager.isUserLoggedIn()) {
            firebaseStorageManager.saveBookStatus(
                book = book,
                inRead = newReadStatus,
                inReading = if (newReadStatus) false else null,
                onSuccess = {
                    if (!isAdded) return@saveBookStatus

                    val message = if (newReadStatus) {
                        "Segnato come letto"
                    } else {
                        "Rimosso dai Letti"
                    }

                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                },
                onError = { message ->
                    if (!isAdded) return@saveBookStatus

                    // Rollback se Firebase fallisce
                    inRead = oldRead
                    inReading = oldReading

                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                }
            )
        } else {
            if (newReadStatus) {
                storageManager.addToRead(book)
                storageManager.removeFromReading(book.id)
                Toast.makeText(requireContext(), "Segnato come letto", Toast.LENGTH_SHORT).show()
            } else {
                storageManager.removeFromRead(book.id)
                Toast.makeText(requireContext(), "Rimosso dai Letti", Toast.LENGTH_SHORT).show()
            }
        }
    }
}