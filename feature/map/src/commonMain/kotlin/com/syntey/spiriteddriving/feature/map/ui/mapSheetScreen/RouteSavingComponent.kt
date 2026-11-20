package com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen

import app.futured.arkitekt.annotation.GenerateFactory
import app.futured.arkitekt.crusecases.execute
import co.touchlab.kermit.Logger
import com.syntey.spiriteddriving.core.domain.error.OfflineException
import com.syntey.spiriteddriving.core.domain.route.Route
import com.syntey.spiriteddriving.core.domain.savedroute.SavedRoute
import com.syntey.spiriteddriving.core.domain.savedroute.SavedRouteId
import com.syntey.spiriteddriving.core.ui.base.AppComponent
import com.syntey.spiriteddriving.core.ui.base.AppComponentContext
import com.syntey.spiriteddriving.feature.map.domain.ObserveSavedRoutesUseCase
import com.syntey.spiriteddriving.feature.map.domain.ObserveSignedInUseCase
import com.syntey.spiriteddriving.feature.map.domain.SaveRouteResult
import com.syntey.spiriteddriving.feature.map.domain.SaveRouteUseCase
import com.syntey.spiriteddriving.feature.map.domain.UnsaveRouteUseCase
import com.syntey.spiriteddriving.feature.map.model.ui.RouteNameDialog
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import org.koin.core.annotation.Factory
import org.koin.core.annotation.InjectedParam

/**
 * Save over the trip: saving and removing a route, the sign-in it waits for, the name dialog when a name is taken, and the saved
 * routes. Lives as long as the map sheet, outside its navigation.
 */
@Factory
@GenerateFactory
internal class RouteSavingComponent(
    @InjectedParam componentContext: AppComponentContext,
    @InjectedParam private val listener: Listener,
    private val observeSignedInUseCase: ObserveSignedInUseCase,
    private val observeSavedRoutesUseCase: ObserveSavedRoutesUseCase,
    private val saveRouteUseCase: SaveRouteUseCase,
    private val unsaveRouteUseCase: UnsaveRouteUseCase,
) : AppComponent<RouteSavingViewState, Nothing>(
    componentContext = componentContext,
    defaultState = RouteSavingViewState(),
) {

    /**
     * Told to open the sign-in sheet when Save is tapped while signed out, when a route is saved, and when a save or removal
     * needs a message.
     */
    interface Listener {
        fun openSignInToSaveRoute()
        fun onSaved(routeId: String, savedRoute: SavedRoute)
        fun onSaveEvent(event: MapSheetUiEvent)
    }

    private val logger = Logger.withTag("RouteSavingComponent")

    // The route Save was tapped on while signed out, saved once the sign-in sheet closes signed in
    private var pendingSaveRouteId: String? = null

    val viewState: StateFlow<RouteSavingViewState> = componentState

    init {
        observeSignedIn()
        observeSavedRoutes()
    }

    // Locked while a route is being saved or removed. [savedOnScreen] is the saved route the route on screen is checked against.
    fun toggle(route: Route, name: String?, savedOnScreen: SavedRoute?) {
        val state = componentState.value
        if (state.isSaving) return
        if (!state.isSignedIn) {
            pendingSaveRouteId = route.id
            listener.openSignInToSaveRoute()
            return
        }
        if (savedOnScreen != null) {
            removeRoute(savedOnScreen.id)
        } else {
            saveRoute(route, name)
        }
    }

    // Saving waits for the sign-in: the route is saved once the sheet closes signed in, and closing it leaves it unsaved
    fun onSignInFinished(isSignedIn: Boolean, routeOnScreen: Route?, name: String?) {
        val routeId = pendingSaveRouteId
        pendingSaveRouteId = null
        if (!isSignedIn || routeId == null) return
        val route = routeOnScreen?.takeIf { it.id == routeId } ?: return
        saveRoute(route, name)
    }

    fun onNameSave(name: String, routeOnScreen: (String) -> Route?) {
        val dialog = componentState.value.nameDialog ?: return
        componentState.update { it.copy(nameDialog = null) }
        val route = routeOnScreen(dialog.routeId) ?: return
        saveRoute(route, name)
    }

    fun onNameDismiss() = componentState.update { it.copy(nameDialog = null) }

    private fun observeSignedIn() = observeSignedInUseCase.execute {
        onNext { isSignedIn -> componentState.update { it.copy(isSignedIn = isSignedIn) } }
        onError { error -> logger.e(error) { "Sign-in not observed" } }
    }

    private fun observeSavedRoutes() = observeSavedRoutesUseCase.execute {
        onNext { routes -> componentState.update { it.copy(savedRoutes = routes.toImmutableList()) } }
        onError { error -> logger.e(error) { "Saved routes not observed" } }
    }

    private fun saveRoute(route: Route, name: String?) {
        setSavingRoute(true)
        saveRouteUseCase.execute(SaveRouteUseCase.Args(route = route, name = name)) {
            onSuccess { result ->
                setSavingRoute(false)
                when (result) {
                    is SaveRouteResult.NameTaken -> showNameDialog(route.id, result)
                    is SaveRouteResult.Saved -> listener.onSaved(route.id, result.savedRoute)
                }
            }
            onError { error ->
                setSavingRoute(false)
                reportSaveError(error, MapSheetUiEvent.SaveRouteOffline, MapSheetUiEvent.SaveRouteFailed, "Route not saved")
            }
        }
    }

    private fun setSavingRoute(isSaving: Boolean) = componentState.update { it.copy(isSaving = isSaving) }

    private fun reportSaveError(error: Throwable, offline: MapSheetUiEvent, failed: MapSheetUiEvent, logMessage: String) {
        if (error is OfflineException) {
            listener.onSaveEvent(offline)
        } else {
            logger.e(error) { logMessage }
            listener.onSaveEvent(failed)
        }
    }

    private fun showNameDialog(routeId: String, taken: SaveRouteResult.NameTaken) = componentState.update {
        it.copy(nameDialog = RouteNameDialog(routeId, taken.takenName, taken.suggestion, taken.takenNames.toImmutableList()))
    }

    private fun removeRoute(savedRouteId: SavedRouteId) {
        setSavingRoute(true)
        unsaveRouteUseCase.execute(savedRouteId) {
            onSuccess { setSavingRoute(false) }
            onError { error ->
                setSavingRoute(false)
                reportSaveError(error, MapSheetUiEvent.RemoveRouteOffline, MapSheetUiEvent.RemoveRouteFailed, "Route not removed")
            }
        }
    }
}
