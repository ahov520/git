package com.antihub.mobile.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val DarkColors = darkColorScheme(
    primary = SakuraPink,
    secondary = MintAqua,
    tertiary = SkyBlue,
    background = NightIndigo,
    surface = NightViolet,
    onPrimary = MoonWhite,
    onBackground = MoonWhite,
    onSurface = MoonWhite,
)

private val LightColors = lightColorScheme(
    primary = SakuraPink,
    secondary = SkyBlue,
    tertiary = MintAqua,
    background = PeachCream,
    surface = MoonWhite,
    onPrimary = MoonWhite,
    onBackground = NightViolet,
    onSurface = NightViolet,
)

private val AppShapes = Shapes(
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(22.dp),
    large = RoundedCornerShape(30.dp),
)

@Composable
fun GitHubMobileTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AppTypography,
        shapes = AppShapes,
        content = content,
    )
}
