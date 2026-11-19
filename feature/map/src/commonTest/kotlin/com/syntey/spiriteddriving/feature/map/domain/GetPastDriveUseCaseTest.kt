package com.syntey.spiriteddriving.feature.map.domain

import com.syntey.spiriteddriving.core.domain.drive.FakeTripHistoryCache
import com.syntey.spiriteddriving.core.domain.drive.PastDrive
import com.syntey.spiriteddriving.core.domain.drive.PastDrivePage
import com.syntey.spiriteddriving.core.domain.route.FakeRoutePlanner
import com.syntey.spiriteddriving.core.domain.route.RouteKind
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Instant

class GetPastDriveUseCaseTest {

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
    fun `a kept drive is found by its ID`() = runTest {
        val cache = FakeTripHistoryCache(PastDrivePage(listOf(drive), nextCursor = null))

        assertEquals(drive, GetPastDriveUseCase(cache).build(drive.id))
    }

    @Test
    fun `an ID that isn't kept finds no drive`() = runTest {
        val cache = FakeTripHistoryCache(PastDrivePage(listOf(drive), nextCursor = null))

        assertNull(GetPastDriveUseCase(cache).build("other"))
    }

    @Test
    fun `without a kept list no drive is found`() = runTest {
        assertNull(GetPastDriveUseCase(FakeTripHistoryCache()).build(drive.id))
    }
}
