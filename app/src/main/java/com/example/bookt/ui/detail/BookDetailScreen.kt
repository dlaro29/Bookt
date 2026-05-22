package com.example.bookt.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.bookt.data.model.Book
import androidx.compose.ui.platform.LocalContext
import coil.request.ImageRequest

@Composable
fun BookDetailScreen(
    book: Book,
    inReading: Boolean,
    inFavorites: Boolean,
    inRead: Boolean,
    isLoadingStatus: Boolean,
    onReadingClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onReadClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF8F5F0)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            BookHeader(
                book = book,
                inRead = inRead,
                isLoadingStatus = isLoadingStatus,
                onReadClick = onReadClick
            )

            BookActionsRow(
                inReading = inReading,
                inFavorites = inFavorites,
                isLoadingStatus = isLoadingStatus,
                onReadingClick = onReadingClick,
                onFavoriteClick = onFavoriteClick
            )

            BookInfoSection(book = book)
        }
    }
}

@Composable
private fun BookHeader(
    book: Book,
    inRead: Boolean,
    isLoadingStatus: Boolean,
    onReadClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1E1E1E))
            .padding(horizontal = 18.dp, vertical = 18.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val context = LocalContext.current

            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(book.thumbnailUrl)
                    .crossfade(false)
                    .build(),
                contentDescription = "Copertina libro",
                modifier = Modifier
                    .width(105.dp)
                    .height(155.dp)
                    .clip(RoundedCornerShape(10.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = book.title,
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 5,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = book.author,
                color = Color(0xFFE0E0E0),
                style = MaterialTheme.typography.labelLarge,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = buildMetaText(book),
                color = Color(0xFFBDBDBD),
                style = MaterialTheme.typography.labelSmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            IconButton(
                onClick = onReadClick,
                enabled = true,
                modifier = Modifier.size(58.dp),
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = if (inRead) Color(0xFF4CAF50) else Color(0xFF333333),
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = if (inRead) Icons.Filled.Check else Icons.Outlined.Check,
                    contentDescription = if (inRead) "Libro letto" else "Segna come letto",
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = if (inRead) "Letto" else "Segna come letto",
                color = if (inRead) Color(0xFF81C784) else Color(0xFFBDBDBD),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun BookActionsRow(
    inReading: Boolean,
    inFavorites: Boolean,
    isLoadingStatus: Boolean,
    onReadingClick: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ActionIcon(
                selected = inReading,
                selectedColor = Color(0xFF2196F3),
                defaultColor = Color(0xFF757575),
                selectedIcon = Icons.Filled.MenuBook,
                defaultIcon = Icons.Outlined.MenuBook,
                label = "Da leggere",
                enabled = true,
                onClick = onReadingClick
            )

            ActionIcon(
                selected = inFavorites,
                selectedColor = Color(0xFFE91E63),
                defaultColor = Color(0xFF757575),
                selectedIcon = Icons.Filled.Favorite,
                defaultIcon = Icons.Outlined.FavoriteBorder,
                label = "Preferito",
                enabled = true,
                onClick = onFavoriteClick
            )
        }
    }
}

@Composable
private fun ActionIcon(
    selected: Boolean,
    selectedColor: Color,
    defaultColor: Color,
    selectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    defaultIcon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconButton(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier.size(50.dp),
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = if (selected) selectedColor.copy(alpha = 0.15f) else Color(0xFFF1F1F1),
                contentColor = if (selected) selectedColor else defaultColor
            )
        ) {
            Icon(
                imageVector = if (selected) selectedIcon else defaultIcon,
                contentDescription = label,
                modifier = Modifier.size(26.dp)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) selectedColor else defaultColor,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1
        )
    }
}

@Composable
private fun BookInfoSection(book: Book) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 6.dp)
    ) {
        InfoCard(title = "Descrizione") {
            Text(
                text = if (book.description.isNotBlank()) {
                    book.description
                } else {
                    "Descrizione non disponibile"
                },
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF333333)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        InfoCard(title = "Informazioni libro") {
            InfoRow(label = "Categoria", value = book.category)
            InfoRow(label = "Editore", value = book.publisher.ifBlank { "-" })
            InfoRow(label = "Pubblicazione", value = book.publishedDate.ifBlank { "-" })
            InfoRow(label = "Pagine", value = book.pageCount?.toString() ?: "-")
            InfoRow(label = "Rating", value = book.rating?.let { "$it / 5" } ?: "Non disponibile")
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun InfoCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111111)
            )

            Spacer(modifier = Modifier.height(8.dp))

            content()
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF777777)
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF222222),
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun buildMetaText(book: Book): String {
    val parts = mutableListOf<String>()

    if (book.publishedDate.isNotBlank()) {
        parts.add(book.publishedDate)
    }

    if (book.category.isNotBlank()) {
        parts.add(book.category)
    }

    book.pageCount?.let {
        parts.add("$it pagine")
    }

    return if (parts.isEmpty()) {
        "Informazioni non disponibili"
    } else {
        parts.joinToString(" • ")
    }
}