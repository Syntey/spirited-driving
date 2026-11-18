package com.syntey.spiriteddriving.feature.routes.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.unit.Dp
import com.syntey.spiriteddriving.core.ui.theme.AppTheme
import com.syntey.spiriteddriving.core.ui.theme.Grid
import com.syntey.spiriteddriving.core.ui.theme.UiPlatform

/**
 * The values of the routes' screens from the Platforms tables of their designs, both platforms side by side.
 *
 * @property sheetContentPadding Around the content of the rating sheet: on Android below the sheet's drag handle, on iOS from
 * the top of the native sheet, below its grabber.
 * @property sheetSpacing Between the items of the rating sheet.
 */
@Immutable
internal data class RoutesPlatformStyle(val sheetContentPadding: PaddingValues, val sheetSpacing: Dp) {
    companion object {
        /** The values of the platform [AppTheme] shows. */
        val current: RoutesPlatformStyle
            @Composable
            @ReadOnlyComposable
            get() = of(AppTheme.platform)

        fun of(platform: UiPlatform): RoutesPlatformStyle = when (platform) {
            UiPlatform.Android -> RoutesPlatformStyle(
                sheetContentPadding = PaddingValues(start = Grid.d4, top = Grid.d0, end = Grid.d4, bottom = Grid.d4),
                sheetSpacing = Grid.d3,
            )

            UiPlatform.Ios -> RoutesPlatformStyle(
                sheetContentPadding = PaddingValues(start = Grid.d3, top = Grid.d5, end = Grid.d3, bottom = Grid.d3_5),
                sheetSpacing = Grid.d2_5,
            )
        }
    }
}
