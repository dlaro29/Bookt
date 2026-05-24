package com.example.bookt.data.repository

import com.example.bookt.data.model.Book
import com.example.bookt.data.remote.RetrofitInstance

class BookRepository {

    suspend fun searchBooks(query: String): BookResult {
        val cleanedQuery = query.trim()
        if (cleanedQuery.isBlank()) {
            return BookResult.Success(emptyList())
        }

        return try {
            val allResults = linkedMapOf<String, Book>()

            val queriesToTry = listOf(
                cleanedQuery,
                "intitle:$cleanedQuery",
                "inauthor:$cleanedQuery"
            )

            for (apiQuery in queriesToTry) {
                val startIndexes = listOf(0)

                for (startIndex in startIndexes) {
                    val response = RetrofitInstance.api.searchBooks(
                        query = apiQuery,
                        maxResults = 20,
                        startIndex = startIndex
                    )

                    val body = response.body()

                    if (response.isSuccessful && !body?.items.isNullOrEmpty()) {
                        val books = body.items!!.mapNotNull { item ->
                            val info = item.volumeInfo ?: return@mapNotNull null

                            Book(
                                id = item.id ?: "",
                                title = info.title ?: "Titolo non disponibile",
                                author = info.authors?.joinToString(", ") ?: "Autore sconosciuto",
                                category = info.categories?.joinToString(", ") ?: "Senza categoria",
                                thumbnailUrl = info.imageLinks?.thumbnail?.replace("http://", "https://") ?: "",
                                description = info.description ?: "",
                                rating = info.averageRating,
                                publisher = info.publisher ?: "",
                                publishedDate = info.publishedDate ?: "",
                                pageCount = info.pageCount
                            )
                        }

                        val filteredBooks = rankFreeSearch(cleanedQuery, books)

                        for (book in filteredBooks) {
                            if (book.id.isNotBlank()) {
                                allResults[book.id] = book
                            }
                        }
                    }
                }
            }

            BookResult.Success(allResults.values.take(30))
        } catch (e: Exception) {
            BookResult.Error("Errore di connessione o caricamento")
        }
    }

    suspend fun searchBooksForChip(query: String, label: String): BookResult {
        return try {
            val allResults = linkedMapOf<String, Book>()

            val queriesToTry = when (label.lowercase()) {
                "fantasy" -> listOf(
                    "subject:fantasy",
                    "fantasy fiction",
                    "epic fantasy",
                    "young adult fantasy"
                )

                "romance" -> listOf(
                    "subject:romance",
                    "romance fiction",
                    "romance novel",
                    "love story fiction"
                )

                "thriller" -> listOf(
                    "subject:thriller",
                    "thriller fiction",
                    "suspense fiction",
                    "mystery thriller"
                )

                "manga" -> listOf(
                    "subject:manga",
                    "manga",
                    "manga comics",
                    "graphic novel manga"
                )

                else -> listOf(query)
            }

            val startIndexes = listOf(0, 40, 80)

            for (apiQuery in queriesToTry) {
                for (startIndex in startIndexes) {
                    val response = RetrofitInstance.api.searchBooks(
                        query = apiQuery,
                        maxResults = 40,
                        startIndex = startIndex
                    )

                    val body = response.body()

                    if (!response.isSuccessful) {
                        continue
                    }

                    if (!body?.items.isNullOrEmpty()) {
                        val books = body.items!!.mapNotNull { item ->
                            val info = item.volumeInfo ?: return@mapNotNull null

                            Book(
                                id = item.id ?: "",
                                title = info.title ?: "Titolo non disponibile",
                                author = info.authors?.joinToString(", ") ?: "Autore sconosciuto",
                                category = info.categories?.joinToString(", ") ?: "Senza categoria",
                                thumbnailUrl = info.imageLinks?.thumbnail?.replace("http://", "https://") ?: "",
                                description = info.description ?: "",
                                rating = info.averageRating,
                                publisher = info.publisher ?: "",
                                publishedDate = info.publishedDate ?: "",
                                pageCount = info.pageCount
                            )
                        }

                        books
                            .filter { book ->
                                book.id.isNotBlank() &&
                                        isGoodCandidateForGenre(book) &&
                                        isRelevantForGenre(book, label)
                            }
                            .forEach { book ->
                                allResults[book.id] = book
                            }
                    }
                }
            }

            val finalBooks = allResults.values
                .sortedWith(
                    compareByDescending<Book> { genreScore(it, label) }
                        .thenByDescending { it.rating ?: -1.0 }
                        .thenByDescending { it.description.length }
                        .thenBy { it.title }
                )
                .take(30)

            BookResult.Success(finalBooks)
        } catch (e: Exception) {
            BookResult.Error("Errore di connessione o caricamento")
        }
    }

