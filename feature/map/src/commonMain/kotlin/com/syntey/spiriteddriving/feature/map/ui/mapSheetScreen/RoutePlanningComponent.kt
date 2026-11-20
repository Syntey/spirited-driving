package com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen

import app.futured.arkitekt.annotation.GenerateFactory
import app.futured.arkitekt.crusecases.execute
import co.touchlab.kermit.Logger
import com.syntey.spiriteddriving.core.domain.error.OfflineException
import com.syntey.spiriteddriving.core.domain.geo.GeoPoint
import com.syntey.spiriteddriving.core.domain.place.Place
import com.syntey.spiriteddriving.core.domain.route.Budget
import com.syntey.spiriteddriving.core.domain.route.BudgetChoice
import com.syntey.spiriteddriving.core.domain.route.BudgetKind
import com.syntey.spiriteddriving.core.domain.route.OutsideCoverageException
import com.syntey.spiriteddriving.core.domain.settings.DistanceUnit
import com.syntey.spiriteddriving.core.ui.base.AppComponent
import com.syntey.spiriteddriving.core.ui.base.AppComponentContext
import com.syntey.spiriteddriving.feature.map.domain.GetBudgetChoiceUseCase
import com.syntey.spiriteddriving.feature.map.domain.NamePointUseCase
import com.syntey.spiriteddriving.feature.map.domain.ObserveDistanceUnitUseCase
import com.syntey.spiriteddriving.feature.map.domain.ObserveVignettesUseCase
import com.syntey.spiriteddriving.feature.map.domain.PlanTripUseCase
import com.syntey.spiriteddriving.feature.map.domain.SaveBudgetChoiceUseCase
import com.syntey.spiriteddriving.feature.map.domain.unnamedPlace
import com.syntey.spiriteddriving.feature.map.model.ui.PlanningStatus
import com.syntey.spiriteddriving.feature.map.model.ui.RoutePlanningState
import com.syntey.spiriteddriving.feature.map.model.ui.SheetHeight
import com.syntey.spiriteddriving.feature.map.tools.Constants
import com.syntey.spiriteddriving.feature.map.tools.inUnit
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import org.koin.core.annotation.Factory
import org.koin.core.annotation.InjectedParam

/**
 * Route planning over the trip: its start, destination and budget, the plan and the selected route. It plans again when the
 * budget, the distance unit or the vignettes change, and when the user's location arrives. A step of the sheet's stack, closed by
 * popping it.
 */
