package com.example.bookt.ui.explore

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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.bookt.R
import com.example.bookt.data.model.Book
import com.example.bookt.data.repository.BookRepository
import com.example.bookt.data.repository.BookResult
import kotlinx.coroutines.launch
import com.example.bookt.ui.theme.BooktTheme
import com.example.bookt.data.auth.AuthManager
import com.example.bookt.data.local.BookStorageManager
import com.example.bookt.data.remote.FirebaseBookStorageManager

class ExploreFragment : Fragment() {

    private val repository = BookRepository()

    private var books by mutableStateOf<List<Book>>(emptyList())
    private var query by mutableStateOf("")
    private var sectionTitle by mutableStateOf("Esplora")
    private var selectedChip by mutableStateOf("Tutti")
    private var isLoading by mutableStateOf(false)
    private var statusMessage by mutableStateOf<String?>(null)
    private lateinit var storageManager: BookStorageManager
    private lateinit var authManager: AuthManager
    private lateinit var firebaseStorageManager: FirebaseBookStorageManager
    private var recommendedBooksCache: List<Book> = emptyList()
    private var recommendedSourceIdsCache: Set<String> = emptySet()

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
                    ExploreScreen(
                        query = query,
                        sectionTitle = sectionTitle,
                        books = books,
                        isLoading = isLoading,
                        statusMessage = statusMessage,
                        selectedChip = selectedChip,
                        onQueryChange = { newQuery ->
                            query = newQuery
                        },
                        onSearchClick = {
                            performSearch()
                        },
                        onChipClick = { chip ->
                            handleChipClick(chip)
                        },
                        onBookClick = { selectedBook ->
                            openBookDetail(selectedBook)
                        }
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (books.isEmpty() && !isLoading) {
            loadDefaultBooks()
        }
    }
    private fun handleChipClick(chip: String) {
        selectedChip = chip

        when (chip) {
            "Tutti" -> {
                query = ""
                loadDefaultBooks()
            }

            "Consigliati" -> {
                query = ""
                loadRecommendedBooks()
            }

            "Fantasy" -> {
                query = "Fantasy"
                loadBooksByCategoryQuery("subject:fantasy", "Fantasy")
            }

            "Romanzi" -> {
                query = "Romanzi"
                loadBooksByCategoryQuery("subject:romance", "Romanzi")
            }

            "Thriller" -> {
                query = "Thriller"
                loadBooksByCategoryQuery("subject:thriller", "Thriller")
            }

            "Manga" -> {
                query = "Manga"
                loadBooksByCategoryQuery("subject:manga", "Manga")
            }
        }
    }

    private fun loadRecommendedBooks() {
        showLoading()
        sectionTitle = "CONSIGLIATI"

        if (authManager.isUserLoggedIn()) {
            val collectedBooks = mutableListOf<Book>()
            var pendingLoads = 2

            fun completeLoad() {
                pendingLoads--

                if (pendingLoads <= 0) {
                    loadRecommendedBooksFrom(collectedBooks)
                }
            }

            firebaseStorageManager.getBooksByStatus(
                statusField = "inFavorites",
                onSuccess = { books ->
                    if (!isAdded) return@getBooksByStatus

                    collectedBooks.addAll(books)
                    completeLoad()
                },
                onError = { message ->
                    if (!isAdded) return@getBooksByStatus

                    statusMessage = message
                    completeLoad()
                }
            )

            firebaseStorageManager.getBooksByStatus(
                statusField = "inRead",
                onSuccess = { books ->
                    if (!isAdded) return@getBooksByStatus

                    collectedBooks.addAll(books)
                    completeLoad()
                },
                onError = { message ->
                    if (!isAdded) return@getBooksByStatus

                    statusMessage = message
                    completeLoad()
                }
            )
        } else {
            val localBooks = storageManager.getFavoriteBooks() + storageManager.getReadBooks()
            loadRecommendedBooksFrom(localBooks)
        }
    }
    private fun loadRecommendedBooksFrom(userBooks: List<Book>) {
        val uniqueUserBooks = userBooks.distinctBy { it.id }
        val currentSourceIds = uniqueUserBooks.map { it.id }.toSet()

        if (
            recommendedBooksCache.isNotEmpty() &&
            recommendedSourceIdsCache == currentSourceIds
        ) {
            showBooks(
                newBooks = recommendedBooksCache,
                title = "CONSIGLIATI"
            )
            return
        }

        if (uniqueUserBooks.isEmpty()) {
            showMessage(
                title = "CONSIGLIATI",
                message = "Aggiungi libri ai preferiti o ai letti per ricevere consigli personalizzati."
            )
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            showLoading()
            sectionTitle = "CONSIGLIATI"

            when (val result = repository.recommendBooksFrom(uniqueUserBooks)) {
                is BookResult.Success -> {
                    val recommendedBooks = result.books

                    if (recommendedBooks.isEmpty()) {
                        showMessage(
                            title = "CONSIGLIATI",
                            message = "Non ho trovato consigli adatti ai tuoi libri."
                        )
                    } else {
                        recommendedBooksCache = recommendedBooks
                        recommendedSourceIdsCache = currentSourceIds

                        showBooks(
                            newBooks = recommendedBooks,
                            title = "CONSIGLIATI"
                        )
                    }
                }

                is BookResult.Error -> {
                    showMessage(
                        title = "CONSIGLIATI",
                        message = result.message
                    )
                }
            }
        }
    }
    private fun performSearch() {
        val cleanQuery = query.trim()

        if (cleanQuery.isBlank()) {
            Toast.makeText(requireContext(), "Inserisci qualcosa da cercare", Toast.LENGTH_SHORT).show()
            return
        }

        selectedChip = ""
        loadBooks(cleanQuery)
    }

