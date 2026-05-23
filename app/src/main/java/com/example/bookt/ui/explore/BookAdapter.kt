package com.example.bookt.ui.explore

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bookt.data.model.Book
import com.example.bookt.databinding.ItemBookBinding

class BookAdapter(
    private var books: List<Book>,
    private val onBookClick: (Book) -> Unit
) : RecyclerView.Adapter<BookAdapter.BookViewHolder>() {

    inner class BookViewHolder(private val binding: ItemBookBinding) :
        RecyclerView.ViewHolder(binding.root) {
        //collega dati libro agli elementi grafici card
        fun bind(book: Book) {
            binding.tvBookTitle.text = book.title
            binding.tvBookAuthor.text = book.author
            binding.tvBookCategory.text =
                if (book.category.isNotBlank()) book.category else "Senza categoria"

            binding.tvBookRating.text =
                book.rating?.let { "★ $it" } ?: "Rating non disponibile"

            if (book.thumbnailUrl.isNotEmpty()) {
                Glide.with(binding.root.context)
                    .load(book.thumbnailUrl)
                    .into(binding.imgBookCover)
            } else {
                binding.imgBookCover.setImageResource(android.R.drawable.ic_menu_report_image)
            }

            binding.root.setOnClickListener {
                onBookClick(book)
            }
        }
    }
    //crea card grafica per ogni libro
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val binding = ItemBookBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return BookViewHolder(binding)
    }
    //inserisce dati libro nella card corrispondente
    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        holder.bind(books[position])
    }
    //restituisce numero di libri
    override fun getItemCount(): Int = books.size
    //aggiorna lista libri
    fun updateBooks(newBooks: List<Book>) {
        books = newBooks
        notifyDataSetChanged()
    }
}