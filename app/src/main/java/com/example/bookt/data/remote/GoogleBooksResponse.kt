package com.example.bookt.data.remote

data class GoogleBooksResponse(
    val items: List<VolumeItem>?
)

data class VolumeItem(
    val id: String?,
    val volumeInfo: VolumeInfo?
)

data class VolumeInfo(
    val title: String?,
    val authors: List<String>?,
    val categories: List<String>?,
    val description: String?,
    val averageRating: Double?,
    val imageLinks: ImageLinks?,
    val publisher: String?,
    val publishedDate: String?,
    val pageCount: Int?
)

data class ImageLinks(
    val thumbnail: String?
)