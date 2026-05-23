package com.example.bookt.ui.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.bookt.R
import com.example.bookt.data.auth.AuthManager
import com.example.bookt.data.local.BookStorageManager
import com.example.bookt.data.model.Book
import com.example.bookt.data.remote.FirebaseBookStorageManager
import com.example.bookt.databinding.FragmentAccountBinding

class AccountFragment : Fragment() {

    private var _binding: FragmentAccountBinding? = null
    private val binding get() = _binding!!

    private lateinit var storageManager: BookStorageManager
    private lateinit var favoritesAdapter: SavedBookAdapter
    private lateinit var readAdapter: SavedBookAdapter
    private lateinit var authManager: AuthManager
    private lateinit var firebaseStorageManager: FirebaseBookStorageManager

    private var pendingCloudLoads = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        storageManager = BookStorageManager(requireContext())
        authManager = AuthManager()
        firebaseStorageManager = FirebaseBookStorageManager()

        setupAuthButtons()
        setupRecyclerViews()
        updateAuthUi()
        loadSavedBooks()
    }

    override fun onResume() {
        super.onResume()

        if (
            ::authManager.isInitialized &&
            ::favoritesAdapter.isInitialized &&
            ::readAdapter.isInitialized
        ) {
            loadSavedBooks()
        }
    }

    private fun setupAuthButtons() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text?.toString()?.trim().orEmpty()
            val password = binding.etPassword.text?.toString()?.trim().orEmpty()

            if (email.isBlank() || password.isBlank()) {
                Toast.makeText(requireContext(), "Inserisci email e password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            authManager.login(
                email = email,
                password = password,
                onSuccess = {
                    if (_binding != null && isAdded) {
                        Toast.makeText(requireContext(), "Accesso eseguito", Toast.LENGTH_SHORT).show()
                        updateAuthUi()
                        loadSavedBooks()
                    }
                },
                onError = { message ->
                    if (_binding != null && isAdded) {
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }

        binding.btnRegister.setOnClickListener {
            val email = binding.etEmail.text?.toString()?.trim().orEmpty()
            val password = binding.etPassword.text?.toString()?.trim().orEmpty()

            if (email.isBlank() || password.isBlank()) {
                Toast.makeText(requireContext(), "Inserisci email e password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 7) {
                Toast.makeText(requireContext(), "La password deve avere almeno 7 caratteri", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            authManager.register(
                email = email,
                password = password,
                onSuccess = {
                    if (_binding != null && isAdded) {
                        Toast.makeText(requireContext(), "Registrazione completata", Toast.LENGTH_SHORT).show()
                        updateAuthUi()
                        loadSavedBooks()
                    }
                },
                onError = { message ->
                    if (_binding != null && isAdded) {
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }

        binding.btnLogout.setOnClickListener {
            authManager.logout()
            Toast.makeText(requireContext(), "Logout effettuato", Toast.LENGTH_SHORT).show()
            updateAuthUi()
            loadSavedBooks()
        }
    }

    private fun updateAuthUi() {
        val user = authManager.getCurrentUser()

        if (user != null) {
            binding.tvAccountStatus.text = "Accesso effettuato come ${user.email}"
            binding.etEmail.visibility = View.GONE
            binding.etPassword.visibility = View.GONE
            binding.btnLogin.visibility = View.GONE
            binding.btnRegister.visibility = View.GONE
            binding.btnLogout.visibility = View.VISIBLE
        } else {
            binding.tvAccountStatus.text = "Accedi o registrati per salvare i tuoi libri nel cloud"
            binding.etEmail.visibility = View.VISIBLE
            binding.etPassword.visibility = View.VISIBLE
            binding.btnLogin.visibility = View.VISIBLE
            binding.btnRegister.visibility = View.VISIBLE
            binding.btnLogout.visibility = View.GONE
        }
    }

    private fun setupRecyclerViews() {
        favoritesAdapter = SavedBookAdapter(
            emptyList(),
            onBookClick = { selectedBook ->
                openBookDetail(
                    book = selectedBook,
                    openedFromFavorites = true
                )
            }
        )

        readAdapter = SavedBookAdapter(
            emptyList(),
            onBookClick = { selectedBook ->
                openBookDetail(
                    book = selectedBook,
                    openedFromRead = true
                )
            }
        )

        binding.rvFavoriteBooks.layoutManager =
            androidx.recyclerview.widget.LinearLayoutManager(
                requireContext(),
                androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL,
                false
            )
        binding.rvFavoriteBooks.adapter = favoritesAdapter

        binding.rvReadBooks.layoutManager =
            androidx.recyclerview.widget.LinearLayoutManager(
                requireContext(),
                androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL,
                false
            )
        binding.rvReadBooks.adapter = readAdapter
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

    private fun loadSavedBooks() {
        if (authManager.isUserLoggedIn()) {
            loadCloudSavedBooks()
        } else {
            loadLocalSavedBooks()
        }
    }

    private fun loadLocalSavedBooks() {
        hideAccountLoading()

        val favoriteBooks = storageManager.getFavoriteBooks()
        val readBooks = storageManager.getReadBooks()

        updateFavoritesUi(favoriteBooks)
        updateReadUi(readBooks)
    }

    private fun loadCloudSavedBooks() {
        showAccountLoading()
        pendingCloudLoads = 2

        firebaseStorageManager.getBooksByStatus(
            statusField = "inFavorites",
            onSuccess = { favoriteBooks ->
                if (_binding != null && isAdded) {
                    updateFavoritesUi(favoriteBooks)
                    completeCloudLoad()
                }
            },
            onError = { message ->
                if (_binding != null && isAdded) {
                    showAccountError(message)
                    completeCloudLoad()
                }
            }
        )

        firebaseStorageManager.getBooksByStatus(
            statusField = "inRead",
            onSuccess = { readBooks ->
                if (_binding != null && isAdded) {
                    updateReadUi(readBooks)
                    completeCloudLoad()
                }
            },
            onError = { message ->
                if (_binding != null && isAdded) {
                    showAccountError(message)
                    completeCloudLoad()
                }
            }
        )
    }

    private fun completeCloudLoad() {
        pendingCloudLoads--

        if (pendingCloudLoads <= 0) {
            hideAccountLoading()
        }
    }

    private fun updateFavoritesUi(favoriteBooks: List<Book>) {
        val safeBinding = _binding ?: return

        safeBinding.tvAccountLoadStatus.visibility = View.GONE

        if (favoriteBooks.isEmpty()) {
            safeBinding.tvEmptyFavorites.visibility = View.VISIBLE
            safeBinding.rvFavoriteBooks.visibility = View.GONE
            favoritesAdapter.updateBooks(emptyList())
        } else {
            safeBinding.tvEmptyFavorites.visibility = View.GONE
            safeBinding.rvFavoriteBooks.visibility = View.VISIBLE
            favoritesAdapter.updateBooks(favoriteBooks)
        }
    }

    private fun updateReadUi(readBooks: List<Book>) {
        val safeBinding = _binding ?: return

        safeBinding.tvAccountLoadStatus.visibility = View.GONE

        if (readBooks.isEmpty()) {
            safeBinding.tvEmptyRead.visibility = View.VISIBLE
            safeBinding.rvReadBooks.visibility = View.GONE
            readAdapter.updateBooks(emptyList())
        } else {
            safeBinding.tvEmptyRead.visibility = View.GONE
            safeBinding.rvReadBooks.visibility = View.VISIBLE
            readAdapter.updateBooks(readBooks)
        }
    }

    private fun showAccountLoading() {
        val safeBinding = _binding ?: return

        safeBinding.progressBarAccount.visibility = View.VISIBLE
        safeBinding.tvAccountLoadStatus.visibility = View.VISIBLE
        safeBinding.tvAccountLoadStatus.text = "Caricamento libreria..."

        safeBinding.rvFavoriteBooks.visibility = View.GONE
        safeBinding.rvReadBooks.visibility = View.GONE
        safeBinding.tvEmptyFavorites.visibility = View.GONE
        safeBinding.tvEmptyRead.visibility = View.GONE
    }

    private fun hideAccountLoading() {
        val safeBinding = _binding ?: return

        safeBinding.progressBarAccount.visibility = View.GONE
        safeBinding.tvAccountLoadStatus.visibility = View.GONE
    }

    private fun showAccountError(message: String) {
        val safeBinding = _binding ?: return

        safeBinding.progressBarAccount.visibility = View.GONE
        safeBinding.tvAccountLoadStatus.visibility = View.VISIBLE
        safeBinding.tvAccountLoadStatus.text = message
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}