package com.syntey.spiriteddriving.core.ui.theme

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Typography
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp

/**
 * How the shared components look on each platform: the values in the Platforms tables of the screen designs. A screen is
 * written once; only these values differ, and both platforms' values are here side by side.
 */
@Immutable
data class PlatformStyle(
    val buttonLarge: ButtonStyle,
    val buttonMedium: ButtonStyle,
    val buttonSmall: ButtonStyle,
    val buttonToggle: ButtonStyle,
    val chip: ChipStyle,
    val segmented: SegmentedStyle,
    val switch: SwitchStyle,
    val textArea: TextAreaStyle,
    val glassButton: GlassButtonStyle,
    val bottomPanel: BottomPanelStyle,
    val closeButton: IconButtonStyle,
    val moreButton: IconButtonStyle,
    /** ⋯ on iOS, ⋮ on Android. */
    val moreIsHorizontal: Boolean,
    val sheetHeader: SheetHeaderStyle,
    /** Cards and list groups in a sheet or list. */
    val cardShape: RoundedCornerShape,
    val message: MessageStyle,
    val list: ListStyle,
    val menu: MenuStyle,
    /** Least size of anything tappable: 48dp on Android, 44pt on iOS. */
    val minTouchTarget: Dp,
) {
    companion object {
        internal fun of(platform: UiPlatform, typography: Typography): PlatformStyle = when (platform) {
            UiPlatform.Android -> android(typography)
            UiPlatform.Ios -> ios(typography)
        }

        private fun android(typography: Typography) = PlatformStyle(
            buttonLarge = ButtonStyle(
                height = Grid.d12,
                // Narrow enough that two buttons side by side fit their labels on one line
                contentPadding = PaddingValues(horizontal = Grid.d4, vertical = Grid.d2),
                iconSize = Grid.d4_5,
                textStyle = typography.labelLarge,
            ),
            buttonMedium = ButtonStyle(
                height = Grid.d10,
                contentPadding = PaddingValues(horizontal = Grid.d4, vertical = Grid.d1_5),
                iconSize = Grid.d4_5,
                textStyle = typography.labelLarge,
            ),
            buttonSmall = ButtonStyle(
                height = Grid.d8,
                contentPadding = PaddingValues(horizontal = Grid.d3, vertical = Grid.d1),
                iconSize = Grid.d4_5,
                textStyle = typography.labelLarge,
            ),
            buttonToggle = ButtonStyle(
                height = Grid.d14,
                contentPadding = PaddingValues(horizontal = Grid.d4, vertical = Grid.d2),
                iconSize = Grid.d6,
                textStyle = typography.labelLarge,
            ),
            chip = ChipStyle(
                height = Grid.d8,
                horizontalPadding = Grid.d4,
                textStyle = typography.labelLarge.withTabularFigures(),
                selectedFontWeight = FontWeight.Medium,
                isOutlined = true,
            ),
            segmented = SegmentedStyle(
                isOutlined = true,
                height = Grid.d10,
                textStyle = typography.labelLarge,
                selectedFontWeight = FontWeight.Medium,
            ),
            switch = SwitchStyle(isIosStyle = false),
            textArea = androidTextArea(typography),
            glassButton = GlassButtonStyle(height = Grid.d12, horizontalPadding = Grid.d5, textStyle = typography.labelLarge),
            bottomPanel = BottomPanelStyle(
                inset = Grid.d0,
                shape = RoundedCornerShape(topStart = Grid.d7, topEnd = Grid.d7),
                contentTop = Grid.d7,
                contentBottom = Grid.d3,
                contentSides = Grid.d6,
            ),
            closeButton = IconButtonStyle(size = Grid.d10, iconSize = Grid.d6, isFilled = false),
            moreButton = IconButtonStyle(size = Grid.d12, iconSize = Grid.d6, isFilled = false),
            moreIsHorizontal = false,
            sheetHeader = SheetHeaderStyle(titleStyle = typography.titleLarge, subtitleStyle = typography.bodyMedium),
            cardShape = RoundedCornerShape(Grid.d4),
            message = MessageStyle(isCapsule = false),
            list = androidList(typography),
            menu = MenuStyle(iconsAfterLabels = false),
            minTouchTarget = Grid.d12,
        )

        private fun androidTextArea(typography: Typography) = TextAreaStyle(
            isOutlined = true,
            contentPadding = PaddingValues(horizontal = Grid.d4, vertical = Grid.d4),
            textStyle = typography.bodyLarge,
        )

        private fun androidList(typography: Typography) = ListStyle(
            isInsetGrouped = false,
            groupInset = Grid.d0,
            rowPadding = PaddingValues(horizontal = Grid.d4, vertical = Grid.d2),
            oneLineHeight = Grid.d14,
            twoLineHeight = Grid.d18,
            iconSize = Grid.d10,
            showsChevrons = false,
            valueAfterLabel = false,
            headlineStyle = typography.bodyLarge,
            supportingStyle = typography.bodyMedium,
            headerStyle = typography.titleSmall,
            footerStyle = typography.bodySmall,
        )

        private fun ios(typography: Typography) = PlatformStyle(
            buttonLarge = ButtonStyle(
                height = Grid.d12_5,
                // Narrow enough that two buttons side by side fit their labels on one line
                contentPadding = PaddingValues(horizontal = Grid.d4, vertical = Grid.d2),
                iconSize = Grid.d4_5,
                textStyle = typography.titleMedium,
            ),
            buttonMedium = ButtonStyle(
                height = Grid.d9,
                contentPadding = PaddingValues(horizontal = Grid.d4, vertical = Grid.d1_5),
                iconSize = Grid.d4_5,
                textStyle = typography.titleSmall,
            ),
            buttonSmall = ButtonStyle(
                height = Grid.d8,
                contentPadding = PaddingValues(horizontal = Grid.d3, vertical = Grid.d1),
                iconSize = Grid.d4_5,
                textStyle = typography.titleSmall,
            ),
            buttonToggle = ButtonStyle(
                height = Grid.d14,
                contentPadding = PaddingValues(horizontal = Grid.d4, vertical = Grid.d2),
                iconSize = Grid.d6,
                textStyle = typography.titleMedium,
            ),
            chip = ChipStyle(
                height = Grid.d9,
                horizontalPadding = Grid.d3_5,
                textStyle = typography.bodyMedium.withTabularFigures(),
                selectedFontWeight = FontWeight.SemiBold,
                isOutlined = false,
            ),
            segmented = iosSegmented(typography),
            switch = SwitchStyle(isIosStyle = true),
            textArea = iosTextArea(typography),
            glassButton = GlassButtonStyle(height = Grid.d11, horizontalPadding = Grid.d4_5, textStyle = typography.titleSmall),
            bottomPanel = BottomPanelStyle(
                inset = Grid.d3,
                shape = RoundedCornerShape(Grid.d10),
                contentTop = Grid.d6_5,
                contentBottom = Grid.d3,
                contentSides = Grid.d5_5,
            ),
            closeButton = IconButtonStyle(size = Grid.d8, iconSize = Grid.d4, isFilled = true),
            moreButton = IconButtonStyle(size = Grid.d11, iconSize = Grid.d5_5, isFilled = false),
            moreIsHorizontal = true,
            sheetHeader = SheetHeaderStyle(titleStyle = typography.headlineSmall, subtitleStyle = typography.bodyMedium),
            cardShape = RoundedCornerShape(Grid.d5_5),
            message = MessageStyle(isCapsule = true),
            list = iosList(typography),
            menu = MenuStyle(iconsAfterLabels = true),
            minTouchTarget = Grid.d11,
        )

        private fun iosTextArea(typography: Typography) = TextAreaStyle(
            isOutlined = false,
            contentPadding = PaddingValues(horizontal = Grid.d4, vertical = Grid.d3),
            textStyle = typography.bodyLarge,
        )

        private fun iosSegmented(typography: Typography) = SegmentedStyle(
            isOutlined = false,
            height = Grid.d8,
            textStyle = typography.bodySmall,
            selectedFontWeight = FontWeight.SemiBold,
        )

        private fun iosList(typography: Typography) = ListStyle(
            isInsetGrouped = true,
            groupInset = Grid.d4,
            rowPadding = PaddingValues(horizontal = Grid.d4, vertical = Grid.d2_5),
            oneLineHeight = Grid.d11,
            twoLineHeight = Grid.d15,
            iconSize = Grid.d7_5,
            showsChevrons = true,
            valueAfterLabel = true,
            headlineStyle = typography.bodyLarge,
            supportingStyle = typography.bodyMedium,
            headerStyle = typography.bodySmall,
            footerStyle = typography.bodySmall,
        )
    }
}

