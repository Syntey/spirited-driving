package com.syntey.spiriteddriving.data.network.plan

import com.syntey.spiriteddriving.core.domain.error.OfflineException
import com.syntey.spiriteddriving.core.domain.geo.GeoPoint
import com.syntey.spiriteddriving.core.domain.geo.Length.Companion.metres
import com.syntey.spiriteddriving.core.domain.place.Place
import com.syntey.spiriteddriving.core.domain.route.Budget
import com.syntey.spiriteddriving.core.domain.route.OutsideCoverageException
import com.syntey.spiriteddriving.core.domain.route.RouteKind
import com.syntey.spiriteddriving.core.domain.route.TripRequest
import com.syntey.spiriteddriving.core.domain.settings.CountryCode
import com.syntey.spiriteddriving.data.network.api.createSpiritedDrivingApi
import com.syntey.spiriteddriving.data.network.injection.NetworkModule
import com.syntey.spiriteddriving.data.network.plugin.ContentNegotiationPlugin
import com.syntey.spiriteddriving.data.network.result.NetworkError
import com.syntey.spiriteddriving.data.network.result.NetworkErrorParser
import com.syntey.spiriteddriving.data.network.result.NetworkResultConverterFactory
import de.jensklingenberg.ktorfit.Ktorfit
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlinx.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

class ApiRoutePlannerTest {

    private var lastRequest: HttpRequestData? = null

    @Test
    fun `recorded response decodes and maps to the fastest and one fun route`() = runTest {
        val planner = planner { respondJson(RECORDED_RESPONSE) }

        val plan = planner.plan(request())

        assertEquals(Instant.parse("2026-11-12T19:39:09Z"), plan.expiresAt)
        assertEquals("1c857d1b-de8f-440d-9b7b-1db3e6ea1248", plan.fastest.id)
        assertEquals(RouteKind.Fastest, plan.fastest.kind)
        assertEquals(3505.seconds, plan.fastest.duration)
        assertEquals(1, plan.fastest.funLevel)
        assertEquals(1, plan.funRoutes.size)
        val funRoute = plan.funRoutes.single()
        assertEquals("9ffcec7a-d1a4-47cc-a760-e7f5d0cf5209", funRoute.id)
        assertEquals(RouteKind.Fun, funRoute.kind)
        assertEquals(943.seconds, funRoute.extraDuration)
        assertEquals(2, funRoute.funLevel)
        assertEquals(2157.metres, funRoute.stats.curvyDistance)
        assertEquals(805.metres, funRoute.stats.ascent)
        assertEquals(3, funRoute.handOff.waypoints.size)
        assertEquals(0.97, funRoute.handOff.followedShare)
        assertNull(plan.closestFunRouteBudget)
    }

    @Test
    fun `recorded response keeps the start name and a missing destination area as null`() = runTest {
        val planner = planner { respondJson(RECORDED_RESPONSE) }

        val plan = planner.plan(request())

        assertEquals("50.07550, 14.43780", plan.fastest.start.name)
        assertEquals(GeoPoint(latitude = 50.0755, longitude = 14.4378), plan.fastest.start.location)
        assertEquals(
            Place(name = "Jizerské hory", area = null, location = GeoPoint(latitude = 50.6, longitude = 15.0)),
            plan.fastest.destination,
        )
    }

    @Test
    fun `plan is posted to the plans path under the API version`() = runTest {
        val planner = planner { respondJson(RECORDED_RESPONSE) }

        planner.plan(request())

        assertEquals(HttpMethod.Post, lastRequest?.method)
        assertTrue(lastRequest?.url?.encodedPath?.endsWith("/v1/plans") == true)
    }

    @Test
    fun `request body carries the budget vignettes and waypoint limit`() = runTest {
        val planner = planner { respondJson(RECORDED_RESPONSE) }

        planner.plan(request())

        val body = requestBody()
        assertTrue("\"type\":\"TIME\"" in body, body)
        assertTrue("\"extraSeconds\":1800" in body, body)
        assertTrue("\"vignettes\":[\"CZ\"]" in body, body)
        assertTrue("\"maxHandOffWaypoints\":9" in body, body)
    }

    @Test
    fun `start without a name is sent without a name key`() = runTest {
        val planner = planner { respondJson(RECORDED_RESPONSE) }

        planner.plan(request())

        val body = requestBody()
        assertTrue("\"start\":{\"location\":{\"lat\":50.0755,\"lon\":14.4378}}" in body, body)
    }

    @Test
    fun `server error is thrown as an HTTP error with its status`() = runTest {
        val planner = planner { respond(content = "", status = HttpStatusCode.ServiceUnavailable) }

        val error = assertFailsWith<NetworkError.HttpError> {
            planner.plan(request())
        }

        assertEquals(503, error.statusCode)
    }

    @Test
    fun `outside the covered area is thrown as outside coverage with the HTTP error as its cause`() = runTest {
        val planner = planner { respondProblem(RECORDED_OUTSIDE_COVERAGE, HttpStatusCode.UnprocessableEntity) }

        val error = assertFailsWith<OutsideCoverageException> { planner.plan(request()) }

        val cause = assertIs<NetworkError.HttpError>(error.cause)
        assertEquals(422, cause.statusCode)
        assertEquals("OUTSIDE_COVERAGE", cause.code)
    }

