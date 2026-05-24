package com.example.bookt.ui.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.example.bookt.ui.theme.BooktTheme

class SavedBooksListFragment : Fragment() {

    private lateinit var storageManager: BookStorageManager
    private lateinit var authManager: AuthManager
    private lateinit var firebaseStorageManager: FirebaseBookStorageManager

    private var books by mutableStateOf<List<Book>>(emptyList())
    private var isLoading by mutableStateOf(false)
    private var errorMessage by mutableStateOf<String?>(null)

    private val type: String
        get() = arguments?.getString("type") ?: "favorites"

    private val screenTitle: String
        get() = arguments?.getString("title") ?: "Preferiti"

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
                BooktTheme {
                    SavedBooksListScreen(
                        title = screenTitle,
                        books = books,
                        isLoading = isLoading,
                        errorMessage = errorMessage,
                        type = type,
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
        loadBooks()
    }

    private fun loadBooks() {
        errorMessage = null

        val statusField = if (type == "favorites") {
            "inFavorites"
        } else {
            "inRead"
        }

        if (authManager.isUserLoggedIn()) {
            isLoading = true

            firebaseStorageManager.getBooksByStatus(
                statusField = statusField,
                onSuccess = { loadedBooks ->
                    if (!isAdded) return@getBooksByStatus

                    books = loadedBooks
                    isLoading = false
                },
                onError = { message ->
                    if (!isAdded) return@getBooksByStatus

                    books = emptyList()
                    errorMessage = message
                    isLoading = false
                }
            )
        } else {
            books = if (type == "favorites") {
                storageManager.getFavoriteBooks()
            } else {
                storageManager.getReadBooks()
            }

            isLoading = false
        }
    }

    private fun openBookDetail(book: Book) {
        val bundle = Bundle().apply {
            putSerializable("book", book)

            if (type == "favorites") {
                putBoolean("opened_from_favorites", true)
            } else {
                putBoolean("opened_from_read", true)
            }
        }

        findNavController().navigate(
            R.id.bookDetailFragment,
            bundle
        )
    }
}
