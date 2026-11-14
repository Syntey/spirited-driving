package com.syntey.spiriteddriving.feature.map.ui.tripPlanScreen

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.syntey.spiriteddriving.core.resources.*
import com.syntey.spiriteddriving.core.ui.components.Showcase
import com.syntey.spiriteddriving.core.ui.preview.ComponentPreviews
import com.syntey.spiriteddriving.feature.map.model.ui.DriveDialog
import com.syntey.spiriteddriving.feature.map.ui.tripPlanScreen.TripPlanScreen.Actions.Companion.noOpActions
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.stringResource

/** The drive dialog over the trip, or nothing when [dialog] is null. */
@Composable
internal fun DriveDialog(dialog: DriveDialog?, actions: TripPlanScreen.Actions) {
    when (dialog) {
        null -> Unit

        DriveDialog.FirstDrive -> AlertDialog(
            onDismissRequest = actions::onDriveDialogDismiss,
            icon = { Icon(painterResource(MR.images.car), contentDescription = null) },
            title = { Text(stringResource(MR.strings.drive_first_title_google)) },
            text = { Text(stringResource(MR.strings.drive_first_message_google)) },
            dismissButton = {
                TextButton(onClick = actions::onDriveDialogDismiss) { Text(stringResource(MR.strings.generic_cancel)) }
            },
            confirmButton = {
                TextButton(onClick = actions::onFirstDriveConfirm) { Text(stringResource(MR.strings.drive_first_open_google)) }
            },
        )

        DriveDialog.MapsAppMissing -> AlertDialog(
            onDismissRequest = actions::onDriveDialogDismiss,
            title = { Text(stringResource(MR.strings.drive_missing_title_google)) },
            text = { Text(stringResource(MR.strings.drive_missing_message_google)) },
            dismissButton = {
                TextButton(onClick = actions::onDriveDialogDismiss) { Text(stringResource(MR.strings.generic_cancel)) }
            },
            confirmButton = {
                TextButton(onClick = actions::onMapsAppStoreClick) { Text(stringResource(MR.strings.drive_missing_store_google)) }
            },
        )
    }
}

@ComponentPreviews
@Composable
private fun DriveDialogFirstDrivePreview() = Showcase {
    DriveDialog(dialog = DriveDialog.FirstDrive, actions = noOpActions())
}

@ComponentPreviews
@Composable
private fun DriveDialogMapsAppMissingPreview() = Showcase {
    DriveDialog(dialog = DriveDialog.MapsAppMissing, actions = noOpActions())
}
