package com.syntey.spiriteddriving.feature.map.model.ui

import com.syntey.spiriteddriving.core.domain.route.Budget
import com.syntey.spiriteddriving.core.domain.route.FakeRoutePlanner
import com.syntey.spiriteddriving.core.domain.route.TripPlan
import com.syntey.spiriteddriving.feature.map.model.ui.RoutePlanningState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.minutes

class RoutePlanningStateTest {

    @Test
    fun `listed routes are the fun routes in plan order, then the fastest`() {
        val plan = recorded.copy(funRoutes = listOf(funRoute("fun-1"), funRoute("fun-2"), funRoute("fun-3")), fastest = fastest)

        val listed = planning().withPlan(plan).listedRoutes

        assertEquals(listOf("fun-1", "fun-2", "fun-3", "fastest"), listed.map { it.id })
    }

    @Test
    fun `no route is listed before a plan`() {
        assertTrue(planning().listedRoutes.isEmpty())
    }

    @Test
    fun `a plan selects its first fun route`() {
        val plan = recorded.copy(funRoutes = listOf(funRoute("fun-1"), funRoute("fun-2")), fastest = fastest)

        val state = planning().withPlan(plan)

        assertEquals("fun-1", state.selectedRouteId)
    }

    @Test
    fun `a plan without a fun route selects the fastest route`() {
        val plan = recorded.copy(funRoutes = emptyList(), fastest = fastest)

        val state = planning().withPlan(plan)

        assertEquals("fastest", state.selectedRouteId)
    }

    @Test
    fun `the selected route is the one with the selected id`() {
        val plan = recorded.copy(funRoutes = listOf(funRoute("fun-1")), fastest = fastest)

        val state = planning().withPlan(plan).copy(selectedRouteId = "fastest")

        assertEquals("fastest", state.selectedRoute?.id)
    }

    @Test
    fun `no route is selected before a plan`() {
        assertNull(planning().selectedRoute)
    }

    private val recorded: TripPlan = FakeRoutePlanner.Recorded

    private val fastest = recorded.fastest.copy(id = "fastest")

    private fun funRoute(id: String) = recorded.funRoutes.single().copy(id = id)

    private fun planning() = RoutePlanningState(
        destination = recorded.fastest.destination,
        budget = Budget.Time(30.minutes),
    )
}
