package com.example.ui.theme

import android.content.res.Configuration
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.example.domain.model.StudioTheme
import java.util.Locale

private val MidnightMetallicColorScheme = darkColorScheme(
    primary = MidnightPrimary,
    onPrimary = Color(0xFF0D0D12),
    primaryContainer = Color(0xFF282834),
    onPrimaryContainer = Color(0xFFFFFFFF),
    secondary = MidnightSecondary,
    onSecondary = Color(0xFF0D0D12),
    tertiary = MidnightTertiary,
    background = StudioBlack,
    onBackground = StudioTextPrimary,
    surface = StudioDarkSurface,
    onSurface = StudioTextPrimary,
    surfaceVariant = StudioDarkSurfaceVariant,
    onSurfaceVariant = StudioTextSecondary,
    outline = StudioBorder
)

private val PurpleNightColorScheme = darkColorScheme(
    primary = PurplePrimary,
    onPrimary = Color(0xFF1B003B),
    primaryContainer = Color(0xFF381A66),
    onPrimaryContainer = Color(0xFFF1E6FF),
    secondary = PurpleSecondary,
    onSecondary = Color(0xFF1B003B),
    tertiary = PurpleTertiary,
    background = Color(0xFF0C0714),
    onBackground = StudioTextPrimary,
    surface = Color(0xFF160E24),
    onSurface = StudioTextPrimary,
    surfaceVariant = Color(0xFF221638),
    onSurfaceVariant = StudioTextSecondary,
    outline = Color(0x33BB86FC)
)

private val DeepOceanColorScheme = darkColorScheme(
    primary = OceanPrimary,
    onPrimary = Color(0xFF00363D),
    primaryContainer = Color(0xFF004D57),
    onPrimaryContainer = Color(0xFFB8F5FF),
    secondary = OceanSecondary,
    onSecondary = Color(0xFF00363D),
    tertiary = OceanTertiary,
    background = Color(0xFF060D17),
    onBackground = StudioTextPrimary,
    surface = Color(0xFF0D1B2D),
    onSurface = StudioTextPrimary,
    surfaceVariant = Color(0xFF142740),
    onSurfaceVariant = StudioTextSecondary,
    outline = Color(0x3300E5FF)
)

private val EmeraldColorScheme = darkColorScheme(
    primary = EmeraldPrimary,
    onPrimary = Color(0xFF003919),
    primaryContainer = Color(0xFF005226),
    onPrimaryContainer = Color(0xFFB8FFD1),
    secondary = EmeraldSecondary,
    onSecondary = Color(0xFF003919),
    tertiary = EmeraldTertiary,
    background = Color(0xFF06120B),
    onBackground = StudioTextPrimary,
    surface = Color(0xFF0D2417),
    onSurface = StudioTextPrimary,
    surfaceVariant = Color(0xFF153623),
    onSurfaceVariant = StudioTextSecondary,
    outline = Color(0x3300E676)
)

private val CrimsonColorScheme = darkColorScheme(
    primary = CrimsonPrimary,
    onPrimary = Color(0xFF3F000B),
    primaryContainer = Color(0xFF5E0013),
    onPrimaryContainer = Color(0xFFFFD9DF),
    secondary = CrimsonSecondary,
    onSecondary = Color(0xFF3F000B),
    tertiary = CrimsonTertiary,
    background = Color(0xFF140608),
    onBackground = StudioTextPrimary,
    surface = Color(0xFF260D11),
    onSurface = StudioTextPrimary,
    surfaceVariant = Color(0xFF38141A),
    onSurfaceVariant = StudioTextSecondary,
    outline = Color(0x33FF3355)
)

private val GoldenColorScheme = darkColorScheme(
    primary = GoldenPrimary,
    onPrimary = Color(0xFF3B2F00),
    primaryContainer = Color(0xFF544400),
    onPrimaryContainer = Color(0xFFFFEFA8),
    secondary = GoldenSecondary,
    onSecondary = Color(0xFF3B2F00),
    tertiary = GoldenTertiary,
    background = Color(0xFF120F06),
    onBackground = StudioTextPrimary,
    surface = Color(0xFF241E0D),
    onSurface = StudioTextPrimary,
    surfaceVariant = Color(0xFF362E15),
    onSurfaceVariant = StudioTextSecondary,
    outline = Color(0x33FFD700)
)

private val NeonColorScheme = darkColorScheme(
    primary = NeonPrimary,
    onPrimary = Color(0xFF3E0020),
    primaryContainer = Color(0xFF5A0030),
    onPrimaryContainer = Color(0xFFFFD8E6),
    secondary = NeonSecondary,
    onSecondary = Color(0xFF00363D),
    tertiary = NeonTertiary,
    background = Color(0xFF0B0616),
    onBackground = StudioTextPrimary,
    surface = Color(0xFF160D2C),
    onSurface = StudioTextPrimary,
    surfaceVariant = Color(0xFF241546),
    onSurfaceVariant = StudioTextSecondary,
    outline = Color(0x33FF1493)
)

private val LightStudioColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    primaryContainer = Color(0xFFE2E2EC),
    onPrimaryContainer = Color(0xFF14141E),
    secondary = LightTextSecondary,
    onSecondary = Color(0xFFFFFFFF),
    tertiary = LightPrimary,
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextSecondary,
    outline = LightBorder
)

fun getStudioColorScheme(theme: StudioTheme): ColorScheme {
    return when (theme) {
        StudioTheme.MidnightMetallic -> MidnightMetallicColorScheme
        StudioTheme.PurpleNight -> PurpleNightColorScheme
        StudioTheme.DeepOcean -> DeepOceanColorScheme
        StudioTheme.Emerald -> EmeraldColorScheme
        StudioTheme.Crimson -> CrimsonColorScheme
        StudioTheme.Golden -> GoldenColorScheme
        StudioTheme.Neon -> NeonColorScheme
        StudioTheme.Light -> LightStudioColorScheme
    }
}

@Composable
fun LyricStudioTheme(
    studioTheme: StudioTheme = StudioTheme.MidnightMetallic,
    language: String = "fa",
    content: @Composable () -> Unit
) {
    val colorScheme = getStudioColorScheme(studioTheme)
    val layoutDirection = if (language == "fa") LayoutDirection.Rtl else LayoutDirection.Ltr
    val context = LocalContext.current
    val locale = remember(language) { Locale(language) }

    val localizedContext = remember(context, language) {
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)
        Locale.setDefault(locale)
        context.createConfigurationContext(config)
    }

    CompositionLocalProvider(
        LocalContext provides localizedContext,
        LocalConfiguration provides localizedContext.resources.configuration,
        LocalLayoutDirection provides layoutDirection
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
