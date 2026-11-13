package com.syntey.spiriteddriving.feature.map.ui.tripPlanScreen

import com.syntey.spiriteddriving.core.domain.geo.GeoBounds
import com.syntey.spiriteddriving.core.domain.geo.GeoPoint
import com.syntey.spiriteddriving.core.domain.geo.Length
import com.syntey.spiriteddriving.core.domain.geo.Length.Companion.kilometres
import com.syntey.spiriteddriving.core.domain.place.Place
import com.syntey.spiriteddriving.core.domain.route.Budget
import com.syntey.spiriteddriving.core.domain.route.FakeRoutePlanner
import com.syntey.spiriteddriving.core.domain.route.Route
import com.syntey.spiriteddriving.core.domain.route.RouteKind
import com.syntey.spiriteddriving.core.domain.route.TripPlan
import com.syntey.spiriteddriving.core.resources.*
import com.syntey.spiriteddriving.feature.map.model.ui.RouteMapState
import com.syntey.spiriteddriving.feature.map.model.ui.RoutePlanningState
import com.syntey.spiriteddriving.feature.map.ui.tripPlanScreen.TripPlanViewState
import dev.icerock.moko.resources.desc.ResourceFormatted
import dev.icerock.moko.resources.desc.StringDesc
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import com.syntey.spiriteddriving.feature.map.tools.Constants

class RouteLineTest {

    @Test
    fun `a fun route's label is its number and its time extra`() {
        val line = stateWith(threeFunRoutes(), Budget.Time(45.minutes)).routeLines.single { it.id == "fun-1" }

        assertEquals(StringDesc.ResourceFormatted(MR.strings.planning_route_label, 1, extra(unitMinutes(27))), line.label)
    }

    @Test
    fun `a fun route's label on a distance budget is its number and its distance extra`() {
        val plan = plan(funRoutes = listOf(funRoute("fun-1", extraDistance = 26.kilometres)))
        val line = stateWith(plan, Budget.Distance(30.kilometres)).routeLines.single { it.id == "fun-1" }

        assertEquals(StringDesc.ResourceFormatted(MR.strings.planning_route_label, 1, extra(km("26"))), line.label)
    }

    @Test
    fun `the fastest route's label is its total time`() {
        val fastest = stateWith(threeFunRoutes()).routeLines.single { it.id == "fastest" }

        assertNull(fastest.number)
        assertEquals(StringDesc.ResourceFormatted(MR.strings.planning_fastest_label, unitMinutes(38)), fastest.label)
    }

    @Test
    fun `the selected line is last and the others keep the list order`() {
        val state = stateWith(threeFunRoutes())
        val selected = state.copy(planning = state.planning?.copy(selectedRouteId = "fun-2")).routeLines

        assertEquals(listOf("fun-1", "fun-3", "fastest", "fun-2"), selected.map { it.id })
        assertTrue(selected.last().isSelected)
    }

    @Test
    fun `a route without points has no line`() {
        val plan = plan(
            funRoutes = listOf(
                funRoute("fun-1"),
                funRoute("fun-2").copy(line = emptyList()),
                funRoute("fun-3"),
            ),
        )

        // fun-1 is selected by default, so it's last
        assertEquals(listOf("fun-3", "fastest", "fun-1"), stateWith(plan).routeLines.map { it.id })
    }

    @Test
    fun `there are no lines without planning`() {
        assertTrue(TripPlanViewState().routeLines.isEmpty())
    }

    @Test
    fun `the route bounds cover every point of every planned line`() {
        val plan = threeFunRoutes()
        val points = plan.funRoutes.flatMap { it.line } + plan.fastest.line

        assertEquals(boundsOf(points), stateWith(plan).routeBounds)
    }

    @Test
    fun `while planning the route bounds are the user's location and the destination`() {
        val origin = GeoPoint(latitude = 50.0, longitude = 14.0)
        val destination = GeoPoint(latitude = 49.5, longitude = 14.5)
        val state = TripPlanViewState(
            map = RouteMapState(userLocation = origin),
            planning = RoutePlanningState(destination = place(destination), budget = Budget.Time(45.minutes)),
        )

        assertEquals(boundsOf(listOf(origin, destination)), state.routeBounds)
    }

    @Test
    fun `a single point's route bounds are at least the minimum span`() {
        val destination = GeoPoint(latitude = 50.0, longitude = 14.0)
        val state = TripPlanViewState(planning = RoutePlanningState(destination = place(destination), budget = Budget.Time(45.minutes)))

        val bounds = assertNotNull(state.routeBounds)
        assertTrue(bounds.northEast.latitude - bounds.southWest.latitude >= Constants.Map.MIN_ROUTE_LAT_SPAN - TOLERANCE)
        assertTrue(bounds.northEast.longitude - bounds.southWest.longitude >= Constants.Map.MIN_ROUTE_LON_SPAN - TOLERANCE)
        assertEquals(destination.latitude, (bounds.northEast.latitude + bounds.southWest.latitude) / 2, TOLERANCE)
        assertEquals(destination.longitude, (bounds.northEast.longitude + bounds.southWest.longitude) / 2, TOLERANCE)
    }

    @Test
    fun `a plan whose lines have no points falls back to the user's location and the destination`() {
        val origin = GeoPoint(latitude = 50.0, longitude = 14.0)
        val destination = GeoPoint(latitude = 49.5, longitude = 14.5)
        val emptyPlan = FakeRoutePlanner.Recorded.let { plan ->
            plan.copy(
                funRoutes = plan.funRoutes.map { it.copy(line = emptyList()) },
                fastest = plan.fastest.copy(line = emptyList()),
            )
        }
        val state = TripPlanViewState(
            map = RouteMapState(userLocation = origin),
            planning = RoutePlanningState(destination = place(destination), budget = Budget.Time(45.minutes)).withPlan(emptyPlan),
        )

        assertEquals(boundsOf(listOf(origin, destination)), state.routeBounds)
    }

    @Test
    fun `there are no route bounds without planning`() {
        assertNull(TripPlanViewState().routeBounds)
    }

    @Test
    fun `the map buttons show without planning`() {
        assertTrue(TripPlanViewState().showsMapButtons)
    }

    @Test
    fun `the map buttons hide while planning`() {
        val state = stateWith(threeFunRoutes())

        assertFalse(state.showsMapButtons)
    }

    private fun boundsOf(points: List<GeoPoint>): GeoBounds = GeoBounds(
        southWest = GeoPoint(latitude = points.minOf { it.latitude }, longitude = points.minOf { it.longitude }),
        northEast = GeoPoint(latitude = points.maxOf { it.latitude }, longitude = points.maxOf { it.longitude }),
    )

    private fun place(location: GeoPoint) = Place(name = null, area = null, location = location)

    private companion object {
        const val TOLERANCE = 1e-9
    }

    private fun stateWith(plan: TripPlan, budget: Budget = Budget.Time(45.minutes)): TripPlanViewState {
        val planning = RoutePlanningState(destination = plan.fastest.destination, budget = budget).withPlan(plan)
        return TripPlanViewState(planning = planning)
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
}
