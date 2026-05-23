package com.example.bookt.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.Typography
import androidx.compose.ui.unit.sp
import com.example.bookt.R

val BooktBackground = Color(0xFF101010)
val BooktSurface = Color(0xFF1A1A1A)
val BooktSurfaceLight = Color(0xFF242424)

val BooktTextPrimary = Color(0xFFFFFFFF)
val BooktTextSecondary = Color(0xFFBDBDBD)
val BooktTextMuted = Color(0xFF777777)

val BooktGreen = Color(0xFF4CAF50)
val BooktBlue = Color(0xFF2196F3)
val BooktPink = Color(0xFFE91E63)
val BooktError = Color(0xFFFFB4AB)

val BooktFont = FontFamily(
    Font(R.font.manrope_variable, FontWeight.Normal),
    Font(R.font.manrope_variable, FontWeight.Medium),
    Font(R.font.manrope_variable, FontWeight.SemiBold),
    Font(R.font.manrope_variable, FontWeight.Bold),
    Font(R.font.manrope_variable, FontWeight.ExtraBold)
)
private val BooktColorScheme = darkColorScheme(
    primary = BooktGreen,
    background = BooktBackground,
    surface = BooktSurface,
    onPrimary = BooktTextPrimary,
    onBackground = BooktTextPrimary,
    onSurface = BooktTextPrimary,
    error = BooktError
)

private val BooktTypography = Typography(
    headlineSmall = TextStyle(
        fontFamily = BooktFont,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 25.sp,
        lineHeight = 31.sp,
        letterSpacing = 0.5.sp
    ),
    titleLarge = TextStyle(
        fontFamily = BooktFont,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.3.sp
    ),
    titleMedium = TextStyle(
        fontFamily = BooktFont,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 18.sp,
        lineHeight = 23.sp,
        letterSpacing = 0.6.sp
    ),
    titleSmall = TextStyle(
        fontFamily = BooktFont,
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.4.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = BooktFont,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 21.sp,
        letterSpacing = 0.sp
    ),
    bodySmall = TextStyle(
        fontFamily = BooktFont,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.sp
    ),
    labelLarge = TextStyle(
        fontFamily = BooktFont,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        lineHeight = 17.sp,
        letterSpacing = 0.2.sp
    ),
    labelMedium = TextStyle(
        fontFamily = BooktFont,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.2.sp
    ),
    labelSmall = TextStyle(
        fontFamily = BooktFont,
        fontWeight = FontWeight.Bold,
        fontSize = 9.sp,
        lineHeight = 10.sp,
        letterSpacing = 0.1.sp
    )
)

@Composable
fun BooktTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = BooktColorScheme,
        typography = BooktTypography,
        content = content
    )
}