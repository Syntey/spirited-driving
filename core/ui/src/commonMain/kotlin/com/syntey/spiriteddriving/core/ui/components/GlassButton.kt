package com.syntey.spiriteddriving.core.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import com.syntey.spiriteddriving.core.ui.glass.GlassSurface
import com.syntey.spiriteddriving.core.ui.theme.AppTheme
import dev.chrisbanes.haze.HazeState

/**
 * A capsule text button on glass, for a button that floats over an illustration or a scrolling list.
 *
 * @param hazeState The state that the content behind the button is captured in, see [GlassSurface].
 */
@Composable
fun GlassButton(
    text: String,
    onClick: () -> Unit,
    hazeState: HazeState,
    modifier: Modifier = Modifier,
) {
    val style = AppTheme.platformStyle.glassButton
    GlassSurface(
        state = hazeState,
        shape = CircleShape,
        modifier = modifier
            .heightIn(min = style.height)
            .clip(CircleShape)
            .clickable(role = Role.Button, onClick = onClick),
    ) {
        Text(
            text = text,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = style.horizontalPadding),
            style = style.textStyle,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
