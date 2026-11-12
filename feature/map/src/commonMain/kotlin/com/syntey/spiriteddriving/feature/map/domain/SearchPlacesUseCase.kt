package com.syntey.spiriteddriving.feature.map.domain

import app.futured.arkitekt.crusecases.UseCase
import com.syntey.spiriteddriving.core.domain.geo.GeoPoint
import com.syntey.spiriteddriving.core.domain.place.PlaceSearch
import com.syntey.spiriteddriving.core.domain.place.PlaceSuggestion
import kotlinx.coroutines.delay
import org.koin.core.annotation.Factory
import kotlin.time.Duration.Companion.milliseconds

/**
 * Suggestions for what the user typed, once they pause typing, so each keystroke doesn't search. Text shorter than 2
 * characters finds nothing, at once.
 */
@Factory
internal class SearchPlacesUseCase(private val placeSearch: PlaceSearch) : UseCase<SearchPlacesUseCase.Args, List<PlaceSuggestion>> {

    override suspend fun build(args: Args): List<PlaceSuggestion> {
        val text = args.text.trim()
        if (text.length < MIN_LENGTH) return emptyList()
        delay(TYPING_PAUSE)
        return placeSearch.search(text, args.near)
    }

    /** [near]: the user's location, if known. */
    data class Args(val text: String, val near: GeoPoint?)

    private companion object {
        const val MIN_LENGTH = 2
        val TYPING_PAUSE = 300.milliseconds
    }
}
