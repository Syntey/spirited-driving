package com.syntey.spiriteddriving.feature.map.domain

import com.syntey.spiriteddriving.core.domain.geo.GeoPoint
import com.syntey.spiriteddriving.core.domain.place.Place
import com.syntey.spiriteddriving.core.domain.route.Budget
import com.syntey.spiriteddriving.core.domain.route.FakeRoutePlanner
import com.syntey.spiriteddriving.core.domain.settings.CountryCode
import com.syntey.spiriteddriving.core.domain.settings.FakeVignetteSettings
import com.syntey.spiriteddriving.core.domain.settings.VignetteSettings
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.minutes

class PlanTripUseCaseTest {
    private val start = Place(name = null, area = null, location = GeoPoint(latitude = 50.0755, longitude = 14.4378))
    private val destination = Place(name = "Karlštejn Castle", area = null, location = GeoPoint(latitude = 49.9394, longitude = 14.1882))
    private val budget = Budget.Time(30.minutes)

    @Test
    fun `the trip is planned from the start to the destination within the budget`() = runTest {
        val planner = FakeRoutePlanner()

        planUseCase(planner, vignetteSettings = FakeVignetteSettings())

        val request = planner.requests.single()
        assertEquals(start, request.start)
        assertEquals(destination, request.destination)
        assertEquals(budget, request.budget)
    }

    @Test
    fun `every country with a vignette is sent with the request`() = runTest {
        val planner = FakeRoutePlanner()

        planUseCase(planner, vignetteSettings = FakeVignetteSettings(mapOf(CountryCode.Czechia to true, CountryCode("SK") to true)))

        assertEquals(setOf(CountryCode.Czechia, CountryCode("SK")), planner.requests.single().vignettes)
    }

    @Test
    fun `a country without a vignette is not sent with the request`() = runTest {
        val planner = FakeRoutePlanner()

        planUseCase(planner, vignetteSettings = FakeVignetteSettings(mapOf(CountryCode.Czechia to true, CountryCode("SK") to false)))

        assertEquals(setOf(CountryCode.Czechia), planner.requests.single().vignettes)
    }

    @Test
    fun `an unanswered vignette question sends no vignette`() = runTest {
        val planner = FakeRoutePlanner()

        planUseCase(planner, vignetteSettings = FakeVignetteSettings())

        assertEquals(emptySet(), planner.requests.single().vignettes)
    }

    @Test
    fun `the request allows nine hand-off waypoints`() = runTest {
        val planner = FakeRoutePlanner()

        planUseCase(planner, vignetteSettings = FakeVignetteSettings())

        assertEquals(9, planner.requests.single().maxHandOffWaypoints)
    }

    @Test
    fun `the planner's plan is returned`() = runTest {
        val result = planUseCase(FakeRoutePlanner(), vignetteSettings = FakeVignetteSettings())

        assertEquals(FakeRoutePlanner.Recorded, result)
    }

    private suspend fun planUseCase(planner: FakeRoutePlanner, vignetteSettings: VignetteSettings) =
        PlanTripUseCase(planner, vignetteSettings).build(PlanTripUseCase.Args(start = start, destination = destination, budget = budget))
}
