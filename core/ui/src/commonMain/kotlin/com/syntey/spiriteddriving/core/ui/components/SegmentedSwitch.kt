package com.syntey.spiriteddriving.core.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import com.syntey.spiriteddriving.core.ui.preview.ComponentPreviews
import com.syntey.spiriteddriving.core.ui.theme.AppTheme
import com.syntey.spiriteddriving.core.ui.theme.Grid
import com.syntey.spiriteddriving.core.ui.theme.SegmentedStyle
import com.syntey.spiriteddriving.core.ui.theme.UiPlatform

/** One segment of a [SegmentedSwitch]. [accessibilityLabel] is what screen readers hear, such as "Kilometres" for "km". */
@Immutable
data class SegmentOption(val label: String, val accessibilityLabel: String = label)

/**
 * A choice of two or three, such as km / mi: a segmented control whose raised pill slides to the selected segment, or
 * jumps there with reduced motion.
 */
@Composable
fun SegmentedSwitch(
    options: List<SegmentOption>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    require(options.isNotEmpty()) { "A segmented switch needs at least one option" }
    PillSegmentedControl(options, selectedIndex, onSelect, AppTheme.platformStyle.segmented, modifier)
}

@Composable
private fun PillSegmentedControl(
    options: List<SegmentOption>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    style: SegmentedStyle,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    // The segments' row is measured, not the track, so the pill is exactly one segment wide with no padding to subtract
    var rowWidthPx by remember { mutableIntStateOf(0) }
    var hasWidth by remember { mutableStateOf(false) }
    LaunchedEffect(rowWidthPx > 0) { if (rowWidthPx > 0) hasWidth = true }
    val segmentWidth = with(density) { (rowWidthPx / options.size).toDp() }
    // The first width snaps the pill into place, so a switch that opens on another segment doesn't slide in from the first
    val pillOffset by animateDpAsState(
        targetValue = segmentWidth * selectedIndex,
        animationSpec = if (!hasWidth || AppTheme.reduceMotion) snap() else spring(stiffness = Spring.StiffnessMediumLow),
    )
    Box(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .height(style.height)
            .width(IntrinsicSize.Max)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = TRACK_ALPHA))
            .padding(Grid.d0_5),
    ) {
        if (rowWidthPx > 0) {
            Box(
                Modifier
                    .offset(x = pillOffset)
                    .width(segmentWidth)
                    .fillMaxHeight()
                    .shadow(Grid.d0_5, CircleShape)
                    .clip(CircleShape)
                    .background(AppTheme.customColors.segmentSelected),
            )
        }
        Row(
            Modifier
                .fillMaxHeight()
                .onSizeChanged { rowWidthPx = it.width }
                .selectableGroup(),
        ) {
            options.forEachIndexed { index, option ->
                PillSegment(
                    option = option,
                    selected = index == selectedIndex,
                    style = style,
                    onClick = { onSelect(index) },
                )
            }
        }
    }
}

// Segments don't ripple, so the segment shows its selection by the pill alone
@Composable
private fun RowScope.PillSegment(option: SegmentOption, selected: Boolean, style: SegmentedStyle, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            )
            .semantics { contentDescription = option.accessibilityLabel }
            .padding(horizontal = Grid.d4),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = option.label,
            style = style.textStyle.selectedIf(selected, style),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
        )
    }
}

private fun TextStyle.selectedIf(selected: Boolean, style: SegmentedStyle): TextStyle =
    if (selected) copy(fontWeight = style.selectedFontWeight) else this

// A faint track that works on solid and glass sheets, like iOS's fill colours
private const val TRACK_ALPHA = 0.08f

@ComponentPreviews
@Composable
private fun SegmentedSwitchPreview() = Showcase { SegmentedSwitchExamples() }

@ComponentPreviews
@Composable
private fun SegmentedSwitchIosPreview() = Showcase(platform = UiPlatform.Ios) { SegmentedSwitchExamples() }

@Composable
private fun SegmentedSwitchExamples() {
    Column(Modifier.padding(Grid.d4), verticalArrangement = Arrangement.spacedBy(Grid.d3)) {
        var distanceUnit by remember { mutableIntStateOf(0) }
        SegmentedSwitch(
            options = listOf(SegmentOption("km", "Kilometres"), SegmentOption("mi", "Miles")),
            selectedIndex = distanceUnit,
            onSelect = { distanceUnit = it },
        )
        var budgetUnit by remember { mutableIntStateOf(1) }
        SegmentedSwitch(
            options = listOf(SegmentOption("min", "Minutes"), SegmentOption("km", "Kilometres")),
            selectedIndex = budgetUnit,
            onSelect = { budgetUnit = it },
        )
    }
}
