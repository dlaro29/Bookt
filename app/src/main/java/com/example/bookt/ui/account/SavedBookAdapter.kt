package com.example.bookt.ui.account

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bookt.data.model.Book
import com.example.bookt.databinding.ItemSavedBookBinding
class SavedBookAdapter(
    private var books: List<Book>,
    private val onBookClick: (Book) -> Unit
) : RecyclerView.Adapter<SavedBookAdapter.SavedBookViewHolder>() {
    inner class SavedBookViewHolder(private val binding: ItemSavedBookBinding) :
        RecyclerView.ViewHolder(binding.root) {
            //collega i dati dei libri salvato
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

    //crea card grafica per ogni libro salvato
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SavedBookViewHolder {
        val binding = ItemSavedBookBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SavedBookViewHolder(binding)
    }
    //inserisce dati libro nella card corrispondente
    override fun onBindViewHolder(holder: SavedBookViewHolder, position: Int) {
        holder.bind(books[position])
    }
    //numero totale di libri salvati
    override fun getItemCount(): Int = books.size
    //aggiorna lista libri salvati
    fun updateBooks(newBooks: List<Book>) {
        books = newBooks
        notifyDataSetChanged()
    }
}