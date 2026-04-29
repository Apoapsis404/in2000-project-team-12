package no.uio.ifi.in2000.team12.team12project.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = dark_green, // Header and button backgrounds etc
    secondary = yellow, // Symbols and lines
    tertiary = red, // Negative graph
    onPrimary = light_green, // Text on buttons etc
    background = Black_almost, // Background on graph, favorite and settings screen
    onBackground = White_nearly, // Text on background
    )

private val LightColorScheme = lightColorScheme(
    primary = light_green, // Header and button backgrounds etc
    secondary = yellow, // Symbols and lines
    tertiary = red, // Negative graph
    onPrimary = dark_green, // Text on buttons etc
    background = White_nearly, // Background on graph, favorite and settings screen
    onBackground = Black_almost, // Text on background




    /* Other default colors to override
    surface = Color(0xFFFFFBFE),
    onSecondary = Color.White,
    onTertiary = Color.White,
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun Team12ProjectTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}