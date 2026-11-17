package com.syntey.spiriteddriving.core.domain.drive

/** The user's answer to "How was the route?". */
enum class Thumb { Up, Down }

/** A reason picked with a thumb, in the order the app shows them: the roads first, then the trip. */
enum class RatingReason(val thumb: Thumb) {
    CurvyRoads(Thumb.Up),
    NiceViews(Thumb.Up),
    LittleTraffic(Thumb.Up),
    SmoothSurface(Thumb.Up),
    WorthTheExtraTime(Thumb.Up),
    BoringRoads(Thumb.Down),
    TooManyTowns(Thumb.Down),
    TooNarrow(Thumb.Down),
    RoughSurface(Thumb.Down),
    HeavyTraffic(Thumb.Down),
    Roadworks(Thumb.Down),
    TookTooLong(Thumb.Down),
    MapsAppWentAnotherWay(Thumb.Down),
    ;

    companion object {
        /** The reasons offered with [thumb], in order. */
        fun of(thumb: Thumb): List<RatingReason> = entries.filter { it.thumb == thumb }
    }
}

/** A drive's rating. Each thumb has its own reasons, so [reasons] only holds ones of [thumb]. */
data class Rating(val thumb: Thumb, val reasons: Set<RatingReason> = emptySet()) {
    init {
        require(reasons.all { it.thumb == thumb }) { "A reason of the other thumb can't be picked" }
    }
}
