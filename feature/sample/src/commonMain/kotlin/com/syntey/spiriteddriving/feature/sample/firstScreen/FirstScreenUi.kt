package com.syntey.spiriteddriving.feature.sample.firstScreen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.syntey.spiriteddriving.feature.sample.firstScreen.FirstScreen.Actions.Companion.noOpActions
import com.syntey.spiriteddriving.core.resources.*
import com.syntey.spiriteddriving.core.ui.components.Showcase
import com.syntey.spiriteddriving.core.ui.tools.EventsEffect
import com.syntey.spiriteddriving.core.ui.tools.onEvent
import dev.icerock.moko.resources.compose.localized
import dev.icerock.moko.resources.compose.stringResource
import dev.icerock.moko.resources.desc.StringDesc

@Composable
fun FirstScreenUi(
    screen: FirstScreen,
    modifier: Modifier = Modifier,
) {
    val actions = screen.actions
    val viewState by screen.viewState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var snackbarMessage: StringDesc? by remember { mutableStateOf(null) }

    Content(viewState = viewState, actions = actions, snackbarHostState = snackbarHostState, modifier = modifier)

    EventsEffect(eventsFlow = screen.events) {
        onEvent<FirstUiEvent.ShowToast> { event ->
            snackbarMessage = event.text
        }
    }

    // StringDesc can only be resolved in composition, so the snackbar is shown from here instead of from the event observer.
    snackbarMessage?.localized()?.let { message ->
        LaunchedEffect(message) {
            snackbarHostState.showSnackbar(message)
            snackbarMessage = null
        }
    }
}

@Composable
private fun Content(
    viewState: FirstViewState,
    actions: FirstScreen.Actions,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets(0),
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(text = viewState.counter.localized())
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = viewState.createdAt.localized())
            Spacer(modifier = Modifier.height(4.dp))
            AnimatedVisibility(viewState.randomPerson != null) {
                viewState.randomPerson?.let { person ->
                    Column {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = person.localized(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { actions.onNext() }) {
                Text(text = stringResource(MR.strings.first_screen_button))
            }
        }
    }
}

@Preview
@Composable
private fun FirstScreenPreview() = Showcase {
    Content(
        viewState = FirstScreenPreviews.viewState(),
        actions = noOpActions(),
        snackbarHostState = remember { SnackbarHostState() },
        modifier = Modifier.fillMaxSize(),
    )
}