/**
 * A button size. [height] is the least height; the button grows with larger text.
 */
@Immutable
data class ButtonStyle(val height: Dp, val contentPadding: PaddingValues, val iconSize: Dp, val textStyle: TextStyle)

/**
 * Choice chips, like the budget chips. Unselected chips are outlined on Android and have a tinted fill on iOS.
 */
@Immutable
data class ChipStyle(
    val height: Dp,
    val horizontalPadding: Dp,
    val textStyle: TextStyle,
    val selectedFontWeight: FontWeight,
    val isOutlined: Boolean,
)

/**
 * A segmented switch. [isOutlined]: Material's outlined segmented button, as on Android; otherwise an iOS segmented
 * control, a raised pill on a track.
 */
@Immutable
data class SegmentedStyle(val isOutlined: Boolean, val height: Dp, val textStyle: TextStyle, val selectedFontWeight: FontWeight)

/** An on/off switch. [isIosStyle]: the iOS-style switch drawn in primary; otherwise Material 3's switch. */
@Immutable
data class SwitchStyle(val isIosStyle: Boolean)

/**
 * A multi-line text field. [isOutlined]: Material's outlined field, as on Android; otherwise a filled card on the grouped
 * card's colour, as iOS forms have it.
 */
@Immutable
data class TextAreaStyle(val isOutlined: Boolean, val contentPadding: PaddingValues, val textStyle: TextStyle)