    suspend fun loadHomeBooks(): BookResult {
        return try {
            val allResults = linkedMapOf<String, Book>()
            var lastErrorCode: Int? = null

            val queriesToTry = listOf(
                "subject:fiction",
                "subject:fantasy",
                "subject:thriller",
                "subject:romance"
            )

            for (apiQuery in queriesToTry) {
                val response = RetrofitInstance.api.searchBooks(
                    query = apiQuery,
                    maxResults = 10,
                    startIndex = 0
                )

                val body = response.body()

                if (!response.isSuccessful) {
                    lastErrorCode = response.code()
                    continue
                }

                if (!body?.items.isNullOrEmpty()) {
                    val books = body.items!!.mapNotNull { item ->
                        val info = item.volumeInfo ?: return@mapNotNull null

                        Book(
                            id = item.id ?: "",
                            title = info.title ?: "Titolo non disponibile",
                            author = info.authors?.joinToString(", ") ?: "Autore sconosciuto",
                            category = info.categories?.joinToString(", ") ?: "Senza categoria",
                            thumbnailUrl = info.imageLinks?.thumbnail?.replace("http://", "https://") ?: "",
                            description = info.description ?: "",
                            rating = info.averageRating,
                            publisher = info.publisher ?: "",
                            publishedDate = info.publishedDate ?: "",
                            pageCount = info.pageCount
                        )
                    }

                    for (book in books) {
                        if (isGoodCandidate(book) && book.id.isNotBlank()) {
                            allResults[book.id] = book
                        }
                    }
                }
            }

            val finalBooks = allResults.values
                .sortedWith(
                    compareByDescending<Book> { it.rating ?: -1.0 }
                        .thenBy { it.title }
                )
                .take(30)

            if (finalBooks.isEmpty()) {
                if (lastErrorCode != null) {
                    BookResult.Error("Google Books non disponibile ora. Codice errore: $lastErrorCode")
                } else {
                    BookResult.Error("Google Books ha risposto, ma non ha restituito libri validi")
                }
            } else {
                BookResult.Success(finalBooks)
            }
        } catch (e: Exception) {
            BookResult.Error("Errore di connessione o caricamento: ${e.message}")
        }
    }

