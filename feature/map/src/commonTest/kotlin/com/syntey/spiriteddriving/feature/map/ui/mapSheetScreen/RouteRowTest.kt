package com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen

import com.syntey.spiriteddriving.core.domain.geo.Length
import com.syntey.spiriteddriving.core.domain.geo.Length.Companion.kilometres
import com.syntey.spiriteddriving.core.domain.route.Budget
import com.syntey.spiriteddriving.core.domain.route.FakeRoutePlanner
import com.syntey.spiriteddriving.core.domain.route.Route
import com.syntey.spiriteddriving.core.domain.route.RouteKind
import com.syntey.spiriteddriving.core.domain.route.TripPlan
import com.syntey.spiriteddriving.core.domain.settings.DistanceUnit
import com.syntey.spiriteddriving.core.resources.*
import com.syntey.spiriteddriving.feature.map.model.ui.RoutePlanningState
import com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen.MapSheetViewState
import dev.icerock.moko.resources.desc.ResourceFormatted
import dev.icerock.moko.resources.desc.StringDesc
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

class RouteRowTest {

    @Test
    fun `fun routes are numbered from 1 and the fastest route has no number`() {
        val rows = stateWith(threeFunRoutes()).routeRows

        assertEquals(listOf(1, 2, 3, null), rows.map { it.number })
    }

    @Test
    fun `the fastest route has no extra`() {
        val fastest = stateWith(threeFunRoutes()).routeRows.last()

        assertNull(fastest.extra)
    }

    @Test
    fun `a time budget shows the extra time with a plus`() {
        val row = stateWith(threeFunRoutes(), Budget.Time(45.minutes)).routeRows.first()

        assertEquals(extra(unitMinutes(27)), row.extra)
    }

    @Test
    fun `a distance budget shows the extra distance with a plus`() {
        val plan = plan(funRoutes = listOf(funRoute("fun-1", extraDistance = 26.kilometres)))
        val row = stateWith(plan, Budget.Distance(30.kilometres)).routeRows.first()

        assertEquals(extra(km("26")), row.extra)
    }

    @Test
    fun `the selected route is the one with the selected id`() {
        val state = stateWith(threeFunRoutes())
        val selected = state.copy(planning = state.planning?.copy(selectedRouteId = "fun-2")).routeRows

        assertEquals(listOf(false, true, false, false), selected.map { it.isSelected })
    }

    @Test
    fun `route 1 is selected when the plan arrives`() {
        assertEquals(listOf(true, false, false, false), stateWith(threeFunRoutes()).routeRows.map { it.isSelected })
    }

    @Test
    fun `there are no rows without planning`() {
        assertTrue(MapSheetViewState().routeRows.isEmpty())
    }

    @Test
    fun `the row's duration and distance follow the unit`() {
        val state = stateWith(threeFunRoutes()).copy(distanceUnit = DistanceUnit.Miles)
        val fastest = state.routeRows.last()

        assertEquals(unitMinutes(38), fastest.duration)
        assertEquals(mi("21"), fastest.distance)
    }

    @Test
    fun `the row shows the curvy distance and the ascent`() {
        val row = stateWith(threeFunRoutes()).routeRows.last()

        assertEquals(km("2.0"), row.curvyDistance)
        assertEquals(m("180"), row.ascent)
    }

    private fun stateWith(plan: TripPlan, budget: Budget = Budget.Time(45.minutes)): MapSheetViewState {
        val planning = RoutePlanningState(destination = plan.fastest.destination, budget = budget).withPlan(plan)
        return MapSheetViewState(planning = planning)
    }

    private fun threeFunRoutes(): TripPlan = plan(
        funRoutes = listOf(
            funRoute("fun-1", extraDuration = 27.minutes),
            funRoute("fun-2", extraDuration = 33.minutes),
            funRoute("fun-3", extraDuration = 41.minutes),
        ),
    )

    private fun plan(funRoutes: List<Route>): TripPlan = FakeRoutePlanner.Recorded.copy(
        funRoutes = funRoutes,
        fastest = FakeRoutePlanner.Recorded.fastest.copy(id = "fastest"),
    )

    private fun funRoute(
        id: String,
        extraDuration: Duration = 27.minutes,
        extraDistance: Length = 25.kilometres,
    ): Route =
        FakeRoutePlanner.Recorded.funRoutes.single().copy(
            id = id,
            kind = RouteKind.Fun,
            extraDuration = extraDuration,
            extraDistance = extraDistance,
        )

    private fun extra(desc: StringDesc) = StringDesc.ResourceFormatted(MR.strings.planning_extra, desc)

    private fun unitMinutes(count: Int) = StringDesc.ResourceFormatted(MR.strings.unit_minutes, count)

    private fun km(number: String) = StringDesc.ResourceFormatted(MR.strings.unit_kilometres, number)

    private fun mi(number: String) = StringDesc.ResourceFormatted(MR.strings.unit_miles, number)

    private fun m(number: String) = StringDesc.ResourceFormatted(MR.strings.unit_metres, number)
}
