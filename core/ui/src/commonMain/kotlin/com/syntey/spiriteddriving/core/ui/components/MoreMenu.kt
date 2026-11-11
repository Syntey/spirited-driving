package com.syntey.spiriteddriving.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import com.syntey.spiriteddriving.core.ui.preview.ComponentPreviews
import com.syntey.spiriteddriving.core.ui.preview.previewIcon
import com.syntey.spiriteddriving.core.ui.theme.AppTheme
import com.syntey.spiriteddriving.core.ui.theme.Grid
import com.syntey.spiriteddriving.core.ui.theme.UiPlatform

/** One action in a [MoreMenuButton]'s menu. A destructive one, such as Delete, is in the error colour. */
@Immutable
data class MoreMenuItem(val label: String, val onClick: () -> Unit, val icon: Painter? = null, val isDestructive: Boolean = false)

/**
 * A [MoreButton] that opens a menu of [items]: corners 12, on surfaceContainer, icons before the labels on Android and
 * after them on iOS. Choosing an item closes the menu first.
 */
@Composable
fun MoreMenuButton(
    items: List<MoreMenuItem>,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier) {
        MoreButton(onClick = { expanded = true }, contentDescription = contentDescription)
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            shape = RoundedCornerShape(Grid.d3),
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ) {
            MoreMenuItems(
                items = items,
                onChosen = { item ->
                    expanded = false
                    item.onClick()
                },
            )
        }
    }
}

@Composable
private fun ColumnScope.MoreMenuItems(items: List<MoreMenuItem>, onChosen: (MoreMenuItem) -> Unit) {
    val iconsAfterLabels = AppTheme.platformStyle.menu.iconsAfterLabels
    val destructiveColors = MenuDefaults.itemColors(
        textColor = MaterialTheme.colorScheme.error,
        leadingIconColor = MaterialTheme.colorScheme.error,
        trailingIconColor = MaterialTheme.colorScheme.error,
    )
    items.forEach { item ->
        val icon: (@Composable () -> Unit)? = item.icon?.let { painter ->
            { Icon(painter = painter, contentDescription = null) }
        }
        DropdownMenuItem(
            text = { Text(item.label, style = MaterialTheme.typography.bodyLarge) },
            onClick = { onChosen(item) },
            leadingIcon = if (iconsAfterLabels) null else icon,
            trailingIcon = if (iconsAfterLabels) icon else null,
            colors = if (item.isDestructive) destructiveColors else MenuDefaults.itemColors(),
        )
    }
}

@Composable
private fun MoreMenuExamples() {
    val items = listOf(
        MoreMenuItem(label = "Rename", onClick = {}, icon = previewIcon()),
        MoreMenuItem(label = "Delete", onClick = {}, icon = previewIcon(), isDestructive = true),
    )
    Column(Modifier.padding(Grid.d4), verticalArrangement = Arrangement.spacedBy(Grid.d3)) {
        MoreMenuButton(items = items, contentDescription = "More")
        // A popup doesn't render in a static preview, so the items are shown on their own
        Surface(shape = RoundedCornerShape(Grid.d3), color = MaterialTheme.colorScheme.surfaceContainer) {
            Column(Modifier.width(IntrinsicSize.Max)) {
                MoreMenuItems(items = items, onChosen = {})
            }
        }
    }
}

@ComponentPreviews
@Composable
private fun MoreMenuPreview() = Showcase {
    MoreMenuExamples()
}

@ComponentPreviews
@Composable
private fun MoreMenuIosPreview() = Showcase(platform = UiPlatform.Ios) {
    MoreMenuExamples()
}
