package com.syntey.spiriteddriving.data.network.savedroute

import com.syntey.spiriteddriving.core.domain.geo.Length.Companion.metres
import com.syntey.spiriteddriving.core.domain.savedroute.SavedRoute
import com.syntey.spiriteddriving.data.network.dto.savedroute.SavedRouteSummaryDto
import com.syntey.spiriteddriving.data.network.plan.toDomain
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

internal fun SavedRouteSummaryDto.toDomain(): SavedRoute = SavedRoute(
    id = id,
    name = name,
    savedAt = Instant.parse(savedAt),
    routeId = route.id,
    start = route.start.toDomain(),
    destination = route.destination.toDomain(),
    duration = route.durationSeconds.seconds,
    distance = route.distanceMeters.metres,
    funLevel = route.funLevel,
    stats = route.stats.toDomain(),
)
