package com.syntey.spiriteddriving.feature.map.model.ui

import com.syntey.spiriteddriving.core.domain.place.PlaceSuggestion
import dev.icerock.moko.resources.desc.StringDesc

/**
 * A suggestion as the sheet shows it.
 *
 * @property boldRanges The parts of the name that match the typed words, as [matchedRanges] finds them.
 * @property distance The straight-line distance from the user, as [distanceText] writes it; null without location access.
 */
data class SuggestionRow(val suggestion: PlaceSuggestion, val boldRanges: List<IntRange>, val distance: StringDesc?)
