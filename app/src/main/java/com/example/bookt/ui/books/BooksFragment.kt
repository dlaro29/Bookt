package com.example.bookt.ui.books

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.bookt.R
import com.example.bookt.data.auth.AuthManager
import com.example.bookt.data.local.BookStorageManager
import com.example.bookt.data.model.Book
import com.example.bookt.data.remote.FirebaseBookStorageManager

class BooksFragment : Fragment() {

    private lateinit var storageManager: BookStorageManager
    private lateinit var authManager: AuthManager
    private lateinit var firebaseStorageManager: FirebaseBookStorageManager

    private var readingBooks by mutableStateOf<List<Book>>(emptyList())
    private var isLoading by mutableStateOf(false)
    private var errorMessage by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        storageManager = BookStorageManager(requireContext())
        authManager = AuthManager()
        firebaseStorageManager = FirebaseBookStorageManager()
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
                MaterialTheme {
                    BooksScreen(
                        books = readingBooks,
                        isLoading = isLoading,
                        errorMessage = errorMessage,
                        onBookClick = { selectedBook ->
                            openBookDetail(selectedBook)
                        }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadReadingBooks()
    }

    private fun loadReadingBooks() {
        isLoading = true
        errorMessage = null

        if (authManager.isUserLoggedIn()) {
            firebaseStorageManager.getBooksByStatus(
                statusField = "inReading",
                onSuccess = { books ->
                    if (!isAdded) return@getBooksByStatus

                    readingBooks = books
                    isLoading = false
                    errorMessage = null
                },
                onError = { message ->
                    if (!isAdded) return@getBooksByStatus

                    readingBooks = emptyList()
                    isLoading = false
                    errorMessage = message
                }
            )
        } else {
            readingBooks = storageManager.getReadingBooks()
            isLoading = false
            errorMessage = null
        }
    }

    private fun openBookDetail(book: Book) {
        val bundle = Bundle().apply {
            putSerializable("book", book)
            putBoolean("opened_from_reading", true)
        }

        findNavController().navigate(
            R.id.bookDetailFragment,
            bundle
        )
    }
}