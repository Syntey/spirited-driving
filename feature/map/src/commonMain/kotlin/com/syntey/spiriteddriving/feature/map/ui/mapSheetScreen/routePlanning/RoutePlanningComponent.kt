package com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen.routePlanning

import app.futured.arkitekt.annotation.GenerateFactory
import app.futured.arkitekt.crusecases.execute
import app.futured.arkitekt.decompose.ext.asStateFlow
import co.touchlab.kermit.Logger
import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.router.slot.SlotNavigation
import com.arkivanov.decompose.router.slot.activate
import com.arkivanov.decompose.router.slot.childSlot
import com.arkivanov.decompose.router.slot.dismiss
import com.arkivanov.essenty.backhandler.BackCallback
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
import com.syntey.spiriteddriving.core.ui.base.derivedStateFlow
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
import com.syntey.spiriteddriving.feature.map.model.ui.StartSearch
import com.syntey.spiriteddriving.feature.map.tools.Constants
import com.syntey.spiriteddriving.feature.map.tools.inUnit
import com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen.MapSheetHost
import com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen.placeSearch.PlaceSearchComponent
import com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen.placeSearch.PlaceSearchComponentFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.update
import org.koin.core.annotation.Factory
import org.koin.core.annotation.InjectedParam

/**
 * Route planning over the trip: its start, destination and budget, the plan and the selected route, and the start search it
 * opens over itself. It plans again when the budget, the distance unit or the vignettes change, and when the user's location
 * arrives. A step of the sheet's stack, closed by popping it; Back closes the start search first, then planning.
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
        userLocation = host.userLocation,
    ),
),
    RoutePlanningScreen,
    RoutePlanningScreen.Actions {

    /**
     * @property destination The destination the trip goes to.
     * @property waitsForStart Whether the start search opens over this step at once, as the user's location isn't usable, so the
     *   first plan waits for the start.
     */
    data class Args(val destination: Place, val waitsForStart: Boolean)

    private val logger = Logger.withTag("RoutePlanningComponent")

    private val startSearchNavigation = SlotNavigation<StartSearch>()

    // The start search reaches the sheet as the destination search does, but the place it chooses is the trip's start
    private val startSearchHost = object : MapSheetHost by host {
        override fun onPlaceChosen(place: Place) = chooseStart(place)
    }

    // Opens at once when the trip waits for its start; closing it then closes planning too
    private val startSearchSlot: StateFlow<ChildSlot<StartSearch, PlaceSearchComponent>> = childSlot(
        source = startSearchNavigation,
        serializer = null,
        key = "startSearch",
        initialConfiguration = { if (args.waitsForStart) StartSearch(closesPlanning = true) else null },
        handleBackButton = false,
        childFactory = { _, childCtx ->
            PlaceSearchComponentFactory.createComponent(childCtx, startSearchHost, PlaceSearchComponent.Args(isStart = true))
        },
    ).asStateFlow()

    override val viewState: StateFlow<RoutePlanningViewState> = derivedStateFlow(
        changes = merge(componentState, startSearchSlot),
    ) { componentState.value.copy(startSearch = startSearchSlot.value.child?.configuration) }

    /** What the parent's view state follows: the planning's state and the start search's while it's open. */
    @OptIn(ExperimentalCoroutinesApi::class)
    val stateChanges: Flow<*> = merge(
        viewState,
        startSearchSlot.flatMapLatest { slot -> slot.child?.instance?.viewState ?: flowOf(Unit) },
    )

    /** The start search, while it's open over planning. */
    val startSearch: PlaceSearchComponent? get() = startSearchSlot.value.child?.instance

    val isStartSearchOpen: Boolean get() = startSearch != null

    override val actions: RoutePlanningScreen.Actions = this

    // Back closes the start search first, then planning; the step's own handler wins over the parent's while it's on top
    private val backCallback = BackCallback {
        if (isStartSearchOpen) closeStartSearch() else close()
    }

    init {
        backHandler.register(backCallback)
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
    override fun onRouteClick(routeId: String) {
        componentState.update { state ->
            val planning = state.planning
            if (planning.listedRoutes.none { it.id == routeId }) state else state.copy(planning = planning.copy(selectedRouteId = routeId))
        }
    }

    override fun onBudgetClick(budget: Budget) = chooseBudget(componentState.value.budgetChoice.with(budget))

    override fun onBudgetKindChange(kind: BudgetKind) = chooseBudget(componentState.value.budgetChoice.copy(kind = kind))

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

    override fun onRetryClick() = plan()

    override fun onStartClick() {
        startSearchNavigation.activate(StartSearch(closesPlanning = false))
        host.requestSheetHeight(SheetHeight.Full)
    }

    override fun onCloseClick() = close()

    // Full keeps the search's state and leaves the keyboard down: the keyboard only comes up from focusing the field
    private fun close() = host.onStepClosed(SheetHeight.Full)

    /** Closes the start search, if it's open; closing it closes planning too when the trip waited for its start. */
    fun closeStartSearch() {
        val closed = startSearchSlot.value.child?.configuration ?: return
        startSearchNavigation.dismiss()
        if (closed.closesPlanning) close() else host.requestSheetHeight(SheetHeight.Half)
    }

    // A chosen start is kept with the recent places, as the destination's is
    private fun chooseStart(place: Place) {
        host.keepRecentPlace(place)
        useStart(place)
    }

    /** My location chosen as the start, once it's usable. */
    fun useMyLocation() = useStart(null)

    // A null start is the user's location. A chosen place cancels the naming of an earlier pick, which would otherwise land on top of
    // the recent places after this one.
    private fun useStart(start: Place?) {
        startSearchNavigation.dismiss()
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
                host.keepRecentPlace(place)
                onNamed(place)
            }
            onError { error ->
                if (error !is OfflineException) logger.e(error) { "Point not named" }
                host.keepRecentPlace(unnamedPlace(point))
            }
        }
    }

    // Every fix, for the map's framing; the first one also plans a trip that waits for it
    fun onUserLocationChange(point: GeoPoint) {
        val wasUnknown = componentState.value.userLocation == null
        componentState.update { it.copy(userLocation = point) }
        if (wasUnknown) planOnceLocated()
    }

    // My location can be chosen as the start before the first location arrives, as right after access is given, so the trip
    // waits for it here. A start picked by hand doesn't wait.
    private fun planOnceLocated() {
        val planning = componentState.value.planning
        val waits = planning.start == null && !isStartSearchOpen && planning.status == PlanningStatus.Planning
        if (waits && planning.plan == null) plan()
    }

    // Each plan cancels the one before, so a retry or a new budget never shows an older answer
    private fun plan() {
        // The step plans when it closes, so there's nothing to show the plan in until then
        val planning = componentState.value.planning.takeUnless { isStartSearchOpen } ?: return
        // Reset before waiting for the user's location, so an earlier plan doesn't stay up meanwhile
        componentState.update {
            it.copy(planning = planning.copy(status = PlanningStatus.Planning, plan = null, selectedRouteId = null))
        }
        val start = planning.start?.let(::forBackend)
            ?: Place(name = null, area = null, location = componentState.value.userLocation ?: return)
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
