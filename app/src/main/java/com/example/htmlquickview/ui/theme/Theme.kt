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

// Material 3 深色模式颜色方案
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFA78BFA),
    onPrimary = Color(0xFF1E1B4B),
    primaryContainer = Color(0xFF312E81),
    onPrimaryContainer = Color(0xFFE9D5FF),
    secondary = Color(0xFFC4B5FD),
    onSecondary = Color(0xFF312E81),
    secondaryContainer = Color(0xFF4C1D95),
    onSecondaryContainer = Color(0xFFE9D5FF),
    tertiary = Color(0xFFF0ABFC),
    onTertiary = Color(0xFF4A1D65),
    tertiaryContainer = Color(0xFF6D28D9),
    onTertiaryContainer = Color(0xFFF5D0FE),
    error = Color(0xFFFCA5A5),
    errorContainer = Color(0xFFDC2626),
    onError = Color(0xFF7F1D1D),
    onErrorContainer = Color(0xFFFEE2E2),
    background = Color(0xFF0F0F14),
    onBackground = Color(0xFFE4E4E7),
    surface = Color(0xFF18181B),
    onSurface = Color(0xFFE4E4E7),
    surfaceVariant = Color(0xFF3F3F46),
    onSurfaceVariant = Color(0xFFA1A1AA),
    outline = Color(0xFF71717A),
    outlineVariant = Color(0xFF3F3F46),
    scrim = Color(0xFF000000),
    inverseSurface = Color(0xFFE4E4E7),
    inverseOnSurface = Color(0xFF18181B),
    inversePrimary = Color(0xFF4C1D95)
)

// Material 3 浅色模式颜色方案
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6D28D9),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE9D5FF),
    onPrimaryContainer = Color(0xFF312E81),
    secondary = Color(0xFF7C3AED),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE9D5FF),
    onSecondaryContainer = Color(0xFF4C1D95),
    tertiary = Color(0xFF9D174D),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFD6E8),
    onTertiaryContainer = Color(0xFF6D0A35),
    error = Color(0xFFDC2626),
    errorContainer = Color(0xFFFEE2E2),
    onError = Color(0xFFFFFFFF),
    onErrorContainer = Color(0xFF7F1D1D),
    background = Color(0xFFFFFFFF),
    onBackground = Color(0xFF18181B),
    surface = Color(0xFFFEFEFE),
    onSurface = Color(0xFF18181B),
    surfaceVariant = Color(0xFFE4E4E7),
    onSurfaceVariant = Color(0xFF4B5563),
    outline = Color(0xFF71717A),
    outlineVariant = Color(0xFFE4E4E7),
    scrim = Color(0xFF000000),
    inverseSurface = Color(0xFF27272A),
    inverseOnSurface = Color(0xFFF4F4F5),
    inversePrimary = Color(0xFFC4B5FD)
)

// Material 3 Typography - 使用Google Sans字体
val HtmlQuickViewTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Light,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    ),
    displayMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 45.sp,
        lineHeight = 52.sp
    ),
    displaySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 36.sp,
        lineHeight = 44.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 32.sp,
        lineHeight = 40.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 28.sp,
        lineHeight = 36.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
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
        fontWeight = FontWeight.Medium,
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

// Material 3 Shapes
val HtmlQuickViewShapes = Shapes(
    extraSmall = androidx.compose.foundation.shape.RoundedCornerShape(4.dp),
    small = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
    medium = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
    large = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
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
        // Android 12+ 支持动态颜色
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
            // 使用surface颜色作为状态栏背景，实现现代效果
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
