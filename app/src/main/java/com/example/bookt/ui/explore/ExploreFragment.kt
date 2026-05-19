package com.example.bookt.ui.explore

import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bookt.R
import com.example.bookt.data.model.Book
import com.example.bookt.data.repository.BookRepository
import com.example.bookt.data.repository.BookResult
import com.example.bookt.databinding.FragmentExploreBinding
import kotlinx.coroutines.launch

class ExploreFragment : Fragment() {

    private var _binding: FragmentExploreBinding? = null
    private val binding get() = _binding!!

    private lateinit var bookAdapter: BookAdapter
    private lateinit var homeBookAdapter: HomeBookAdapter

    private val repository = BookRepository()

    private var homeBooks: List<Book> = emptyList()
    private val genreCache = mutableMapOf<String, List<Book>>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExploreBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupResultsRecyclerView()
        setupHomeRecyclerView()
        setupSearch()
        setupCategoryCards()
        setupBackPressed()

        loadHomeBooks()
    }

    private fun setupResultsRecyclerView() {
        bookAdapter = BookAdapter(emptyList()) { selectedBook ->
            openBookDetail(selectedBook)
        }

        binding.rvBooks.layoutManager = LinearLayoutManager(requireContext())
        binding.rvBooks.adapter = bookAdapter
    }

    private fun setupHomeRecyclerView() {
        homeBookAdapter = HomeBookAdapter(emptyList()) { selectedBook ->
            openBookDetail(selectedBook)
        }

        binding.rvHomeBooks.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        binding.rvHomeBooks.adapter = homeBookAdapter
    }

    private fun setupSearch() {
        binding.searchInputLayout.setEndIconOnClickListener {
            performSearch()
        }

        binding.etSearch.setOnEditorActionListener { _, actionId, event ->
            val isSearchAction = actionId == EditorInfo.IME_ACTION_SEARCH
            val isEnterPressed =
                event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN

            if (isSearchAction || isEnterPressed) {
                performSearch()
                true
            } else {
                false
            }
        }
    }

    private fun setupCategoryCards() {
        binding.cardFantasy.setOnClickListener {
            binding.etSearch.setText("Fantasy")
            loadBooksByCategoryQuery("subject:fantasy", "Fantasy")
        }

        binding.cardRomance.setOnClickListener {
            binding.etSearch.setText("Romance")
            loadBooksByCategoryQuery("subject:romance", "Romance")
        }

        binding.cardThriller.setOnClickListener {
            binding.etSearch.setText("Thriller")
            loadBooksByCategoryQuery("subject:thriller", "Thriller")
        }

        binding.cardManga.setOnClickListener {
            binding.etSearch.setText("Manga")
            loadBooksByCategoryQuery("subject:manga", "Manga")
        }
    }

    private fun setupBackPressed() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val isInResultsPage =
                        binding.rvBooks.visibility == View.VISIBLE ||
                                binding.tvStatusMessage.visibility == View.VISIBLE ||
                                binding.tvSectionTitle.visibility == View.VISIBLE

                    if (isInResultsPage) {
                        returnToHome()
                    } else {
                        requireActivity().finish()
                    }
                }
            }
        )
    }

    private fun performSearch() {
        val query = binding.etSearch.text?.toString()?.trim().orEmpty()

        if (query.isBlank()) {
            Toast.makeText(
                requireContext(),
                "Inserisci qualcosa da cercare",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        loadBooks(query)
    }

    private fun loadHomeBooks() {
        viewLifecycleOwner.lifecycleScope.launch {
            showHomeLoading()

            when (val result = repository.loadHomeBooks()) {
                is BookResult.Success -> {
                    homeBooks = result.books

                    if (homeBooks.isEmpty()) {
                        showHomeMessage(
                            title = "Nessun risultato",
                            message = "Non sono riuscito a caricare i libri iniziali"
                        )
                    } else {
                        showHomeContent(homeBooks)
                    }
                }

                is BookResult.Error -> {
                    showHomeMessage(
                        title = "Errore",
                        message = result.message
                    )
                }
            }
        }
    }

    private fun loadBooks(query: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            showResultsLoading("Caricamento...")

            when (val result = repository.searchBooks(query)) {
                is BookResult.Success -> {
                    val books = result.books

                    if (books.isEmpty()) {
                        showMessage(
                            title = "Nessun risultato",
                            message = "Non ho trovato libri per \"$query\""
                        )
                    } else {
                        showResults(
                            books = books,
                            title = "Risultati per \"$query\""
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

    private fun loadBooksByCategoryQuery(query: String, label: String) {
        val cachedBooks = genreCache[label]

        if (!cachedBooks.isNullOrEmpty()) {
            showResults(
                books = cachedBooks,
                title = label
            )
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            showResultsLoading(label)

            when (val result = repository.searchBooksForChip(query, label)) {
                is BookResult.Success -> {
                    val books = result.books

                    if (books.isEmpty()) {
                        showMessage(
                            title = "Nessun risultato",
                            message = "Non ho trovato libri validi per $label"
                        )
                    } else {
                        genreCache[label] = books

                        showResults(
                            books = books,
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

    private fun showHomeLoading() {
        binding.progressBarBooks.visibility = View.VISIBLE
        binding.homeScrollView.visibility = View.GONE
        binding.rvBooks.visibility = View.GONE
        binding.tvSectionTitle.visibility = View.GONE
        binding.tvStatusMessage.visibility = View.GONE
    }

    private fun showHomeContent(books: List<Book>) {
        binding.progressBarBooks.visibility = View.GONE
        binding.homeScrollView.visibility = View.VISIBLE
        binding.rvBooks.visibility = View.GONE
        binding.tvSectionTitle.visibility = View.GONE
        binding.tvStatusMessage.visibility = View.GONE

        homeBookAdapter.updateBooks(books.take(10))
    }

    private fun showResultsLoading(title: String) {
        binding.progressBarBooks.visibility = View.VISIBLE
        binding.homeScrollView.visibility = View.GONE
        binding.rvBooks.visibility = View.GONE
        binding.tvStatusMessage.visibility = View.GONE
        binding.tvSectionTitle.visibility = View.VISIBLE
        binding.tvSectionTitle.text = title
    }

    private fun showResults(books: List<Book>, title: String) {
        binding.progressBarBooks.visibility = View.GONE
        binding.homeScrollView.visibility = View.GONE
        binding.tvStatusMessage.visibility = View.GONE
        binding.rvBooks.visibility = View.VISIBLE
        binding.tvSectionTitle.visibility = View.VISIBLE
        binding.tvSectionTitle.text = title

        bookAdapter.updateBooks(books)
    }

    private fun showMessage(title: String, message: String) {
        binding.progressBarBooks.visibility = View.GONE
        binding.homeScrollView.visibility = View.GONE
        binding.rvBooks.visibility = View.GONE
        binding.tvStatusMessage.visibility = View.VISIBLE
        binding.tvSectionTitle.visibility = View.VISIBLE
        binding.tvSectionTitle.text = title
        binding.tvStatusMessage.text = message

        bookAdapter.updateBooks(emptyList())
    }

    private fun showHomeMessage(title: String, message: String) {
        binding.progressBarBooks.visibility = View.GONE
        binding.homeScrollView.visibility = View.GONE
        binding.rvBooks.visibility = View.GONE
        binding.tvStatusMessage.visibility = View.VISIBLE
        binding.tvSectionTitle.visibility = View.VISIBLE
        binding.tvSectionTitle.text = title
        binding.tvStatusMessage.text = message

        bookAdapter.updateBooks(emptyList())
    }

    private fun returnToHome() {
        binding.etSearch.setText("")
        bookAdapter.updateBooks(emptyList())

        if (homeBooks.isNotEmpty()) {
            showHomeContent(homeBooks)
        } else {
            loadHomeBooks()
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}