package com.syntey.spiriteddriving.core.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import com.syntey.spiriteddriving.core.ui.preview.ComponentPreviews
import com.syntey.spiriteddriving.core.ui.theme.Grid

/**
 * A dialog with one text field, which starts with [initialText] selected and gets the focus, so typing replaces it.
 * [error] gives the message under the field for a text, or null when there's none; [canConfirm] decides whether Confirm is
 * enabled for a text. Input longer than [maxLength] is ignored.
 */
@Composable
fun TextFieldDialog(
    title: String,
    message: String?,
    initialText: String,
    confirmLabel: String,
    dismissLabel: String,
    error: (String) -> String?,
    canConfirm: (String) -> Boolean,
    maxLength: Int,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var value by remember { mutableStateOf(TextFieldValue(initialText, TextRange(0, initialText.length))) }
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }
    val text = value.text
    val errorText = error(text)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                message?.let { Text(it) }
                OutlinedTextField(
                    value = value,
                    onValueChange = { typed -> if (typed.text.length <= maxLength) value = typed },
                    singleLine = true,
                    isError = errorText != null,
                    supportingText = if (errorText != null) {
                        { Text(errorText, color = MaterialTheme.colorScheme.error) }
                    } else {
                        null
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { if (canConfirm(text)) onConfirm(text) }),
                    modifier = Modifier.fillMaxWidth().focusRequester(focusRequester).padding(top = Grid.d2),
                )
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(dismissLabel) } },
        confirmButton = {
            TextButton(onClick = { onConfirm(text) }, enabled = canConfirm(text)) { Text(confirmLabel) }
        },
    )
}

@ComponentPreviews
@Composable
private fun TextFieldDialogPreview() = Showcase {
    TextFieldDialog(
        title = "Name this route",
        message = "You already have a route called Karlštejn Castle.",
        initialText = "Karlštejn Castle from Smíchov",
        confirmLabel = "Save",
        dismissLabel = "Cancel",
        error = { null },
        canConfirm = { it.isNotBlank() },
        maxLength = 40,
        onConfirm = {},
        onDismiss = {},
    )
}

@ComponentPreviews
@Composable
private fun TextFieldDialogTakenPreview() = Showcase {
    TextFieldDialog(
        title = "Name this route",
        message = "You already have a route called Karlštejn Castle.",
        initialText = "Karlštejn Castle",
        confirmLabel = "Save",
        dismissLabel = "Cancel",
        error = { "You already have a route with this name." },
        canConfirm = { false },
        maxLength = 40,
        onConfirm = {},
        onDismiss = {},
    )
}
