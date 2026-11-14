package com.syntey.spiriteddriving.feature.map.ui.tripPlanScreen

import app.futured.arkitekt.annotation.GenerateFactory
import app.futured.arkitekt.crusecases.execute
import co.touchlab.kermit.Logger
import com.arkivanov.essenty.backhandler.BackCallback
import com.arkivanov.essenty.lifecycle.doOnStart
import com.syntey.spiriteddriving.core.domain.error.OfflineException
import com.syntey.spiriteddriving.core.domain.geo.GeoPoint
import com.syntey.spiriteddriving.core.domain.place.Place
import com.syntey.spiriteddriving.core.domain.place.PlaceSuggestion
import com.syntey.spiriteddriving.core.domain.route.Budget
import com.syntey.spiriteddriving.core.domain.route.BudgetChoice
import com.syntey.spiriteddriving.core.domain.route.BudgetKind
import com.syntey.spiriteddriving.core.domain.route.OutsideCoverageException
import com.syntey.spiriteddriving.core.domain.route.Route
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
import com.syntey.spiriteddriving.feature.map.domain.GetRecentPlacesUseCase
import com.syntey.spiriteddriving.feature.map.domain.HandOffUseCase
import com.syntey.spiriteddriving.feature.map.domain.IsNoteClosedUseCase
import com.syntey.spiriteddriving.feature.map.domain.NamePointUseCase
import com.syntey.spiriteddriving.feature.map.domain.ObserveCoverageUseCase
import com.syntey.spiriteddriving.feature.map.domain.ObserveDistanceUnitUseCase
import com.syntey.spiriteddriving.feature.map.domain.ObserveUserLocationUseCase
import com.syntey.spiriteddriving.feature.map.domain.ObserveVignettesUseCase
import com.syntey.spiriteddriving.feature.map.domain.OpenLocationSettingsUseCase
import com.syntey.spiriteddriving.feature.map.domain.OpenMapsAppStoreUseCase
import com.syntey.spiriteddriving.feature.map.domain.PlanTripUseCase
import com.syntey.spiriteddriving.feature.map.domain.RecordPendingDrivesUseCase
import com.syntey.spiriteddriving.feature.map.domain.RemoveRecentPlaceUseCase
import com.syntey.spiriteddriving.feature.map.domain.SaveBudgetChoiceUseCase
import com.syntey.spiriteddriving.feature.map.domain.RequestLocationAccessUseCase
import com.syntey.spiriteddriving.feature.map.domain.RequestPreciseLocationUseCase
import com.syntey.spiriteddriving.feature.map.domain.SearchPlacesUseCase
import com.syntey.spiriteddriving.feature.map.domain.SearchPlacesUseCase.SearchUpdate
import com.syntey.spiriteddriving.feature.map.domain.StartDriveUseCase
import com.syntey.spiriteddriving.feature.map.domain.unnamedPlace
import com.syntey.spiriteddriving.feature.map.model.ui.DriveDialog
import com.syntey.spiriteddriving.feature.map.model.ui.LocateButton
import com.syntey.spiriteddriving.feature.map.model.ui.MyLocationRow
import com.syntey.spiriteddriving.feature.map.model.ui.PlanningStatus
import com.syntey.spiriteddriving.feature.map.model.ui.RoutePlanningState
import com.syntey.spiriteddriving.feature.map.model.ui.SearchState
import com.syntey.spiriteddriving.feature.map.model.ui.SearchStatus
import com.syntey.spiriteddriving.feature.map.model.ui.SheetHeight
import com.syntey.spiriteddriving.feature.map.model.ui.StartSearch
import com.syntey.spiriteddriving.feature.map.model.ui.allowsLocation
import com.syntey.spiriteddriving.feature.map.tools.Constants
import com.syntey.spiriteddriving.feature.map.tools.inUnit
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import org.koin.core.annotation.Factory
import org.koin.core.annotation.InjectedParam
import kotlin.time.Clock

