package com.futulink.android.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

/**
 * The app ships a single light brand scheme. Dark theme and dynamic color are out of scope for
 * this assignment, and a fixed scheme keeps the branding predictable on every device.
 */
private val FutuLinkColorScheme = lightColorScheme(
    primary = CobaltBlue,
    onPrimary = SurfaceWhite,
    primaryContainer = CobaltBlueLight,
    onPrimaryContainer = CobaltBlueDark,
    secondary = Teal,
    onSecondary = SurfaceWhite,
    secondaryContainer = TealLight,
    onSecondaryContainer = TealDark,
    background = CoolGreyBackground,
    onBackground = TextPrimary,
    surface = SurfaceWhite,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceMuted,
    onSurfaceVariant = TextSecondary,
    // Material 3 components such as NavigationBar paint themselves with the surface-container
    // roles; without these they would fall back to the purple-tinted baseline palette.
    surfaceBright = SurfaceWhite,
    surfaceDim = SurfaceDim,
    surfaceContainerLowest = SurfaceWhite,
    surfaceContainerLow = SurfaceContainerLow,
    surfaceContainer = CoolGreyBackground,
    surfaceContainerHigh = SurfaceContainerHigh,
    surfaceContainerHighest = SurfaceContainerHighest,
    surfaceTint = CobaltBlue,
    outline = OutlineGrey,
    outlineVariant = OutlineGrey,
    error = ErrorRed,
    onError = SurfaceWhite,
    errorContainer = ErrorRedLight,
    onErrorContainer = ErrorRed,
)

@Composable
fun FutuLinkTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = FutuLinkColorScheme,
        typography = FutuLinkTypography,
        content = content,
    )
}
