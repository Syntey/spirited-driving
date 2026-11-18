package com.syntey.spiriteddriving.core.domain.drive

import com.syntey.spiriteddriving.core.domain.route.FakeRoutePlanner
import com.syntey.spiriteddriving.core.domain.route.Route
import kotlin.time.Instant

/**
 * [TripHistorySource] that keeps [drives] in memory. A drive's full route is the recorded plan's route with its route
 * ID, or its fun route. Set [error] to make every call throw it.
 */
class FakeTripHistorySource(drives: List<PastDrive> = emptyList()) : TripHistorySource {

    var drives: List<PastDrive> = drives
        private set

    var error: Throwable? = null

    var clearCount: Int = 0
        private set

    override suspend fun list(cursor: String?, limit: Int): PastDrivePage {
        error?.let { throw it }
        val start = cursor?.toInt() ?: 0
        val page = drives.drop(start).take(limit)
        val next = start + page.size
        return PastDrivePage(drives = page, nextCursor = next.takeIf { it < drives.size }?.toString())
    }

    override suspend fun getRoute(driveId: String, maxHandOffWaypoints: Int): Route {
        error?.let { throw it }
        val drive = drives.firstOrNull { it.id == driveId } ?: throw PastDriveNotFoundException()
        return (listOf(FakeRoutePlanner.Recorded.fastest) + FakeRoutePlanner.Recorded.funRoutes)
            .firstOrNull { it.id == drive.routeId } ?: FakeRoutePlanner.Recorded.funRoutes.first()
    }

    override suspend fun clear() {
        error?.let { throw it }
        clearCount++
        drives = emptyList()
    }

    companion object {
        val STARTED_AT: Instant = Instant.parse("2026-11-08T09:30:00Z")

        /** The recorded plan's fun route, driven at [STARTED_AT]. */
        val Recorded: PastDrive = FakeRoutePlanner.Recorded.funRoutes.first()
            .toPastDrive(id = "drive-1", startedAt = STARTED_AT)
    }
}

/** This route as a past drive with [id], handed off at [startedAt]. */
fun Route.toPastDrive(id: String, startedAt: Instant, rating: Rating? = null): PastDrive = PastDrive(
    id = id,
    startedAt = startedAt,
    routeId = this.id,
    kind = kind,
    start = start,
    destination = destination,
    duration = duration,
    distance = distance,
    funLevel = funLevel,
    rating = rating,
)
