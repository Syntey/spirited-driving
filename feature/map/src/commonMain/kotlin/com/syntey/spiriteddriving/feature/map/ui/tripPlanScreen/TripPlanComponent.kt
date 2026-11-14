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
import com.syntey.spiriteddriving.core.domain.settings.Note
import com.syntey.spiriteddriving.core.platform.binding.LocationAccess
import com.syntey.spiriteddriving.core.ui.base.AppComponentContext
import com.syntey.spiriteddriving.core.ui.base.ScreenComponent
import com.syntey.spiriteddriving.core.ui.base.executeWithLifecycle
import com.syntey.spiriteddriving.feature.map.domain.AddRecentPlaceUseCase
import com.syntey.spiriteddriving.feature.map.domain.CloseNoteUseCase
import com.syntey.spiriteddriving.feature.map.domain.GetDistanceUnitUseCase
import com.syntey.spiriteddriving.feature.map.domain.GetLocationAccessUseCase
import com.syntey.spiriteddriving.feature.map.domain.GetRecentPlacesUseCase
import com.syntey.spiriteddriving.feature.map.domain.IsNoteClosedUseCase
import com.syntey.spiriteddriving.feature.map.domain.MIN_SEARCH_LENGTH
import com.syntey.spiriteddriving.feature.map.domain.NamePointUseCase
import com.syntey.spiriteddriving.feature.map.domain.ObserveCoverageUseCase
import com.syntey.spiriteddriving.feature.map.domain.ObserveUserLocationUseCase
import com.syntey.spiriteddriving.feature.map.domain.OpenLocationSettingsUseCase
import com.syntey.spiriteddriving.feature.map.domain.PlanTripUseCase
import com.syntey.spiriteddriving.feature.map.domain.RemoveRecentPlaceUseCase
import com.syntey.spiriteddriving.feature.map.domain.RequestLocationAccessUseCase
import com.syntey.spiriteddriving.feature.map.domain.RequestPreciseLocationUseCase
import com.syntey.spiriteddriving.feature.map.domain.SearchPlacesUseCase
import com.syntey.spiriteddriving.feature.map.domain.SearchPlacesUseCase.SearchUpdate
import com.syntey.spiriteddriving.feature.map.domain.unnamedPlace
import com.syntey.spiriteddriving.feature.map.model.ui.LocateButton
import com.syntey.spiriteddriving.feature.map.model.ui.PlanningStatus
import com.syntey.spiriteddriving.feature.map.model.ui.RoutePlanningState
import com.syntey.spiriteddriving.feature.map.model.ui.SearchState
import com.syntey.spiriteddriving.feature.map.model.ui.SearchStatus
import com.syntey.spiriteddriving.feature.map.model.ui.SheetHeight
import com.syntey.spiriteddriving.feature.map.model.ui.allowsLocation
import kotlin.time.Duration.Companion.minutes
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import org.koin.core.annotation.Factory
import org.koin.core.annotation.InjectedParam

