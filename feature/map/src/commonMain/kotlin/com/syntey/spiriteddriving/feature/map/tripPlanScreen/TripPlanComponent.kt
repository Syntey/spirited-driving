package com.syntey.spiriteddriving.feature.map.tripPlanScreen

import app.futured.arkitekt.annotation.GenerateFactory
import app.futured.arkitekt.crusecases.execute
import co.touchlab.kermit.Logger
import com.arkivanov.essenty.backhandler.BackCallback
import com.arkivanov.essenty.lifecycle.doOnStart
import com.syntey.spiriteddriving.core.domain.error.OfflineException
import com.syntey.spiriteddriving.core.domain.place.PlaceSuggestion
import com.syntey.spiriteddriving.core.domain.settings.Note
import com.syntey.spiriteddriving.core.platform.binding.LocationAccess
import com.syntey.spiriteddriving.core.ui.base.AppComponentContext
import com.syntey.spiriteddriving.core.ui.base.ScreenComponent
import com.syntey.spiriteddriving.core.ui.base.executeWithLifecycle
import com.syntey.spiriteddriving.feature.map.domain.CloseNoteUseCase
import com.syntey.spiriteddriving.feature.map.domain.GetDistanceUnitUseCase
import com.syntey.spiriteddriving.feature.map.domain.GetLocationAccessUseCase
import com.syntey.spiriteddriving.feature.map.domain.IsNoteClosedUseCase
import com.syntey.spiriteddriving.feature.map.domain.ObserveCoverageUseCase
import com.syntey.spiriteddriving.feature.map.domain.ObserveUserLocationUseCase
import com.syntey.spiriteddriving.feature.map.domain.OpenLocationSettingsUseCase
import com.syntey.spiriteddriving.feature.map.domain.RequestLocationAccessUseCase
import com.syntey.spiriteddriving.feature.map.domain.RequestPreciseLocationUseCase
import com.syntey.spiriteddriving.feature.map.domain.MIN_SEARCH_LENGTH
import com.syntey.spiriteddriving.feature.map.domain.SearchPlacesUseCase
import com.syntey.spiriteddriving.feature.map.domain.SearchPlacesUseCase.SearchUpdate
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import org.koin.core.annotation.Factory
import org.koin.core.annotation.InjectedParam

@Factory
@GenerateFactory
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

    // Back closes the search while the sheet is pulled up
    private val closeSearchCallback = BackCallback(isEnabled = false) { closeSearch() }

    init {
        backHandler.register(closeSearchCallback)
        // On every start, so access turned on in the system settings shows when the user comes back
        doOnStart(isOneTime = false) { readLocationAccess() }
        loadCoverage()
        readPreciseLocationNote()
        // From the phone's region, so it's known before the first distance shows
        getDistanceUnitUseCase.execute {
            onSuccess { unit -> componentState.update { it.copy(distanceUnit = unit) } }
            onError { error -> logger.e(error) { "Distance unit not read" } }
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

    // Until route planning exists, choosing a place only drops its pin.
    override fun onSuggestionClick(suggestion: PlaceSuggestion) {
        if (!suggestion.isCovered) {
            sendUiEvent(TripPlanUiEvent.NotCovered)
            return
        }
        componentState.update { it.copy(map = it.map.withDestination(suggestion.place.location)) }
        closeSearch()
    }

    override fun onLocateClick() = locate()
    override fun onMapPanned() = componentState.update { it.copy(map = it.map.copy(followsUser = false)) }

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
        componentState.update { it.copy(search = SearchState()) }
    }

    private fun closeSearch() {
        clearSearch()
        setSheetHeight(SheetHeight.Collapsed)
    }

    private fun setSheetHeight(height: SheetHeight) {
        componentState.update { it.copy(sheetHeight = height) }
        closeSearchCallback.isEnabled = height != SheetHeight.Collapsed
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
