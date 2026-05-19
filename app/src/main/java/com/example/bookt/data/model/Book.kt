package com.example.bookt.data.model

import android.accessibilityservice.GestureDescription
import java.io.Serializable

data class Book(
    val id: String,
    val title: String,
    val author: String,
    val category: String,
    val thumbnailUrl: String,
    val description: String = "",
    val rating: Double? = null,
    val publisher: String = "",
    val publishedDate: String = "",
    val pageCount: Int? = null
): Serializable