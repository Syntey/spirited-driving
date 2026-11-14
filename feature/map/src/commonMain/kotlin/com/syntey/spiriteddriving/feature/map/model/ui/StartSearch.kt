package com.syntey.spiriteddriving.feature.map.model.ui

/**
 * The search step for the trip's start, open over route planning.
 *
 * @property destinationSearch The destination search as it was when the step opened; closing the step brings it back.
 * @property closesPlanning The step opened because a destination was chosen without a usable location, so closing it closes
 *   route planning too.
 */
data class StartSearch(val destinationSearch: SearchState, val closesPlanning: Boolean)
