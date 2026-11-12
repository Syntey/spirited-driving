package com.syntey.spiriteddriving.feature.map.model.ui

import com.syntey.spiriteddriving.core.domain.place.Place
import dev.icerock.moko.resources.desc.StringDesc

/**
 * A recent place as the sheet shows it.
 *
 * @property isCovered Whether routes can start or end there; a place that isn't is dimmed and says so.
 * @property boldRanges The parts of the name that match the typed words, as [matchedRanges] finds them.
 * @property distance The straight-line distance from the user, as [distanceText] writes it; null without location access.
 */
data class RecentPlaceRow(val place: Place, val isCovered: Boolean, val boldRanges: List<IntRange>, val distance: StringDesc?)
