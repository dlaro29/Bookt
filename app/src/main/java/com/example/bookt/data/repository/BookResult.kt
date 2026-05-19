package com.example.bookt.data.repository

import com.example.bookt.data.model.Book

sealed class BookResult {
    data class Success(val books: List<Book>) : BookResult()
    data class Error(val message: String) : BookResult()
}
