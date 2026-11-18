package com.syntey.spiriteddriving.data.network.savedroute

import com.syntey.spiriteddriving.core.domain.error.OfflineException
import com.syntey.spiriteddriving.core.domain.savedroute.SavedRouteNotFoundException
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
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

class ApiSavedRoutesSourceTest {

    private var lastRequest: HttpRequestData? = null

    @Test
    fun `list decodes the saved routes`() = runTest {
        val source = source { respondJson(RECORDED_LIST, HttpStatusCode.OK) }

        val saved = source.list()

        assertEquals(listOf(SAVED_ID), saved.map { it.id })
        assertEquals(HttpMethod.Get, lastRequest?.method)
        assertTrue(lastRequest?.url?.encodedPath?.endsWith("/v1/me/saved-routes") == true, lastRequest?.url.toString())
    }

    @Test
    fun `save sends the route ID and name as the body`() = runTest {
        val source = source { respondJson(SUMMARY, HttpStatusCode.Created) }

        source.save(routeId = ROUTE_ID, name = "Karlštejn Castle")

        assertEquals(HttpMethod.Post, lastRequest?.method)
        val body = requestBody()
        assertTrue("\"routeId\":\"$ROUTE_ID\"" in body, body)
        assertTrue("\"name\":\"Karlštejn Castle\"" in body, body)
    }

    @Test
    fun `a created saved route is returned`() = runTest {
        val source = source { respondJson(SUMMARY, HttpStatusCode.Created) }

        val saved = source.save(routeId = ROUTE_ID, name = "Karlštejn Castle")

        assertEquals(SAVED_ID, saved.id)
    }

    @Test
    fun `an already saved route is returned`() = runTest {
        val source = source { respondJson(SUMMARY, HttpStatusCode.OK) }

        val saved = source.save(routeId = ROUTE_ID, name = "Karlštejn Castle")

        assertEquals(SAVED_ID, saved.id)
    }

    @Test
    fun `get sends the hand-off waypoint limit as a query`() = runTest {
        val source = source { respondJson(FULL_SAVED_ROUTE, HttpStatusCode.OK) }

        source.getRoute(id = SAVED_ID, maxHandOffWaypoints = 9)

        assertEquals(HttpMethod.Get, lastRequest?.method)
        assertTrue(lastRequest?.url?.encodedPath?.endsWith("/v1/me/saved-routes/$SAVED_ID") == true, lastRequest?.url.toString())
        assertEquals("9", lastRequest?.url?.parameters?.get("maxHandOffWaypoints"))
    }

    @Test
    fun `get maps the full route`() = runTest {
        val source = source { respondJson(FULL_SAVED_ROUTE, HttpStatusCode.OK) }

        val route = source.getRoute(id = SAVED_ID, maxHandOffWaypoints = 9)

        assertEquals(ROUTE_ID, route.id)
        assertEquals(3900.seconds, route.duration)
        assertEquals(3, route.line.size)
        assertEquals(2, route.handOff.waypoints.size)
    }

    @Test
    fun `get of a missing saved route is thrown as not found`() = runTest {
        val source = source { respondJson("", HttpStatusCode.NotFound) }

        val error = assertFailsWith<SavedRouteNotFoundException> { source.getRoute(id = SAVED_ID, maxHandOffWaypoints = 9) }

        val cause = assertIs<NetworkError.HttpError>(error.cause)
        assertEquals(404, cause.statusCode)
    }

    @Test
    fun `rename sends the name as the body with PATCH`() = runTest {
        val source = source { respondJson(SUMMARY, HttpStatusCode.OK) }

        val renamed = source.rename(id = SAVED_ID, name = "Castle day")

        assertEquals(HttpMethod.Patch, lastRequest?.method)
        assertTrue("\"name\":\"Castle day\"" in requestBody(), requestBody())
        assertEquals(SAVED_ID, renamed.id)
    }

    @Test
    fun `rename of a missing saved route is thrown as not found`() = runTest {
        val source = source { respondJson("", HttpStatusCode.NotFound) }

        assertFailsWith<SavedRouteNotFoundException> { source.rename(id = SAVED_ID, name = "Castle day") }
    }

    @Test
    fun `delete sends DELETE to the saved route's path`() = runTest {
        val source = source { respond(content = "", status = HttpStatusCode.NoContent) }

        source.delete(SAVED_ID)

        assertEquals(HttpMethod.Delete, lastRequest?.method)
        assertTrue(lastRequest?.url?.encodedPath?.endsWith("/v1/me/saved-routes/$SAVED_ID") == true, lastRequest?.url.toString())
    }

    @Test
    fun `a no content response is a deleted saved route`() = runTest {
        val source = source { respond(content = "", status = HttpStatusCode.NoContent) }

        source.delete(SAVED_ID)
    }

    @Test
    fun `no connection is thrown as offline`() = runTest {
        val source = source { throw IOException("Network is unreachable") }

        assertFailsWith<OfflineException> { source.list() }
    }

    private fun requestBody(): String = (lastRequest?.body as? TextContent)?.text ?: error("The request body is not text")

    private fun source(handler: MockRequestHandleScope.(HttpRequestData) -> HttpResponseData): ApiSavedRoutesSource {
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
        return ApiSavedRoutesSource(api)
    }

    private fun MockRequestHandleScope.respondJson(body: String, status: HttpStatusCode) = respond(
        content = body,
        status = status,
        headers = headersOf(HttpHeaders.ContentType to listOf("application/json")),
    )

    private companion object {
        const val SAVED_ID = "2b7d4e1a-8c3f-4a9e-b5d6-0f1e2d3c4b5a"
        const val ROUTE_ID = "9c4e7a2d-1b3f-4c8e-8d5a-6f7b2e9c0a1d"

        const val SUMMARY = """
            {"id":"$SAVED_ID","name":"Karlštejn Castle","savedAt":"2026-11-08T09:30:00Z","route":{"id":"$ROUTE_ID","kind":"FUN","start":{"location":{"lat":50.0755,"lon":14.4378},"name":"Vinohradská 12","area":"Prague 2"},"destination":{"location":{"lat":49.9394,"lon":14.1882},"name":"Karlštejn Castle","area":"Karlštejn, Central Bohemia"},"durationSeconds":3900,"distanceMeters":58000,"extraDurationSeconds":1620,"extraDistanceMeters":25000,"funLevel":4,"stats":{"curvyDistanceMeters":21000,"ascentMeters":640}}}
        """

        const val RECORDED_LIST = "{\"items\":[$SUMMARY]}"

        const val FULL_SAVED_ROUTE = """
            {"id":"$SAVED_ID","name":"Karlštejn Castle","savedAt":"2026-11-08T09:30:00Z","route":{"id":"$ROUTE_ID","kind":"FUN","start":{"location":{"lat":50.0755,"lon":14.4378},"name":"Vinohradská 12","area":"Prague 2"},"destination":{"location":{"lat":49.9394,"lon":14.1882},"name":"Karlštejn Castle","area":"Karlštejn, Central Bohemia"},"durationSeconds":3900,"distanceMeters":58000,"extraDurationSeconds":1620,"extraDistanceMeters":25000,"funLevel":4,"stats":{"curvyDistanceMeters":21000,"ascentMeters":640},"polyline":"_p~iF~ps|U_ulLnnqC_mqNvxq`@","handOff":{"waypoints":[{"lat":50.0755,"lon":14.4378},{"lat":50.001,"lon":14.215}],"followedShare":0.97},"attribution":"© OpenStreetMap contributors"}}
        """
    }
}
