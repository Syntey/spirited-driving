package com.syntey.spiriteddriving.feature.map.model.ui

import com.syntey.spiriteddriving.core.domain.place.Place
import com.syntey.spiriteddriving.core.domain.place.PlaceSuggestion
import com.syntey.spiriteddriving.feature.map.domain.MIN_SEARCH_LENGTH
import com.syntey.spiriteddriving.feature.map.tools.placeMatches
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

private const val MAX_MATCHING_RECENT_PLACES = 3

/**
 * The destination search in the map's sheet.
 *
 * @property text What the user typed.
 * @property suggestions The places found for [text], best match first. The previous ones stay while a search runs, so the
 *   list doesn't flash.
 * @property status Where the search is for [text].
 * @property isSlow Whether a search has gone on for 0.5 s without an answer, so the sheet shows progress.
 * @property recentPlaces The places the user chose last, kept on the device, last used first.
 */
data class SearchState(
    val text: String = "",
    val suggestions: ImmutableList<PlaceSuggestion> = persistentListOf(),
    val status: SearchStatus = SearchStatus.Idle,
    val isSlow: Boolean = false,
    val recentPlaces: ImmutableList<Place> = persistentListOf(),
) {
    /** The recent places the sheet shows: all of them while the field is blank, else up to three that match [text]. */
    val shownRecentPlaces: List<Place>
        get() = if (text.isBlank()) {
            recentPlaces
        } else {
            recentPlaces.filter { placeMatches(name = it.name, area = it.area, text = text) }.take(MAX_MATCHING_RECENT_PLACES)
        }

    /** The suggestions the sheet shows, without those that are the same place as a [shownRecentPlaces] row. */
    val shownSuggestions: List<PlaceSuggestion>
        get() = suggestions.filterNot { suggestion -> shownRecentPlaces.any { suggestion.place.isSameAs(it) } }

    /** The header "Recent" shows above the recent places while the field is blank. */
    val showsRecentHeader: Boolean get() = text.isBlank() && recentPlaces.isNotEmpty()

    /** The hint shows in place of the places while the field is blank and there are no recent places to show. */
    val showsHint: Boolean get() = text.isBlank() && recentPlaces.isEmpty()

    /**
     * The sheet says no place matches, once the search for a long enough text has answered with none, and no recent place
     * matches either.
     */
    val showsNoResults: Boolean
        get() = status == SearchStatus.Found &&
            shownSuggestions.isEmpty() &&
            shownRecentPlaces.isEmpty() &&
            text.trim().length >= MIN_SEARCH_LENGTH

    /** A progress indicator shows while a search is slow to answer. */
    val showsProgress: Boolean get() = status == SearchStatus.Searching && isSlow

    /** Why the search for [text] failed; null while it hasn't. */
    val failure: SearchFailure?
        get() = when (status) {
            SearchStatus.Offline -> SearchFailure.Offline
            SearchStatus.Failed -> SearchFailure.Failed
            else -> null
        }

    /** The failure fills the sheet when no recent place shows above it. */
    val showsFailureAlone: Boolean get() = failure != null && shownRecentPlaces.isEmpty()
}

/** Where the destination search is. */
enum class SearchStatus {
    /** Nothing searched for the text: it's empty or under 2 characters. */
    Idle,

    /** Waiting for an answer for the text; the previous suggestions stay. */
    Searching,

    /** The answer for the text. */
    Found,

    /** The search failed without a connection. */
    Offline,

    /** The search failed for another reason. */
    Failed,
}

/** Why a search found nothing. */
enum class SearchFailure {
    /** No connection. */
    Offline,

    /** Any other reason. */
    Failed,
}
