package com.syntey.spiriteddriving.feature.map.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import com.syntey.spiriteddriving.core.resources.*
import com.syntey.spiriteddriving.core.ui.theme.AppTheme
import com.syntey.spiriteddriving.core.ui.theme.Grid
import com.syntey.spiriteddriving.core.ui.theme.IconButtonStyle
import com.syntey.spiriteddriving.core.ui.theme.UiPlatform
import dev.icerock.moko.resources.ImageResource

/**
 * The map's values from the Platforms tables of its designs, both platforms side by side.
 *
 * @property sheetContentPadding Around the content of the map's sheet: on Android below the sheet's drag handle, on iOS
 * from the top of the native sheet, below its grabber.
 * @property sheetSpacing Between the items of the sheet's content.
 * @property preciseLocationIcon The icon of the "Precise location is off" note: the locate target on Android, an outline arrow
 *   like the locate button on iOS.
 * @property closesSearchWithBackArrow Closing the search: a back arrow in place of the field's search icon on Android, a
 *   Cancel text button next to the field on iOS.
 * @property routeList The route list's rows: on a card on iOS, on the sheet on Android.
 */
@Immutable
internal data class MapPlatformStyle(
    val sheetContentPadding: PaddingValues,
    val sheetSpacing: Dp,
    val preciseLocationIcon: ImageResource,
    val closesSearchWithBackArrow: Boolean,
    val searchField: SearchFieldStyle,
    val routeList: RouteListStyle,
) {
    companion object {
        /** The values of the platform [AppTheme] shows. */
        val current: MapPlatformStyle
            @Composable
            @ReadOnlyComposable
            get() = of(AppTheme.platform, MaterialTheme.typography)

        fun of(platform: UiPlatform, typography: Typography): MapPlatformStyle = when (platform) {
            UiPlatform.Android -> MapPlatformStyle(
                sheetContentPadding = PaddingValues(start = Grid.d4, top = Grid.d0, end = Grid.d4, bottom = Grid.d4),
                sheetSpacing = Grid.d3,
                preciseLocationIcon = MR.images.locate,
                closesSearchWithBackArrow = true,
                searchField = SearchFieldStyle(
                    height = Grid.d14,
                    iconSize = Grid.d6,
                    iconGap = Grid.d3,
                    horizontalPadding = Grid.d4,
                    textStyle = typography.bodyLarge,
                    isTranslucent = false,
                    actionButton = IconButtonStyle(size = Grid.d10, iconSize = Grid.d6, isFilled = false),
                ),
                routeList = RouteListStyle(
                    rowsOnCard = false,
                    isSelectedRowLifted = false,
                    rowShape = RoundedCornerShape(Grid.d4),
                ),
            )

            UiPlatform.Ios -> MapPlatformStyle(
                sheetContentPadding = PaddingValues(start = Grid.d3, top = Grid.d5, end = Grid.d3, bottom = Grid.d3_5),
                sheetSpacing = Grid.d2_5,
                preciseLocationIcon = MR.images.locate_arrow,
                closesSearchWithBackArrow = false,
                searchField = SearchFieldStyle(
                    height = Grid.d12,
                    iconSize = Grid.d5,
                    iconGap = Grid.d2,
                    horizontalPadding = Grid.d4,
                    textStyle = typography.bodyLarge,
                    isTranslucent = true,
                    actionButton = IconButtonStyle(size = Grid.d6, iconSize = Grid.d4, isFilled = false),
                ),
                routeList = RouteListStyle(
                    rowsOnCard = true,
                    isSelectedRowLifted = true,
                    // The card's corners less its 4dp inset, so the rows' corners and the ring stay concentric with it
                    rowShape = RoundedCornerShape(Grid.d4_5),
                ),
            )
        }
    }
}

/**
 * A search field. [height] is the least height. [isTranslucent]: a grey fill that lets the glass behind show through,
 * with a grey icon, as iOS fields have; otherwise a solid Material search bar.
 * [actionButton] is the back arrow and the clear button inside the field.
 */
@Immutable
internal data class SearchFieldStyle(
    val height: Dp,
    val iconSize: Dp,
    val iconGap: Dp,
    val horizontalPadding: Dp,
    val textStyle: TextStyle,
    val isTranslucent: Boolean,
    val actionButton: IconButtonStyle,
)

/**
 * The route list's rows. [rowsOnCard]: the rows sit on one translucent card, as the cells of an iOS group do; otherwise
 * they sit on the sheet. [isSelectedRowLifted]: the selected row is a light lift off the card; otherwise a grey fill.
 * [rowShape] is the corner of each row, which the selection ring follows.
 */
@Immutable
internal data class RouteListStyle(val rowsOnCard: Boolean, val isSelectedRowLifted: Boolean, val rowShape: RoundedCornerShape)
