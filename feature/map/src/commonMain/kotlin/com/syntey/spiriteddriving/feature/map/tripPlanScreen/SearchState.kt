package com.syntey.spiriteddriving.feature.map.tripPlanScreen

import com.syntey.spiriteddriving.core.domain.place.PlaceSuggestion
import com.syntey.spiriteddriving.feature.map.domain.MIN_SEARCH_LENGTH
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/**
 * The destination search in the map's sheet.
 *
 * @property text What the user typed.
 * @property suggestions The places found for [text], best match first. The previous ones stay while a search runs, so the
 *   list doesn't flash.
 * @property status Where the search is for [text].
 * @property isSlow Whether a search has gone on for 0.5 s without an answer, so the sheet shows progress.
 */
data class SearchState(
    val text: String = "",
    val suggestions: ImmutableList<PlaceSuggestion> = persistentListOf(),
    val status: SearchStatus = SearchStatus.Idle,
    val isSlow: Boolean = false,
) {
    /** The hint shows in place of the suggestions while the field is empty. */
    val showsHint: Boolean get() = text.isEmpty()

    /** The sheet says no place matches, once the search for a long enough text has answered with none. */
    val showsNoResults: Boolean
        get() = status == SearchStatus.Found && suggestions.isEmpty() && text.trim().length >= MIN_SEARCH_LENGTH

    /** A progress indicator shows while a search is slow to answer. */
    val showsProgress: Boolean get() = status == SearchStatus.Searching && isSlow
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
