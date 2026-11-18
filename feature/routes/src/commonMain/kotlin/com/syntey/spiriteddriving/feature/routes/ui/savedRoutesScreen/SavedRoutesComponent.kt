package com.syntey.spiriteddriving.feature.routes.ui.savedRoutesScreen

import app.futured.arkitekt.annotation.GenerateFactory
import app.futured.arkitekt.crusecases.execute
import co.touchlab.kermit.Logger
import com.arkivanov.essenty.lifecycle.doOnCreate
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.syntey.spiriteddriving.core.domain.error.OfflineException
import com.syntey.spiriteddriving.core.resources.*
import com.syntey.spiriteddriving.core.ui.base.AppComponentContext
import com.syntey.spiriteddriving.core.ui.base.ScreenComponent
import com.syntey.spiriteddriving.feature.routes.domain.DeleteSavedRouteUseCase
import com.syntey.spiriteddriving.feature.routes.domain.DeleteSavedRoutesLaterUseCase
import com.syntey.spiriteddriving.feature.routes.domain.ObserveDistanceUnitUseCase
import com.syntey.spiriteddriving.feature.routes.domain.ObserveSavedRoutesUseCase
import com.syntey.spiriteddriving.feature.routes.domain.RefreshSavedRoutesUseCase
import com.syntey.spiriteddriving.feature.routes.domain.RenameSavedRouteUseCase
import com.syntey.spiriteddriving.feature.routes.model.ui.RenameDialog
import dev.icerock.moko.resources.desc.StringDesc
import dev.icerock.moko.resources.desc.desc
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import org.koin.core.annotation.Factory
import org.koin.core.annotation.InjectedParam

@Factory
@GenerateFactory
internal class SavedRoutesComponent(
    @InjectedParam componentContext: AppComponentContext,
    @InjectedParam override val navigation: SavedRoutesScreenNavigation,
    private val observeSavedRoutesUseCase: ObserveSavedRoutesUseCase,
    private val observeDistanceUnitUseCase: ObserveDistanceUnitUseCase,
    private val refreshSavedRoutesUseCase: RefreshSavedRoutesUseCase,
    private val renameSavedRouteUseCase: RenameSavedRouteUseCase,
    private val deleteSavedRouteUseCase: DeleteSavedRouteUseCase,
    private val deleteSavedRoutesLaterUseCase: DeleteSavedRoutesLaterUseCase,
) : ScreenComponent<SavedRoutesViewState, SavedRoutesUiEvent, SavedRoutesScreenNavigation>(
    componentContext = componentContext,
    defaultState = SavedRoutesViewState(),
),
    SavedRoutesScreen,
    SavedRoutesScreenNavigation by navigation,
    SavedRoutesScreen.Actions {

    private val logger = Logger.withTag("SavedRoutesComponent")

    override val viewState: StateFlow<SavedRoutesViewState> = componentState
    override val title: StringDesc = MR.strings.saved_routes_title.desc()
    override val actions: SavedRoutesScreen.Actions = this

    init {
        doOnCreate {
            observeSavedRoutes()
            observeDistanceUnit()
            refresh()
        }
        // The deletes still hidden were confirmed by closing the list, and its own work stops with it
        doOnDestroy { deleteSavedRoutesLaterUseCase(componentState.value.deletingIds) }
    }

    override fun onRouteClick(id: String) = openOnMap(id)

    override fun onRenameClick(id: String) {
        val routes = componentState.value.savedRoutes ?: return
        val route = routes.firstOrNull { it.id == id } ?: return
        val dialog = RenameDialog(
            savedRouteId = id,
            currentName = route.name,
            otherNames = routes.filterNot { it.id == id }.map { it.name }.toImmutableList(),
        )
        componentState.update { it.copy(renameDialog = dialog) }
    }

    override fun onRenameSave(name: String) {
        val dialog = componentState.value.renameDialog ?: return
        if (!dialog.canSave(name)) return
        componentState.update { it.copy(renameDialog = null) }
        renameRoute(id = dialog.savedRouteId, name = name)
    }

    override fun onRenameDismiss() {
        componentState.update { it.copy(renameDialog = null) }
    }

    override fun onDeleteClick(id: String) {
        componentState.update { it.copy(deletingIds = (it.deletingIds + id).toImmutableSet()) }
        sendUiEvent(SavedRoutesUiEvent.RouteDeleted(id))
    }

    override fun onDeleteUndo(id: String) = restoreRow(id)

    override fun onDeleteMessageGone(id: String) = deleteRoute(id)

    override fun onRetry() {
        componentState.update { it.copy(status = SavedRoutesStatus.Loading) }
        refresh()
    }

    override fun onClose() = close()

    private fun observeSavedRoutes() = observeSavedRoutesUseCase.execute {
        onNext { routes ->
            componentState.update { state ->
                state.copy(
                    savedRoutes = routes?.toImmutableList(),
                    status = if (routes != null) SavedRoutesStatus.Ready else state.status,
                )
            }
        }
        onError { error -> logger.e(error) { "Saved routes not observed" } }
    }

    private fun observeDistanceUnit() = observeDistanceUnitUseCase.execute {
        onNext { unit -> componentState.update { it.copy(distanceUnit = unit) } }
        onError { error -> logger.e(error) { "Distance unit not observed" } }
    }

    // Keeps what's shown when there is something: a refresh that fails then changes nothing
    private fun refresh() = refreshSavedRoutesUseCase.execute {
        onSuccess { componentState.update { state -> state.copy(status = SavedRoutesStatus.Ready) } }
        onError { error ->
            if (componentState.value.savedRoutes == null) {
                val status = if (error is OfflineException) SavedRoutesStatus.Offline else SavedRoutesStatus.Failed
                componentState.update { state -> state.copy(status = status) }
            }
            // Offline is expected here, so it isn't logged; the list loads again when the user tries
            if (error !is OfflineException) logger.e(error) { "Saved routes not refreshed" }
        }
    }

    private fun renameRoute(id: String, name: String) = renameSavedRouteUseCase.execute(
        RenameSavedRouteUseCase.Args(id = id, name = name),
    ) {
        onError { error ->
            if (error is OfflineException) {
                sendUiEvent(SavedRoutesUiEvent.ChangesOffline)
            } else {
                logger.e(error) { "Saved route not renamed" }
                sendUiEvent(SavedRoutesUiEvent.RenameFailed)
            }
        }
    }

    private fun deleteRoute(id: String) = deleteSavedRouteUseCase.execute(id) {
        onSuccess { restoreRow(id) }
        onError { error ->
            // The row comes back where it was, and the message says why it's still there
            restoreRow(id)
            if (error is OfflineException) {
                sendUiEvent(SavedRoutesUiEvent.ChangesOffline)
            } else {
                logger.e(error) { "Saved route not deleted" }
                sendUiEvent(SavedRoutesUiEvent.DeleteFailed)
            }
        }
    }

    private fun restoreRow(id: String) {
        componentState.update { it.copy(deletingIds = (it.deletingIds - id).toImmutableSet()) }
    }
}