    private fun showLoading() {
        isLoading = true
        statusMessage = null
        sectionTitle = "Caricamento..."
        books = emptyList()
    }

    private fun showBooks(newBooks: List<Book>, title: String) {
        isLoading = false
        statusMessage = null
        sectionTitle = title
        books = newBooks
    }

    private fun showMessage(title: String, message: String) {
        isLoading = false
        sectionTitle = title
        statusMessage = message
        books = emptyList()
    }

    private fun loadDefaultBooks() {
        viewLifecycleOwner.lifecycleScope.launch {
            showLoading()

            when (val result = repository.loadHomeBooks()) {
                is BookResult.Success -> {
                    val loadedBooks = result.books

                    if (loadedBooks.isEmpty()) {
                        showMessage(
                            title = "Nessun risultato",
                            message = "Non sono riuscito a caricare i libri iniziali"
                        )
                    } else {
                        showBooks(
                            newBooks = loadedBooks,
                            title = "Esplora"
                        )
                    }
                }

                is BookResult.Error -> {
                    showMessage(
                        title = "Errore",
                        message = result.message
                    )
                }
            }
        }
    }

    private fun loadBooks(searchQuery: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            showLoading()

            when (val result = repository.searchBooks(searchQuery)) {
                is BookResult.Success -> {
                    val loadedBooks = result.books

                    if (loadedBooks.isEmpty()) {
                        showMessage(
                            title = "Nessun risultato",
                            message = "Non ho trovato libri per \"$searchQuery\""
                        )
                    } else {
                        showBooks(
                            newBooks = loadedBooks,
                            title = "Risultati per \"$searchQuery\""
                        )
                    }
                }

                is BookResult.Error -> {
                    showMessage(
                        title = "Errore",
                        message = result.message
                    )
                }
            }
        }
    }

    private fun loadBooksByCategoryQuery(apiQuery: String, label: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            showLoading()

            when (val result = repository.searchBooksForChip(apiQuery, label)) {
                is BookResult.Success -> {
                    val loadedBooks = result.books

                    if (loadedBooks.isEmpty()) {
                        showMessage(
                            title = "Nessun risultato",
                            message = "Non ho trovato libri per $label"
                        )
                    } else {
                        showBooks(
                            newBooks = loadedBooks,
                            title = label
                        )
                    }
                }

                is BookResult.Error -> {
                    showMessage(
                        title = "Errore",
                        message = result.message
                    )
                }
            }
        }
    }

    private fun openBookDetail(book: Book) {
        val bundle = Bundle().apply {
            putSerializable("book", book)
        }

        findNavController().navigate(
            R.id.action_exploreFragment_to_bookDetailFragment,
            bundle
        )
    }
}