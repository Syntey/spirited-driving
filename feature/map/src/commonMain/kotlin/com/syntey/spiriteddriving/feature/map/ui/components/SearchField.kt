package com.syntey.spiriteddriving.feature.map.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import com.syntey.spiriteddriving.core.resources.*
import com.syntey.spiriteddriving.core.ui.components.AppIconButton
import com.syntey.spiriteddriving.core.ui.components.CarSpinner
import com.syntey.spiriteddriving.core.ui.components.IconAction
import com.syntey.spiriteddriving.core.ui.components.Showcase
import com.syntey.spiriteddriving.core.ui.preview.ComponentPreviews
import com.syntey.spiriteddriving.core.ui.theme.AppTheme
import com.syntey.spiriteddriving.core.ui.theme.Grid
import com.syntey.spiriteddriving.core.ui.theme.UiPlatform
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.stringResource

/**
 * The search field, such as "Where to?" on the map's sheet: a capsule with a translucent fill on iOS, a Material search
 * bar on Android. [back], on Android while the search is open, takes the search icon's place. [clear] shows while there's
 * text, and [onFocus] runs each time the field gets focus. [isLoading] shows progress in the clear button's place, and
 * [requestsFocus] focuses the field as it appears.
 */
@Composable
internal fun SearchField(
    text: String,
    onTextChange: (String) -> Unit,
    placeholder: String,
    icon: Painter,
    clear: IconAction,
    onFocus: () -> Unit,
    modifier: Modifier = Modifier,
    back: IconAction? = null,
    isLoading: Boolean = false,
    requestsFocus: Boolean = false,
) {
    val style = MapPlatformStyle.current.searchField
    val keyboard = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(requestsFocus) {
        if (requestsFocus) focusRequester.requestFocus()
    }
    // Typing goes into this copy at once, so it never waits for the view state. The only change from outside is clearing the
    // text, by Clear, Close or choosing a place.
    var value by remember { mutableStateOf(TextFieldValue(text, selection = TextRange(text.length))) }
    LaunchedEffect(text.isEmpty()) {
        if (text.isEmpty()) value = TextFieldValue()
    }
    val container = if (style.isTranslucent) {
        AppTheme.customColors.systemFill
    } else {
        MaterialTheme.colorScheme.surfaceContainerHigh
    }
    // A back arrow takes the search icon's place exactly: its touch target reaches past the icon into the padding and the gap
    val reach = if (back != null) {
        (maxOf(LocalMinimumInteractiveComponentSize.current, style.actionButton.size) - style.actionButton.iconSize) / 2
    } else {
        Grid.d0
    }
    Row(
        modifier = modifier
            .heightIn(min = style.height)
            .clip(CircleShape)
            .background(container)
            .padding(start = (style.horizontalPadding - reach).coerceAtLeast(Grid.d0), end = style.horizontalPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (back != null) {
            AppIconButton(icon = back.icon, contentDescription = back.label, onClick = back.onClick, style = style.actionButton)
        } else {
            Icon(
                painter = icon,
                contentDescription = null,
                modifier = Modifier.size(style.iconSize),
                tint = if (style.isTranslucent) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
            )
        }
        Spacer(Modifier.width((style.iconGap - reach).coerceAtLeast(Grid.d0)))
        BasicTextField(
            value = value,
            onValueChange = { new ->
                if (new.text != value.text) onTextChange(new.text)
                value = new
            },
            modifier = Modifier.weight(1f).focusRequester(focusRequester).onFocusChanged { if (it.isFocused) onFocus() },
            textStyle = style.textStyle.copy(color = MaterialTheme.colorScheme.onSurface),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            singleLine = true,
            keyboardOptions = KeyboardOptions(autoCorrectEnabled = false, imeAction = ImeAction.Search),
            // Search only hides the keyboard: choosing the first suggestion for the user could plan a route to the wrong place
            keyboardActions = KeyboardActions(onSearch = { keyboard?.hide() }),
            decorationBox = { field ->
                Box(contentAlignment = Alignment.CenterStart) {
                    if (value.text.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = style.textStyle,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    field()
                }
            },
        )
        if (value.text.isNotEmpty()) {
            // The spinner sits in the clear button's box, so the field doesn't move while a search is slow
            if (isLoading) {
                Box(
                    Modifier.minimumInteractiveComponentSize().size(style.actionButton.size),
                    contentAlignment = Alignment.Center,
                ) {
                    CarSpinner(Modifier.size(style.actionButton.iconSize))
                }
            } else {
                AppIconButton(
                    icon = clear.icon,
                    contentDescription = clear.label,
                    onClick = {
                        value = TextFieldValue()
                        clear.onClick()
                    },
                    style = style.actionButton,
                )
            }
        }
    }
}

@ComponentPreviews
@Composable
private fun SearchFieldPreview() = Showcase {
    SearchField(
        text = "",
        onTextChange = {},
        placeholder = stringResource(MR.strings.map_search_placeholder),
        icon = painterResource(MR.images.search),
        clear = IconAction(painterResource(MR.images.close), stringResource(MR.strings.search_clear)) {},
        onFocus = {},
        modifier = Modifier.fillMaxWidth().padding(Grid.d4),
    )
}

@ComponentPreviews
@Composable
private fun SearchFieldIosPreview() = Showcase(platform = UiPlatform.Ios) {
    SearchField(
        text = "",
        onTextChange = {},
        placeholder = stringResource(MR.strings.map_search_placeholder),
        icon = painterResource(MR.images.search),
        clear = IconAction(painterResource(MR.images.close), stringResource(MR.strings.search_clear)) {},
        onFocus = {},
        modifier = Modifier.fillMaxWidth().padding(Grid.d4),
    )
}

@ComponentPreviews
@Composable
private fun SearchFieldTextPreview() = Showcase {
    SearchField(
        text = "karlst",
        onTextChange = {},
        placeholder = stringResource(MR.strings.map_search_placeholder),
        icon = painterResource(MR.images.search),
        clear = IconAction(painterResource(MR.images.close), stringResource(MR.strings.search_clear)) {},
        onFocus = {},
        back = IconAction(painterResource(MR.images.back), stringResource(MR.strings.generic_close)) {},
        modifier = Modifier.fillMaxWidth().padding(Grid.d4),
    )
}

@ComponentPreviews
@Composable
private fun SearchFieldTextIosPreview() = Showcase(platform = UiPlatform.Ios) {
    SearchField(
        text = "karlst",
        onTextChange = {},
        placeholder = stringResource(MR.strings.map_search_placeholder),
        icon = painterResource(MR.images.search),
        clear = IconAction(painterResource(MR.images.close), stringResource(MR.strings.search_clear)) {},
        onFocus = {},
        modifier = Modifier.fillMaxWidth().padding(Grid.d4),
    )
}
