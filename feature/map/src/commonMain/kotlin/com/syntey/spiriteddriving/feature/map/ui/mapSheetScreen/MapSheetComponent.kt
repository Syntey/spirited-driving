package com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen

import app.futured.arkitekt.annotation.GenerateFactory
import app.futured.arkitekt.crusecases.execute
import co.touchlab.kermit.Logger
import com.arkivanov.essenty.backhandler.BackCallback
import com.arkivanov.essenty.lifecycle.doOnStart
import com.syntey.spiriteddriving.core.domain.drive.DriveQuestion
import com.syntey.spiriteddriving.core.domain.drive.Rating
import com.syntey.spiriteddriving.core.domain.drive.RatingReason
import com.syntey.spiriteddriving.core.domain.drive.Thumb
import com.syntey.spiriteddriving.core.domain.error.OfflineException
import com.syntey.spiriteddriving.core.domain.geo.GeoPoint
import com.syntey.spiriteddriving.core.domain.place.Place
import com.syntey.spiriteddriving.core.domain.place.PlaceSuggestion
import com.syntey.spiriteddriving.core.domain.route.Budget
import com.syntey.spiriteddriving.core.domain.route.BudgetChoice
import com.syntey.spiriteddriving.core.domain.route.BudgetKind
import com.syntey.spiriteddriving.core.domain.route.OutsideCoverageException
import com.syntey.spiriteddriving.core.domain.route.Route
import com.syntey.spiriteddriving.core.domain.savedroute.SavedRoute
import com.syntey.spiriteddriving.core.domain.savedroute.SavedRouteId
import com.syntey.spiriteddriving.core.domain.settings.DistanceUnit
import com.syntey.spiriteddriving.core.domain.settings.Note
import com.syntey.spiriteddriving.core.platform.binding.LocationAccess
import com.syntey.spiriteddriving.core.platform.binding.MapsAppResult
import com.syntey.spiriteddriving.core.ui.base.AppComponentContext
import com.syntey.spiriteddriving.core.ui.base.ScreenComponent
import com.syntey.spiriteddriving.core.ui.base.executeWithLifecycle
import com.syntey.spiriteddriving.feature.map.domain.AddRecentPlaceUseCase
import com.syntey.spiriteddriving.feature.map.domain.CloseNoteUseCase
import com.syntey.spiriteddriving.feature.map.domain.GetBudgetChoiceUseCase
import com.syntey.spiriteddriving.feature.map.domain.GetLocationAccessUseCase
import com.syntey.spiriteddriving.feature.map.domain.GetPastDriveUseCase
import com.syntey.spiriteddriving.feature.map.domain.GetRecentPlacesUseCase
import com.syntey.spiriteddriving.feature.map.domain.HandOffUseCase
import com.syntey.spiriteddriving.feature.map.domain.IsNoteClosedUseCase
import com.syntey.spiriteddriving.feature.map.domain.LoadPastDriveRouteUseCase
import com.syntey.spiriteddriving.feature.map.domain.LoadSavedRouteUseCase
import com.syntey.spiriteddriving.feature.map.domain.NamePointUseCase
import com.syntey.spiriteddriving.feature.map.domain.ObserveCoverageUseCase
import com.syntey.spiriteddriving.feature.map.domain.ObserveDistanceUnitUseCase
import com.syntey.spiriteddriving.feature.map.domain.ObserveUserLocationUseCase
import com.syntey.spiriteddriving.feature.map.domain.ObserveVignettesUseCase
import com.syntey.spiriteddriving.feature.map.domain.OpenLocationSettingsUseCase
import com.syntey.spiriteddriving.feature.map.domain.OpenMapsAppStoreUseCase
import com.syntey.spiriteddriving.feature.map.domain.PlanTripUseCase
import com.syntey.spiriteddriving.feature.map.domain.RemoveRecentPlaceUseCase
import com.syntey.spiriteddriving.feature.map.domain.SaveBudgetChoiceUseCase
import com.syntey.spiriteddriving.feature.map.domain.RequestLocationAccessUseCase
import com.syntey.spiriteddriving.feature.map.domain.RequestPreciseLocationUseCase
import com.syntey.spiriteddriving.feature.map.domain.SearchPlacesUseCase
import com.syntey.spiriteddriving.feature.map.domain.SendPendingDrivesUseCase
import com.syntey.spiriteddriving.feature.map.domain.SearchPlacesUseCase.SearchUpdate
import com.syntey.spiriteddriving.feature.map.domain.StartDriveUseCase
import com.syntey.spiriteddriving.feature.map.domain.DrivePrompt
import com.syntey.spiriteddriving.feature.map.domain.GetDriveQuestionUseCase
import com.syntey.spiriteddriving.feature.map.domain.MarkNotDrivenUseCase
import com.syntey.spiriteddriving.feature.map.domain.ObserveOpenedReminderUseCase
import com.syntey.spiriteddriving.feature.map.domain.ObserveSavedRoutesUseCase
import com.syntey.spiriteddriving.feature.map.domain.ObserveSignedInUseCase
import com.syntey.spiriteddriving.feature.map.domain.SaveRouteResult
import com.syntey.spiriteddriving.feature.map.domain.SaveRouteUseCase
import com.syntey.spiriteddriving.feature.map.domain.UnsaveRouteUseCase
import com.syntey.spiriteddriving.feature.map.domain.ShouldOfferReminderUseCase
import com.syntey.spiriteddriving.feature.map.domain.TurnOnRemindersUseCase
import com.syntey.spiriteddriving.feature.map.domain.RateDriveUseCase
import com.syntey.spiriteddriving.feature.map.domain.SkipDriveQuestionUseCase
import com.syntey.spiriteddriving.feature.map.domain.unnamedPlace
import com.syntey.spiriteddriving.feature.map.model.ui.DriveDialog
import com.syntey.spiriteddriving.feature.map.model.ui.LocateButton
import com.syntey.spiriteddriving.feature.map.model.ui.MyLocationRow
import com.syntey.spiriteddriving.feature.map.model.ui.PlanningStatus
import com.syntey.spiriteddriving.feature.map.model.ui.RatingStep
import com.syntey.spiriteddriving.feature.map.model.ui.ReminderBubble
import com.syntey.spiriteddriving.feature.map.model.ui.RouteNameDialog
import com.syntey.spiriteddriving.feature.map.model.ui.RoutePlanningState
import com.syntey.spiriteddriving.feature.map.model.ui.SearchState
import com.syntey.spiriteddriving.feature.map.model.ui.OneRouteOrigin
import com.syntey.spiriteddriving.feature.map.model.ui.OneRouteStatus
import com.syntey.spiriteddriving.feature.map.model.ui.OneRouteStep
import com.syntey.spiriteddriving.feature.map.model.ui.SearchStatus
import com.syntey.spiriteddriving.feature.map.model.ui.SheetHeight
import com.syntey.spiriteddriving.feature.map.model.ui.savedAs
import com.syntey.spiriteddriving.feature.map.model.ui.StartSearch
import com.syntey.spiriteddriving.feature.map.model.ui.allowsLocation
import com.syntey.spiriteddriving.feature.map.tools.Constants
import com.syntey.spiriteddriving.core.resources.datetime.driveDayText
import com.syntey.spiriteddriving.core.resources.datetime.driveTimeText
import com.syntey.spiriteddriving.feature.map.tools.inUnit
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.datetime.TimeZone
import org.koin.core.annotation.Factory
import org.koin.core.annotation.InjectedParam
import kotlin.time.Clock

