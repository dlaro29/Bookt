package com.example.bookt.ui.books

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bookt.data.model.Book
import com.example.bookt.databinding.ItemSavedBookBinding

class ReadingBookAdapter(
    private var books: List<Book>,
    private val onBookClick: (Book) -> Unit
) : RecyclerView.Adapter<ReadingBookAdapter.ReadingBookViewHolder>() {
    inner class ReadingBookViewHolder(private val binding: ItemSavedBookBinding) :
        RecyclerView.ViewHolder(binding.root) {
        //collega i dati dei libri da leggere
        fun bind(book: Book) {
            binding.tvSavedBookTitle.text = book.title
            binding.tvSavedBookAuthor.text =
                if (book.author.isNotBlank()) book.author else "Autore sconosciuto"
            binding.tvSavedBookCategory.text =
                if (book.category.isNotBlank()) book.category else "Senza categoria"

            if (book.thumbnailUrl.isNotEmpty()) {
                Glide.with(binding.root.context)
                    .load(book.thumbnailUrl)
                    .into(binding.imgSavedBookCover)
            } else {
                binding.imgSavedBookCover.setImageResource(android.R.drawable.ic_menu_report_image)
            }
            binding.root.setOnClickListener {
                onBookClick(book)
            }
        }
    }
    //crea card grafica per ogni libro da leggere
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReadingBookViewHolder {
        val binding = ItemSavedBookBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ReadingBookViewHolder(binding)
    }
    //inserisce dati libro nella card corrispondente
    override fun onBindViewHolder(holder: ReadingBookViewHolder, position: Int) {
        holder.bind(books[position])
    }
    //restituisce numero di libri da leggere
    override fun getItemCount(): Int = books.size
    //aggiorna lista libri da leggere
    fun updateBooks(newBooks: List<Book>) {
        books = newBooks
        notifyDataSetChanged()
    }
}