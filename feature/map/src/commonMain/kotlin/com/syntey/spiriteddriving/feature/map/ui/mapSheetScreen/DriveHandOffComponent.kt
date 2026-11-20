package com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen

import app.futured.arkitekt.annotation.GenerateFactory
import app.futured.arkitekt.crusecases.execute
import co.touchlab.kermit.Logger
import com.syntey.spiriteddriving.core.domain.error.OfflineException
import com.syntey.spiriteddriving.core.domain.route.Route
import com.syntey.spiriteddriving.core.domain.settings.Note
import com.syntey.spiriteddriving.core.platform.binding.MapsAppResult
import com.syntey.spiriteddriving.core.ui.base.AppComponent
import com.syntey.spiriteddriving.core.ui.base.AppComponentContext
import com.syntey.spiriteddriving.feature.map.domain.CloseNoteUseCase
import com.syntey.spiriteddriving.feature.map.domain.HandOffUseCase
import com.syntey.spiriteddriving.feature.map.domain.IsNoteClosedUseCase
import com.syntey.spiriteddriving.feature.map.domain.OpenMapsAppStoreUseCase
import com.syntey.spiriteddriving.feature.map.domain.StartDriveUseCase
import com.syntey.spiriteddriving.feature.map.model.ui.DriveDialog
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import org.koin.core.annotation.Factory
import org.koin.core.annotation.InjectedParam
import kotlin.time.Clock

/**
 * Drive over the trip: the first-drive note and dialog, the missing maps app and its store, the hand-off to the maps app, and
 * starting the drive. Lives as long as the map sheet, outside its navigation.
 */
@Factory
@GenerateFactory
internal class DriveHandOffComponent(
    @InjectedParam componentContext: AppComponentContext,
    @InjectedParam private val listener: Listener,
    private val handOffUseCase: HandOffUseCase,
    private val startDriveUseCase: StartDriveUseCase,
    private val openMapsAppStoreUseCase: OpenMapsAppStoreUseCase,
    private val isNoteClosedUseCase: IsNoteClosedUseCase,
    private val closeNoteUseCase: CloseNoteUseCase,
) : AppComponent<DriveHandOffViewState, Nothing>(
    componentContext = componentContext,
    defaultState = DriveHandOffViewState(),
) {

    /** Told when a route was handed off to the maps app, so the parent can show the drive as started. */
    interface Listener {
        fun onHandedOff()
    }

    private val logger = Logger.withTag("DriveHandOffComponent")

    // The route waiting for the first-drive dialog's answer, with its number
    private var pendingDrive: PendingDrive? = null

    val viewState: StateFlow<DriveHandOffViewState> = componentState

    init {
        readFirstDriveNote()
    }

    // Shows the first-drive dialog until the user has gone past it; after that the hand-off goes on at once
    fun drive(route: Route, routeNumber: Int?) {
        if (componentState.value.isFirstDriveExplained) {
            handOff(route, routeNumber)
        } else {
            pendingDrive = PendingDrive(route, routeNumber)
            componentState.update { it.copy(dialog = DriveDialog.FirstDrive) }
        }
    }

    // Closing the note comes before the hand-off, so the dialog doesn't come back when the maps app turns out to be missing
    fun onFirstDriveConfirm() {
        val pending = pendingDrive
        componentState.update { it.copy(dialog = null, isFirstDriveExplained = true) }
        closeNoteUseCase.execute(Note.FirstDrive) {
            onError { error -> logger.e(error) { "First drive note not closed" } }
        }
        pending?.let { handOff(it.route, it.routeNumber) }
    }

    fun onDialogDismiss() = componentState.update { it.copy(dialog = null) }

    fun onMapsAppStoreClick() {
        componentState.update { it.copy(dialog = null) }
        openMapsAppStoreUseCase.execute {
            onError { error -> logger.e(error) { "Maps app store not opened" } }
        }
    }

    private fun handOff(route: Route, routeNumber: Int?) {
        handOffUseCase.execute(route) {
            onSuccess { result ->
                when (result) {
                    MapsAppResult.Opened -> {
                        listener.onHandedOff()
                        startDrive(route, routeNumber)
                    }

                    MapsAppResult.Missing -> componentState.update { it.copy(dialog = DriveDialog.MapsAppMissing) }
                }
            }
            onError { error -> logger.e(error) { "Route not handed off" } }
        }
    }

    // Errors are only logged: the drive stays stored and is recorded on a later try
    private fun startDrive(route: Route, routeNumber: Int?) {
        startDriveUseCase.execute(StartDriveUseCase.Args(route = route, routeNumber = routeNumber, at = Clock.System.now())) {
            onError { error -> if (error !is OfflineException) logger.e(error) { "Drive not started" } }
        }
    }

    private fun readFirstDriveNote() = isNoteClosedUseCase.execute(Note.FirstDrive) {
        onSuccess { isClosed -> componentState.update { it.copy(isFirstDriveExplained = isClosed) } }
        onError { error -> logger.e(error) { "First drive note not read" } }
    }

    private class PendingDrive(val route: Route, val routeNumber: Int?)
}