@Factory
@GenerateFactory
// Each action of the planning and each read on start is a function here
@Suppress("TooManyFunctions")
internal class RoutePlanningComponent(
    @InjectedParam componentContext: AppComponentContext,
    @InjectedParam private val host: MapSheetHost,
    @InjectedParam private val args: Args,
    private val planTripUseCase: PlanTripUseCase,
    private val getBudgetChoiceUseCase: GetBudgetChoiceUseCase,
    private val saveBudgetChoiceUseCase: SaveBudgetChoiceUseCase,
    private val observeDistanceUnitUseCase: ObserveDistanceUnitUseCase,
    private val observeVignettesUseCase: ObserveVignettesUseCase,
    private val namePointUseCase: NamePointUseCase,
) : AppComponent<RoutePlanningViewState, Nothing>(
    componentContext = componentContext,
    defaultState = RoutePlanningViewState(
        planning = RoutePlanningState(destination = args.destination, budget = Constants.Budgets.DEFAULT_CHOICE.budget),
        budgetChoice = Constants.Budgets.DEFAULT_CHOICE,
        distanceUnit = DistanceUnit.Kilometres,
    ),
) {

    /**
     * @property destination The destination the trip goes to.
     * @property waitsForStart Whether the start search opens over this step at once, as the user's location isn't usable, so the
     *   first plan waits for the start.
     */
    data class Args(val destination: Place, val waitsForStart: Boolean)

    private val logger = Logger.withTag("RoutePlanningComponent")

    val viewState: StateFlow<RoutePlanningViewState> = componentState

    init {
        observeDistanceUnit()
        observeVignettes()
        readBudgetChoice()
    }

    // Observed, so a unit picked in Settings applies while the map is under the Profile sheet
    private fun observeDistanceUnit() = observeDistanceUnitUseCase.execute {
        onNext { unit -> applyDistanceUnit(unit) }
        onError { error -> logger.e(error) { "Distance unit not observed" } }
    }

    // A kept distance moves to the chip nearest to it in the new unit. A planned distance budget moves too, and plans again.
    private fun applyDistanceUnit(unit: DistanceUnit) {
        componentState.update { it.copy(distanceUnit = unit, budgetChoice = it.budgetChoice.inUnit(unit)) }
        val state = componentState.value
        val planning = state.planning
        // While planning, the choice's budget is the planning's, so the choice's distance is the one to move to
        if (planning.budget !is Budget.Distance || planning.budget == state.budgetChoice.distance) return
        componentState.update { it.copy(planning = planning.copy(budget = state.budgetChoice.distance)) }
        plan()
    }

    // The first value is the vignettes at start, which the trip plans with already; each change after it plans again
    private fun observeVignettes() {
        var isFirst = true
        observeVignettesUseCase.execute {
            onNext {
                if (isFirst) {
                    isFirst = false
                } else {
                    plan()
                }
            }
            onError { error -> logger.e(error) { "Vignettes not observed" } }
        }
    }

    // The first plan waits for the kept choice, so it starts with it; a choice that can't be read plans with the default
    private fun readBudgetChoice() = getBudgetChoiceUseCase.execute {
        // Snapped to the unit in use, and not saved again: the kept choice is snapped on each read
        onSuccess { choice ->
            componentState.update {
                val snapped = choice.inUnit(it.distanceUnit)
                it.copy(budgetChoice = snapped, planning = it.planning.copy(budget = snapped.budget))
            }
            planUnlessWaitingForStart()
        }
        onError { error ->
            logger.e(error) { "Budget choice not read" }
            planUnlessWaitingForStart()
        }
    }

    // A trip without a usable location waits for its start, which useStart plans with
    private fun planUnlessWaitingForStart() {
        if (!args.waitsForStart) plan()
    }

    // Each route tapped in the list or on the map is selected, if it's one of the planned routes
    fun onRouteClick(routeId: String) {
        componentState.update { state ->
            val planning = state.planning
            if (planning.listedRoutes.none { it.id == routeId }) state else state.copy(planning = planning.copy(selectedRouteId = routeId))
        }
    }

    fun onBudgetClick(budget: Budget) = chooseBudget(componentState.value.budgetChoice.with(budget))

    fun onBudgetKindChange(kind: BudgetKind) = chooseBudget(componentState.value.budgetChoice.copy(kind = kind))

    // Kept at once, so the next plan starts with it even when this one is cancelled
    private fun chooseBudget(choice: BudgetChoice) {
        componentState.update { it.copy(budgetChoice = choice) }
        saveBudgetChoiceUseCase.execute(choice) {
            onError { error -> logger.e(error) { "Budget choice not kept" } }
        }
        val planning = componentState.value.planning
        if (planning.budget == choice.budget) return
        componentState.update { it.copy(planning = planning.copy(budget = choice.budget)) }
        plan()
    }

    fun onRetry() = plan()

    // A null start is the user's location. A chosen place cancels the naming of an earlier pick, which would otherwise land on top of
    // the recent places after this one.
    fun useStart(start: Place?) {
        if (start != null) useCaseJobPool[namePointUseCase]?.cancel()
        componentState.update {
            it.copy(planning = it.planning.copy(start = start))
        }
        host.requestSheetHeight(SheetHeight.Half)
        plan()
    }

    fun pickStart(point: GeoPoint) {
        useStart(unnamedPlace(point))
        // Same point, so the name only swaps in the start and doesn't plan again
        nameAt(point) { place ->
            componentState.update { state ->
                val planning = state.planning
                if (planning.start?.location == point) state.copy(planning = planning.copy(start = place)) else state
            }
        }
    }

    // The pin drops at once; the name comes later and only reaches the recent places, and the planning header
    fun nameDestination(point: GeoPoint) = nameAt(point) { place ->
        componentState.update { state ->
            val planning = state.planning
            if (planning.destination.location == point) state.copy(planning = planning.copy(destination = place)) else state
        }
    }

    private fun nameAt(point: GeoPoint, onNamed: (Place) -> Unit) {
        namePointUseCase.execute(point) {
            onSuccess { place ->
                host.onPlaceNamed(place)
                onNamed(place)
            }
            onError { error ->
                if (error !is OfflineException) logger.e(error) { "Point not named" }
                host.onPlaceNamed(unnamedPlace(point))
            }
        }
    }

    // My location can be chosen as the start before the first location arrives, as right after access is given, so the trip
    // waits for it here. A start picked by hand doesn't wait.
    fun onUserLocationArrived() {
        val planning = componentState.value.planning
        val waits = planning.start == null && !host.isStartSearchOpen && planning.status == PlanningStatus.Planning
        if (waits && planning.plan == null) plan()
    }

    // A route handed off to the maps app lets the rating step take the place of this trip when the user comes back
    fun markHandedOff() = componentState.update { it.copy(planning = it.planning.copy(isHandedOff = true)) }

    // Each plan cancels the one before, so a retry or a new budget never shows an older answer
    private fun plan() {
        // The step plans when it closes, so there's nothing to show the plan in until then
        val planning = componentState.value.planning.takeUnless { host.isStartSearchOpen } ?: return
        // Reset before waiting for the user's location, so an earlier plan doesn't stay up meanwhile
        componentState.update {
            it.copy(planning = planning.copy(status = PlanningStatus.Planning, plan = null, selectedRouteId = null))
        }
        val start = planning.start?.let(::forBackend)
            ?: Place(name = null, area = null, location = host.userLocation ?: return)
        val args = PlanTripUseCase.Args(
            start = start,
            destination = forBackend(planning.destination),
            budget = planning.budget,
        )
        planTripUseCase.execute(args) {
            onSuccess { plan ->
                componentState.update { it.copy(planning = it.planning.withPlan(plan)) }
            }
            onError { error ->
                // Offline and outside the covered area are answers the user can act on, so they aren't logged
                val status = when (error) {
                    is OfflineException -> PlanningStatus.Offline

                    is OutsideCoverageException -> PlanningStatus.NotCovered

                    else -> {
                        logger.e(error) { "Trip not planned" }
                        PlanningStatus.Failed
                    }
                }
                componentState.update { it.copy(planning = it.planning.copy(status = status)) }
            }
        }
    }
}

// An unnamed point's area is only its coordinates for the header; without both, the backend names it
private fun forBackend(place: Place): Place =
    place.takeIf { it.name != null } ?: Place(name = null, area = null, location = place.location)
