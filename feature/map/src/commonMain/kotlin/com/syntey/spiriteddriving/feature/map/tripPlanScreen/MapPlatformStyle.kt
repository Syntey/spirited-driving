package com.syntey.spiriteddriving.feature.map.tripPlanScreen

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import com.syntey.spiriteddriving.core.ui.theme.AppTheme
import com.syntey.spiriteddriving.core.ui.theme.Grid
import com.syntey.spiriteddriving.core.ui.theme.UiPlatform

/**
 * The map's values from the Platforms tables of its designs, both platforms side by side.
 *
 * @property sheetContentPadding Around the content of the map's sheet: on Android below the sheet's drag handle, on iOS
 * from the top of the native sheet, below its grabber.
 */
@Immutable
internal data class MapPlatformStyle(val sheetContentPadding: PaddingValues, val searchField: SearchFieldStyle) {
    companion object {
        /** The values of the platform [AppTheme] shows. */
        val current: MapPlatformStyle
            @Composable
            @ReadOnlyComposable
            get() = of(AppTheme.platform, MaterialTheme.typography)

        fun of(platform: UiPlatform, typography: Typography): MapPlatformStyle = when (platform) {
            UiPlatform.Android -> MapPlatformStyle(
                sheetContentPadding = PaddingValues(start = Grid.d4, top = Grid.d0, end = Grid.d4, bottom = Grid.d4),
                searchField = SearchFieldStyle(
                    height = Grid.d14,
                    iconSize = Grid.d6,
                    iconGap = Grid.d3,
                    horizontalPadding = Grid.d4,
                    textStyle = typography.bodyLarge,
                    isTranslucent = false,
                ),
            )

            UiPlatform.Ios -> MapPlatformStyle(
                sheetContentPadding = PaddingValues(start = Grid.d3, top = Grid.d5, end = Grid.d3, bottom = Grid.d3_5),
                searchField = SearchFieldStyle(
                    height = Grid.d12,
                    iconSize = Grid.d5,
                    iconGap = Grid.d2,
                    horizontalPadding = Grid.d4,
                    textStyle = typography.bodyLarge,
                    isTranslucent = true,
                ),
            )
        }
    }
}

/**
 * A search field. [height] is the least height. [isTranslucent]: a grey fill that lets the glass behind show through,
 * with a grey icon, as iOS fields have; otherwise a solid Material search bar.
 */
@Immutable
internal data class SearchFieldStyle(
    val height: Dp,
    val iconSize: Dp,
    val iconGap: Dp,
    val horizontalPadding: Dp,
    val textStyle: TextStyle,
    val isTranslucent: Boolean,
)
