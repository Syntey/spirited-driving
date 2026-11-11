package com.syntey.spiriteddriving.core.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import com.syntey.spiriteddriving.core.ui.preview.ComponentPreviews
import com.syntey.spiriteddriving.core.ui.theme.AppTheme
import com.syntey.spiriteddriving.core.ui.theme.Grid
import com.syntey.spiriteddriving.core.ui.theme.UiPlatform
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Shows the messages of [hostState] at the bottom of a screen or sheet: a capsule on inverseSurface on iOS, Material's
 * snackbar on Android, both with the action in inversePrimary. Show a message with [showMessage].
 */
@Composable
fun MessageHost(hostState: SnackbarHostState, modifier: Modifier = Modifier) {
    SnackbarHost(hostState, modifier) { data ->
        MessageBar(
            message = data.visuals.message,
            actionLabel = data.visuals.actionLabel,
            onAction = data::performAction,
        )
    }
}

/**
 * Shows [message] for 4 seconds, or 5 with an action such as Undo, so there's time to reach it. Returns true when the
 * action was tapped.
 */
suspend fun SnackbarHostState.showMessage(message: String, actionLabel: String? = null): Boolean =
    withTimeoutOrNull(if (actionLabel != null) ACTION_MESSAGE_MILLIS else MESSAGE_MILLIS) {
        // Indefinite, so the timeout decides: cancelling showSnackbar dismisses the message
        showSnackbar(message, actionLabel, duration = SnackbarDuration.Indefinite)
    } == SnackbarResult.ActionPerformed

private const val MESSAGE_MILLIS = 4_000L
private const val ACTION_MESSAGE_MILLIS = 5_000L

@Composable
private fun MessageBar(message: String, actionLabel: String?, onAction: () -> Unit, modifier: Modifier = Modifier) {
    val style = AppTheme.platformStyle.message
    val colors = MaterialTheme.colorScheme
    if (!style.isCapsule) {
        val action: (@Composable () -> Unit)? = if (actionLabel == null) {
            null
        } else {
            {
                TextButton(
                    onClick = onAction,
                    colors = ButtonDefaults.textButtonColors(contentColor = colors.inversePrimary),
                ) {
                    Text(actionLabel)
                }
            }
        }
        Snackbar(
            modifier = modifier.padding(Grid.d3),
            action = action,
            containerColor = colors.inverseSurface,
            contentColor = colors.inverseOnSurface,
            actionContentColor = colors.inversePrimary,
        ) {
            Text(message)
        }
    } else {
        Box(modifier.fillMaxWidth().padding(Grid.d3), contentAlignment = Alignment.Center) {
            Surface(
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                shape = CircleShape,
                color = colors.inverseSurface,
                contentColor = colors.inverseOnSurface,
            ) {
                Row(
                    Modifier.padding(
                        start = Grid.d5,
                        end = if (actionLabel != null) Grid.d2 else Grid.d5,
                    ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Grid.d4),
                ) {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f, fill = false).padding(vertical = Grid.d3),
                    )
                    if (actionLabel != null) {
                        // The label is about 20pt tall, so the touch target is set on the box around it
                        Box(
                            Modifier
                                .minimumInteractiveComponentSize()
                                .clip(CircleShape)
                                .clickable(role = Role.Button, onClick = onAction)
                                .padding(horizontal = Grid.d3),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = actionLabel,
                                style = MaterialTheme.typography.titleSmall,
                                color = colors.inversePrimary,
                            )
                        }
                    }
                }
            }
        }
    }
}

@ComponentPreviews
@Composable
private fun MessageBarPreview() = Showcase {
    MessageBarExamples()
}

@ComponentPreviews
@Composable
private fun MessageBarIosPreview() = Showcase(platform = UiPlatform.Ios) {
    MessageBarExamples()
}

@Composable
private fun MessageBarExamples() {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(Grid.d3)) {
        MessageBar("Route deleted", "Undo", {})
        MessageBar("You're offline. Changes need a connection.", null, {})
    }
}
