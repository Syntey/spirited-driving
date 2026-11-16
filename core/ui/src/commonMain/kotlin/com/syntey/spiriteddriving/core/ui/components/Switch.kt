package com.syntey.spiriteddriving.core.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import com.syntey.spiriteddriving.core.ui.preview.ComponentPreviews
import com.syntey.spiriteddriving.core.ui.theme.AppTheme
import com.syntey.spiriteddriving.core.ui.theme.Grid
import com.syntey.spiriteddriving.core.ui.theme.UiPlatform

private val TRACK_WIDTH = Grid.d13
private val TRACK_HEIGHT = Grid.d8
private val THUMB_SIZE = Grid.d7
private val THUMB_INSET = Grid.d0_5
private val THUMB_ON_X = TRACK_WIDTH - THUMB_SIZE - THUMB_INSET

/**
 * An on/off switch: Material 3's on Android, an iOS-style switch on iOS. With [onCheckedChange] null the switch isn't
 * clickable itself, so the row it's in toggles it.
 */
@Composable
fun ToggleSwitch(checked: Boolean, onCheckedChange: ((Boolean) -> Unit)?, modifier: Modifier = Modifier) {
    if (AppTheme.platformStyle.switch.isIosStyle) {
        IosStyleSwitch(checked = checked, onCheckedChange = onCheckedChange, modifier = modifier)
    } else {
        Switch(checked = checked, onCheckedChange = onCheckedChange, modifier = modifier)
    }
}

@Composable
private fun IosStyleSwitch(checked: Boolean, onCheckedChange: ((Boolean) -> Unit)?, modifier: Modifier = Modifier) {
    val trackColor by animateColorAsState(
        targetValue = if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHighest,
        label = "switch track",
    )
    val thumbX by animateDpAsState(targetValue = if (checked) THUMB_ON_X else THUMB_INSET, label = "switch thumb")
    val clickable = onCheckedChange?.let { Modifier.toggleable(value = checked, role = Role.Switch, onValueChange = it) }
        ?: Modifier

    Box(
        modifier = modifier
            .width(TRACK_WIDTH)
            .height(TRACK_HEIGHT)
            .clip(CircleShape)
            .background(trackColor)
            .then(clickable),
    ) {
        Box(
            modifier = Modifier
                .offset(x = thumbX, y = THUMB_INSET)
                .size(THUMB_SIZE)
                .shadow(elevation = Grid.d0_5, shape = CircleShape)
                .background(Color.White, CircleShape),
        )
    }
}

@ComponentPreviews
@Composable
private fun ToggleSwitchPreview() = Showcase { ToggleSwitchExamples() }

@ComponentPreviews
@Composable
private fun ToggleSwitchIosPreview() = Showcase(platform = UiPlatform.Ios) { ToggleSwitchExamples() }

@Composable
private fun ToggleSwitchExamples() {
    Row(
        Modifier.padding(Grid.d4),
        horizontalArrangement = Arrangement.spacedBy(Grid.d4),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ToggleSwitch(checked = true, onCheckedChange = {})
        ToggleSwitch(checked = false, onCheckedChange = {})
    }
}
