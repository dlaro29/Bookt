package com.example.bookt.ui.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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

class AccountFragment : Fragment() {

    private lateinit var storageManager: BookStorageManager
    private lateinit var authManager: AuthManager
    private lateinit var firebaseStorageManager: FirebaseBookStorageManager

    private var email by mutableStateOf("")
    private var password by mutableStateOf("")
    private var userEmail by mutableStateOf<String?>(null)
    private var isLoggedIn by mutableStateOf(false)

    private var favoriteBooks by mutableStateOf<List<Book>>(emptyList())
    private var readBooks by mutableStateOf<List<Book>>(emptyList())

    private var isLoading by mutableStateOf(false)
    private var errorMessage by mutableStateOf<String?>(null)
    private var pendingCloudLoads = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        storageManager = BookStorageManager(requireContext())
        authManager = AuthManager()
        firebaseStorageManager = FirebaseBookStorageManager()

        updateAuthState()
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
                    AccountScreen(
                        isLoggedIn = isLoggedIn,
                        userEmail = userEmail,
                        email = email,
                        password = password,
                        favoriteBooks = favoriteBooks,
                        readBooks = readBooks,
                        isLoading = isLoading,
                        errorMessage = errorMessage,
                        onEmailChange = { email = it },
                        onPasswordChange = { password = it },
                        onLoginClick = { login() },
                        onRegisterClick = { register() },
                        onLogoutClick = { logout() },
                        onFavoriteBookClick = { selectedBook ->
                            openBookDetail(
                                book = selectedBook,
                                openedFromFavorites = true
                            )
                        },
                        onReadBookClick = { selectedBook ->
                            openBookDetail(
                                book = selectedBook,
                                openedFromRead = true
                            )
                        }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateAuthState()
        loadSavedBooks()
    }

    private fun updateAuthState() {
        val user = authManager.getCurrentUser()
        isLoggedIn = user != null
        userEmail = user?.email
    }

    private fun login() {
        val cleanEmail = email.trim()
        val cleanPassword = password.trim()

        if (cleanEmail.isBlank() || cleanPassword.isBlank()) {
            Toast.makeText(requireContext(), "Inserisci email e password", Toast.LENGTH_SHORT).show()
            return
        }

        isLoading = true
        errorMessage = null

        authManager.login(
            email = cleanEmail,
            password = cleanPassword,
            onSuccess = {
                if (!isAdded) return@login

                Toast.makeText(requireContext(), "Accesso eseguito", Toast.LENGTH_SHORT).show()
                password = ""
                updateAuthState()
                loadSavedBooks()
            },
            onError = { message ->
                if (!isAdded) return@login

                isLoading = false
                errorMessage = message
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun register() {
        val cleanEmail = email.trim()
        val cleanPassword = password.trim()

        if (cleanEmail.isBlank() || cleanPassword.isBlank()) {
            Toast.makeText(requireContext(), "Inserisci email e password", Toast.LENGTH_SHORT).show()
            return
        }

        if (cleanPassword.length < 7) {
            Toast.makeText(requireContext(), "La password deve avere almeno 7 caratteri", Toast.LENGTH_SHORT).show()
            return
        }

        isLoading = true
        errorMessage = null

        authManager.register(
            email = cleanEmail,
            password = cleanPassword,
            onSuccess = {
                if (!isAdded) return@register

                Toast.makeText(requireContext(), "Registrazione completata", Toast.LENGTH_SHORT).show()
                password = ""
                updateAuthState()
                loadSavedBooks()
            },
            onError = { message ->
                if (!isAdded) return@register

                isLoading = false
                errorMessage = message
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun logout() {
        authManager.logout()

        email = ""
        password = ""
        favoriteBooks = emptyList()
        readBooks = emptyList()
        errorMessage = null
        isLoading = false

        updateAuthState()
        loadSavedBooks()

        Toast.makeText(requireContext(), "Logout effettuato", Toast.LENGTH_SHORT).show()
    }

    private fun loadSavedBooks() {
        errorMessage = null

        if (authManager.isUserLoggedIn()) {
            loadCloudSavedBooks()
        } else {
            loadLocalSavedBooks()
        }
    }

    private fun loadLocalSavedBooks() {
        isLoading = false
        favoriteBooks = storageManager.getFavoriteBooks()
        readBooks = storageManager.getReadBooks()
    }

    private fun loadCloudSavedBooks() {
        isLoading = true
        pendingCloudLoads = 2

        firebaseStorageManager.getBooksByStatus(
            statusField = "inFavorites",
            onSuccess = { books ->
                if (!isAdded) return@getBooksByStatus

                favoriteBooks = books
                completeCloudLoad()
            },
            onError = { message ->
                if (!isAdded) return@getBooksByStatus

                errorMessage = message
                favoriteBooks = emptyList()
                completeCloudLoad()
            }
        )

        firebaseStorageManager.getBooksByStatus(
            statusField = "inRead",
            onSuccess = { books ->
                if (!isAdded) return@getBooksByStatus

                readBooks = books
                completeCloudLoad()
            },
            onError = { message ->
                if (!isAdded) return@getBooksByStatus

                errorMessage = message
                readBooks = emptyList()
                completeCloudLoad()
            }
        )
    }

    private fun completeCloudLoad() {
        pendingCloudLoads--

        if (pendingCloudLoads <= 0) {
            isLoading = false
        }
    }

    private fun openBookDetail(
        book: Book,
        openedFromFavorites: Boolean = false,
        openedFromRead: Boolean = false
    ) {
        val bundle = Bundle().apply {
            putSerializable("book", book)
            putBoolean("opened_from_favorites", openedFromFavorites)
            putBoolean("opened_from_read", openedFromRead)
        }

        findNavController().navigate(
            R.id.bookDetailFragment,
            bundle
        )
    }
}