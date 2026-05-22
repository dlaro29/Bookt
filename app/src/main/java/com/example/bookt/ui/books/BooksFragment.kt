package com.example.bookt.ui.books

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
import com.example.bookt.databinding.FragmentBooksBinding

class BooksFragment : Fragment() {

    private var _binding: FragmentBooksBinding? = null
    private val binding get() = _binding!!

    private lateinit var readingBookAdapter: ReadingBookAdapter
    private lateinit var storageManager: BookStorageManager
    private lateinit var authManager: AuthManager
    private lateinit var firebaseStorageManager: FirebaseBookStorageManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBooksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        storageManager = BookStorageManager(requireContext())
        authManager = AuthManager()
        firebaseStorageManager = FirebaseBookStorageManager()

        setupRecyclerView()
        loadReadingBooks()
    }

    override fun onResume() {
        super.onResume()

        if (::authManager.isInitialized && ::readingBookAdapter.isInitialized) {
            loadReadingBooks()
        }
    }

    private fun setupRecyclerView() {
        readingBookAdapter = ReadingBookAdapter(
            emptyList(),
            onBookClick = { selectedBook ->
                openBookDetail(selectedBook)
            },
            onRemoveClick = { selectedBook ->
                removeReadingBook(selectedBook)
            }
        )

        binding.rvReadingBooks.layoutManager =
            androidx.recyclerview.widget.GridLayoutManager(requireContext(), 3)

        binding.rvReadingBooks.adapter = readingBookAdapter
    }

    private fun loadReadingBooks() {
        showReadingLoading()

        if (authManager.isUserLoggedIn()) {
            firebaseStorageManager.getBooksByStatus(
                statusField = "inReading",
                onSuccess = { readingBooks ->
                    if (_binding != null) {
                        updateReadingUi(readingBooks)
                    }
                },
                onError = { message ->
                    if (isAdded && _binding != null) {
                        showReadingError(message)
                    }
                }
            )
        } else {
            val readingBooks = storageManager.getReadingBooks()
            updateReadingUi(readingBooks)
        }
    }

    private fun removeReadingBook(selectedBook: Book) {
        if (authManager.isUserLoggedIn()) {
            showLoading()

            firebaseStorageManager.saveBookStatus(
                book = selectedBook,
                inReading = false,
                onSuccess = {
                    if (_binding != null && isAdded) {
                        Toast.makeText(
                            requireContext(),
                            "Rimosso da Da leggere",
                            Toast.LENGTH_SHORT
                        ).show()
                        loadReadingBooks()
                    }
                },
                onError = { message ->
                    if (_binding != null && isAdded) {
                        showError(message)
                    }
                }
            )
        } else {
            storageManager.removeFromReading(selectedBook.id)
            Toast.makeText(
                requireContext(),
                "Rimosso da Da leggere",
                Toast.LENGTH_SHORT
            ).show()
            loadReadingBooks()
        }
    }

    private fun showReadingLoading() {
        val safeBinding = _binding ?: return

        safeBinding.progressBarReading.visibility = View.VISIBLE
        safeBinding.tvReadingStatus.visibility = View.GONE
        safeBinding.tvEmptyReading.visibility = View.GONE
        safeBinding.rvReadingBooks.visibility = View.GONE
    }

    private fun showReadingError(message: String) {
        val safeBinding = _binding ?: return

        safeBinding.progressBarReading.visibility = View.GONE
        safeBinding.tvReadingStatus.visibility = View.VISIBLE
        safeBinding.tvReadingStatus.text = message
        safeBinding.tvEmptyReading.visibility = View.GONE
        safeBinding.rvReadingBooks.visibility = View.GONE
        readingBookAdapter.updateBooks(emptyList())
    }
    private fun showLoading() {
        val safeBinding = _binding ?: return

        safeBinding.progressBarReading.visibility = View.VISIBLE
        safeBinding.tvReadingStatus.visibility = View.VISIBLE
        safeBinding.tvReadingStatus.text = "Caricamento libri..."
        safeBinding.tvEmptyReading.visibility = View.GONE
        safeBinding.rvReadingBooks.visibility = View.GONE
    }

    private fun showError(message: String) {
        val safeBinding = _binding ?: return

        safeBinding.progressBarReading.visibility = View.GONE
        safeBinding.rvReadingBooks.visibility = View.GONE
        safeBinding.tvEmptyReading.visibility = View.GONE
        safeBinding.tvReadingStatus.visibility = View.VISIBLE
        safeBinding.tvReadingStatus.text = message
        readingBookAdapter.updateBooks(emptyList())
    }

    private fun updateReadingUi(readingBooks: List<Book>) {
        val safeBinding = _binding ?: return

        safeBinding.progressBarReading.visibility = View.GONE
        safeBinding.tvReadingStatus.visibility = View.GONE

        if (readingBooks.isEmpty()) {
            safeBinding.tvEmptyReading.visibility = View.VISIBLE
            safeBinding.rvReadingBooks.visibility = View.GONE
            readingBookAdapter.updateBooks(emptyList())
        } else {
            safeBinding.tvEmptyReading.visibility = View.GONE
            safeBinding.rvReadingBooks.visibility = View.VISIBLE
            readingBookAdapter.updateBooks(readingBooks)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}