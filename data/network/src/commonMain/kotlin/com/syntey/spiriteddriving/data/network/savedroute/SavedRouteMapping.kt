package com.syntey.spiriteddriving.data.network.savedroute

import com.syntey.spiriteddriving.core.domain.geo.Length.Companion.metres
import com.syntey.spiriteddriving.core.domain.route.RouteSummary
import com.syntey.spiriteddriving.core.domain.savedroute.SavedRoute
import com.syntey.spiriteddriving.core.domain.savedroute.SavedRouteId
import com.syntey.spiriteddriving.data.network.dto.plan.RouteSummaryDto
import com.syntey.spiriteddriving.data.network.dto.savedroute.SavedRouteSummaryDto
import com.syntey.spiriteddriving.data.network.plan.toDomain
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

internal fun SavedRouteSummaryDto.toDomain(): SavedRoute = SavedRoute(
    id = SavedRouteId(id),
    name = name,
    savedAt = Instant.parse(savedAt),
    route = route.toDomain(),
)

internal fun RouteSummaryDto.toDomain(): RouteSummary = RouteSummary(
    id = id,
    start = start.toDomain(),
    destination = destination.toDomain(),
    duration = durationSeconds.seconds,
    distance = distanceMeters.metres,
    funLevel = funLevel,
    stats = stats.toDomain(),
)
