package com.syntey.spiriteddriving.feature.onboarding.art

/**
 * Where the panorama sits on a screen: [unit] pixels per unit of the panorama, with its top [shiftUp] pixels above the
 * screen's top.
 */
internal data class ArtFit(val unit: Float, val shiftUp: Float) {

    /** The panorama's x at the left edge of a screen [width] pixels wide, with page [position] in the middle. */
    fun leftEdge(width: Float, position: Float): Float {
        val visibleWidth = width / unit
        return ArtRoad.PAGE_WIDTH * position + (ArtRoad.PAGE_WIDTH - visibleWidth) / 2
    }

    companion object {
        /** The cars' centres stay between these heights of the panorama on every page and while swiping (`ArtFitTest`). */
        const val CARS_TOP = 200f
        const val CARS_BOTTOM = 412f

        /** How far a car and its shadow reach from its centre, whichever way it faces. Dusk headlights may reach further. */
        const val CAR_REACH = 26f

        /** A landscape phone leaves almost no room above the panel: there the cars go under it rather than shrink to dots. */
        const val MIN_SCALE = 0.5f

        /**
         * Fits the panorama to a screen [height] pixels tall, so the cars stay between [clearTop] and [clearBottom], in
         * pixels from the top. It fills the height when they fit there. Otherwise it moves up, and shrinks only when moving
         * up would take the cars above [clearTop].
         */
        fun of(height: Float, clearTop: Float, clearBottom: Float): ArtFit {
            val full = height / ArtRoad.PAGE_HEIGHT
            val top = CARS_TOP - CAR_REACH
            val bottom = CARS_BOTTOM + CAR_REACH
            val shiftNeeded = bottom * full - clearBottom
            return when {
                shiftNeeded <= 0f -> ArtFit(unit = full, shiftUp = 0f)

                shiftNeeded <= top * full - clearTop -> ArtFit(unit = full, shiftUp = shiftNeeded)

                else -> {
                    val unit = maxOf((clearBottom - clearTop) / (bottom - top), full * MIN_SCALE)
                    // Never down: there is no scenery above the panorama.
                    ArtFit(unit = unit, shiftUp = (top * unit - clearTop).coerceAtLeast(0f))
                }
            }
        }
    }
}