    @Test
    fun `no route is thrown as the HTTP error with its problem code`() = runTest {
        val planner = planner {
            respondProblem(
                content = PROBLEM_NO_ROUTE,
                status = HttpStatusCode.UnprocessableEntity,
            )
        }

        val error = assertFailsWith<NetworkError.HttpError> { planner.plan(request()) }

        assertEquals("NO_ROUTE", error.code)
    }

    @Test
    fun `server error with an empty body is thrown as the HTTP error without a code`() = runTest {
        val planner = planner { respond(content = "", status = HttpStatusCode.InternalServerError) }

        val error = assertFailsWith<NetworkError.HttpError> { planner.plan(request()) }

        assertEquals(500, error.statusCode)
        assertNull(error.code)
    }

    @Test
    fun `no connection is thrown as offline`() = runTest {
        val planner = planner { throw IOException("Network is unreachable") }

        assertFailsWith<OfflineException> { planner.plan(request()) }
    }

    private fun request() = TripRequest(
        start = Place(name = null, area = null, location = GeoPoint(latitude = 50.0755, longitude = 14.4378)),
        destination = Place(
            name = "Karlštejn Castle",
            area = "Karlštejn, Central Bohemia",
            location = GeoPoint(latitude = 49.9394, longitude = 14.1882),
        ),
        budget = Budget.Time(extra = 1800.seconds),
        vignettes = setOf(CountryCode.Czechia),
        maxHandOffWaypoints = 9,
    )

    private fun requestBody(): String = (lastRequest?.body as? TextContent)?.text ?: error("The request body is not text")

    private fun planner(handler: MockRequestHandleScope.(HttpRequestData) -> HttpResponseData): ApiRoutePlanner {
        val client = HttpClient(
            MockEngine { request ->
                lastRequest = request
                handler(request)
            },
        ) {
            ContentNegotiationPlugin(NetworkModule().restApiJson()).install(this)
        }
        val api = Ktorfit.Builder()
            .baseUrl("http://localhost/v1/")
            .httpClient(client)
            .converterFactories(NetworkResultConverterFactory(NetworkErrorParser()))
            .build()
            .createSpiritedDrivingApi()
        return ApiRoutePlanner(api)
    }

    private fun MockRequestHandleScope.respondJson(body: String) = respond(
        content = body,
        status = HttpStatusCode.OK,
        headers = headersOf(HttpHeaders.ContentType to listOf("application/json")),
    )

    private fun MockRequestHandleScope.respondProblem(content: String, status: HttpStatusCode) = respond(
        content = content,
        status = status,
        headers = headersOf(HttpHeaders.ContentType to listOf("application/problem+json")),
    )

    private companion object {
        // Recorded from the local backend
        const val RECORDED_OUTSIDE_COVERAGE =
            """{"title":"Outside the covered area","status":422,"code":"OUTSIDE_COVERAGE","outside":["DESTINATION"],"type":"about:blank"}"""

        const val PROBLEM_NO_ROUTE = """{"type":"about:blank","title":"No route","status":422,"code":"NO_ROUTE"}"""

        // Recorded from the local backend. The polylines and the hand-off waypoints are trimmed, so the lines are
        // shorter than the real routes; the ids, durations and stats are as the backend sent them.
        const val RECORDED_RESPONSE =
            """{"expiresAt":"2026-11-12T19:39:09Z","fastest":{"id":"1c857d1b-de8f-440d-9b7b-1db3e6ea1248","kind":"FASTEST","start":{"location":{"lat":50.0755,"lon":14.4378},"name":"50.07550, 14.43780"},"destination":{"location":{"lat":50.6,"lon":15.0},"name":"Jizerské hory"},"durationSeconds":3505,"distanceMeters":85115,"extraDurationSeconds":0,"extraDistanceMeters":0,"funLevel":1,"stats":{"curvyDistanceMeters":1726,"ascentMeters":789},"polyline":"{jspHg{bwAbuGnjG","handOff":{"waypoints":[{"lat":50.0754989456108,"lon":14.437746861598312}],"followedShare":1.0},"attribution":"© OpenStreetMap contributors"},"funRoutes":[{"id":"9ffcec7a-d1a4-47cc-a760-e7f5d0cf5209","kind":"FUN","start":{"location":{"lat":50.0755,"lon":14.4378},"name":"50.07550, 14.43780"},"destination":{"location":{"lat":50.6,"lon":15.0},"name":"Jizerské hory"},"durationSeconds":4448,"distanceMeters":89010,"extraDurationSeconds":943,"extraDistanceMeters":3895,"funLevel":2,"stats":{"curvyDistanceMeters":2157,"ascentMeters":805},"polyline":"{jspHg{bwAjpEfbR","handOff":{"waypoints":[{"lat":50.0754989456108,"lon":14.437746861598312},{"lat":50.11052119630213,"lon":14.509449548317251},{"lat":50.21401593158886,"lon":14.735474933223559}],"followedShare":0.97},"attribution":"© OpenStreetMap contributors"}]}"""
    }
}