    suspend fun recommendBooksFrom(userBooks: List<Book>): BookResult {
        if (userBooks.isEmpty()) {
            return BookResult.Error("Aggiungi libri ai preferiti o ai letti per ricevere consigli personalizzati")
        }

        return try {
            val allResults = linkedMapOf<String, Book>()
            val userBookIds = userBooks.map { it.id }.toSet()

            val referenceBooks = userBooks
                .distinctBy { it.id }
                .takeLast(10)

            val favoriteCategories = referenceBooks
                .flatMap { it.category.split(",") }
                .map { it.trim() }
                .filter {
                    it.isNotBlank() &&
                            it.lowercase() != "senza categoria"
                }
                .groupingBy { it }
                .eachCount()
                .entries
                .sortedByDescending { it.value }
                .map { it.key }
                .take(2)

            val favoriteAuthors = referenceBooks
                .flatMap { it.author.split(",") }
                .map { it.trim() }
                .filter {
                    it.isNotBlank() &&
                            it.lowercase() != "autore sconosciuto"
                }
                .groupingBy { it }
                .eachCount()
                .entries
                .sortedByDescending { it.value }
                .map { it.key }
                .take(1)

            val queriesToTry = mutableListOf<String>()

            favoriteCategories.forEach { category ->
                queriesToTry.add("subject:$category")
                queriesToTry.add("$category fiction")
            }

            favoriteAuthors.forEach { author ->
                queriesToTry.add("inauthor:$author")
            }

            if (queriesToTry.isEmpty()) {
                return BookResult.Error("Non ho abbastanza informazioni sui tuoi libri per generare consigli")
            }

            val startIndexes = listOf(0, 20, 40).shuffled().take(2)

            for (apiQuery in queriesToTry.distinct().take(5)) {
                for (startIndex in startIndexes) {
                    val response = RetrofitInstance.api.searchBooks(
                        query = apiQuery,
                        maxResults = 15,
                        startIndex = startIndex
                    )

                    val body = response.body()

                    if (!response.isSuccessful) {
                        continue
                    }

                    if (!body?.items.isNullOrEmpty()) {
                        val books = body.items!!.mapNotNull { item ->
                            val info = item.volumeInfo ?: return@mapNotNull null

                            Book(
                                id = item.id ?: "",
                                title = info.title ?: "Titolo non disponibile",
                                author = info.authors?.joinToString(", ") ?: "Autore sconosciuto",
                                category = info.categories?.joinToString(", ") ?: "Senza categoria",
                                thumbnailUrl = info.imageLinks?.thumbnail?.replace("http://", "https://") ?: "",
                                description = info.description ?: "",
                                rating = info.averageRating,
                                publisher = info.publisher ?: "",
                                publishedDate = info.publishedDate ?: "",
                                pageCount = info.pageCount
                            )
                        }

                        books
                            .filter { book ->
                                book.id.isNotBlank() &&
                                        !userBookIds.contains(book.id) &&
                                        isGoodCandidate(book)
                            }
                            .forEach { book ->
                                allResults[book.id] = book
                            }
                    }
                }
            }

            val scoredBooks = allResults.values
                .map { book ->
                    book to recommendationScore(
                        book = book,
                        favoriteAuthors = favoriteAuthors,
                        favoriteCategories = favoriteCategories
                    )
                }
                .filter { (_, score) -> score >= 4 }
                .sortedWith(
                    compareByDescending<Pair<Book, Int>> { it.second }
                        .thenByDescending { it.first.rating ?: -1.0 }
                        .thenByDescending { it.first.description.length }
                        .thenBy { it.first.title }
                )
                .map { it.first }

            val finalBooks = diversifyByAuthor(scoredBooks).take(30)

            if (finalBooks.isEmpty()) {
                BookResult.Error("Non sono riuscito a trovare consigli adatti ai tuoi libri")
            } else {
                BookResult.Success(finalBooks)
            }
        } catch (e: Exception) {
            BookResult.Error("Errore durante il caricamento dei consigli")
        }
    }
    private fun rankFreeSearch(query: String, books: List<Book>): List<Book> {
        val words = query.lowercase().split(" ").filter { it.isNotBlank() }

        return books
            .filter { isGoodCandidate(it) }
            .mapNotNull { book ->
                val title = book.title.lowercase()
                val author = book.author.lowercase()
                val category = book.category.lowercase()

                var score = 0

                words.forEach { word ->
                    if (title.contains(word)) score += 5
                    if (author.contains(word)) score += 3
                    if (category.contains(word)) score += 2
                }

                if (book.thumbnailUrl.isNotBlank()) score += 2
                if (book.rating != null) score += 2

                if (score < 5) return@mapNotNull null

                book to score
            }
            .sortedWith(
                compareByDescending<Pair<Book, Int>> { it.second }
                    .thenByDescending { it.first.rating ?: -1.0 }
                    .thenBy { it.first.title }
            )
            .map { it.first }
            .distinctBy { it.id }
    }

