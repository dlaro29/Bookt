package com.example.bookt.ui.detail

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.bookt.data.auth.AuthManager
import com.example.bookt.data.local.BookStorageManager
import com.example.bookt.data.model.Book
import com.example.bookt.data.remote.BookStatus
import com.example.bookt.data.remote.FirebaseBookStorageManager
import com.example.bookt.databinding.FragmentBookDetailBinding

class BookDetailFragment : Fragment() {

    private var _binding: FragmentBookDetailBinding? = null
    private val binding get() = _binding!!

    private var book: Book? = null
    private lateinit var storageManager: BookStorageManager
    private lateinit var authManager: AuthManager
    private lateinit var firebaseStorageManager: FirebaseBookStorageManager

    private var cloudBookStatus = BookStatus()
    private var openedFromReading = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        storageManager = BookStorageManager(requireContext())
        authManager = AuthManager()
        firebaseStorageManager = FirebaseBookStorageManager()

        book = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getSerializable("book", Book::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getSerializable("book") as? Book
        }

        openedFromReading = arguments?.getBoolean("opened_from_reading", false) ?: false
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBookDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        showBookDetails()

        if (openedFromReading) {
            cloudBookStatus = cloudBookStatus.copy(
                inReading = true,
                inRead = false
            )
        }

        setupButtons()

