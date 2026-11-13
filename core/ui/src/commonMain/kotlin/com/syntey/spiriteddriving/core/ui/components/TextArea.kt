package com.syntey.spiriteddriving.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.Shape
import com.syntey.spiriteddriving.core.ui.preview.ComponentPreviews
import com.syntey.spiriteddriving.core.ui.theme.AppTheme
import com.syntey.spiriteddriving.core.ui.theme.Grid
import com.syntey.spiriteddriving.core.ui.theme.UiPlatform

/**
 * A multi-line text field that grows with its text, such as the feedback message. On iOS it's a filled card on the grouped
 * card's colour; on Android a Material outlined field, which turns primary while it has focus. A field that isn't [enabled]
 * shows its text and takes no input.
 */
@Composable
fun TextArea(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    minLines: Int = DEFAULT_MIN_LINES,
) {
    val style = AppTheme.platformStyle.textArea
    val shape: Shape = AppTheme.platformStyle.cardShape
    val colors = MaterialTheme.colorScheme
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()

    val container = if (style.isOutlined) Color.Transparent else AppTheme.customColors.listGroup
    val outline = when {
        !style.isOutlined -> Modifier
        focused -> Modifier.border(Grid.d0_5, colors.primary, shape)
        else -> Modifier.border(Grid.d0_25, colors.outline, shape)
    }

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(container)
            .then(outline),
        enabled = enabled,
        textStyle = style.textStyle.copy(color = colors.onSurface),
        cursorBrush = SolidColor(colors.primary),
        minLines = minLines,
        interactionSource = interactionSource,
        decorationBox = { field ->
            Box(Modifier.padding(style.contentPadding)) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = style.textStyle,
                        color = colors.onSurfaceVariant,
                    )
                }
                field()
            }
        },
    )
}

// Three lines show the field as a box to write in, and it grows from there
private const val DEFAULT_MIN_LINES = 3

@ComponentPreviews
@Composable
private fun TextAreaPreview() = Showcase {
    TextAreaExamples()
}

@ComponentPreviews
@Composable
private fun TextAreaIosPreview() = Showcase(platform = UiPlatform.Ios) {
    TextAreaExamples()
}

@Composable
private fun TextAreaExamples() {
    Column(Modifier.padding(Grid.d4), verticalArrangement = Arrangement.spacedBy(Grid.d3)) {
        TextArea(value = "", onValueChange = {}, placeholder = "What's on your mind?")
        TextArea(
            value = "The lane closure near the lake was missing from the route. Otherwise the twisty bit was great.",
            onValueChange = {},
            placeholder = "What's on your mind?",
        )
    }
}
