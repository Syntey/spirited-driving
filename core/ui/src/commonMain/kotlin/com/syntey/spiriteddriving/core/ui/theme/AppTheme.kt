package com.syntey.spiriteddriving.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import com.arkivanov.decompose.extensions.compose.stack.animation.LocalStackAnimationProvider

/**
 * The app's theme: a Material 3 theme with the app's own colours (no dynamic colour), light or dark as the system is, and
 * the type sizes and component styles of [platform].
 *
 * @param platform Whose look to take; previews pass [UiPlatform.Ios] to show the iOS look.
 */
@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    platform: UiPlatform = UiPlatform.Current,
    content: @Composable () -> Unit,
) {
    val typography = when (platform) {
        UiPlatform.Android -> AndroidTypography
        UiPlatform.Ios -> IosTypography
    }
    val platformStyle = remember(platform) { PlatformStyle.of(platform, typography) }

    CompositionLocalProvider(
        LocalStackAnimationProvider provides TemplateStackAnimationProvider,
        LocalUiPlatform provides platform,
        LocalCustomDimensions provides customDimensions,
        LocalPlatformStyle provides platformStyle,
        LocalMinimumInteractiveComponentSize provides platformStyle.minTouchTarget,
        LocalCustomColors provides if (darkTheme) DarkCustomColors else LightCustomColors,
    ) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColors else LightColors,
            typography = typography,
            shapes = AppShapes,
        ) {
            ProvideAccessibilitySettings(content)
        }
    }
}

/**
 * What the theme provides besides Material's colours, type and shapes.
 */
object AppTheme {

    val platform: UiPlatform
        @Composable
        @ReadOnlyComposable
        get() = LocalUiPlatform.current

    val platformStyle: PlatformStyle
        @Composable
        @ReadOnlyComposable
        get() = LocalPlatformStyle.current

    val dimensions: Dimensions
        @Composable
        @ReadOnlyComposable
        get() = LocalCustomDimensions.current

    val customColors: CustomColors
        @Composable
        @ReadOnlyComposable
        get() = LocalCustomColors.current

    /**
     * Whether the user asked for less motion: Reduce Motion on iOS, animations turned off on Android. Animations that move
     * things across the screen fade instead.
     */
    val reduceMotion: Boolean
        @Composable
        @ReadOnlyComposable
        get() = LocalReduceMotion.current
}

internal val LocalCustomDimensions = staticCompositionLocalOf<Dimensions> { error("No Dimensions were provided") }

private val LocalUiPlatform = staticCompositionLocalOf { UiPlatform.Current }

internal val LocalReduceMotion = compositionLocalOf { false }

/**
 * Reads the system's accessibility settings that change how the UI draws: [AppTheme.reduceMotion], and on iOS Reduce
 * Transparency and Increase Contrast for glass, which Haze doesn't read by itself.
 */
@Composable
internal expect fun ProvideAccessibilitySettings(content: @Composable () -> Unit)
