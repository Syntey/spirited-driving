package com.syntey.spiriteddriving.feature.map.domain

import com.syntey.spiriteddriving.core.domain.drive.FakeTripHistoryCache
import com.syntey.spiriteddriving.core.domain.drive.FakeTripHistorySource
import com.syntey.spiriteddriving.core.domain.drive.PastDrive
import com.syntey.spiriteddriving.core.domain.drive.PastDriveNotFoundException
import com.syntey.spiriteddriving.core.domain.route.FakeRoutePlanner
import com.syntey.spiriteddriving.core.domain.route.RouteKind
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.time.Instant

class LoadPastDriveRouteUseCaseTest {

    private val route = FakeRoutePlanner.Recorded.funRoutes.first()
    private val drive = PastDrive(
        id = "drive-1",
        startedAt = Instant.parse("2026-11-05T13:20:00Z"),
        routeId = route.id,
        kind = RouteKind.Fun,
        start = route.start,
        destination = route.destination,
        duration = route.duration,
        distance = route.distance,
        funLevel = route.funLevel,
    )

    @Test
    fun `a past drive's route kept on the device is used without asking the backend`() = runTest {
        val cache = FakeTripHistoryCache()
        cache.putRoute(drive.id, route)
        // Empty, so asking it for the drive would throw
        val source = FakeTripHistorySource(drives = emptyList())

        assertEquals(route, LoadPastDriveRouteUseCase(source, cache).build(drive.id))
    }

    @Test
    fun `a past drive's route not kept on the device comes from the backend`() = runTest {
        val source = FakeTripHistorySource(drives = listOf(drive))

        val loaded = LoadPastDriveRouteUseCase(source, FakeTripHistoryCache()).build(drive.id)

        assertEquals(drive.routeId, loaded.id)
    }

    @Test
    fun `a route loaded from the backend is kept on the device`() = runTest {
        val cache = FakeTripHistoryCache()

        LoadPastDriveRouteUseCase(FakeTripHistorySource(drives = listOf(drive)), cache).build(drive.id)

        assertNotNull(cache.getRoute(drive.id))
    }

    @Test
    fun `a past drive that is gone on the backend fails`() = runTest {
        assertFailsWith<PastDriveNotFoundException> {
            LoadPastDriveRouteUseCase(FakeTripHistorySource(drives = emptyList()), FakeTripHistoryCache()).build(drive.id)
        }
    }
}
