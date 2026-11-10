package com.syntey.spiriteddriving.core.ui.preview

import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_NO
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_TYPE_NORMAL
import androidx.compose.ui.tooling.preview.Preview

@Preview(
    name = "1 - Light mode",
    group = "UI mode",
    uiMode = UI_MODE_NIGHT_NO or UI_MODE_TYPE_NORMAL,
)
@Preview(
    name = "2 - Dark mode",
    group = "UI mode",
    uiMode = UI_MODE_NIGHT_YES or UI_MODE_TYPE_NORMAL,
)
annotation class DarkLightPreviews

// 0.85 is the smallest font scale Android offers, close to iOS's smallest Dynamic Type size.
@Preview(
    name = "3 - Small font",
    group = "Font scales",
    fontScale = 0.85f,
)
@Preview(
    name = "4 - Large font",
    group = "Font scales",
    fontScale = 1.5f,
)
annotation class FontScalePreviews

@Preview(name = "7 - PHONE PIXEL XHDPI", group = "phoneScreenDensity", device = "spec:width=1080px,height=2340px,dpi=320")
annotation class PhoneScreenDensityXHDPIPreview

@Preview(name = "8 - PHONE PIXEL XXHDPI", group = "phoneScreenDensity", device = "spec:width=1080px,height=2340px,dpi=480")
annotation class PhoneScreenDensityXXHDPIPreview

@Preview(name = "9 - PHONE PIXEL XXXHDPI", group = "phoneScreenDensity", device = "spec:width=1080px,height=2340px,dpi=640")
annotation class PhoneScreenDensityXXXHDPIPreview

@Preview(
    fontScale = 1.5f,
    name = "5 - Large screen density font scale",
    group = "phoneScreenDensityFontScale",
    device = "spec:width=1080px,height=2340px,dpi=480",
)
@Preview(
    fontScale = 2.0f,
    name = "6 - Largest screen density font scale",
    group = "phoneScreenDensityFontScale",
    device = "spec:width=1080px,height=2340px,dpi=640",
)
annotation class ScreenDensityWithFontScalePreviews

@PhoneScreenDensityXHDPIPreview
@PhoneScreenDensityXXHDPIPreview
@PhoneScreenDensityXXXHDPIPreview
annotation class PhoneScreenDensityPreviews

/**
 * Previews of a composable smaller than a screen, like a component in `:core:ui`: light and dark, small and large font.
 */
@DarkLightPreviews
@FontScalePreviews
annotation class ComponentPreviews

/**
 * Previews of a screen's content: light and dark, and large fonts on dense phone screens.
 */
@DarkLightPreviews
@ScreenDensityWithFontScalePreviews
annotation class ScreenPreviews