    private fun isGoodCandidate(book: Book): Boolean {
        val title = book.title.trim()
        val author = book.author.trim()
        val category = book.category.trim()
        val description = book.description.trim()
        val publisher = book.publisher.trim()
        val pageCount = book.pageCount

        val fullText = """
        $title
        $author
        $category
        $description
        $publisher
    """.trimIndent().lowercase()

        val badWords = listOf(
            "catalog",
            "catalogue",
            "catalogo",
            "bibliography",
            "index",
            "directory",
            "proceedings",
            "conference",
            "report",
            "document",
            "documents",
            "selected papers",
            "study guide",
            "teacher guide",
            "teaching guide",
            "workbook",
            "activity book",
            "manual",
            "handbook",
            "summary",
            "analysis",
            "test prep",
            "exam prep",
            "notebook",
            "journal",
            "planner",
            "diary",
            "calendar",
            "coloring book",
            "colouring book",
            "blank book",
            "composition book",
            "lined notebook",
            "lesson plan"
        )

        if (title.length < 2) return false
        if (book.thumbnailUrl.isBlank()) return false

        if (author.isBlank()) return false
        if (author.lowercase() == "autore sconosciuto") return false

        if (description.length < 40 && publisher.isBlank()) return false

        if (pageCount != null) {
            if (pageCount < 40) return false
            if (pageCount > 1500) return false
        }

        if (badWords.any { fullText.contains(it) }) return false

        return true
    }
    private fun isGoodCandidateForGenre(book: Book): Boolean {
        val title = book.title.trim()
        val author = book.author.trim()
        val category = book.category.trim()
        val description = book.description.trim()
        val publisher = book.publisher.trim()
        val pageCount = book.pageCount

        val fullText = """
        $title
        $author
        $category
        $description
        $publisher
    """.trimIndent().lowercase()

        val badWords = listOf(
            "catalog",
            "catalogue",
            "catalogo",
            "bibliography",
            "index",
            "directory",
            "proceedings",
            "conference",
            "report",
            "document",
            "documents",
            "selected papers",
            "study guide",
            "teacher guide",
            "teaching guide",
            "workbook",
            "activity book",
            "manual",
            "handbook",
            "summary",
            "analysis",
            "test prep",
            "exam prep",
            "notebook",
            "journal",
            "planner",
            "diary",
            "calendar",
            "coloring book",
            "colouring book",
            "blank book",
            "composition book",
            "lined notebook",
            "lesson plan"
        )

        if (title.length < 2) return false
        if (book.thumbnailUrl.isBlank()) return false

        if (author.isBlank()) return false
        if (author.lowercase() == "autore sconosciuto") return false

        if (pageCount != null) {
            if (pageCount < 35) return false
            if (pageCount > 1800) return false
        }

        if (badWords.any { fullText.contains(it) }) return false

        return true
    }
    private fun genreScore(book: Book, label: String): Int {
        val category = book.category.lowercase()
        val description = book.description.lowercase()
        val title = book.title.lowercase()

        val categoryTerms: List<String>
        val textTerms: List<String>

        when (label.lowercase()) {
            "fantasy" -> {
                categoryTerms = listOf(
                    "fantasy",
                    "juvenile fiction / fantasy",
                    "young adult fiction / fantasy",
                    "fiction / fantasy"
                )

                textTerms = listOf(
                    "fantasy",
                    "magic",
                    "magical",
                    "wizard",
                    "witch",
                    "dragon",
                    "kingdom",
                    "quest",
                    "sorcery",
                    "supernatural",
                    "paranormal"
                )
            }

            "romance" -> {
                categoryTerms = listOf(
                    "romance",
                    "fiction / romance",
                    "young adult fiction / romance",
                    "love stories"
                )

                textTerms = listOf(
                    "romance",
                    "romantic",
                    "love",
                    "love story",
                    "falling in love",
                    "relationship",
                    "relationships",
                    "passion"
                )
            }

            "thriller" -> {
                categoryTerms = listOf(
                    "thriller",
                    "thrillers",
                    "suspense",
                    "mystery",
                    "detective",
                    "crime"
                )

                textTerms = listOf(
                    "thriller",
                    "suspense",
                    "mystery",
                    "detective",
                    "crime",
                    "murder",
                    "investigation",
                    "psychological"
                )
            }

            "manga" -> {
                categoryTerms = listOf(
                    "manga",
                    "comics",
                    "graphic novels",
                    "comic books"
                )

                textTerms = listOf(
                    "manga",
                    "anime",
                    "japanese",
                    "graphic novel"
                )
            }

            else -> return 0
        }

        var score = 0

        categoryTerms.forEach { term ->
            if (category.contains(term)) score += 5
        }

        textTerms.forEach { term ->
            if (description.contains(term)) score += 2
            if (title.contains(term)) score += 1
        }

        return score
    }
    private fun isRelevantForGenre(book: Book, label: String): Boolean {
        val category = book.category.lowercase()
        val description = book.description.lowercase()
        val title = book.title.lowercase()

        val nonFictionCategories = listOf(
            "history",
            "poetry",
            "literary criticism",
            "education",
            "reference",
            "biography",
            "autobiography",
            "religion",
            "philosophy",
            "social science",
            "political science",
            "business",
            "economics",
            "language arts"
        )

        val score = genreScore(book, label)

        val hasBadCategory = nonFictionCategories.any { bad ->
            category.split(",").any { singleCategory ->
                singleCategory.trim().contains(bad)
            }
        }

        val hasStrongGenreCategory = when (label.lowercase()) {
            "fantasy" -> category.contains("fantasy")
            "romance" -> category.contains("romance") || category.contains("love stories")
            "thriller" -> category.contains("thriller") ||
                    category.contains("suspense") ||
                    category.contains("mystery") ||
                    category.contains("crime")
            "manga" -> category.contains("manga") ||
                    category.contains("comics") ||
                    category.contains("graphic novels")
            else -> true
        }

        if (hasStrongGenreCategory) {
            return true
        }

        if (hasBadCategory) {
            return false
        }

        return score >= 2
    }
    private fun recommendationScore(
        book: Book,
        favoriteAuthors: List<String>,
        favoriteCategories: List<String>
    ): Int {
        val bookAuthor = book.author.lowercase()
        val bookCategory = book.category.lowercase()
        val bookTitle = book.title.lowercase()
        val bookDescription = book.description.lowercase()

        var score = 0

        favoriteCategories.forEach { category ->
            val cleanCategory = category.lowercase()

            if (bookCategory.contains(cleanCategory)) score += 8
            if (bookDescription.contains(cleanCategory)) score += 3
            if (bookTitle.contains(cleanCategory)) score += 2
        }

        favoriteAuthors.forEach { author ->
            val cleanAuthor = author.lowercase()

            if (bookAuthor.contains(cleanAuthor)) score += 3
        }

        if (book.rating != null) score += 2
        if (book.thumbnailUrl.isNotBlank()) score += 2
        if (book.description.length > 80) score += 1

        return score
    }

    private fun diversifyByAuthor(books: List<Book>): List<Book> {
        val result = mutableListOf<Book>()
        val authorCounts = mutableMapOf<String, Int>()

        for (book in books) {
            val mainAuthor = book.author
                .split(",")
                .firstOrNull()
                ?.trim()
                ?.lowercase()
                ?: "unknown"

            val currentCount = authorCounts[mainAuthor] ?: 0

            if (currentCount < 2) {
                result.add(book)
                authorCounts[mainAuthor] = currentCount + 1
            }
        }

        if (result.size < 30) {
            val existingIds = result.map { it.id }.toSet()

            books
                .filter { it.id !in existingIds }
                .forEach { result.add(it) }
        }

        return result
    }
}