@file:Suppress("MagicNumber")

package com.syntey.spiriteddriving.core.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Tonal palettes in HCT: racing red #BD162C, asphalt #4E5864, amber #F2A900, burnt orange error and a cool grey neutral
// with the asphalt hue. Light uses tone 40 for accents, dark tone 80, except the dark primary at tone 58, which stays red
// instead of turning pink. Every text colour meets WCAG AA (4.5:1) on its background in both themes.

internal val LightColors: ColorScheme = lightColorScheme(
    primary = Color(0xFFBD162C),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFFDAD8),
    onPrimaryContainer = Color(0xFF92001C),
    inversePrimary = Color(0xFFFFB3B1),
    secondary = Color(0xFF555F6B),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD9E3F2),
    onSecondaryContainer = Color(0xFF3E4853),
    tertiary = Color(0xFF7F5700),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFDEAD),
    onTertiaryContainer = Color(0xFF604100),
    error = Color(0xFF9F4200),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDBCB),
    onErrorContainer = Color(0xFF793000),
    background = Color(0xFFF9F9FC),
    onBackground = Color(0xFF1A1C1E),
    surface = Color(0xFFF9F9FC),
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = Color(0xFFDEE3EB),
    onSurfaceVariant = Color(0xFF42474E),
    surfaceTint = Color(0xFFBD162C),
    inverseSurface = Color(0xFF2F3033),
    inverseOnSurface = Color(0xFFF1F0F4),
    outline = Color(0xFF72777F),
    outlineVariant = Color(0xFFC2C7CF),
    scrim = Color(0xFF000000),
    surfaceBright = Color(0xFFF9F9FC),
    surfaceContainer = Color(0xFFEEEDF1),
    surfaceContainerHigh = Color(0xFFE8E8EB),
    surfaceContainerHighest = Color(0xFFE2E2E5),
    surfaceContainerLow = Color(0xFFF3F3F7),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceDim = Color(0xFFDADADD),
)

internal val DarkColors: ColorScheme = darkColorScheme(
    primary = Color(0xFFFC4952),
    onPrimary = Color(0xFF410007),
    primaryContainer = Color(0xFF92001C),
    onPrimaryContainer = Color(0xFFFFDAD8),
    inversePrimary = Color(0xFFBD162C),
    secondary = Color(0xFFBDC7D5),
    onSecondary = Color(0xFF27313C),
    secondaryContainer = Color(0xFF3E4853),
    onSecondaryContainer = Color(0xFFD9E3F2),
    tertiary = Color(0xFFFFBA3B),
    onTertiary = Color(0xFF432C00),
    tertiaryContainer = Color(0xFF604100),
    onTertiaryContainer = Color(0xFFFFDEAD),
    error = Color(0xFFFFB692),
    onError = Color(0xFF562000),
    errorContainer = Color(0xFF793000),
    onErrorContainer = Color(0xFFFFDBCB),
    background = Color(0xFF121316),
    onBackground = Color(0xFFE2E2E5),
    surface = Color(0xFF121316),
    onSurface = Color(0xFFE2E2E5),
    surfaceVariant = Color(0xFF42474E),
    onSurfaceVariant = Color(0xFFC2C7CF),
    surfaceTint = Color(0xFFFC4952),
    inverseSurface = Color(0xFFE2E2E5),
    inverseOnSurface = Color(0xFF2F3033),
    outline = Color(0xFF8C9199),
    outlineVariant = Color(0xFF42474E),
    scrim = Color(0xFF000000),
    surfaceBright = Color(0xFF38393C),
    surfaceContainer = Color(0xFF1E2022),
    surfaceContainerHigh = Color(0xFF282A2D),
    surfaceContainerHighest = Color(0xFF333538),
    surfaceContainerLow = Color(0xFF1A1C1E),
    surfaceContainerLowest = Color(0xFF0C0E11),
    surfaceDim = Color(0xFF121316),
)

/**
 * Colours outside the Material roles, each with a light and a dark value. A new one goes here, not in a new composition local.
 *
 * Route lines are the same on the map and in the route list. The fastest route is asphalt grey, as it's the reference, not
 * the goal. Blue, green and yellow are left out, since the base maps use them for the user's location, water, parks and
 * major roads.
 *
 * [routeCasing] is the casing around a route line on the map, so the line reads over any road colour.
 *
 * [listGroup] is the card behind grouped rows on iOS, white in light and raised in dark, as in iOS Settings.
 * [segmentSelected] is the raised pill of the selected segment in an iOS segmented control: white in light, a light grey
 * in dark, so it stands out from the track.
 * [systemFill] is the translucent grey of iOS fields on glass, such as the search field on the map's sheet.
 */
@Immutable
data class CustomColors(
    val routeFastest: Color,
    val routeFun1: Color,
    val routeFun2: Color,
    val routeFun3: Color,
    val routeCasing: Color,
    val listGroup: Color,
    val segmentSelected: Color,
    val systemFill: Color,
)

internal val LightCustomColors = CustomColors(
    routeFastest = Color(0xFF616C78),
    routeFun1 = Color(0xFFBD162C),
    routeFun2 = Color(0xFF6E37DD),
    routeFun3 = Color(0xFF008488),
    routeCasing = Color(0xFFFFFFFF),
    listGroup = Color(0xFFFFFFFF),
    segmentSelected = Color(0xFFFFFFFF),
    systemFill = Color(0x1F767680),
)

internal val DarkCustomColors = CustomColors(
    routeFastest = Color(0xFFA2ACBA),
    routeFun1 = Color(0xFFFC4952),
    routeFun2 = Color(0xFFAC8AFF),
    routeFun3 = Color(0xFF00BEC3),
    routeCasing = Color(0xFF121212),
    listGroup = Color(0xFF282A2D),
    segmentSelected = Color(0xFF5B5F66),
    systemFill = Color(0x42767680),
)

internal val LocalCustomColors = staticCompositionLocalOf { LightCustomColors }
