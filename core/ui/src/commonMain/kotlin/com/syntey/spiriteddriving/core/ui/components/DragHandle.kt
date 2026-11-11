package com.syntey.spiriteddriving.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.syntey.spiriteddriving.core.ui.preview.ComponentPreviews
import com.syntey.spiriteddriving.core.ui.theme.Grid

private const val DRAG_HANDLE_ALPHA = 0.4f

/**
 * The bar at the top of an Android sheet; iOS sheets show the system's grabber. Pass it as a sheet's `dragHandle`.
 * Material's own handle has 22 dp above and below it; this one has 12 dp, which keeps the collapsed sheet low.
 */
@Composable
fun DragHandle(modifier: Modifier = Modifier) {
    Box(
        modifier
            .padding(vertical = Grid.d3)
            .size(width = Grid.d8, height = Grid.d1)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = DRAG_HANDLE_ALPHA)),
    )
}

@ComponentPreviews
@Composable
private fun DragHandlePreview() = Showcase {
    DragHandle()
}
