package com.example.htmlquickview.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

// 品牌主色 - 现代渐变蓝紫色系
private val PrimaryBlue = Color(0xFF6366F1)    // Indigo 500
private val PrimaryBlueLight = Color(0xFF818CF8)  // Indigo 400
private val PrimaryPurple = Color(0xFF8B5CF6)     // Violet 500

// 深色模式颜色方案 - 现代化深色主题
private val DarkColorScheme = darkColorScheme(
    primary = PrimaryBlue,
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF1E1B4B),
    onPrimaryContainer = Color(0xFFE0E7FF),
    secondary = PrimaryPurple,
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFF312E81),
    onSecondaryContainer = Color(0xFFC7D2FE),
    tertiary = Color(0xFFEC4899),               // Pink 500
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFF831843),
    onTertiaryContainer = Color(0xFFFCE7F3),
    error = Color(0xFFF87171),                  // Red 400
    errorContainer = Color(0xFFB91C1C),
    onError = Color(0xFFFFFFFF),
    onErrorContainer = Color(0xFFFEE2E2),
    background = Color(0xFF0F0F1A),              // 深邃背景
    onBackground = Color(0xFFE2E8F0),
    surface = Color(0xFF16162A),                // 深色卡片
    onSurface = Color(0xFFE2E8F0),
    surfaceVariant = Color(0xFF1F1F3A),         // 卡片变体
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = Color(0xFF475569),
    outlineVariant = Color(0xFF334155),
    scrim = Color(0xFF000000),
    inverseSurface = Color(0xFFE2E8F0),
    inverseOnSurface = Color(0xFF0F0F1A),
    inversePrimary = PrimaryBlueLight,
    surfaceTint = PrimaryBlue
)

// 浅色模式颜色方案 - 现代化浅色主题
private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE0E7FF),
    onPrimaryContainer = Color(0xFF312E81),
    secondary = PrimaryPurple,
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFEDE9FE),
    onSecondaryContainer = Color(0xFF4C1D95),
    tertiary = Color(0xFFDB2777),               // Pink 600
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFCE7F3),
    onTertiaryContainer = Color(0xFF831843),
    error = Color(0xFFDC2626),
    errorContainer = Color(0xFFFEE2E2),
    onError = Color(0xFFFFFFFF),
    onErrorContainer = Color(0xFF7F1D1D),
    background = Color(0xFFF8FAFC),              // 浅灰背景
    onBackground = Color(0xFF1E293B),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1E293B),
    surfaceVariant = Color(0xFFF1F5F9),        // 浅色卡片变体
    onSurfaceVariant = Color(0xFF64748B),
    outline = Color(0xFFCBD5E1),
    outlineVariant = Color(0xFFE2E8F0),
    scrim = Color(0xFF000000),
    inverseSurface = Color(0xFF1E293B),
    inverseOnSurface = Color(0xFFF8FAFC),
    inversePrimary = PrimaryBlueLight,
    surfaceTint = PrimaryBlue
)

// Material 3 Typography - 优化字体样式
val HtmlQuickViewTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    ),
    displayMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 45.sp,
        lineHeight = 52.sp
    ),
    displaySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        lineHeight = 44.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp,
        lineHeight = 40.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 36.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.1.sp
    ),
    titleSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)

// Material 3 Shapes - 现代化圆角
val HtmlQuickViewShapes = Shapes(
    extraSmall = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
    small = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
    medium = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
    large = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
    extraLarge = androidx.compose.foundation.shape.RoundedCornerShape(28.dp)
)

// 主题模式枚举
enum class ThemeMode {
    SYSTEM,    // 跟随系统
    LIGHT,     // 浅色模式
    DARK       // 深色模式
}

// 全局主题状态管理器
object ThemeManager {
    var currentTheme by mutableStateOf(ThemeMode.SYSTEM)
}

// 设置主题
fun setThemeMode(mode: ThemeMode) {
    ThemeManager.currentTheme = mode
}

@Composable
fun HtmlQuickViewTheme(
    content: @Composable () -> Unit
) {
    val themeMode = ThemeManager.currentTheme
    
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val colorScheme = when {
        // Android 12+ 支持动态颜色（可选）
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // 使用surface颜色作为状态栏背景
            window.statusBarColor = colorScheme.surface.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = HtmlQuickViewTypography,
        shapes = HtmlQuickViewShapes,
        content = content
    )
}
