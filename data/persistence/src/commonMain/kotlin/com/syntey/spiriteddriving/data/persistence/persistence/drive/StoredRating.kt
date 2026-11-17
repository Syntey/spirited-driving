package com.syntey.spiriteddriving.data.persistence.persistence.drive

import com.syntey.spiriteddriving.core.domain.drive.Rating
import com.syntey.spiriteddriving.core.domain.drive.RatingReason
import com.syntey.spiriteddriving.core.domain.drive.Thumb
import kotlinx.serialization.Serializable

/**
 * A rating as kept on the device. Its names are written out, not taken from the enums, so renaming an enum entry doesn't
 * change the stored format.
 */
@Serializable
internal data class StoredRating(val thumb: String, val reasons: List<String>)

internal fun Rating.toStored() = StoredRating(
    thumb = thumb.toStored(),
    reasons = reasons.sortedBy(RatingReason::ordinal).map { it.toStored() },
)

/** Null when the thumb isn't known; an unknown reason is dropped. */
internal fun StoredRating.toDomain(): Rating? {
    val thumb = thumb.toThumb() ?: return null
    val reasons = reasons.mapNotNull { it.toReason() }.filter { it.thumb == thumb }.toSet()
    return Rating(thumb = thumb, reasons = reasons)
}

private fun Thumb.toStored(): String = when (this) {
    Thumb.Up -> "up"
    Thumb.Down -> "down"
}

private fun String.toThumb(): Thumb? = when (this) {
    "up" -> Thumb.Up
    "down" -> Thumb.Down
    else -> null
}

private fun RatingReason.toStored(): String = when (this) {
    RatingReason.CurvyRoads -> "curvy_roads"
    RatingReason.NiceViews -> "nice_views"
    RatingReason.LittleTraffic -> "little_traffic"
    RatingReason.SmoothSurface -> "smooth_surface"
    RatingReason.WorthTheExtraTime -> "worth_the_extra_time"
    RatingReason.BoringRoads -> "boring_roads"
    RatingReason.TooManyTowns -> "too_many_towns"
    RatingReason.TooNarrow -> "too_narrow"
    RatingReason.RoughSurface -> "rough_surface"
    RatingReason.HeavyTraffic -> "heavy_traffic"
    RatingReason.Roadworks -> "roadworks"
    RatingReason.TookTooLong -> "took_too_long"
    RatingReason.MapsAppWentAnotherWay -> "maps_app_went_another_way"
}

private val REASONS_BY_STORED = RatingReason.entries.associateBy { it.toStored() }

private fun String.toReason(): RatingReason? = REASONS_BY_STORED[this]
