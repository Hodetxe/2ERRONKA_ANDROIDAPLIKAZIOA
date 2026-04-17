package com.example.androidapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object AppColors {
    val Primary = Color(0xFF6366F1)
    val Secondary = Color(0xFF4E8EF7)
    val LoginGradientStart = Color(0xFF4F8DF7)
    val BrandDark = Color(0xFF4338CA)
    val PrimaryHover = Color(0xFF4F46E5)
    val Background = Color(0xFFF9FAFB)
    val Surface = Color(0xFFFFFFFF)
    val TextPrimary = Color(0xFF1E293B)
    val TextStrong = Color(0xFF0F172A)
    val TextSecondary = Color(0xFF475569)
    val Border = Color(0xFFE5E7EB)
    val Danger = Color(0xFFEF4444)
    val DangerHover = Color(0xFFDC2626)
    val Success = Color(0xFF22C55E)
    val SuccessSoft = Color(0xFFDCFCE7)
}

private val AppLightColorScheme = lightColorScheme(
    primary = AppColors.Primary,
    onPrimary = AppColors.Surface,
    primaryContainer = AppColors.Secondary.copy(alpha = 0.16f),
    onPrimaryContainer = AppColors.TextStrong,
    secondary = AppColors.Secondary,
    onSecondary = AppColors.Surface,
    secondaryContainer = AppColors.Secondary.copy(alpha = 0.14f),
    onSecondaryContainer = AppColors.TextStrong,
    tertiary = AppColors.BrandDark,
    onTertiary = AppColors.Surface,
    background = AppColors.Background,
    onBackground = AppColors.TextPrimary,
    surface = AppColors.Surface,
    onSurface = AppColors.TextPrimary,
    surfaceVariant = AppColors.Background,
    onSurfaceVariant = AppColors.TextSecondary,
    outline = AppColors.Border,
    error = AppColors.Danger,
    onError = AppColors.Surface,
    errorContainer = AppColors.Danger.copy(alpha = 0.12f),
    onErrorContainer = AppColors.DangerHover
)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AppLightColorScheme,
        content = content
    )
}