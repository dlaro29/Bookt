package com.example.bookt.ui.account

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.bookt.data.model.Book
import com.example.bookt.ui.theme.BooktFont
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Row

@Composable
fun SavedBooksListScreen(
    title: String,
    books: List<Book>,
    isLoading: Boolean,
    errorMessage: String?,
    type: String,
    onBookClick: (Book) -> Unit
) {
    var sortMode by remember {
        mutableStateOf(SavedBooksSortMode.RECENT)
    }

    val sortedBooks = remember(books, sortMode) {
        when (sortMode) {
            SavedBooksSortMode.RECENT -> books

            SavedBooksSortMode.AUTHOR -> books.sortedWith(
                compareBy<Book> { it.author.lowercase() }
                    .thenBy { it.title.lowercase() }
            )

            SavedBooksSortMode.CATEGORY -> books.sortedWith(
                compareBy<Book> { it.category.lowercase() }
                    .thenBy { it.author.lowercase() }
                    .thenBy { it.title.lowercase() }
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF101010))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp, vertical = 18.dp)
        ) {
            Text(
                text = title.uppercase(),
                color = Color.White,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (type == "favorites") {
                    "Tutti i libri che hai aggiunto ai preferiti"
                } else {
                    "Tutti i libri che hai segnato come letti"
                },
                color = Color(0xFFBDBDBD),
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (books.isNotEmpty()) {
                SortChipsRow(
                    selectedSortMode = sortMode,
                    onSortModeChange = { sortMode = it }
                )

                Spacer(modifier = Modifier.height(18.dp))
            }
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 60.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color.White)
                    }
                }

                errorMessage != null -> {
                    MessageState(
                        type = type,
                        title = "Errore",
                        message = errorMessage
                    )
                }

                books.isEmpty() -> {
                    MessageState(
                        type = type,
                        title = "Nessun libro",
                        message = if (type == "favorites") {
                            "Non hai ancora aggiunto libri ai preferiti."
                        } else {
                            "Non hai ancora segnato libri come letti."
                        }
                    )
                }

                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(
                            items = sortedBooks,
                            key = { it.id }
                        ) { book ->
                            SavedBookGridCard(
                                book = book,
                                onClick = { onBookClick(book) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SortChipsRow(
    selectedSortMode: SavedBooksSortMode,
    onSortModeChange: (SavedBooksSortMode) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SortChip(
            text = "Recenti",
            selected = selectedSortMode == SavedBooksSortMode.RECENT,
            onClick = { onSortModeChange(SavedBooksSortMode.RECENT) }
        )

        SortChip(
            text = "Autore",
            selected = selectedSortMode == SavedBooksSortMode.AUTHOR,
            onClick = { onSortModeChange(SavedBooksSortMode.AUTHOR) }
        )

        SortChip(
            text = "Genere",
            selected = selectedSortMode == SavedBooksSortMode.CATEGORY,
            onClick = { onSortModeChange(SavedBooksSortMode.CATEGORY) }
        )
    }
}

@Composable
private fun SortChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = text.uppercase(),
                fontFamily = BooktFont,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 11.sp
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = Color(0xFF4CAF50),
            selectedLabelColor = Color.White,
            containerColor = Color(0xFF1A1A1A),
            labelColor = Color(0xFFBDBDBD)
        )
    )
}

@Composable
private fun SavedBookGridCard(
    book: Book,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A1A1A)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.63f)
        ) {
            AsyncImage(
                model = book.thumbnailUrl,
                contentDescription = "Copertina libro",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(18.dp)),
                contentScale = ContentScale.Crop
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(132.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color(0x99000000),
                                Color(0xF2000000)
                            )
                        )
                    )
            )

            Text(
                text = book.title,
                color = Color.White,
                fontFamily = BooktFont,
                fontSize = 9.sp,
                lineHeight = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 7.dp, end = 7.dp, bottom = 9.dp)
            )
        }
    }
}

@Composable
private fun MessageState(
    type: String,
    title: String,
    message: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 58.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = if (type == "favorites") {
                Icons.Outlined.FavoriteBorder
            } else {
                Icons.Outlined.Book
            },
            contentDescription = null,
            tint = Color(0xFF777777)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = title,
            color = Color.White,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = message,
            color = Color(0xFFBDBDBD),
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center
        )
    }
}

private enum class SavedBooksSortMode {
    RECENT,
    AUTHOR,
    CATEGORY
}