        if (authManager.isUserLoggedIn()) {
            loadCloudBookStatus()
        } else {
            updateButtonsState()
        }
    }

    private fun loadCloudBookStatus() {
        val currentBook = book ?: return

        firebaseStorageManager.getBookStatus(
            bookId = currentBook.id,
            onSuccess = { status ->
                if (_binding == null || !isAdded) return@getBookStatus

                cloudBookStatus = status
                setupButtons()
                updateButtonsState()
            },
            onError = {
                if (_binding == null || !isAdded) return@getBookStatus

                setupButtons()
                updateButtonsState()
            }
        )
    }

    private fun showBookDetails() {
        val safeBinding = _binding ?: return
        val currentBook = book ?: return

        safeBinding.tvDetailTitle.text = currentBook.title
        safeBinding.tvDetailAuthor.text = currentBook.author
        safeBinding.tvDetailCategory.text = currentBook.category
        safeBinding.tvDetailRating.text =
            currentBook.rating?.let { "Rating: $it" } ?: "Rating non disponibile"

        safeBinding.tvDetailDescription.text =
            if (currentBook.description.isNotBlank()) {
                currentBook.description
            } else {
                "Descrizione non disponibile"
            }

        if (currentBook.thumbnailUrl.isNotEmpty()) {
            Glide.with(requireContext())
                .load(currentBook.thumbnailUrl)
                .into(safeBinding.imgDetailCover)
        } else {
            safeBinding.imgDetailCover.setImageResource(android.R.drawable.ic_menu_report_image)
        }

        safeBinding.tvDetailPublisher.text =
            if (currentBook.publisher.isNotBlank()) {
                "Editore: ${currentBook.publisher}"
            } else {
                "Editore non disponibile"
            }

        safeBinding.tvDetailPublishedDate.text =
            if (currentBook.publishedDate.isNotBlank()) {
                "Pubblicazione: ${currentBook.publishedDate}"
            } else {
                "Data di pubblicazione non disponibile"
            }

        safeBinding.tvDetailPageCount.text =
            currentBook.pageCount?.let { "Pagine: $it" } ?: "Numero pagine non disponibile"
    }

    private fun setupButtons() {
        val safeBinding = _binding ?: return
        val currentBook = book ?: return

        updateButtonsState()

        safeBinding.btnAddToReading.setOnClickListener {
            if (authManager.isUserLoggedIn()) {
                val newValue = !cloudBookStatus.inReading

                firebaseStorageManager.saveBookStatus(
                    book = currentBook,
                    inReading = newValue,
                    inRead = if (newValue) false else cloudBookStatus.inRead,
                    onSuccess = {
                        if (_binding == null || !isAdded) return@saveBookStatus

                        cloudBookStatus = cloudBookStatus.copy(
                            inReading = newValue,
                            inRead = if (newValue) false else cloudBookStatus.inRead
                        )

                        Toast.makeText(
                            requireContext(),
                            if (newValue) "Aggiunto a Da leggere" else "Rimosso da Da leggere",
                            Toast.LENGTH_SHORT
                        ).show()

                        updateButtonsState()
                    },
                    onError = { message ->
                        if (_binding == null || !isAdded) return@saveBookStatus
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                    }
                )
            } else {
                if (isInReading(currentBook.id)) {
                    storageManager.removeFromReading(currentBook.id)
                    openedFromReading = false
                    Toast.makeText(requireContext(), "Rimosso da Da leggere", Toast.LENGTH_SHORT).show()
                } else {
                    storageManager.addToReading(currentBook)
                    storageManager.removeFromRead(currentBook.id)
                    openedFromReading = true
                    Toast.makeText(requireContext(), "Aggiunto a Da leggere", Toast.LENGTH_SHORT).show()
                }

                updateButtonsState()
            }
        }

        safeBinding.btnAddToFavorites.setOnClickListener {
            if (authManager.isUserLoggedIn()) {
                val newValue = !cloudBookStatus.inFavorites

                firebaseStorageManager.saveBookStatus(
                    book = currentBook,
                    inFavorites = newValue,
                    onSuccess = {
                        if (_binding == null || !isAdded) return@saveBookStatus

                        cloudBookStatus = cloudBookStatus.copy(
                            inFavorites = newValue
                        )

                        Toast.makeText(
                            requireContext(),
                            if (newValue) "Aggiunto ai Preferiti" else "Rimosso dai Preferiti",
                            Toast.LENGTH_SHORT
                        ).show()

                        updateButtonsState()
                    },
                    onError = { message ->
                        if (_binding == null || !isAdded) return@saveBookStatus
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                    }
                )
            } else {
                if (isInFavorites(currentBook.id)) {
                    storageManager.removeFromFavorites(currentBook.id)
                    Toast.makeText(requireContext(), "Rimosso dai Preferiti", Toast.LENGTH_SHORT).show()
                } else {
                    storageManager.addToFavorites(currentBook)
                    Toast.makeText(requireContext(), "Aggiunto ai Preferiti", Toast.LENGTH_SHORT).show()
                }

                updateButtonsState()
            }
        }

        safeBinding.btnMarkAsRead.setOnClickListener {
            if (authManager.isUserLoggedIn()) {
                val newValue = !cloudBookStatus.inRead

                firebaseStorageManager.saveBookStatus(
                    book = currentBook,
                    inRead = newValue,
                    inReading = if (newValue) false else cloudBookStatus.inReading,
                    onSuccess = {
                        if (_binding == null || !isAdded) return@saveBookStatus

                        cloudBookStatus = cloudBookStatus.copy(
                            inRead = newValue,
                            inReading = if (newValue) false else cloudBookStatus.inReading
                        )

                        Toast.makeText(
                            requireContext(),
                            if (newValue) "Segnato come Letto" else "Rimosso dai Letti",
                            Toast.LENGTH_SHORT
                        ).show()

                        updateButtonsState()
                    },
                    onError = { message ->
                        if (_binding == null || !isAdded) return@saveBookStatus
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                    }
                )
            } else {
                if (isInRead(currentBook.id)) {
                    storageManager.removeFromRead(currentBook.id)
                    Toast.makeText(requireContext(), "Rimosso dai Letti", Toast.LENGTH_SHORT).show()
                } else {
                    storageManager.addToRead(currentBook)
                    storageManager.removeFromReading(currentBook.id)
                    openedFromReading = false
                    Toast.makeText(requireContext(), "Segnato come Letto", Toast.LENGTH_SHORT).show()
                }

                updateButtonsState()
            }
        }
    }

    private fun updateButtonsState() {
        val safeBinding = _binding ?: return
        val currentBook = book ?: return

        if (authManager.isUserLoggedIn()) {
            safeBinding.btnAddToReading.text =
                if (cloudBookStatus.inReading) "Rimuovi da Da leggere" else "Aggiungi a Da leggere"

            safeBinding.btnAddToFavorites.text =
                if (cloudBookStatus.inFavorites) "Rimuovi dai Preferiti" else "Aggiungi ai Preferiti"

            safeBinding.btnMarkAsRead.text =
                if (cloudBookStatus.inRead) "Rimuovi dai Letti" else "Segna come Letto"
        } else {
            safeBinding.btnAddToReading.text =
                if (isInReading(currentBook.id) || openedFromReading) {
                    "Rimuovi da Da leggere"
                } else {
                    "Aggiungi a Da leggere"
                }

            safeBinding.btnAddToFavorites.text =
                if (isInFavorites(currentBook.id)) "Rimuovi dai Preferiti" else "Aggiungi ai Preferiti"

            safeBinding.btnMarkAsRead.text =
                if (isInRead(currentBook.id)) "Rimuovi dai Letti" else "Segna come Letto"
        }
    }

    private fun isInReading(bookId: String): Boolean {
        return storageManager.getReadingBooks().any { it.id == bookId }
    }

    private fun isInFavorites(bookId: String): Boolean {
        return storageManager.getFavoriteBooks().any { it.id == bookId }
    }

    private fun isInRead(bookId: String): Boolean {
        return storageManager.getReadBooks().any { it.id == bookId }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}