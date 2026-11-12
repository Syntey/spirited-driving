package com.syntey.spiriteddriving.core.domain.place

/**
 * A place found by [PlaceSearch].
 *
 * @property isCovered Whether routes can start or end there.
 * @property kind What kind of place it is, for its icon.
 */
data class PlaceSuggestion(val place: Place, val isCovered: Boolean, val kind: PlaceKind)
