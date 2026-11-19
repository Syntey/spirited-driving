package com.syntey.spiriteddriving.data.network.drive

import com.syntey.spiriteddriving.core.domain.drive.PastDrive
import com.syntey.spiriteddriving.core.domain.drive.PastDrivePage
import com.syntey.spiriteddriving.core.domain.drive.Rating
import com.syntey.spiriteddriving.core.domain.geo.Length.Companion.metres
import com.syntey.spiriteddriving.data.network.dto.drive.DrivePageDto
import com.syntey.spiriteddriving.data.network.dto.drive.DriveSummaryDto
import com.syntey.spiriteddriving.data.network.dto.drive.RatingDto
import com.syntey.spiriteddriving.data.network.plan.toDomain
import com.syntey.spiriteddriving.data.network.plan.toRouteKind
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

internal fun DrivePageDto.toDomain(): PastDrivePage = PastDrivePage(
    drives = items.map { it.toDomain() },
    nextCursor = nextCursor,
)

internal fun DriveSummaryDto.toDomain(): PastDrive = PastDrive(
    id = id,
    startedAt = Instant.parse(startedAt),
    routeId = route.id,
    kind = route.kind.toRouteKind(),
    start = route.start.toDomain(),
    destination = route.destination.toDomain(),
    duration = route.durationSeconds.seconds,
    distance = route.distanceMeters.metres,
    funLevel = route.funLevel,
    rating = rating?.toDomain(),
)

// A thumb the app doesn't know leaves the drive unrated; a reason it doesn't know, or one of the other thumb, is dropped
internal fun RatingDto.toDomain(): Rating? {
    val rated = thumb.toThumb() ?: return null
    val picked = reasons.mapNotNull { it.toRatingReason() }.filter { it.thumb == rated }
    return Rating(thumb = rated, reasons = picked.toSet())
}
