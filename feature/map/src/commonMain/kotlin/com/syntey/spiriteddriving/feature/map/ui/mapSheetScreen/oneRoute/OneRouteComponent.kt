package com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen.oneRoute

import app.futured.arkitekt.annotation.GenerateFactory
import app.futured.arkitekt.crusecases.execute
import co.touchlab.kermit.Logger
import com.arkivanov.essenty.backhandler.BackCallback
import com.syntey.spiriteddriving.core.domain.error.OfflineException
import com.syntey.spiriteddriving.core.domain.route.Route
import com.syntey.spiriteddriving.core.domain.savedroute.SavedRoute
import com.syntey.spiriteddriving.core.domain.savedroute.SavedRouteId
import com.syntey.spiriteddriving.core.ui.base.AppComponent
import com.syntey.spiriteddriving.core.ui.base.AppComponentContext
import com.syntey.spiriteddriving.feature.map.domain.LoadPastDriveRouteUseCase
import com.syntey.spiriteddriving.feature.map.domain.LoadSavedRouteUseCase
import com.syntey.spiriteddriving.feature.map.domain.ObserveDistanceUnitUseCase
import com.syntey.spiriteddriving.feature.map.model.ui.OneRouteOrigin
import com.syntey.spiriteddriving.feature.map.model.ui.OneRouteStatus
import com.syntey.spiriteddriving.feature.map.model.ui.OneRouteStep
import com.syntey.spiriteddriving.feature.map.model.ui.savedAs
import com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen.MapSheetHost
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import org.koin.core.annotation.Factory
import org.koin.core.annotation.InjectedParam

/**
 * One route on its own: a saved route or a past drive, loaded and shown ready to drive, with Retry and Close. A step of the
 * sheet's stack, which replaces everything above the root search; closing it pops it, and so cancels its loads.
 */
@Factory
@GenerateFactory
internal class OneRouteComponent(
    @InjectedParam componentContext: AppComponentContext,
    @InjectedParam private val host: MapSheetHost,
    @InjectedParam private val args: Args,
    private val loadSavedRouteUseCase: LoadSavedRouteUseCase,
    private val loadPastDriveRouteUseCase: LoadPastDriveRouteUseCase,
    private val observeDistanceUnitUseCase: ObserveDistanceUnitUseCase,
) : AppComponent<OneRouteViewState, Nothing>(
    componentContext = componentContext,
    defaultState = OneRouteViewState(step = OneRouteStep(origin = args.origin)),
),
    OneRouteScreen,
    OneRouteScreen.Actions {

    /** @property origin Where the route comes from. */
    data class Args(val origin: OneRouteOrigin)

    private val logger = Logger.withTag("OneRouteComponent")

    override val viewState: StateFlow<OneRouteViewState> = componentState

    override val actions: OneRouteScreen.Actions = this

    // Back closes the step as its close button does; the step's own handler wins over the parent's while it's on top
    private val backCallback = BackCallback { close() }

    init {
        backHandler.register(backCallback)
        load(args.origin)
        observeDistanceUnit()
    }

    // Observed, so a unit picked in Settings applies while the route is shown
    private fun observeDistanceUnit() = observeDistanceUnitUseCase.execute {
        onNext { unit -> componentState.update { it.copy(distanceUnit = unit) } }
        onError { error -> logger.e(error) { "Distance unit not observed" } }
    }

    override fun onRetryClick() = load(componentState.value.step.origin)

    override fun onCloseClick() = close()

    private fun close() = host.onStepClosed()

    // Saving the route again makes a new saved route, which the step is about from then on
    fun savedAs(routeId: String, savedRoute: SavedRoute) = componentState.update { it.copy(step = it.step.savedAs(routeId, savedRoute)) }

    private fun load(origin: OneRouteOrigin) = when (origin) {
        is OneRouteOrigin.Saved -> loadSavedRoute(origin.savedRoute.id)
        is OneRouteOrigin.Past -> loadPastDriveRoute(origin.drive.id)
    }

    private fun loadSavedRoute(savedRouteId: SavedRouteId) {
        updateStep(savedRouteId.value) { it.copy(status = OneRouteStatus.Loading) }
        loadSavedRouteUseCase.execute(savedRouteId) {
            onSuccess { route -> showLoadedRoute(savedRouteId.value, route) }
            onError { error -> showLoadFailure(savedRouteId.value, error, "Saved route not loaded") }
        }
    }

    private fun loadPastDriveRoute(driveId: String) {
        updateStep(driveId) { it.copy(status = OneRouteStatus.Loading) }
        loadPastDriveRouteUseCase.execute(driveId) {
            onSuccess { route -> showLoadedRoute(driveId, route) }
            onError { error -> showLoadFailure(driveId, error, "Past drive route not loaded") }
        }
    }

    private fun showLoadedRoute(originId: String, route: Route) {
        updateStep(originId) { it.copy(route = route, status = OneRouteStatus.Ready) }
    }

    private fun showLoadFailure(originId: String, error: Throwable, message: String) {
        val status = if (error is OfflineException) {
            OneRouteStatus.Offline
        } else {
            logger.e(error) { message }
            OneRouteStatus.Failed
        }
        updateStep(originId) { it.copy(status = status) }
    }

    // Changes the step only while it's still the step of this origin: it may have closed meanwhile
    private fun updateStep(originId: String, change: (OneRouteStep) -> OneRouteStep) {
        componentState.update { state ->
            val step = state.step.takeIf { it.origin.id == originId } ?: return@update state
            state.copy(step = change(step))
        }
    }
}
