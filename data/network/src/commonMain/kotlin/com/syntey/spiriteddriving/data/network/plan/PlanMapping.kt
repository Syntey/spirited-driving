package com.syntey.spiriteddriving.data.network.plan

import com.syntey.spiriteddriving.core.domain.geo.Length.Companion.metres
import com.syntey.spiriteddriving.core.domain.geo.decodePolyline
import com.syntey.spiriteddriving.core.domain.place.Place
import com.syntey.spiriteddriving.core.domain.route.Budget
import com.syntey.spiriteddriving.core.domain.route.OutsideCoverageException
import com.syntey.spiriteddriving.core.domain.route.HandOff
import com.syntey.spiriteddriving.core.domain.route.Route
import com.syntey.spiriteddriving.core.domain.route.RouteKind
import com.syntey.spiriteddriving.core.domain.route.RouteStats
import com.syntey.spiriteddriving.core.domain.route.TripPlan
import com.syntey.spiriteddriving.core.domain.route.TripRequest
import com.syntey.spiriteddriving.data.network.dto.plan.BudgetDto
import com.syntey.spiriteddriving.data.network.dto.plan.DistanceBudgetDto
import com.syntey.spiriteddriving.data.network.dto.plan.HandOffDto
import com.syntey.spiriteddriving.data.network.dto.plan.PlaceDto
import com.syntey.spiriteddriving.data.network.dto.plan.PlaceInputDto
import com.syntey.spiriteddriving.data.network.dto.plan.PlanDto
import com.syntey.spiriteddriving.data.network.dto.plan.PlanRequestDto
import com.syntey.spiriteddriving.data.network.dto.plan.RouteDto
import com.syntey.spiriteddriving.data.network.dto.plan.RouteStatsDto
import com.syntey.spiriteddriving.data.network.dto.plan.TimeBudgetDto
import com.syntey.spiriteddriving.data.network.geo.toDomain
import com.syntey.spiriteddriving.data.network.geo.toDto
import com.syntey.spiriteddriving.data.network.result.NetworkError
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
import com.syntey.spiriteddriving.data.network.tools.Constants

internal fun NetworkError.HttpError.toPlanError(): Throwable? = when (code) {
    Constants.ProblemCode.OUTSIDE_COVERAGE -> OutsideCoverageException(this)
    else -> null
}

internal fun TripRequest.toDto(): PlanRequestDto = PlanRequestDto(
    start = start.toDto(),
    destination = destination.toDto(),
    budget = budget.toDto(),
    vignettes = vignettes.mapTo(mutableSetOf()) { it.value },
    maxHandOffWaypoints = maxHandOffWaypoints,
)

internal fun Place.toDto(): PlaceInputDto = PlaceInputDto(location = location.toDto(), name = name, area = area)

internal fun Budget.toDto(): BudgetDto = when (this) {
    is Budget.Time -> TimeBudgetDto(extraSeconds = extra.inWholeSeconds.toInt())
    is Budget.Distance -> DistanceBudgetDto(extraMeters = extra.inWholeMetres)
}

internal fun BudgetDto.toDomain(): Budget = when (this) {
    is TimeBudgetDto -> Budget.Time(extra = extraSeconds.seconds)
    is DistanceBudgetDto -> Budget.Distance(extra = extraMeters.metres)
}

internal fun PlanDto.toDomain(): TripPlan = TripPlan(
    expiresAt = Instant.parse(expiresAt),
    fastest = fastest.toDomain(),
    funRoutes = funRoutes.map { it.toDomain() },
    closestFunRouteBudget = closestFunRouteBudget?.toDomain(),
)

internal fun RouteDto.toDomain(): Route = Route(
    id = id,
    kind = kind.toRouteKind(),
    start = start.toDomain(),
    destination = destination.toDomain(),
    duration = durationSeconds.seconds,
    distance = distanceMeters.metres,
    extraDuration = extraDurationSeconds.seconds,
    extraDistance = extraDistanceMeters.metres,
    funLevel = funLevel,
    stats = stats.toDomain(),
    line = decodePolyline(polyline),
    handOff = handOff.toDomain(),
    attribution = attribution,
)

// A kind the app doesn't know yet is shown as a fun route, so a new backend kind doesn't fail the whole plan
private fun String.toRouteKind(): RouteKind = when (this) {
    Constants.RouteKindCode.FASTEST -> RouteKind.Fastest
    else -> RouteKind.Fun
}

internal fun RouteStatsDto.toDomain(): RouteStats = RouteStats(
    curvyDistance = curvyDistanceMeters.metres,
    ascent = ascentMeters.metres,
)

private fun HandOffDto.toDomain(): HandOff = HandOff(
    waypoints = waypoints.map { it.toDomain() },
    followedShare = followedShare,
)

internal fun PlaceDto.toDomain(): Place = Place(name = name, area = area, location = location.toDomain())