@Factory
@GenerateFactory
// Each action of the sheet and each read on start is a function here, for both the search and route planning
@Suppress("TooManyFunctions")
internal class TripPlanComponent(
    @InjectedParam componentContext: AppComponentContext,
    @InjectedParam override val navigation: TripPlanScreenNavigation,
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
    private val recordPendingDrivesUseCase: RecordPendingDrivesUseCase,
    private val openMapsAppStoreUseCase: OpenMapsAppStoreUseCase,
) : ScreenComponent<TripPlanViewState, TripPlanUiEvent, TripPlanScreenNavigation>(
    componentContext = componentContext,
    defaultState = TripPlanViewState(),
),
    TripPlanScreen,
    TripPlanScreenNavigation by navigation,
    TripPlanScreen.Actions {

    private val logger = Logger.withTag("TripPlanComponent")

    override val viewState: StateFlow<TripPlanViewState> = componentState
    override val actions: TripPlanScreen.Actions = this

    // Back closes the innermost step first: the start step, then route planning, then the search while the sheet is pulled up
    private val backCallback = BackCallback(isEnabled = false) {
        val state = componentState.value
        if (state.startSearch != null) {
            closeStartSearch()
        } else if (state.planning != null) {
            onRoutePlanningClose()
        } else {
            closeSearch()
        }
    }

    init {
        backHandler.register(backCallback)
        // On every start, so access turned on in the system settings shows when the user comes back
        doOnStart(isOneTime = false) { readLocationAccess() }
        loadCoverage()
        readPreciseLocationNote()
        observeDistanceUnit()
        observeVignettes()
        readRecentPlaces()
        readBudgetChoice()
        readFirstDriveNote()
        recordPendingDrives()
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
            sendUiEvent(TripPlanUiEvent.NotCovered)
            return
        }
        choose(suggestion.place)
    }

    // A recent place counts as covered until the covered area is loaded, as it was covered when chosen
    override fun onRecentPlaceClick(place: Place) {
        if (!componentState.value.map.covers(place.location)) {
            sendUiEvent(TripPlanUiEvent.NotCovered)
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
        componentState.update { it.copy(map = it.map.withDestination(place.location)) }
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
            onSuccess { tripPlan ->
                componentState.update { it.copy(planning = it.planning?.withPlan(tripPlan)) }
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
            it.copy(planning = it.planning?.copy(start = start), map = it.map.copy(start = start?.location))
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
    override fun onMapPanned() = componentState.update { it.copy(map = it.map.copy(followsUser = false)) }

    // The pin drops at once; the name comes later and only reaches the recent places, and the planning header
    override fun onMapLongPress(point: GeoPoint) {
        if (!componentState.value.map.covers(point)) {
            sendUiEvent(TripPlanUiEvent.NotCovered)
            return
        }
        if (componentState.value.startSearch != null) {
            pickStart(point)
            return
        }
        componentState.update { it.copy(map = it.map.withPickedPoint(point)) }
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
        componentState.update { it.copy(planning = null, startSearch = null, map = it.map.copy(destination = null, start = null)) }
        // Full brings the search back as it was, without the keyboard: the keyboard only comes from focusing the field
        setSheetHeight(SheetHeight.Full)
    }

    override fun onPlanningRetry() = plan()

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
        val route = state.planning?.selectedRoute ?: return
        if (state.isFirstDriveExplained) {
            handOff(route)
        } else {
            componentState.update { it.copy(driveDialog = DriveDialog.FirstDrive) }
        }
    }

    // Closing the note comes before the hand-off, so the dialog doesn't come back when the maps app turns out to be missing
    override fun onFirstDriveConfirm() {
        val route = componentState.value.planning?.selectedRoute
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

    private fun handOff(route: Route) {
        handOffUseCase.execute(route) {
            onSuccess { result ->
                when (result) {
                    MapsAppResult.Opened -> startDrive(route)
                    MapsAppResult.Missing -> componentState.update { it.copy(driveDialog = DriveDialog.MapsAppMissing) }
                }
            }
            onError { error -> logger.e(error) { "Route not handed off" } }
        }
    }

    // Errors are only logged: the drive stays stored and is recorded on a later try
    private fun startDrive(route: Route) {
        startDriveUseCase.execute(StartDriveUseCase.Args(route = route, at = Clock.System.now())) {
            onError { error -> if (error !is OfflineException) logger.e(error) { "Drive not started" } }
        }
    }

    private fun readFirstDriveNote() = isNoteClosedUseCase.execute(Note.FirstDrive) {
        onSuccess { isClosed -> componentState.update { it.copy(isFirstDriveExplained = isClosed) } }
        onError { error -> logger.e(error) { "First drive note not read" } }
    }

    // Drives from an earlier start without a connection; offline is expected here, so it isn't logged
    private fun recordPendingDrives() = recordPendingDrivesUseCase.execute {
        onError { error -> if (error !is OfflineException) logger.e(error) { "Pending drives not recorded" } }
    }

    override fun onMyLocationClick() = useMyLocation()

    private fun useMyLocation() {
        when (componentState.value.myLocationRow) {
            MyLocationRow.Available -> useStart(null)
            MyLocationRow.NeedsPreciseLocation -> askForPreciseLocation(then = ::useMyLocationOnceUsable)
            MyLocationRow.NeedsLocationAccess -> askForLocationAccess(then = ::useMyLocationOnceUsable)
            MyLocationRow.OutsideCoveredArea -> sendUiEvent(TripPlanUiEvent.NotCovered)
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
        LocateButton.NotFollowing -> componentState.update { it.copy(map = it.map.copy(followsUser = true)) }
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
        backCallback.isEnabled = state.sheetHeight != SheetHeight.Collapsed || state.planning != null
    }

    // The first covered area sets the overview; until it's loaded the map shows the launch region, undimmed
    private fun loadCoverage() = observeCoverageUseCase.execute {
        onNext { coverage -> componentState.update { it.copy(map = it.map.withCoverage(coverage)) } }
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
        componentState.update { it.copy(locationAccess = access, map = it.map.withAccess(access)) }
        if (access.allowsLocation) startLocationUpdates() else stopLocationUpdates()
    }

    // Updates pause while the screen is stopped and resume on start, so access read again on start doesn't restart them
    private fun startLocationUpdates() {
        if (useCaseJobPool[observeUserLocationUseCase]?.isActive == true) return
        observeUserLocationUseCase.executeWithLifecycle {
            onNext { point ->
                val wasUnknown = componentState.value.map.userLocation == null
                componentState.update { it.copy(map = it.map.withUserLocation(point)) }
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
private fun TripPlanViewState.withRecentPlaces(places: List<Place>): TripPlanViewState =
    copy(search = search.copy(recentPlaces = places.toImmutableList()))

// An unnamed point's area is only its coordinates for the header; without both, the backend names it
private fun forBackend(place: Place): Place =
    place.takeIf { it.name != null } ?: Place(name = null, area = null, location = place.location)
