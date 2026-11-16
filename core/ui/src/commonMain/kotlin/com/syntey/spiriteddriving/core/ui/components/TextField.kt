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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.ImeAction
import com.syntey.spiriteddriving.core.ui.preview.ComponentPreviews
import com.syntey.spiriteddriving.core.ui.theme.AppTheme
import com.syntey.spiriteddriving.core.ui.theme.Grid
import com.syntey.spiriteddriving.core.ui.theme.UiPlatform

/**
 * A text field. On iOS it's a filled card on the grouped card's colour; on Android a Material outlined field, which turns
 * primary while it has focus. With [maxLines] of 1 it's a single line; otherwise it grows with its text from [minLines],
 * such as the feedback message. A field that isn't [enabled] shows its text and takes no input.
 */
@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    minLines: Int = 1,
    maxLines: Int = Int.MAX_VALUE,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    val style = AppTheme.platformStyle.textField
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
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        // A single-line field takes no line breaks, the return key runs its IME action instead
        singleLine = maxLines == 1,
        minLines = minLines,
        maxLines = maxLines,
        cursorBrush = SolidColor(colors.primary),
        interactionSource = interactionSource,
        decorationBox = { field ->
            Box(Modifier.padding(style.contentPadding)) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = style.textStyle,
                        color = colors.onSurfaceVariant,
                        maxLines = maxLines,
                    )
                }
                field()
            }
        },
    )
}

@ComponentPreviews
@Composable
private fun TextFieldPreview() = Showcase {
    TextFieldExamples()
}

@ComponentPreviews
@Composable
private fun TextFieldIosPreview() = Showcase(platform = UiPlatform.Ios) {
    TextFieldExamples()
}

@Composable
private fun TextFieldExamples() {
    Column(Modifier.padding(Grid.d4), verticalArrangement = Arrangement.spacedBy(Grid.d3)) {
        AppTextField(
            value = "",
            onValueChange = {},
            placeholder = "Name",
            maxLines = 1,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        )
        AppTextField(value = "", onValueChange = {}, placeholder = "What's on your mind?", minLines = 3)
        AppTextField(
            value = "The lane closure near the lake was missing from the route. Otherwise the twisty bit was great.",
            onValueChange = {},
            placeholder = "What's on your mind?",
            minLines = 3,
        )
    }
}