@Factory
@GenerateFactory
// Each action of the sheet is a function here, and route planning adds six to the search's thirty
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
    private val getDistanceUnitUseCase: GetDistanceUnitUseCase,
    private val getRecentPlacesUseCase: GetRecentPlacesUseCase,
    private val addRecentPlaceUseCase: AddRecentPlaceUseCase,
    private val removeRecentPlaceUseCase: RemoveRecentPlaceUseCase,
    private val namePointUseCase: NamePointUseCase,
    private val planTripUseCase: PlanTripUseCase,
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

    // Back closes route planning, or the search while the sheet is pulled up
    private val backCallback = BackCallback(isEnabled = false) {
        if (componentState.value.planning != null) onRoutePlanningClose() else closeSearch()
    }

    init {
        backHandler.register(backCallback)
        // On every start, so access turned on in the system settings shows when the user comes back
        doOnStart(isOneTime = false) { readLocationAccess() }
        loadCoverage()
        readPreciseLocationNote()
        // From the phone's region, so it's known before the first distance shows
        getDistanceUnitUseCase.execute {
            onSuccess { unit -> componentState.update { it.copy(distanceUnit = unit) } }
            onError { error -> logger.e(error) { "Distance unit not read" } }
        }
        getRecentPlacesUseCase.execute {
            onSuccess { places -> componentState.update { it.withRecentPlaces(places) } }
            onError { error -> logger.e(error) { "Recent places not read" } }
        }
    }

    override fun onSearchFocus() = setSheetHeight(SheetHeight.Full)

    override fun onSearchTextChange(text: String) {
        componentState.update { it.copy(search = it.search.copy(text = text)) }
        search(text)
    }

    override fun onSearchClear() = clearSearch()

    override fun onSearchRetry() = search(componentState.value.search.text)

    override fun onSearchClose() = closeSearch()

    override fun onSheetHeightChange(height: SheetHeight) = setSheetHeight(height)

    // Without the user's location there's no start to plan from, so choosing a place only drops its pin
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
        componentState.update { it.copy(map = it.map.withDestination(place.location)) }
        // The search stays as it was, so closing route planning returns to it
        if (componentState.value.canPlanFromUser) startPlanning(place) else closeSearch()
    }

    private fun startPlanning(destination: Place) {
        componentState.update { it.copy(planning = RoutePlanningState(destination = destination, budget = DEFAULT_BUDGET)) }
        setSheetHeight(SheetHeight.Half)
        plan()
    }

    // Each plan cancels the one before, so a retry or a new budget never shows an older answer
    private fun plan() {
        val state = componentState.value
        val planning = state.planning ?: return
        val userLocation = state.map.userLocation ?: return
        componentState.update {
            it.copy(planning = RoutePlanningState(destination = planning.destination, budget = planning.budget))
        }
        val args = PlanTripUseCase.Args(
            start = Place(name = null, area = null, location = userLocation),
            // An unnamed point's area is only its coordinates for the header; without both, the backend names it
            destination =
                planning.destination.takeIf {
                    it.name != null
                } ?: Place(name = null, area = null, location = planning.destination.location),
            budget = planning.budget,
        )
        planTripUseCase.execute(args) {
            onSuccess { tripPlan ->
                componentState.update { it.copy(planning = it.planning?.withPlan(tripPlan)) }
            }
            onError { error ->
                val isOffline = error is OfflineException
                if (!isOffline) logger.e(error) { "Trip not planned" }
                val status = if (isOffline) PlanningStatus.Offline else PlanningStatus.Failed
                componentState.update { it.copy(planning = it.planning?.copy(status = status)) }
            }
        }
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
        val canPlan = componentState.value.canPlanFromUser
        componentState.update { it.copy(map = it.map.withPickedPoint(point)) }
        if (canPlan) startPlanning(unnamedPlace(point)) else closeSearch()
        namePointUseCase.execute(point) {
            onSuccess { place ->
                keep(place)
                componentState.update { state ->
                    val planning = state.planning
                    if (planning?.destination?.location == point) state.copy(planning = planning.copy(destination = place)) else state
                }
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

    override fun onRoutePlanningClose() {
        useCaseJobPool[planTripUseCase]?.cancel()
        componentState.update { it.copy(planning = null, map = it.map.copy(destination = null)) }
        // Full brings the search back as it was, without the keyboard: the keyboard only comes from focusing the field
        setSheetHeight(SheetHeight.Full)
    }

    override fun onPlanningRetry() = plan()

    // Profile isn't built yet
    override fun onProfileClick() = Unit

    override fun onPreciseLocationClick() = askForPreciseLocation()

    // Hidden at once, so the note doesn't wait for the close to be saved
    override fun onPreciseLocationNoteClose() {
        componentState.update { it.copy(isPreciseLocationNoteClosed = true) }
        closeNoteUseCase.execute(Note.PreciseLocation) {
            onError { error -> logger.e(error) { "Precise location note not closed" } }
        }
    }

    private fun locate() = when (componentState.value.locateButton) {
        LocateButton.Following -> Unit
        LocateButton.NotFollowing -> componentState.update { it.copy(map = it.map.copy(followsUser = true)) }
        LocateButton.NoAccess -> askForLocationAccess()
    }

    // Each search cancels the one before, which may still wait for the pause in typing, so only the current text's answer shows
    private fun search(text: String) {
        val isLongEnough = text.trim().length >= MIN_SEARCH_LENGTH
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

    private fun askForLocationAccess() {
        // iOS asks only once, so on iOS a denial is always for good and this opens the settings
        if (componentState.value.locationAccess == LocationAccess.DeniedForGood) {
            openLocationSettingsUseCase.execute { onError { error -> logger.e(error) { "System settings not opened" } } }
        } else {
            requestLocationAccessUseCase.execute {
                onSuccess(::applyLocationAccess)
                onError { error -> logger.e(error) { "Location prompt failed" } }
            }
        }
    }

    private fun readPreciseLocationNote() = isNoteClosedUseCase.execute(Note.PreciseLocation) {
        onSuccess { isClosed -> componentState.update { it.copy(isPreciseLocationNoteClosed = isClosed) } }
        onError { error -> logger.e(error) { "Precise location note not read" } }
    }

    private fun askForPreciseLocation() = requestPreciseLocationUseCase.execute {
        onSuccess(::applyLocationAccess)
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
            onNext { point -> componentState.update { it.copy(map = it.map.withUserLocation(point)) } }
            onError { error -> logger.e(error) { "Location updates failed" } }
        }
    }

    // Running flow use cases are kept in the job pool, by use case
    private fun stopLocationUpdates() {
        useCaseJobPool[observeUserLocationUseCase]?.cancel()
    }
}

// The recent places as the search keeps them, with the rest of the search state kept as it is
private fun TripPlanViewState.withRecentPlaces(places: List<Place>): TripPlanViewState =
    copy(search = search.copy(recentPlaces = places.toImmutableList()))

// The budget chips come later; until then every plan uses this fixed budget in time
private val DEFAULT_BUDGET: Budget = Budget.Time(30.minutes)
