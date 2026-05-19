package com.example.bookt.data.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface GoogleBooksApi {

    @GET("volumes")
    suspend fun searchBooks(
        @Query("q") query: String,
        @Query("printType") printType: String = "books",
        @Query("maxResults") maxResults: Int = 40,
        @Query("startIndex") startIndex: Int = 0,
        @Query("key") apiKey: String = RetrofitInstance.GOOGLE_BOOKS_API_KEY
    ): Response<GoogleBooksResponse>
}