@Factory
@GenerateFactory
// Each action of the sheet and each read on start is a function here, for both the search and route planning
@Suppress("TooManyFunctions", "LargeClass")
internal class MapSheetComponent(
    @InjectedParam componentContext: AppComponentContext,
    @InjectedParam override val navigation: MapSheetScreenNavigation,
    private val getLocationAccessUseCase: GetLocationAccessUseCase,
    private val requestLocationAccessUseCase: RequestLocationAccessUseCase,
    private val openLocationSettingsUseCase: OpenLocationSettingsUseCase,
    private val observeUserLocationUseCase: ObserveUserLocationUseCase,
    private val observeCoverageUseCase: ObserveCoverageUseCase,
    private val requestPreciseLocationUseCase: RequestPreciseLocationUseCase,
    private val isNoteClosedUseCase: IsNoteClosedUseCase,
    private val closeNoteUseCase: CloseNoteUseCase,
    private val searchPlacesUseCase: SearchPlacesUseCase,
    private val observeDistanceUnitUseCase: ObserveDistanceUnitUseCase,
    private val observeVignettesUseCase: ObserveVignettesUseCase,
    private val getRecentPlacesUseCase: GetRecentPlacesUseCase,
    private val addRecentPlaceUseCase: AddRecentPlaceUseCase,
    private val removeRecentPlaceUseCase: RemoveRecentPlaceUseCase,
    private val namePointUseCase: NamePointUseCase,
    private val planTripUseCase: PlanTripUseCase,
    private val getBudgetChoiceUseCase: GetBudgetChoiceUseCase,
    private val saveBudgetChoiceUseCase: SaveBudgetChoiceUseCase,
    private val handOffUseCase: HandOffUseCase,
    private val startDriveUseCase: StartDriveUseCase,
    private val sendPendingDrivesUseCase: SendPendingDrivesUseCase,
    private val openMapsAppStoreUseCase: OpenMapsAppStoreUseCase,
    private val getDriveQuestionUseCase: GetDriveQuestionUseCase,
    private val rateDriveUseCase: RateDriveUseCase,
    private val skipDriveQuestionUseCase: SkipDriveQuestionUseCase,
    private val markNotDrivenUseCase: MarkNotDrivenUseCase,
    private val observeOpenedReminderUseCase: ObserveOpenedReminderUseCase,
    private val shouldOfferReminderUseCase: ShouldOfferReminderUseCase,
    private val turnOnRemindersUseCase: TurnOnRemindersUseCase,
    private val observeSignedInUseCase: ObserveSignedInUseCase,
    private val observeSavedRoutesUseCase: ObserveSavedRoutesUseCase,
    private val saveRouteUseCase: SaveRouteUseCase,
    private val unsaveRouteUseCase: UnsaveRouteUseCase,
    private val loadSavedRouteUseCase: LoadSavedRouteUseCase,
    private val getPastDriveUseCase: GetPastDriveUseCase,
    private val loadPastDriveRouteUseCase: LoadPastDriveRouteUseCase,
) : ScreenComponent<MapSheetViewState, MapSheetUiEvent, MapSheetScreenNavigation>(
    componentContext = componentContext,
    defaultState = MapSheetViewState(),
),
    MapSheetScreen,
    MapSheetScreenNavigation by navigation,
    MapSheetScreen.Actions {

    private val logger = Logger.withTag("MapSheetComponent")

    // The route Save was tapped on while signed out, saved once the sign-in sheet closes signed in
    private var pendingSaveRouteId: String? = null

    override val viewState: StateFlow<MapSheetViewState> = componentState
    override val actions: MapSheetScreen.Actions = this

    // Back closes the innermost step first: the rating step, then a saved route, then the start step, then route planning,
    // then the search while the sheet is pulled up
    private val backCallback = BackCallback(isEnabled = false) {
        val state = componentState.value
        if (state.sheetStep != null) {
            closeSheetStep()
        } else if (state.startSearch != null) {
            closeStartSearch()
        } else if (state.planning != null) {
            onRoutePlanningClose()
        } else {
            closeSearch()
        }
    }

    init {
        backHandler.register(backCallback)
        // On every start, so access turned on in the system settings shows when the user comes back, and so coming back from
        // the maps app asks about the drive
        doOnStart(isOneTime = false) {
            readLocationAccess()
            askAboutDrive()
        }
        loadCoverage()
        readPreciseLocationNote()
        observeDistanceUnit()
        observeVignettes()
        readRecentPlaces()
        readBudgetChoice()
        readFirstDriveNote()
        sendPendingDrives()
        observeOpenedReminder()
        observeSignedIn()
        observeSavedRoutes()
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
        val planning = state.planning ?: return
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
                } else if (componentState.value.planning != null) {
                    plan()
                }
            }
            onError { error -> logger.e(error) { "Vignettes not observed" } }
        }
    }

    private fun readRecentPlaces() = getRecentPlacesUseCase.execute {
        onSuccess { places -> componentState.update { it.withRecentPlaces(places) } }
        onError { error -> logger.e(error) { "Recent places not read" } }
    }

    private fun readBudgetChoice() = getBudgetChoiceUseCase.execute {
        // Snapped to the unit in use, and not saved again: the kept choice is snapped on each read
        onSuccess { choice -> componentState.update { it.copy(budgetChoice = choice.inUnit(it.distanceUnit)) } }
        onError { error -> logger.e(error) { "Budget choice not read" } }
    }

    override fun onSearchFocus() = setSheetHeight(SheetHeight.Full)

    override fun onSearchTextChange(text: String) {
        componentState.update { it.copy(search = it.search.copy(text = text)) }
        search(text)
    }

    override fun onSearchClear() = clearSearch()

    override fun onSearchRetry() = search(componentState.value.search.text)

    override fun onSearchClose() = closeSearchStep()

    // The start step has no collapsed layout, so dragging it down closes it
    override fun onSheetHeightChange(height: SheetHeight) {
        if (componentState.value.startSearch != null && height == SheetHeight.Collapsed) closeStartSearch()
        setSheetHeight(height)
    }

    override fun onSuggestionClick(suggestion: PlaceSuggestion) {
        if (!suggestion.isCovered) {
            sendUiEvent(MapSheetUiEvent.NotCovered)
            return
        }
        choose(suggestion.place)
    }

    // A recent place counts as covered until the covered area is loaded, as it was covered when chosen
    override fun onRecentPlaceClick(place: Place) {
        if (!componentState.value.map.covers(place.location)) {
            sendUiEvent(MapSheetUiEvent.NotCovered)
            return
        }
        choose(place)
    }

    // Hidden at once, so the row doesn't wait for the removal to be saved
    override fun onRecentPlaceRemove(place: Place) {
        componentState.update { it.withRecentPlaces(it.search.recentPlaces.filterNot { recent -> recent == place }) }
        removeRecentPlaceUseCase.execute(place) {
            onSuccess { places -> componentState.update { it.withRecentPlaces(places) } }
            onError { error -> logger.e(error) { "Recent place not removed" } }
        }
    }

    private fun choose(place: Place) {
        // A point still being named would otherwise land on top of the recent places after this one
        useCaseJobPool[namePointUseCase]?.cancel()
        keep(place)
        if (componentState.value.startSearch != null) {
            useStart(place)
            return
        }
        componentState.update { it.copy(baseMap = it.baseMap.framing(place.location)) }
        startPlanning(place)
    }

    // Without a usable location the trip waits for a start, so the start step opens and closing it closes planning
    private fun startPlanning(destination: Place) {
        val usable = componentState.value.hasUsableLocation
        componentState.update {
            it.copy(planning = RoutePlanningState(destination = destination, budget = it.budgetChoice.budget))
        }
        if (usable) {
            setSheetHeight(SheetHeight.Half)
            plan()
        } else {
            openStartSearch(closesPlanning = true)
        }
    }

    // Each plan cancels the one before, so a retry or a new budget never shows an older answer
    private fun plan() {
        val state = componentState.value
        // The step plans when it closes, so there's nothing to show the plan in until then
        val planning = state.planning?.takeIf { state.startSearch == null } ?: return
        // Reset before waiting for the user's location, so an earlier plan doesn't stay up meanwhile
        componentState.update {
            it.copy(planning = planning.copy(status = PlanningStatus.Planning, plan = null, selectedRouteId = null))
        }
        val start = planning.start?.let(::forBackend)
            ?: Place(name = null, area = null, location = state.map.userLocation ?: return)
        val args = PlanTripUseCase.Args(
            start = start,
            destination = forBackend(planning.destination),
            budget = planning.budget,
        )
        planTripUseCase.execute(args) {
            onSuccess { plan ->
                componentState.update { it.copy(planning = it.planning?.withPlan(plan)) }
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
                componentState.update { it.copy(planning = it.planning?.copy(status = status)) }
            }
        }
    }

    override fun onStartClick() = openStartSearch(closesPlanning = false)

    // The destination search is kept, so closing the step brings it back as it was
    private fun openStartSearch(closesPlanning: Boolean) {
        useCaseJobPool[searchPlacesUseCase]?.cancel()
        componentState.update {
            it.copy(
                startSearch = StartSearch(destinationSearch = it.search, closesPlanning = closesPlanning),
                search = SearchState(recentPlaces = it.search.recentPlaces),
            )
        }
        setSheetHeight(SheetHeight.Full)
    }

    // Returns the step that was open, or null; recent places may have changed meanwhile, so the current ones stay
    private fun restoreDestinationSearch(): StartSearch? {
        val closed = componentState.value.startSearch ?: return null
        useCaseJobPool[searchPlacesUseCase]?.cancel()
        componentState.update {
            it.copy(startSearch = null, search = closed.destinationSearch.copy(recentPlaces = it.search.recentPlaces))
        }
        return closed
    }

    private fun closeStartSearch() {
        val closed = restoreDestinationSearch() ?: return
        if (closed.closesPlanning) onRoutePlanningClose() else setSheetHeight(SheetHeight.Half)
    }

    private fun closeSearchStep() {
        if (componentState.value.startSearch != null) closeStartSearch() else closeSearch()
    }

    // A null start is the user's location
    private fun useStart(start: Place?) {
        restoreDestinationSearch()
        componentState.update {
            it.copy(planning = it.planning?.copy(start = start))
        }
        setSheetHeight(SheetHeight.Half)
        plan()
    }

    private fun keep(place: Place) {
        addRecentPlaceUseCase.execute(place) {
            onSuccess { places -> componentState.update { it.withRecentPlaces(places) } }
            onError { error -> logger.e(error) { "Recent place not kept" } }
        }
    }

    override fun onLocateClick() = locate()
    override fun onMapPanned() = componentState.update { it.copy(baseMap = it.baseMap.copy(followsUser = false)) }

    // The pin drops at once; the name comes later and only reaches the recent places, and the planning header
    // The rating step is about the drive just made, not a new trip, so a long press doesn't start one
    override fun onMapLongPress(point: GeoPoint) {
        if (componentState.value.ratingStep == null) pressMap(point)
    }

    private fun pressMap(point: GeoPoint) {
        if (!componentState.value.map.covers(point)) {
            sendUiEvent(MapSheetUiEvent.NotCovered)
            return
        }
        if (componentState.value.startSearch != null) {
            pickStart(point)
            return
        }
        // A new trip replaces the saved route on screen
        closeOneRouteStep()
        componentState.update { it.copy(baseMap = it.baseMap.copy(followsUser = false)) }
        startPlanning(unnamedPlace(point))
        namePoint(point) { place ->
            componentState.update { state ->
                val planning = state.planning
                if (planning?.destination?.location == point) state.copy(planning = planning.copy(destination = place)) else state
            }
        }
    }

    private fun pickStart(point: GeoPoint) {
        useStart(unnamedPlace(point))
        // Same point, so the name only swaps in the start and doesn't plan again
        namePoint(point) { place ->
            componentState.update { state ->
                val planning = state.planning
                if (planning?.start?.location == point) state.copy(planning = planning.copy(start = place)) else state
            }
        }
    }

    private fun namePoint(point: GeoPoint, onNamed: (Place) -> Unit) {
        namePointUseCase.execute(point) {
            onSuccess { place ->
                keep(place)
                onNamed(place)
            }
            onError { error ->
                if (error !is OfflineException) logger.e(error) { "Point not named" }
                keep(unnamedPlace(point))
            }
        }
    }

    override fun onRouteClick(routeId: String) {
        componentState.update { state ->
            val planning = state.planning ?: return@update state
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
        val planning = componentState.value.planning ?: return
        if (planning.budget == choice.budget) return
        componentState.update { it.copy(planning = planning.copy(budget = choice.budget)) }
        plan()
    }

    override fun onRoutePlanningClose() {
        useCaseJobPool[planTripUseCase]?.cancel()
        componentState.update { it.copy(planning = null, startSearch = null) }
        // Full brings the search back as it was, without the keyboard: the keyboard only comes from focusing the field
        setSheetHeight(SheetHeight.Full)
    }

    override fun onPlanningRetry() = plan()

    // A saved route's route is loaded in place of a plan; the start and destination pins frame it until it is
    override fun showSavedRoute(savedRouteId: SavedRouteId) {
        val savedRoute = componentState.value.savedRoutes.firstOrNull { it.id == savedRouteId } ?: return
        showOneRoute(OneRouteOrigin.Saved(savedRoute))
    }

    // Nothing shows when the drive isn't kept on the device, as the list it was chosen from is what keeps it
    override fun showPastDrive(driveId: String) {
        getPastDriveUseCase.execute(driveId) {
            onSuccess { drive -> drive?.let { showOneRoute(OneRouteOrigin.Past(it)) } }
            onError { error -> logger.e(error) { "Past drive not read" } }
        }
    }

    // Plans from where the user is now, as a new trip does, so the rating step, a route on its own and the search make way
    override fun planNewRoutes(driveId: String) {
        getPastDriveUseCase.execute(driveId) {
            onSuccess { drive -> drive?.let { planTripTo(it.destination) } }
            onError { error -> logger.e(error) { "Past drive not read" } }
        }
    }

    private fun showOneRoute(origin: OneRouteOrigin) {
        if (componentState.value.ratingStep != null) closeRatingStep()
        useCaseJobPool[planTripUseCase]?.cancel()
        restoreDestinationSearch()
        clearSearch()
        componentState.update {
            it.copy(
                planning = null,
                startSearch = null,
                sheetStep = OneRouteStep(origin = origin),
                baseMap = it.baseMap.copy(followsUser = false),
            )
        }
        setSheetHeight(SheetHeight.Half)
        loadOneRoute(origin)
    }

    private fun planTripTo(destination: Place) {
        if (componentState.value.ratingStep != null) closeRatingStep()
        closeOneRouteStep()
        restoreDestinationSearch()
        clearSearch()
        onRecentPlaceClick(destination)
    }

    override fun onOneRouteRetry() = retryOneRoute()

    override fun onOneRouteClose() = closeOneRoute()

    private fun retryOneRoute() {
        val origin = componentState.value.oneRouteStep?.origin ?: return
        loadOneRoute(origin)
    }

    private fun closeOneRoute() {
        closeOneRouteStep()
        setSheetHeight(SheetHeight.Collapsed)
    }

    private fun closeOneRouteStep() {
        cancelOneRouteLoads()
        componentState.update { it.copy(sheetStep = it.sheetStep.takeUnless { step -> step is OneRouteStep }) }
    }

    private fun closeSheetStep() {
        when (componentState.value.sheetStep) {
            is RatingStep -> closeRatingStep()
            is OneRouteStep -> closeOneRoute()
            null -> Unit
        }
    }

    private fun cancelOneRouteLoads() {
        useCaseJobPool[loadSavedRouteUseCase]?.cancel()
        useCaseJobPool[loadPastDriveRouteUseCase]?.cancel()
    }

    override fun onSavedRoutesClick() {
        if (componentState.value.isSignedIn) openSavedRoutes() else openSignInForSavedRoutes()
    }

    private fun loadOneRoute(origin: OneRouteOrigin) = when (origin) {
        is OneRouteOrigin.Saved -> loadSavedRoute(origin.savedRoute.id)
        is OneRouteOrigin.Past -> loadPastDriveRoute(origin.drive.id)
    }

    private fun loadSavedRoute(savedRouteId: SavedRouteId) {
        updateOneRouteStep(savedRouteId.value) { it.copy(status = OneRouteStatus.Loading) }
        loadSavedRouteUseCase.execute(savedRouteId) {
            onSuccess { route -> showLoadedRoute(savedRouteId.value, route) }
            onError { error -> showLoadFailure(savedRouteId.value, error, "Saved route not loaded") }
        }
    }

    private fun loadPastDriveRoute(driveId: String) {
        updateOneRouteStep(driveId) { it.copy(status = OneRouteStatus.Loading) }
        loadPastDriveRouteUseCase.execute(driveId) {
            onSuccess { route -> showLoadedRoute(driveId, route) }
            onError { error -> showLoadFailure(driveId, error, "Past drive route not loaded") }
        }
    }

    private fun showLoadedRoute(originId: String, route: Route) {
        updateOneRouteStep(originId) { it.copy(route = route, status = OneRouteStatus.Ready) }
    }

    private fun showLoadFailure(originId: String, error: Throwable, message: String) {
        val status = if (error is OfflineException) {
            OneRouteStatus.Offline
        } else {
            logger.e(error) { message }
            OneRouteStatus.Failed
        }
        updateOneRouteStep(originId) { it.copy(status = status) }
    }

    // Changes the step only while it's still the step of this origin: it may have closed meanwhile
    private fun updateOneRouteStep(originId: String, change: (OneRouteStep) -> OneRouteStep) {
        componentState.update { state ->
            val step = state.oneRouteStep?.takeIf { it.origin.id == originId } ?: return@update state
            state.copy(sheetStep = change(step))
        }
    }

    override fun onProfileClick() = openProfile()

    override fun onPreciseLocationClick() = askForPreciseLocation()

    // Hidden at once, so the note doesn't wait for the close to be saved
    override fun onPreciseLocationNoteClose() {
        componentState.update { it.copy(isPreciseLocationNoteClosed = true) }
        closeNoteUseCase.execute(Note.PreciseLocation) {
            onError { error -> logger.e(error) { "Precise location note not closed" } }
        }
    }

    override fun onDriveClick() {
        val state = componentState.value
        val route = state.driveRoute ?: return
        if (state.isFirstDriveExplained) {
            handOff(route)
        } else {
            componentState.update { it.copy(driveDialog = DriveDialog.FirstDrive) }
        }
    }

    // Closing the note comes before the hand-off, so the dialog doesn't come back when the maps app turns out to be missing
    override fun onFirstDriveConfirm() {
        val route = componentState.value.driveRoute
        componentState.update { it.copy(driveDialog = null, isFirstDriveExplained = true) }
        closeNoteUseCase.execute(Note.FirstDrive) {
            onError { error -> logger.e(error) { "First drive note not closed" } }
        }
        route?.let(::handOff)
    }

    override fun onDriveDialogDismiss() = componentState.update { it.copy(driveDialog = null) }

    override fun onMapsAppStoreClick() {
        componentState.update { it.copy(driveDialog = null) }
        openMapsAppStoreUseCase.execute {
            onError { error -> logger.e(error) { "Maps app store not opened" } }
        }
    }

    override fun onRatingThumbClick(thumb: Thumb) = pickThumb(thumb)

    override fun onRatingReasonClick(reason: RatingReason) = toggleReason(reason)

    override fun onRatingDone() = closeRatingStep()

    override fun onRatingClose() = closeRatingStep()

    override fun onNotDrivenClick() = markNotDriven()

    override fun onReminderClick() = openReminder()

    override fun onReminderDismiss() = dismissReminder()

    override fun onReminderOfferClick() = turnOnReminders()

    private fun askAboutDrive() {
        if (componentState.value.ratingStep != null) return
        getDriveQuestionUseCase.execute(Clock.System.now()) {
            onSuccess { prompt -> if (prompt != null) showDrivePrompt(prompt) }
            onError { error -> logger.e(error) { "Drive question not read" } }
        }
    }

    private fun showDrivePrompt(prompt: DrivePrompt) {
        val state = componentState.value
        // A trip planned or a saved route opened after the hand-off isn't thrown away
        val canRatingStepTakeOver = (state.planning == null || state.planning.isHandedOff) &&
            (state.oneRouteStep?.isHandedOff ?: true)
        if (prompt.kind == DrivePrompt.Kind.RatingStep && canRatingStepTakeOver) {
            openRatingStep(prompt.question)
        } else {
            // A rating step that can't take the planning's place waits as the reminder, so the question isn't lost while
            // the user plans another trip
            showReminder(prompt.question)
        }
    }

    private fun showReminder(question: DriveQuestion) {
        val day = driveDayText(question.startedAt, Clock.System.now(), TimeZone.currentSystemDefault())
        componentState.update { it.copy(reminder = ReminderBubble(question = question, day = day)) }
    }

    private fun openReminder() {
        val reminder = componentState.value.reminder ?: return
        openRatingStep(reminder.question)
    }

    // Dismissing is for good: the question is skipped, so the drive isn't asked about again
    private fun dismissReminder() {
        val reminder = componentState.value.reminder ?: return
        componentState.update { it.copy(reminder = null) }
        skipDriveQuestion(reminder.question.driveId)
    }

    // The step takes the place of the search and of any planning, and the map shows the drive's ends
    private fun openRatingStep(question: DriveQuestion) {
        useCaseJobPool[planTripUseCase]?.cancel()
        cancelOneRouteLoads()
        val now = Clock.System.now()
        componentState.update {
            it.copy(
                planning = null,
                startSearch = null,
                reminder = null,
                sheetStep = RatingStep(
                    question = question,
                    driveTime = driveTimeText(question.startedAt, now, TimeZone.currentSystemDefault()),
                ),
                baseMap = it.baseMap.copy(followsUser = false),
            )
        }
        setSheetHeight(SheetHeight.Half)
    }

    // Tapping the thumb already picked changes nothing and sends nothing
    private fun pickThumb(thumb: Thumb) {
        val step = componentState.value.ratingStep ?: return
        if (step.rating?.thumb == thumb) return
        updateRating(Rating(thumb))
        // The reasons come below the thumbs, so a collapsed sheet opens to show them
        if (componentState.value.sheetHeight == SheetHeight.Collapsed) setSheetHeight(SheetHeight.Half)
        sendRating()
        if (step.rating == null) offerReminder()
    }

    // Asked after the first thumb only: the offer is for the user who has just rated a drive
    private fun offerReminder() = shouldOfferReminderUseCase.execute {
        onSuccess { shouldOffer -> if (shouldOffer) showReminderOffer() }
        onError { error -> logger.e(error) { "Reminder offer not read" } }
    }

    private fun showReminderOffer() = updateRatingStep { it.copy(showsReminderOffer = true) }

    // Hidden at once, so the offer doesn't wait for the settings to open
    private fun turnOnReminders() {
        updateRatingStep { it.copy(showsReminderOffer = false) }
        turnOnRemindersUseCase.execute {
            onError { error -> logger.e(error) { "Reminders not turned on" } }
        }
    }

    // A tap on a drive's notification opens its rating step, even over planning, as the user asked for it; the step that
    // already shows that drive stays as it is
    private fun observeOpenedReminder() = observeOpenedReminderUseCase.execute {
        onNext { question ->
            if (componentState.value.ratingStep?.question?.driveId != question.driveId) openRatingStep(question)
        }
        onError { error -> logger.e(error) { "Opened reminder not observed" } }
    }

    // Only the reasons of the picked thumb can be picked
    private fun toggleReason(reason: RatingReason) {
        val rating = componentState.value.ratingStep?.rating ?: return
        if (reason.thumb != rating.thumb) return
        val reasons = if (reason in rating.reasons) rating.reasons - reason else rating.reasons + reason
        updateRating(Rating(rating.thumb, reasons))
        sendRating()
    }

    private fun updateRating(rating: Rating) = updateRatingStep { it.copy(rating = rating) }

    // Changes the step only while the rating step is open, as a tap may arrive after it closed
    private fun updateRatingStep(change: (RatingStep) -> RatingStep) {
        componentState.update { state ->
            val step = state.sheetStep as? RatingStep ?: return@update state
            state.copy(sheetStep = change(step))
        }
    }

    // Each send cancels the one before, as a repeated execute does, so the latest rating is the one kept
    private fun sendRating() {
        val step = componentState.value.ratingStep ?: return
        val rating = step.rating ?: return
        rateDriveUseCase.execute(RateDriveUseCase.Args(driveId = step.question.driveId, rating = rating)) {
            // Offline, the rating is sent later, so it isn't logged
            onError { error -> if (error !is OfflineException) logger.e(error) { "Rating not sent" } }
        }
    }

    // A step closed without a thumb drops the drive's question, so it isn't asked about again
    private fun closeRatingStep() {
        val step = componentState.value.ratingStep ?: return
        if (step.rating == null) skipDriveQuestion(step.question.driveId)
        hideRatingStep()
    }

    private fun skipDriveQuestion(driveId: String) = skipDriveQuestionUseCase.execute(driveId) {
        onError { error -> logger.e(error) { "Drive question not skipped" } }
    }

    private fun hideRatingStep() {
        componentState.update { it.copy(sheetStep = it.sheetStep.takeUnless { step -> step is RatingStep }) }
        setSheetHeight(SheetHeight.Collapsed)
    }

    // Marked not driven, the question is already cleared, so the step closes without a skip
    private fun markNotDriven() {
        val step = componentState.value.ratingStep ?: return
        markNotDrivenUseCase.execute(step.question.driveId) {
            // Offline, the removal is sent later, so it isn't logged
            onError { error -> if (error !is OfflineException) logger.e(error) { "Drive not marked as not driven" } }
        }
        hideRatingStep()
    }

    private fun handOff(route: Route) {
        handOffUseCase.execute(route) {
            onSuccess { result ->
                when (result) {
                    MapsAppResult.Opened -> {
                        // A new drive replaces the reminder about an earlier one
                        componentState.update {
                            it.copy(
                                planning = it.planning?.copy(isHandedOff = true),
                                sheetStep = it.sheetStep.let { step ->
                                    if (step is OneRouteStep) step.copy(isHandedOff = true) else step
                                },
                                reminder = null,
                            )
                        }
                        startDrive(route)
                    }

                    MapsAppResult.Missing -> componentState.update { it.copy(driveDialog = DriveDialog.MapsAppMissing) }
                }
            }
            onError { error -> logger.e(error) { "Route not handed off" } }
        }
    }

    // Errors are only logged: the drive stays stored and is recorded on a later try
    private fun startDrive(route: Route) {
        val routeNumber = componentState.value.routeRows.firstOrNull { it.id == route.id }?.number
        startDriveUseCase.execute(StartDriveUseCase.Args(route = route, routeNumber = routeNumber, at = Clock.System.now())) {
            onError { error -> if (error !is OfflineException) logger.e(error) { "Drive not started" } }
        }
    }

    private fun readFirstDriveNote() = isNoteClosedUseCase.execute(Note.FirstDrive) {
        onSuccess { isClosed -> componentState.update { it.copy(isFirstDriveExplained = isClosed) } }
        onError { error -> logger.e(error) { "First drive note not read" } }
    }

    // Drives from an earlier start without a connection; offline is expected here, so it isn't logged
    private fun sendPendingDrives() = sendPendingDrivesUseCase.execute {
        onError { error -> if (error !is OfflineException) logger.e(error) { "Pending drives not sent" } }
    }

    private fun observeSignedIn() = observeSignedInUseCase.execute {
        onNext { isSignedIn -> componentState.update { it.copy(isSignedIn = isSignedIn) } }
        onError { error -> logger.e(error) { "Sign-in not observed" } }
    }

    private fun observeSavedRoutes() = observeSavedRoutesUseCase.execute {
        onNext { routes -> componentState.update { it.copy(savedRoutes = routes.toImmutableList()) } }
        onError { error -> logger.e(error) { "Saved routes not observed" } }
    }

    override fun onSaveClick() = toggleSavedRoute()

    override fun onRouteNameSave(name: String) = saveNamedRoute(name)

    override fun onRouteNameDismiss() = componentState.update { it.copy(nameDialog = null) }

    // Saving waits for the sign-in: the route is saved once the sheet closes signed in, and closing it leaves it unsaved
    override fun onSignInToSaveFinished(isSignedIn: Boolean) = continueSaveAfterSignIn(isSignedIn)

    private fun continueSaveAfterSignIn(isSignedIn: Boolean) {
        val routeId = pendingSaveRouteId
        pendingSaveRouteId = null
        if (!isSignedIn || routeId == null) return
        val state = componentState.value
        val route = state.driveRoute?.takeIf { it.id == routeId } ?: return
        saveRoute(route, name = (state.oneRouteStep?.origin as? OneRouteOrigin.Saved)?.savedRoute?.name)
    }

    // A saved route's step saves its route under the saved route's name, and is checked against that saved route; a past drive's
    // step saves under the destination's name, and is checked against the drive's route
    private fun toggleSavedRoute() {
        val state = componentState.value
        val route = state.driveRoute?.takeUnless { state.isSavingRoute } ?: return
        if (!state.isSignedIn) {
            pendingSaveRouteId = route.id
            openSignInToSaveRoute()
            return
        }
        val saved = state.savedRouteOnScreen
        if (saved != null) {
            removeRoute(saved.id)
        } else {
            saveRoute(route, name = (state.oneRouteStep?.origin as? OneRouteOrigin.Saved)?.savedRoute?.name)
        }
    }

    private fun saveNamedRoute(name: String) {
        val dialog = componentState.value.nameDialog ?: return
        componentState.update { it.copy(nameDialog = null) }
        val route = componentState.value.routeOnScreen(dialog.routeId) ?: return
        saveRoute(route, name)
    }

    private fun saveRoute(route: Route, name: String?) {
        setSavingRoute(true)
        saveRouteUseCase.execute(SaveRouteUseCase.Args(route = route, name = name)) {
            onSuccess { result ->
                setSavingRoute(false)
                when (result) {
                    is SaveRouteResult.NameTaken -> showNameDialog(route.id, result)
                    is SaveRouteResult.Saved -> pointStepAtSavedRoute(route.id, result.savedRoute)
                }
            }
            onError { error ->
                setSavingRoute(false)
                reportSaveError(error, MapSheetUiEvent.SaveRouteOffline, MapSheetUiEvent.SaveRouteFailed, "Route not saved")
            }
        }
    }

    private fun setSavingRoute(isSaving: Boolean) = componentState.update { it.copy(isSavingRoute = isSaving) }

    // Saving a saved route's route again makes a new saved route, which the step is about from then on
    private fun pointStepAtSavedRoute(routeId: String, savedRoute: SavedRoute) = componentState.update { state ->
        val step = state.oneRouteStep ?: return@update state
        state.copy(sheetStep = step.savedAs(routeId, savedRoute))
    }

    private fun reportSaveError(error: Throwable, offline: MapSheetUiEvent, failed: MapSheetUiEvent, logMessage: String) {
        if (error is OfflineException) {
            sendUiEvent(offline)
        } else {
            logger.e(error) { logMessage }
            sendUiEvent(failed)
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

    override fun onMyLocationClick() = useMyLocation()

    private fun useMyLocation() {
        when (componentState.value.myLocationRow) {
            MyLocationRow.Available -> useStart(null)
            MyLocationRow.NeedsPreciseLocation -> askForPreciseLocation(then = ::useMyLocationOnceUsable)
            MyLocationRow.NeedsLocationAccess -> askForLocationAccess(then = ::useMyLocationOnceUsable)
            MyLocationRow.OutsideCoveredArea -> sendUiEvent(MapSheetUiEvent.NotCovered)
            null -> Unit
        }
    }

    // Access given in the prompt makes the user's location the start, unless it's still not usable or the step has closed
    private fun useMyLocationOnceUsable() {
        val state = componentState.value
        if (state.startSearch != null && state.hasUsableLocation) useStart(null)
    }

    private fun locate() = when (componentState.value.locateButton) {
        LocateButton.Following -> Unit
        LocateButton.NotFollowing -> componentState.update { it.copy(baseMap = it.baseMap.copy(followsUser = true)) }
        LocateButton.NoAccess -> askForLocationAccess()
    }

    // Each search cancels the one before, which may still wait for the pause in typing, so only the current text's answer shows
    private fun search(text: String) {
        val isLongEnough = text.trim().length >= Constants.Search.MIN_LENGTH
        componentState.update { state ->
            state.copy(
                search = if (isLongEnough) {
                    state.search.copy(status = SearchStatus.Searching, isSlow = false)
                } else {
                    state.search.copy(status = SearchStatus.Idle, suggestions = persistentListOf(), isSlow = false)
                },
            )
        }
        val args = SearchPlacesUseCase.Args(text, near = componentState.value.map.userLocation)
        searchPlacesUseCase.execute(args) {
            onNext { update ->
                componentState.update { state ->
                    val current = state.search
                    state.copy(
                        search = when (update) {
                            SearchUpdate.Slow -> current.copy(isSlow = true)

                            is SearchUpdate.Found -> current.copy(
                                suggestions = update.suggestions.toImmutableList(),
                                status = if (isLongEnough) SearchStatus.Found else SearchStatus.Idle,
                                isSlow = false,
                            )
                        },
                    )
                }
            }
            onError { error ->
                val isOffline = error is OfflineException
                if (!isOffline) logger.e(error) { "Place search failed" }
                val status = if (isOffline) SearchStatus.Offline else SearchStatus.Failed
                componentState.update { state ->
                    state.copy(search = state.search.copy(status = status, suggestions = persistentListOf(), isSlow = false))
                }
            }
        }
    }

    private fun clearSearch() {
        useCaseJobPool[searchPlacesUseCase]?.cancel()
        componentState.update { it.copy(search = SearchState(recentPlaces = it.search.recentPlaces)) }
    }

    private fun closeSearch() {
        clearSearch()
        setSheetHeight(SheetHeight.Collapsed)
    }

    // Route planning changes the sheet's height too, so every change of either goes through here to keep Back right
    private fun setSheetHeight(height: SheetHeight) {
        componentState.update { it.copy(sheetHeight = height) }
        syncBackCallback()
    }

    private fun syncBackCallback() {
        val state = componentState.value
        backCallback.isEnabled = state.sheetHeight != SheetHeight.Collapsed ||
            state.planning != null ||
            state.sheetStep != null
    }

    // The first covered area sets the overview; until it's loaded the map shows the launch region, undimmed
    private fun loadCoverage() = observeCoverageUseCase.execute {
        onNext { coverage -> componentState.update { it.copy(baseMap = it.baseMap.withCoverage(coverage)) } }
        onError { error -> logger.e(error) { "Covered area not loaded" } }
    }

    private fun readLocationAccess() = getLocationAccessUseCase.execute {
        onSuccess(::applyLocationAccess)
        onError { error -> logger.e(error) { "Location access not read" } }
    }

    private fun askForLocationAccess(then: () -> Unit = {}) {
        // iOS asks only once, so on iOS a denial is always for good and this opens the settings
        if (componentState.value.locationAccess == LocationAccess.DeniedForGood) {
            openLocationSettingsUseCase.execute { onError { error -> logger.e(error) { "System settings not opened" } } }
        } else {
            requestLocationAccessUseCase.execute {
                onSuccess { access ->
                    applyLocationAccess(access)
                    then()
                }
                onError { error -> logger.e(error) { "Location prompt failed" } }
            }
        }
    }

    private fun readPreciseLocationNote() = isNoteClosedUseCase.execute(Note.PreciseLocation) {
        onSuccess { isClosed -> componentState.update { it.copy(isPreciseLocationNoteClosed = isClosed) } }
        onError { error -> logger.e(error) { "Precise location note not read" } }
    }

    private fun askForPreciseLocation(then: () -> Unit = {}) = requestPreciseLocationUseCase.execute {
        onSuccess { access ->
            applyLocationAccess(access)
            then()
        }
        onError { error -> logger.e(error) { "Precise location prompt failed" } }
    }

    private fun applyLocationAccess(access: LocationAccess) {
        componentState.update { it.copy(locationAccess = access, baseMap = it.baseMap.withAccess(access)) }
        if (access.allowsLocation) startLocationUpdates() else stopLocationUpdates()
    }

    // Updates pause while the screen is stopped and resume on start, so access read again on start doesn't restart them
    private fun startLocationUpdates() {
        if (useCaseJobPool[observeUserLocationUseCase]?.isActive == true) return
        observeUserLocationUseCase.executeWithLifecycle {
            onNext { point ->
                val wasUnknown = componentState.value.baseMap.userLocation == null
                componentState.update { it.copy(baseMap = it.baseMap.withUserLocation(point)) }
                if (wasUnknown) planWaitingForLocation()
            }
            onError { error -> logger.e(error) { "Location updates failed" } }
        }
    }

    // My location can be chosen as the start before the first location arrives, as right after access is given, so the trip
    // waits for it here. A start picked by hand doesn't wait.
    private fun planWaitingForLocation() {
        val state = componentState.value
        val planning = state.planning ?: return
        val waits = planning.start == null && state.startSearch == null && planning.status == PlanningStatus.Planning
        if (waits && planning.plan == null) plan()
    }

    // Running flow use cases are kept in the job pool, by use case
    private fun stopLocationUpdates() {
        useCaseJobPool[observeUserLocationUseCase]?.cancel()
    }
}

// The recent places as the search keeps them, with the rest of the search state kept as it is
private fun MapSheetViewState.withRecentPlaces(places: List<Place>): MapSheetViewState =
    copy(search = search.copy(recentPlaces = places.toImmutableList()))

// An unnamed point's area is only its coordinates for the header; without both, the backend names it
private fun forBackend(place: Place): Place =
    place.takeIf { it.name != null } ?: Place(name = null, area = null, location = place.location)
