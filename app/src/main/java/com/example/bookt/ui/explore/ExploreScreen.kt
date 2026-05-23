package com.example.bookt.ui.explore

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.bookt.data.model.Book
import com.example.bookt.ui.theme.BooktFont

@Composable
fun ExploreScreen(
    query: String,
    sectionTitle: String,
    books: List<Book>,
    isLoading: Boolean,
    statusMessage: String?,
    selectedChip: String,
    onQueryChange: (String) -> Unit,
    onSearchClick: () -> Unit,
    onChipClick: (String) -> Unit,
    onBookClick: (Book) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF101010)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp, vertical = 18.dp)
        ) {
            Text(
                text = "ESPLORA",
                color = Color.White,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(14.dp))

            SearchBox(
                query = query,
                onQueryChange = onQueryChange,
                onSearchClick = onSearchClick
            )

            Spacer(modifier = Modifier.height(12.dp))

            GenreChipsRow(
                selectedChip = selectedChip,
                onChipClick = onChipClick
            )

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = sectionTitle.uppercase(),
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            when {
                isLoading -> {
                    LoadingState()
                }

                statusMessage != null -> {
                    MessageState(
                        title = sectionTitle,
                        message = statusMessage
                    )
                }

                books.isEmpty() -> {
                    MessageState(
                        title = "Nessun risultato",
                        message = "Prova a cercare un altro titolo o autore."
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        contentPadding = PaddingValues(bottom = 18.dp)
                    ) {
                        items(
                            items = books,
                            key = { it.id }
                        ) { book ->
                            ExploreBookCard(
                                book = book,
                                onClick = {
                                    onBookClick(book)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchBox(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearchClick: () -> Unit
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        label = {
            Text("Cerca un libro o autore")
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = null
            )
        },
        trailingIcon = {
            IconButton(onClick = onSearchClick) {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = "Cerca"
                )
            }
        },
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Search
        ),
        keyboardActions = KeyboardActions(
            onSearch = {
                onSearchClick()
            }
        ),
        shape = RoundedCornerShape(18.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedLabelColor = Color(0xFF4CAF50),
            unfocusedLabelColor = Color(0xFFBDBDBD),
            focusedBorderColor = Color(0xFF4CAF50),
            unfocusedBorderColor = Color(0xFF555555),
            cursorColor = Color(0xFF4CAF50),
            focusedLeadingIconColor = Color(0xFF4CAF50),
            unfocusedLeadingIconColor = Color(0xFFBDBDBD),
            focusedTrailingIconColor = Color(0xFF4CAF50),
            unfocusedTrailingIconColor = Color(0xFFBDBDBD)
        )
    )
}

@Composable
private fun GenreChipsRow(
    selectedChip: String,
    onChipClick: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        GenreChip(
            text = "Tutti",
            selected = selectedChip == "Tutti",
            onClick = { onChipClick("Tutti") }
        )

        GenreChip(
            text = "Fantasy",
            selected = selectedChip == "Fantasy",
            onClick = { onChipClick("Fantasy") }
        )

        GenreChip(
            text = "Romanzi",
            selected = selectedChip == "Romanzi",
            onClick = { onChipClick("Romanzi") }
        )

        GenreChip(
            text = "Thriller",
            selected = selectedChip == "Thriller",
            onClick = { onChipClick("Thriller") }
        )

        GenreChip(
            text = "Fumetti",
            selected = selectedChip == "Manga",
            onClick = { onChipClick("Manga") }
        )
    }
}

@Composable
private fun GenreChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    AssistChip(
        onClick = onClick,
        label = {
            Text(text)
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = if (selected) Color(0xFF4CAF50) else Color(0xFF1A1A1A),
            labelColor = if (selected) Color.White else Color(0xFFBDBDBD)
        )
    )
}

@Composable
private fun ExploreBookCard(
    book: Book,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A1A1A)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            AsyncImage(
                model = book.thumbnailUrl,
                contentDescription = "Copertina libro",
                modifier = Modifier
                    .width(82.dp)
                    .height(122.dp)
                    .clip(RoundedCornerShape(14.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .height(122.dp)
            ) {
                Text(
                    text = book.title,
                    color = Color.White,
                    fontFamily = BooktFont,
                    fontSize = 15.sp,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(5.dp))

                Text(
                    text = book.author.ifBlank { "Autore sconosciuto" },
                    color = Color(0xFFBDBDBD),
                    fontFamily = BooktFont,
                    fontSize = 12.sp,
                    lineHeight = 15.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = book.category.ifBlank { "Senza categoria" },
                    color = Color(0xFF4CAF50),
                    fontFamily = BooktFont,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = book.rating?.let { "★ $it" } ?: "Rating non disponibile",
                    color = Color(0xFFDDDDDD),
                    fontFamily = BooktFont,
                    fontSize = 11.sp,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 55.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = Color.White
        )
    }
}

@Composable
private fun MessageState(
    title: String,
    message: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 55.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Outlined.MenuBook,
            contentDescription = null,
            tint = Color(0xFF777777)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = title,
            color = Color.White,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
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