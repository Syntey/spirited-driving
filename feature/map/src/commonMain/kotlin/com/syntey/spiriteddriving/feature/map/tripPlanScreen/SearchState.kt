package com.syntey.spiriteddriving.feature.map.tripPlanScreen

import com.syntey.spiriteddriving.core.domain.place.PlaceSuggestion
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/**
 * The destination search in the map's sheet.
 *
 * @property text What the user typed.
 * @property suggestions The places found for [text], best match first. The previous ones stay while a search runs, so the
 *   list doesn't flash.
 */
data class SearchState(val text: String = "", val suggestions: ImmutableList<PlaceSuggestion> = persistentListOf()) {
    /** The hint shows in place of the suggestions while the field is empty. */
    val showsHint: Boolean get() = text.isEmpty()
}
