package com.example.bookt.ui.explore

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bookt.data.model.Book
import com.example.bookt.databinding.ItemHomeBookBinding

class HomeBookAdapter(
    private var books: List<Book>,
    private val onBookClick: (Book) -> Unit
) : RecyclerView.Adapter<HomeBookAdapter.HomeBookViewHolder>() {

    inner class HomeBookViewHolder(private val binding: ItemHomeBookBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(book: Book) {
            binding.tvHomeBookTitle.text = book.title
            binding.tvHomeBookAuthor.text = book.author

            if (book.thumbnailUrl.isNotEmpty()) {
                Glide.with(binding.root.context)
                    .load(book.thumbnailUrl)
                    .into(binding.imgHomeBookCover)
            } else {
                binding.imgHomeBookCover.setImageResource(android.R.drawable.ic_menu_report_image)
            }

            binding.root.setOnClickListener {
                onBookClick(book)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeBookViewHolder {
        val binding = ItemHomeBookBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return HomeBookViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HomeBookViewHolder, position: Int) {
        holder.bind(books[position])
    }

    override fun getItemCount(): Int = books.size

    fun updateBooks(newBooks: List<Book>) {
        books = newBooks
        notifyDataSetChanged()
    }
}