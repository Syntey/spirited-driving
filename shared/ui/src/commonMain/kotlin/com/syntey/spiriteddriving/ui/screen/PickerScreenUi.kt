package com.syntey.spiriteddriving.ui.screen

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.syntey.spiriteddriving.feature.ui.picker.PickerScreen
import com.syntey.spiriteddriving.feature.ui.picker.PickerScreen.Actions.Companion.noOpActions
import com.syntey.spiriteddriving.feature.ui.picker.PickerState
import com.syntey.spiriteddriving.feature.ui.picker.pickerStatePreviews
import com.syntey.spiriteddriving.resources.*
import com.syntey.spiriteddriving.ui.components.Showcase
import dev.icerock.moko.resources.compose.localized
import dev.icerock.moko.resources.compose.stringResource

/**
 * Picker is presented as a sheet on both platforms, so unlike the other screens it draws its own header.
 */
@Composable
fun PickerScreenUi(
    pickerScreen: PickerScreen,
    modifier: Modifier = Modifier,
) {
    val actions = pickerScreen.actions
    val viewState by pickerScreen.viewState.collectAsStateWithLifecycle()

    Content(viewState = viewState, actions = actions, modifier = modifier)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Content(
    viewState: PickerState,
    actions: PickerScreen.Actions,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(MR.strings.picker_title)) },
                actions = {
                    IconButton(onClick = actions::onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = stringResource(MR.strings.generic_close))
                    }
                },
                windowInsets = WindowInsets(0),
            )
        },
        contentWindowInsets = WindowInsets(0),
    ) { paddings ->
        Box(Modifier.padding(paddings)) {
            Crossfade(targetState = viewState.isLoading) { isLoading ->
                when (isLoading) {
                    true -> {
                        Box(Modifier.fillMaxWidth()) {
                            CircularProgressIndicator(
                                Modifier
                                    .align(Alignment.Center)
                                    .padding(16.dp),
                            )
                        }
                    }

                    false -> {
                        Column {
                            for (item in viewState.items) {
                                val title = item.localized()
                                ListItem(
                                    headlineContent = {
                                        Text(title)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            actions.onPick(title)
                                        },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private class PickerUiPreviewProvider : PreviewParameterProvider<PickerState> {
    override val values: Sequence<PickerState> = pickerStatePreviews().asSequence()
}

@Preview
@Composable
private fun PickerUiPreview(
    @PreviewParameter(PickerUiPreviewProvider::class) state: PickerState,
) = Showcase {
    Content(
        viewState = state,
        actions = noOpActions(),
        modifier = Modifier.fillMaxWidth(),
    )
}
