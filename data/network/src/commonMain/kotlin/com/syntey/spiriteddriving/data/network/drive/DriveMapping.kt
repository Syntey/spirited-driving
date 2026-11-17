package com.syntey.spiriteddriving.data.network.drive

import com.syntey.spiriteddriving.core.domain.drive.Drive
import com.syntey.spiriteddriving.core.domain.drive.Rating
import com.syntey.spiriteddriving.core.domain.drive.RatingReason
import com.syntey.spiriteddriving.core.domain.drive.Thumb
import com.syntey.spiriteddriving.data.network.dto.drive.RatingInputDto
import com.syntey.spiriteddriving.data.network.dto.drive.RecordDriveRequestDto
import com.syntey.spiriteddriving.data.network.tools.Constants

internal fun Drive.toDto(): RecordDriveRequestDto = RecordDriveRequestDto(
    routeId = routeId,
    startedAt = startedAt.toString(),
)

internal fun Rating.toDto(): RatingInputDto = RatingInputDto(
    thumb = thumb.toCode(),
    reasons = reasons.sortedBy(RatingReason::ordinal).map { it.toCode() },
)

private fun Thumb.toCode(): String = when (this) {
    Thumb.Up -> Constants.ThumbCode.UP
    Thumb.Down -> Constants.ThumbCode.DOWN
}

private fun RatingReason.toCode(): String = when (this) {
    RatingReason.CurvyRoads -> Constants.RatingReasonCode.CURVY_ROADS
    RatingReason.NiceViews -> Constants.RatingReasonCode.NICE_VIEWS
    RatingReason.LittleTraffic -> Constants.RatingReasonCode.LITTLE_TRAFFIC
    RatingReason.SmoothSurface -> Constants.RatingReasonCode.SMOOTH_SURFACE
    RatingReason.WorthTheExtraTime -> Constants.RatingReasonCode.WORTH_THE_EXTRA_TIME
    RatingReason.BoringRoads -> Constants.RatingReasonCode.BORING_ROADS
    RatingReason.TooManyTowns -> Constants.RatingReasonCode.TOO_MANY_TOWNS
    RatingReason.TooNarrow -> Constants.RatingReasonCode.TOO_NARROW
    RatingReason.RoughSurface -> Constants.RatingReasonCode.ROUGH_SURFACE
    RatingReason.HeavyTraffic -> Constants.RatingReasonCode.HEAVY_TRAFFIC
    RatingReason.Roadworks -> Constants.RatingReasonCode.ROADWORKS
    RatingReason.TookTooLong -> Constants.RatingReasonCode.TOOK_TOO_LONG
    RatingReason.MapsAppWentAnotherWay -> Constants.RatingReasonCode.MAPS_APP_WENT_ANOTHER_WAY
}
