package com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen

import com.syntey.spiriteddriving.feature.map.model.ui.RatingStep

/**
 * What the rating step shows.
 *
 * @property step The rating step: the drive's question, the thumb and reasons picked so far, and the reminder offer.
 */
internal data class RatingViewState(val step: RatingStep)
