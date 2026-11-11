@file:Suppress("MagicNumber")

package com.syntey.spiriteddriving.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

// The system font on each platform (`FontFamily.Default`, the Material 3 default): SF Pro on iOS, Roboto on Android. Sizes
// follow each platform, so text matches the native parts around it. Display roles are not used.

private val Material = Typography()

/**
 * The Material 3 type scale with bolder headlines and titles.
 */
internal val AndroidTypography = Typography(
    headlineLarge = Material.headlineLarge.copy(fontWeight = FontWeight.Bold),
    headlineMedium = Material.headlineMedium.copy(fontWeight = FontWeight.Bold),
    headlineSmall = Material.headlineSmall.copy(fontWeight = FontWeight.Bold),
    titleLarge = Material.titleLarge.copy(fontWeight = FontWeight.SemiBold),
    titleMedium = Material.titleMedium.copy(fontWeight = FontWeight.SemiBold),
    titleSmall = Material.titleSmall.copy(fontWeight = FontWeight.SemiBold),
    bodyLarge = Material.bodyLarge.copy(fontWeight = FontWeight.Normal),
    bodyMedium = Material.bodyMedium.copy(fontWeight = FontWeight.Normal),
    bodySmall = Material.bodySmall.copy(fontWeight = FontWeight.Normal),
    labelLarge = Material.labelLarge.copy(fontWeight = FontWeight.Medium),
    labelMedium = Material.labelMedium.copy(fontWeight = FontWeight.Medium),
    labelSmall = Material.labelSmall.copy(fontWeight = FontWeight.Medium),
)

/**
 * The Material 3 roles at the sizes of the iOS text styles each one stands for, named in the comments.
 */
internal val IosTypography = Typography(
    headlineLarge = Material.headlineLarge.ios(34.sp, 41.sp, FontWeight.Bold), // Large Title
    headlineMedium = Material.headlineMedium.ios(28.sp, 34.sp, FontWeight.Bold), // Title 1
    headlineSmall = Material.headlineSmall.ios(22.sp, 28.sp, FontWeight.Bold), // Title 2
    titleLarge = Material.titleLarge.ios(20.sp, 25.sp, FontWeight.SemiBold), // Title 3
    titleMedium = Material.titleMedium.ios(17.sp, 22.sp, FontWeight.SemiBold), // Headline
    titleSmall = Material.titleSmall.ios(15.sp, 20.sp, FontWeight.SemiBold), // Subheadline
    bodyLarge = Material.bodyLarge.ios(17.sp, 22.sp, FontWeight.Normal), // Body
    bodyMedium = Material.bodyMedium.ios(15.sp, 20.sp, FontWeight.Normal), // Subheadline
    bodySmall = Material.bodySmall.ios(13.sp, 18.sp, FontWeight.Normal), // Footnote
    labelLarge = Material.labelLarge.ios(16.sp, 21.sp, FontWeight.Medium), // Callout
    labelMedium = Material.labelMedium.ios(12.sp, 16.sp, FontWeight.Medium), // Caption 1
    labelSmall = Material.labelSmall.ios(11.sp, 13.sp, FontWeight.Medium), // Caption 2
)

// SF Pro spaces its own letters, so the Material letter spacing is left out.
private fun TextStyle.ios(fontSize: TextUnit, lineHeight: TextUnit, fontWeight: FontWeight) =
    copy(fontSize = fontSize, lineHeight = lineHeight, fontWeight = fontWeight, letterSpacing = 0.sp)

/**
 * Tabular figures, for times, distances and budgets: values line up in lists and don't shift while they change.
 */
fun TextStyle.withTabularFigures(): TextStyle = copy(fontFeatureSettings = "tnum")
