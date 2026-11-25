package com.syntey.spiriteddriving.feature.map.model.ui

/**
 * The search step for the trip's start, open over route planning.
 *
 * @property closesPlanning The step opened because a destination was chosen without a usable location, so closing it closes
 *   route planning too.
 */
data class StartSearch(val closesPlanning: Boolean)
