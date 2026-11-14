package com.syntey.spiriteddriving.data.network.plan

import com.syntey.spiriteddriving.core.domain.geo.GeoPoint
import com.syntey.spiriteddriving.core.domain.geo.Length.Companion.kilometres
import com.syntey.spiriteddriving.core.domain.geo.Length.Companion.metres
import com.syntey.spiriteddriving.core.domain.geo.Length.Companion.miles
import com.syntey.spiriteddriving.core.domain.place.Place
import com.syntey.spiriteddriving.core.domain.route.Budget
import com.syntey.spiriteddriving.core.domain.route.HandOff
import com.syntey.spiriteddriving.core.domain.route.Route
import com.syntey.spiriteddriving.core.domain.route.RouteKind
import com.syntey.spiriteddriving.core.domain.route.RouteStats
import com.syntey.spiriteddriving.core.domain.route.TripPlan
import com.syntey.spiriteddriving.core.domain.route.TripRequest
import com.syntey.spiriteddriving.core.domain.settings.CountryCode
import com.syntey.spiriteddriving.data.network.dto.geo.GeoPointDto
import com.syntey.spiriteddriving.data.network.dto.plan.BudgetDto
import com.syntey.spiriteddriving.data.network.dto.plan.DistanceBudgetDto
import com.syntey.spiriteddriving.data.network.dto.plan.HandOffDto
import com.syntey.spiriteddriving.data.network.dto.plan.PlaceDto
import com.syntey.spiriteddriving.data.network.dto.plan.PlaceInputDto
import com.syntey.spiriteddriving.data.network.dto.plan.PlanDto
import com.syntey.spiriteddriving.data.network.dto.plan.RouteDto
import com.syntey.spiriteddriving.data.network.dto.plan.RouteStatsDto
import com.syntey.spiriteddriving.data.network.dto.plan.TimeBudgetDto
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PlanMappingTest {

    private val prague = GeoPoint(latitude = 50.0755, longitude = 14.4378)
    private val karlstejn = GeoPoint(latitude = 49.9394, longitude = 14.1882)

    @Test
    fun `a start without a name is sent without name or area`() {
        val request = request(start = Place(name = null, area = null, location = prague))

        val dto = request.toDto()

        assertEquals(PlaceInputDto(location = GeoPointDto(lat = 50.0755, lon = 14.4378)), dto.start)
    }

    @Test
    fun `a destination keeps its name and area`() {
        val dto = request(destination = Place(name = "Karlštejn Castle", area = "Karlštejn", location = karlstejn)).toDto()

        assertEquals(
            PlaceInputDto(
                location = GeoPointDto(lat = 49.9394, lon = 14.1882),
                name = "Karlštejn Castle",
                area = "Karlštejn",
            ),
            dto.destination,
        )
    }

    @Test
    fun `a time budget is sent as whole seconds`() {
        val dto = request(budget = Budget.Time(extra = 1800.seconds)).toDto()

        assertEquals(TimeBudgetDto(extraSeconds = 1800), dto.budget)
    }

    @Test
    fun `a distance budget is sent as metres`() {
        val dto = request(budget = Budget.Distance(extra = 30.kilometres)).toDto()

        assertEquals(DistanceBudgetDto(extraMeters = 30000), dto.budget)
    }

    @Test
    fun `a distance budget of 1 mile is sent as 1609 metres`() {
        val dto = request(budget = Budget.Distance(extra = 1.miles)).toDto()

        assertEquals(DistanceBudgetDto(extraMeters = 1609), dto.budget)
    }

    @Test
    fun `vignette countries are sent as their codes`() {
        val dto = request(vignettes = setOf(CountryCode.Czechia, CountryCode("SK"))).toDto()

        assertEquals(setOf("CZ", "SK"), dto.vignettes)
    }

    @Test
    fun `max hand-off waypoints is sent as it is`() {
        val dto = request(maxHandOffWaypoints = 9).toDto()

        assertEquals(9, dto.maxHandOffWaypoints)
    }

    @Test
    fun `a time budget maps both ways`() {
        assertEquals(TimeBudgetDto(extraSeconds = 1800), Budget.Time(extra = 1800.seconds).toDto())
        assertEquals(Budget.Time(extra = 1800.seconds), TimeBudgetDto(extraSeconds = 1800).toDomain())
    }

    @Test
    fun `a distance budget maps both ways`() {
        assertEquals(DistanceBudgetDto(extraMeters = 30000), Budget.Distance(extra = 30.kilometres).toDto())
        assertEquals(Budget.Distance(extra = 30.kilometres), DistanceBudgetDto(extraMeters = 30000).toDomain())
    }

    @Test
    fun `every route field maps to the route`() {
        val route = routeDto(kind = "FUN").toDomain()

        assertEquals(
            Route(
                id = "9c4e7a2d-1b3f-4c8e-8d5a-6f7b2e9c0a1d",
                kind = RouteKind.Fun,
                start = Place(name = "Vinohradská 12", area = "Prague 2", location = prague),
                destination = Place(name = "Karlštejn Castle", area = "Karlštejn, Central Bohemia", location = karlstejn),
                duration = 3900.seconds,
                distance = 58.kilometres,
                extraDuration = 1620.seconds,
                extraDistance = 25.kilometres,
                funLevel = 4,
                stats = RouteStats(curvyDistance = 21.kilometres, ascent = 640.metres),
                line = listOf(GeoPoint(38.5, -120.2), GeoPoint(40.7, -120.95), GeoPoint(43.252, -126.453)),
                handOff = HandOff(
                    waypoints = listOf(prague, GeoPoint(latitude = 50.001, longitude = 14.215)),
                    followedShare = 0.97,
                ),
                attribution = "© OpenStreetMap contributors",
            ),
            route,
        )
    }

    @Test
    fun `FASTEST maps to the fastest route`() {
        assertEquals(RouteKind.Fastest, routeDto(kind = "FASTEST").toDomain().kind)
    }

    @Test
    fun `an unknown kind maps to a fun route`() {
        assertEquals(RouteKind.Fun, routeDto(kind = "SOMETHING_NEW").toDomain().kind)
    }

    @Test
    fun `plan keeps its expiry and its routes`() {
        val plan = planDto(closestFunRouteBudget = null).toDomain()

        assertEquals(Instant.parse("2026-11-07T18:20:00Z"), plan.expiresAt)
        assertEquals(RouteKind.Fastest, plan.fastest.kind)
        assertEquals(listOf(RouteKind.Fun), plan.funRoutes.map { it.kind })
    }

    @Test
    fun `closest fun route budget maps when present`() {
        val plan = planDto(closestFunRouteBudget = TimeBudgetDto(extraSeconds = 2400)).toDomain()

        assertEquals(Budget.Time(extra = 2400.seconds), plan.closestFunRouteBudget)
    }

    @Test
    fun `closest fun route budget is null when absent`() {
        val plan = planDto(closestFunRouteBudget = null).toDomain()

        assertNull(plan.closestFunRouteBudget)
    }

    private fun request(
        start: Place = Place(name = null, area = null, location = prague),
        destination: Place = Place(name = "Karlštejn Castle", area = "Karlštejn", location = karlstejn),
        budget: Budget = Budget.Time(extra = 1800.seconds),
        vignettes: Set<CountryCode> = setOf(CountryCode.Czechia),
        maxHandOffWaypoints: Int = 9,
    ) = TripRequest(
        start = start,
        destination = destination,
        budget = budget,
        vignettes = vignettes,
        maxHandOffWaypoints = maxHandOffWaypoints,
    )

    private fun planDto(closestFunRouteBudget: BudgetDto?) = PlanDto(
        expiresAt = "2026-11-07T18:20:00Z",
        fastest = routeDto(kind = "FASTEST"),
        funRoutes = listOf(routeDto(kind = "FUN")),
        closestFunRouteBudget = closestFunRouteBudget,
    )

    private fun routeDto(kind: String) = RouteDto(
        id = "9c4e7a2d-1b3f-4c8e-8d5a-6f7b2e9c0a1d",
        kind = kind,
        start = PlaceDto(location = GeoPointDto(lat = 50.0755, lon = 14.4378), name = "Vinohradská 12", area = "Prague 2"),
        destination = PlaceDto(
            location = GeoPointDto(lat = 49.9394, lon = 14.1882),
            name = "Karlštejn Castle",
            area = "Karlštejn, Central Bohemia",
        ),
        durationSeconds = 3900,
        distanceMeters = 58000,
        extraDurationSeconds = 1620,
        extraDistanceMeters = 25000,
        funLevel = 4,
        stats = RouteStatsDto(curvyDistanceMeters = 21000, ascentMeters = 640),
        // Google's documented example line, which decodes to three points
        polyline = "_p~iF~ps|U_ulLnnqC_mqNvxq`@",
        handOff = HandOffDto(
            waypoints = listOf(GeoPointDto(lat = 50.0755, lon = 14.4378), GeoPointDto(lat = 50.001, lon = 14.215)),
            followedShare = 0.97,
        ),
        attribution = "© OpenStreetMap contributors",
    )
}
