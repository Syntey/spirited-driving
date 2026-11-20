package com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen

import app.futured.arkitekt.annotation.GenerateFactory
import app.futured.arkitekt.crusecases.execute
import co.touchlab.kermit.Logger
import com.syntey.spiriteddriving.core.domain.error.OfflineException
import com.syntey.spiriteddriving.core.domain.geo.GeoPoint
import com.syntey.spiriteddriving.core.domain.place.Place
import com.syntey.spiriteddriving.core.domain.place.PlaceSuggestion
import com.syntey.spiriteddriving.core.ui.base.AppComponent
import com.syntey.spiriteddriving.core.ui.base.AppComponentContext
import com.syntey.spiriteddriving.feature.map.domain.AddRecentPlaceUseCase
import com.syntey.spiriteddriving.feature.map.domain.GetRecentPlacesUseCase
import com.syntey.spiriteddriving.feature.map.domain.RemoveRecentPlaceUseCase
import com.syntey.spiriteddriving.feature.map.domain.SearchPlacesUseCase
import com.syntey.spiriteddriving.feature.map.domain.SearchPlacesUseCase.SearchUpdate
import com.syntey.spiriteddriving.feature.map.model.ui.SearchState
import com.syntey.spiriteddriving.feature.map.model.ui.SearchStatus
import com.syntey.spiriteddriving.feature.map.model.ui.SheetHeight
import com.syntey.spiriteddriving.feature.map.tools.Constants
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import org.koin.core.annotation.Factory
import org.koin.core.annotation.InjectedParam

/**
 * The place search of the map sheet: the text, the suggestions and the recent places. It is the destination search at the root of
 * the sheet's stack, and the start search when pushed over route planning. What it chooses goes to the host.
 */
@Factory
@GenerateFactory
internal class PlaceSearchComponent(
    @InjectedParam componentContext: AppComponentContext,
    @InjectedParam private val host: MapSheetHost,
    @InjectedParam args: Args,
    private val searchPlacesUseCase: SearchPlacesUseCase,
    private val getRecentPlacesUseCase: GetRecentPlacesUseCase,
    private val addRecentPlaceUseCase: AddRecentPlaceUseCase,
    private val removeRecentPlaceUseCase: RemoveRecentPlaceUseCase,
) : AppComponent<PlaceSearchViewState, Nothing>(
    componentContext = componentContext,
    defaultState = PlaceSearchViewState(),
) {

    /** @property isStart Whether this is the start search; the start shows the My location row. */
    data class Args(val isStart: Boolean)

    private val logger = Logger.withTag(if (args.isStart) "StartSearchComponent" else "PlaceSearchComponent")

    val viewState: StateFlow<PlaceSearchViewState> = componentState

    init {
        readRecentPlaces()
    }

    private fun readRecentPlaces() = getRecentPlacesUseCase.execute {
        onSuccess { places -> componentState.update { it.withRecentPlaces(places) } }
        onError { error -> logger.e(error) { "Recent places not read" } }
    }

    fun onFocus() = host.requestSheetHeight(SheetHeight.Full)

    fun onTextChange(text: String) {
        componentState.update { it.copy(search = it.search.copy(text = text)) }
        search(text)
    }

    fun onRetry() = search(componentState.value.search.text)

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
        val args = SearchPlacesUseCase.Args(text, near = host.userLocation)
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

    fun clear() {
        useCaseJobPool[searchPlacesUseCase]?.cancel()
        componentState.update { it.copy(search = SearchState(recentPlaces = it.search.recentPlaces)) }
    }

    fun onSuggestionClick(suggestion: PlaceSuggestion) {
        if (!suggestion.isCovered) {
            host.onNotCovered()
            return
        }
        host.onPlaceChosen(suggestion.place)
    }

    // A recent place counts as covered until the covered area is loaded, since it was covered when it was chosen
    fun onRecentPlaceClick(place: Place) {
        if (!host.covers(place.location)) {
            host.onNotCovered()
            return
        }
        host.onPlaceChosen(place)
    }

    // Hidden at once, so the row doesn't wait for the removal to be saved
    fun onRecentPlaceRemove(place: Place) {
        componentState.update { it.withRecentPlaces(it.search.recentPlaces.filterNot { recent -> recent == place }) }
        removeRecentPlaceUseCase.execute(place) {
            onSuccess { places -> recentPlacesChanged(places) }
            onError { error -> logger.e(error) { "Recent place not removed" } }
        }
    }

    // Kept by the root search for a chosen or named place, so every search lists it
    fun keep(place: Place) {
        addRecentPlaceUseCase.execute(place) {
            onSuccess { places -> recentPlacesChanged(places) }
            onError { error -> logger.e(error) { "Recent place not kept" } }
        }
    }

    // Shown here, and passed to the other searches, which show the same recent places
    private fun recentPlacesChanged(places: List<Place>) {
        componentState.update { it.withRecentPlaces(places) }
        host.onRecentPlacesChanged(places)
    }

    fun showRecentPlaces(places: List<Place>) = componentState.update { it.withRecentPlaces(places) }
}

// The recent places as the search keeps them, with the rest of the search state kept as it is
private fun PlaceSearchViewState.withRecentPlaces(places: List<Place>): PlaceSearchViewState =
    copy(search = search.copy(recentPlaces = places.toImmutableList()))