/**
 * A capsule button on glass, over an illustration or a scrolling list.
 */
@Immutable
data class GlassButtonStyle(val height: Dp, val horizontalPadding: Dp, val textStyle: TextStyle)

/**
 * A round icon button. [size] is the visible circle; the touch target is at least [PlatformStyle.minTouchTarget].
 * [isFilled] gives it a faint fill, as close buttons have in iOS sheets.
 */
@Immutable
data class IconButtonStyle(val size: Dp, val iconSize: Dp, val isFilled: Boolean)

/** The title, subtitle line and close button at the top of a sheet. */
@Immutable
data class SheetHeaderStyle(val titleStyle: TextStyle, val subtitleStyle: TextStyle)

/** A short message at the bottom of a screen or sheet. [isCapsule]: a centred capsule as on iOS, else Material's snackbar. */
@Immutable
data class MessageStyle(val isCapsule: Boolean)

/**
 * Rows and groups. [isInsetGrouped]: rows on a rounded card inset by [groupInset], with separators, as iOS lists;
 * otherwise Material list items on the sheet. [showsChevrons]: rows that open a screen end in a chevron.
 * [valueAfterLabel]: a row's value sits at its end, as on iOS, rather than below the label.
 */
@Immutable
data class ListStyle(
    val isInsetGrouped: Boolean,
    val groupInset: Dp,
    val rowPadding: PaddingValues,
    val oneLineHeight: Dp,
    val twoLineHeight: Dp,
    val iconSize: Dp,
    val showsChevrons: Boolean,
    val valueAfterLabel: Boolean,
    val headlineStyle: TextStyle,
    val supportingStyle: TextStyle,
    val headerStyle: TextStyle,
    val footerStyle: TextStyle,
)

/** A menu, such as the one a more button opens. [iconsAfterLabels]: icons at the end, as iOS menus have them. */
@Immutable
data class MenuStyle(val iconsAfterLabels: Boolean)

/**
 * A glass panel at the bottom of a full-screen view. It floats inset from the edges on iOS and spans the width on Android.
 *
 * @property inset Space between the panel and the screen edges at the sides and bottom.
 * @property contentBottom Space below the content, on top of what the system needs at the bottom of the screen.
 */
@Immutable
data class BottomPanelStyle(val inset: Dp, val shape: RoundedCornerShape, val contentTop: Dp, val contentBottom: Dp, val contentSides: Dp)

internal val LocalPlatformStyle = staticCompositionLocalOf { PlatformStyle.of(UiPlatform.Android, AndroidTypography) }
