package com.syntey.spiriteddriving.core.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.semantics.Role
import com.syntey.spiriteddriving.core.ui.preview.ComponentPreviews
import com.syntey.spiriteddriving.core.ui.preview.previewIcon
import com.syntey.spiriteddriving.core.ui.theme.AppTheme
import com.syntey.spiriteddriving.core.ui.theme.Grid
import com.syntey.spiriteddriving.core.ui.theme.UiPlatform

/** The colours of a selected [AppChip], taken from [ChipDefaultColors]. Unselected chips look the same in both. */
@Immutable
data class AppChipColors(val selectedContainerColor: Color, val selectedContentColor: Color)

/**
 * The colours of [AppChip]: a choice out of a row, or any number out of a set.
 */
object ChipDefaultColors {

    /** One choice out of a row, such as a budget: filled with primary. */
    @Composable
    fun choice(): AppChipColors = AppChipColors(
        selectedContainerColor = MaterialTheme.colorScheme.primary,
        selectedContentColor = MaterialTheme.colorScheme.onPrimary,
    )

    /**
     * Any number out of a set, such as the reasons after a rating: amber, as the theme gives ratings to tertiary. Pass a
     * check icon to [AppChip] so each selected chip shows one.
     */
    @Composable
    fun filter(): AppChipColors = AppChipColors(
        selectedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
        selectedContentColor = MaterialTheme.colorScheme.onTertiaryContainer,
    )
}

/**
 * A chip. Unselected, it's outlined on Android and has a tinted fill on iOS; selected, it takes [colors]. Without
 * [onClick] it only shows a value, such as the budget chips in First launch. With a [checkIcon] it's one of any number, a
 * checkbox, and a selected one shows the check; without it, it's one choice out of a row, a radio button.
 */
@Composable
fun AppChip(
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    colors: AppChipColors = ChipDefaultColors.choice(),
    checkIcon: Painter? = null,
) {
    val style = AppTheme.platformStyle.chip
    val scheme = MaterialTheme.colorScheme
    // The touch target is larger than the chip, so the chip is centred inside it
    Box(
        modifier = if (onClick != null) modifier.minimumInteractiveComponentSize() else modifier,
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            shape = CircleShape,
            color = when {
                selected -> colors.selectedContainerColor
                style.isOutlined -> Color.Transparent
                else -> scheme.surfaceContainerHighest
            },
            contentColor = when {
                selected -> colors.selectedContentColor
                style.isOutlined -> scheme.onSurfaceVariant
                else -> scheme.onSurface
            },
            border = if (style.isOutlined && !selected) BorderStroke(Grid.d0_25, scheme.outlineVariant) else null,
        ) {
            ChipContent(
                label = label,
                selected = selected,
                checkIcon = checkIcon,
                modifier = Modifier
                    .clip(CircleShape)
                    .then(onClick?.let { Modifier.chipClick(selected, checkIcon != null, it) } ?: Modifier),
            )
        }
    }
}

@Composable
private fun ChipContent(label: String, selected: Boolean, checkIcon: Painter?, modifier: Modifier = Modifier) {
    val style = AppTheme.platformStyle.chip
    Box(
        modifier = modifier
            .heightIn(min = style.height)
            .padding(horizontal = style.horizontalPadding),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Grid.d1_5),
        ) {
            if (selected && checkIcon != null) {
                Icon(painter = checkIcon, contentDescription = null, modifier = Modifier.size(Grid.d4_5))
            }
            Text(
                text = label,
                style = if (selected) style.textStyle.copy(fontWeight = style.selectedFontWeight) else style.textStyle,
                maxLines = 1,
            )
        }
    }
}

// A set takes any number of picks, so each chip is a checkbox; a choice out of a row is one radio button of several.
private fun Modifier.chipClick(selected: Boolean, isCheckbox: Boolean, onClick: () -> Unit): Modifier =
    if (isCheckbox) {
        toggleable(value = selected, role = Role.Checkbox, onValueChange = { onClick() })
    } else {
        selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
    }

@ComponentPreviews
@Composable
private fun ChipsPreview() = Showcase { ChipExamples() }

@ComponentPreviews
@Composable
private fun ChipsIosPreview() = Showcase(platform = UiPlatform.Ios) { ChipExamples() }

@Composable
private fun ChipExamples() {
    Column(Modifier.padding(Grid.d4), verticalArrangement = Arrangement.spacedBy(Grid.d3)) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(Grid.d2), verticalArrangement = Arrangement.spacedBy(Grid.d2)) {
            AppChip(label = "+15 min", selected = false, onClick = {})
            AppChip(label = "+30 min", selected = true, onClick = {})
            AppChip(label = "+45 min", selected = false, onClick = {})
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(Grid.d2), verticalArrangement = Arrangement.spacedBy(Grid.d2)) {
            AppChip(label = "Curvy roads", selected = true, onClick = {}, colors = ChipDefaultColors.filter(), checkIcon = previewIcon())
            AppChip(label = "Nice views", selected = true, onClick = {}, colors = ChipDefaultColors.filter(), checkIcon = previewIcon())
            AppChip(label = "Little traffic", selected = false, onClick = {
            }, colors = ChipDefaultColors.filter(), checkIcon = previewIcon())
            AppChip(label = "Smooth surface", selected = false, onClick = {
            }, colors = ChipDefaultColors.filter(), checkIcon = previewIcon())
        }
    }
}
