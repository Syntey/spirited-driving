package com.syntey.spiriteddriving.data.network.drive

import com.syntey.spiriteddriving.core.domain.drive.PastDriveNotFoundException
import com.syntey.spiriteddriving.core.domain.error.OfflineException
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

class ApiTripHistorySourceTest {

    private var lastRequest: HttpRequestData? = null

    @Test
    fun `list sends GET to the drives path with the limit`() = runTest {
        val source = source { respondJson(RECORDED_PAGE, HttpStatusCode.OK) }

        source.list(cursor = null, limit = 20)

        assertEquals(HttpMethod.Get, lastRequest?.method)
        assertTrue(lastRequest?.url?.encodedPath?.endsWith("/v1/me/drives") == true, lastRequest?.url.toString())
        assertEquals("20", lastRequest?.url?.parameters?.get("limit"))
    }

    @Test
    fun `list sends no cursor on the first page`() = runTest {
        val source = source { respondJson(RECORDED_PAGE, HttpStatusCode.OK) }

        source.list(cursor = null, limit = 20)

        assertNull(lastRequest?.url?.parameters?.get("cursor"))
    }

    @Test
    fun `list sends the cursor of the page after`() = runTest {
        val source = source { respondJson(RECORDED_PAGE, HttpStatusCode.OK) }

        source.list(cursor = "opaque-cursor", limit = 20)

        assertEquals("opaque-cursor", lastRequest?.url?.parameters?.get("cursor"))
    }

    @Test
    fun `list decodes the drives and the next cursor`() = runTest {
        val source = source { respondJson(RECORDED_PAGE, HttpStatusCode.OK) }

        val page = source.list(cursor = null, limit = 20)

        assertEquals(listOf(DRIVE_ID), page.drives.map { it.id })
        assertEquals("opaque-cursor", page.nextCursor)
    }

    @Test
    fun `get sends the hand-off waypoint limit as a query`() = runTest {
        val source = source { respondJson(FULL_DRIVE, HttpStatusCode.OK) }

        source.getRoute(driveId = DRIVE_ID, maxHandOffWaypoints = 9)

        assertEquals(HttpMethod.Get, lastRequest?.method)
        assertTrue(lastRequest?.url?.encodedPath?.endsWith("/v1/drives/$DRIVE_ID") == true, lastRequest?.url.toString())
        assertEquals("9", lastRequest?.url?.parameters?.get("maxHandOffWaypoints"))
    }

    @Test
    fun `get maps the drive's full route`() = runTest {
        val source = source { respondJson(FULL_DRIVE, HttpStatusCode.OK) }

        val route = source.getRoute(driveId = DRIVE_ID, maxHandOffWaypoints = 9)

        assertEquals(ROUTE_ID, route.id)
        assertEquals(3900.seconds, route.duration)
        assertEquals(3, route.line.size)
        assertEquals(2, route.handOff.waypoints.size)
    }

    @Test
    fun `get of a missing drive is thrown as not found`() = runTest {
        val source = source { respondJson("", HttpStatusCode.NotFound) }

        val error = assertFailsWith<PastDriveNotFoundException> { source.getRoute(driveId = DRIVE_ID, maxHandOffWaypoints = 9) }

        val cause = assertIs<NetworkError.HttpError>(error.cause)
        assertEquals(404, cause.statusCode)
    }

    @Test
    fun `get of another user's drive is thrown as not found`() = runTest {
        val source = source { respondJson("", HttpStatusCode.Forbidden) }

        assertFailsWith<PastDriveNotFoundException> { source.getRoute(driveId = DRIVE_ID, maxHandOffWaypoints = 9) }
    }

    @Test
    fun `clear sends DELETE to the drives path`() = runTest {
        val source = source { respond(content = "", status = HttpStatusCode.NoContent) }

        source.clear()

        assertEquals(HttpMethod.Delete, lastRequest?.method)
        assertTrue(lastRequest?.url?.encodedPath?.endsWith("/v1/me/drives") == true, lastRequest?.url.toString())
    }

    @Test
    fun `no connection is thrown as offline`() = runTest {
        val source = source { throw IOException("Network is unreachable") }

        assertFailsWith<OfflineException> { source.list(cursor = null, limit = 20) }
    }

    private fun source(handler: MockRequestHandleScope.(HttpRequestData) -> HttpResponseData): ApiTripHistorySource {
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
        return ApiTripHistorySource(api)
    }

    private fun MockRequestHandleScope.respondJson(body: String, status: HttpStatusCode) = respond(
        content = body,
        status = status,
        headers = headersOf(HttpHeaders.ContentType to listOf("application/json")),
    )

    private companion object {
        const val DRIVE_ID = "2b7d4e1a-8c3f-4a9e-b5d6-0f1e2d3c4b5a"
        const val ROUTE_ID = "9c4e7a2d-1b3f-4c8e-8d5a-6f7b2e9c0a1d"

        // Shaped like the Route and RouteSummary schemas, with the fields the app ignores left in
        const val ROUTE_SUMMARY = """
            {
              "id": "$ROUTE_ID", "kind": "FUN", "funLevel": 4,
              "start": {"location": {"lat": 50.0755, "lon": 14.4378}, "name": "Vinohradská 12", "area": "Prague 2"},
              "destination": {"location": {"lat": 49.9394, "lon": 14.1882}, "name": "Karlštejn Castle", "area": "Karlštejn, Central Bohemia"},
              "durationSeconds": 3900, "distanceMeters": 58000, "extraDurationSeconds": 1620, "extraDistanceMeters": 25000,
              "stats": {"curvyDistanceMeters": 21000, "ascentMeters": 640}, "vignetteCountries": ["CZ"]
            }
        """

        const val RECORDED_PAGE = """
            {
              "items": [
                {
                  "id": "$DRIVE_ID",
                  "startedAt": "2026-11-08T09:30:00Z",
                  "route": $ROUTE_SUMMARY,
                  "rating": {"thumb": "UP", "reasons": ["CURVY_ROADS"], "ratedAt": "2026-11-08T10:05:00Z"}
                }
              ],
              "nextCursor": "opaque-cursor"
            }
        """

        const val FULL_DRIVE = """
            {
              "id": "$DRIVE_ID",
              "startedAt": "2026-11-08T09:30:00Z",
              "route": {
                "id": "$ROUTE_ID", "kind": "FUN", "funLevel": 4,
                "start": {"location": {"lat": 50.0755, "lon": 14.4378}, "name": "Vinohradská 12", "area": "Prague 2"},
                "destination": {"location": {"lat": 49.9394, "lon": 14.1882}, "name": "Karlštejn Castle", "area": "Karlštejn, Central Bohemia"},
                "durationSeconds": 3900, "distanceMeters": 58000, "extraDurationSeconds": 1620, "extraDistanceMeters": 25000,
                "stats": {"curvyDistanceMeters": 21000, "ascentMeters": 640}, "vignetteCountries": ["CZ"],
                "polyline": "_p~iF~ps|U_ulLnnqC_mqNvxq`@",
                "handOff": {
                  "waypoints": [{"lat": 50.0755, "lon": 14.4378}, {"lat": 50.001, "lon": 14.215}],
                  "followedShare": 0.97
                },
                "attribution": "© OpenStreetMap contributors"
              }
            }
        """
    }
}
