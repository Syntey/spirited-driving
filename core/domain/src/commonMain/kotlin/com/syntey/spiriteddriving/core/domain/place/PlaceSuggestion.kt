package com.syntey.spiriteddriving.core.domain.place

/**
 * A place found by [PlaceSearch].
 *
 * @property isCovered Whether routes can start or end there.
 */
data class PlaceSuggestion(val place: Place, val isCovered: Boolean)
