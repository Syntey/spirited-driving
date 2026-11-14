package com.syntey.spiriteddriving.feature.map.domain

import app.futured.arkitekt.crusecases.FlowUseCase
import com.syntey.spiriteddriving.core.domain.geo.GeoPoint
import com.syntey.spiriteddriving.core.domain.place.PlaceSearch
import com.syntey.spiriteddriving.core.domain.place.PlaceSuggestion
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import org.koin.core.annotation.Factory
import com.syntey.spiriteddriving.feature.map.tools.Constants

/**
 * Suggestions for what the user typed. The search waits for a pause in typing, so each keystroke doesn't search. Text
 * shorter than [Constants.Search.MIN_LENGTH] characters finds nothing, at once. A search that takes longer than 0.5 s first emits
 * [SearchUpdate.Slow], so the sheet can show that it's still working.
 */
@Factory
internal class SearchPlacesUseCase(private val placeSearch: PlaceSearch) :
    FlowUseCase<SearchPlacesUseCase.Args, SearchPlacesUseCase.SearchUpdate> {

    override fun build(args: Args): Flow<SearchUpdate> = channelFlow {
        val text = args.text.trim()
        if (text.length < Constants.Search.MIN_LENGTH) {
            send(SearchUpdate.Found(emptyList()))
            return@channelFlow
        }
        delay(Constants.Search.TYPING_PAUSE)
        val slowNotice = launch {
            delay(Constants.Search.SLOW_AFTER)
            send(SearchUpdate.Slow)
        }
        val suggestions = placeSearch.search(text, args.near)
        slowNotice.cancel()
        send(SearchUpdate.Found(suggestions))
    }

    /** [near]: the user's location, if known. */
    data class Args(val text: String, val near: GeoPoint?)

    /** What the search reports, in order. */
    sealed interface SearchUpdate {

        /** No answer 0.5 s after the search started. */
        data object Slow : SearchUpdate

        /** The answer for the text: [suggestions], best match first. */
        data class Found(val suggestions: List<PlaceSuggestion>) : SearchUpdate
    }
}
