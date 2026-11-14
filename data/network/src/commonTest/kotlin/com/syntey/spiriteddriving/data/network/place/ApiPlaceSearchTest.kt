package com.syntey.spiriteddriving.data.network.place

import com.syntey.spiriteddriving.core.domain.error.OfflineException
import com.syntey.spiriteddriving.core.domain.geo.GeoPoint
import com.syntey.spiriteddriving.core.domain.place.Place
import com.syntey.spiriteddriving.core.domain.place.PlaceKind
import com.syntey.spiriteddriving.core.domain.place.PlaceSuggestion
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
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlinx.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ApiPlaceSearchTest {

    private var lastRequest: HttpRequestData? = null

    @Test
    fun `recorded response decodes and maps in its order`() = runTest {
        val search = search { respondJson(RECORDED_RESPONSE) }

        val suggestions = search.search(text = "karlst", near = null)

        assertEquals(2, suggestions.size)
        assertEquals(
            PlaceSuggestion(
                place = Place(
                    name = "Karlštejn",
                    area = "okres Beroun, Central Bohemian Region",
                    location = GeoPoint(latitude = 49.933809, longitude = 14.1832917),
                ),
                isCovered = true,
                kind = PlaceKind.Town,
            ),
            suggestions.first(),
        )
        assertEquals("Karlštejnská", suggestions.last().place.name)
        assertEquals(PlaceKind.Street, suggestions.last().kind)
    }

    @Test
    fun `text is sent as q`() = runTest {
        val search = search { respondJson(RECORDED_RESPONSE) }

        search.search(text = "karlst", near = null)

        assertEquals("karlst", lastRequest?.url?.parameters?.get("q"))
    }

    @Test
    fun `near is sent as lat and lon`() = runTest {
        val search = search { respondJson(RECORDED_RESPONSE) }

        search.search(text = "karlst", near = GeoPoint(latitude = 50.0755, longitude = 14.4378))

        assertEquals("50.0755", lastRequest?.url?.parameters?.get("lat"))
        assertEquals("14.4378", lastRequest?.url?.parameters?.get("lon"))
    }

    @Test
    fun `no near sends no lat or lon`() = runTest {
        val search = search { respondJson(RECORDED_RESPONSE) }

        search.search(text = "karlst", near = null)

        assertNull(lastRequest?.url?.parameters?.get("lat"))
        assertNull(lastRequest?.url?.parameters?.get("lon"))
    }

    @Test
    fun `request goes to the place search path under the API version`() = runTest {
        val search = search { respondJson(RECORDED_RESPONSE) }

        search.search(text = "karlst", near = null)

        assertTrue(lastRequest?.url?.encodedPath?.endsWith("/v1/places/search") == true)
    }

    @Test
    fun `server error is thrown as an HTTP error with its status`() = runTest {
        val search = search { respond(content = "", status = HttpStatusCode.ServiceUnavailable) }

        val error = assertFailsWith<NetworkError.HttpError> { search.search(text = "karlst", near = null) }

        assertEquals(503, error.statusCode)
    }

    @Test
    fun `no connection is thrown as offline`() = runTest {
        val search = search { throw IOException("Network is unreachable") }

        assertFailsWith<OfflineException> { search.search(text = "karlst", near = null) }
    }

    @Test
    fun `recorded reverse lookup maps to the named place at the point`() = runTest {
        val search = search { respondJson(RECORDED_REVERSE_RESPONSE) }

        val place = search.placeAt(GeoPoint(latitude = 49.9394, longitude = 14.1882))

        assertEquals(
            Place(
                name = "Karlštejn",
                area = "Budňany, Středočeský kraj",
                location = GeoPoint(latitude = 49.9394, longitude = 14.1882),
            ),
            place,
        )
    }

    @Test
    fun `point is sent as lat and lon to the reverse lookup path`() = runTest {
        val search = search { respondJson(RECORDED_REVERSE_RESPONSE) }

        search.placeAt(GeoPoint(latitude = 49.9394, longitude = 14.1882))

        assertEquals("49.9394", lastRequest?.url?.parameters?.get("lat"))
        assertEquals("14.1882", lastRequest?.url?.parameters?.get("lon"))
        assertTrue(lastRequest?.url?.encodedPath?.endsWith("places/reverse") == true)
    }

    @Test
    fun `not found reverse lookup is null`() = runTest {
        val search = search {
            respond(
                content = PROBLEM_NOT_FOUND,
                status = HttpStatusCode.NotFound,
                headers = headersOf(HttpHeaders.ContentType to listOf("application/problem+json")),
            )
        }

        assertNull(search.placeAt(GeoPoint(latitude = 45.0, longitude = -30.0)))
    }

    @Test
    fun `server error on reverse lookup is thrown`() = runTest {
        val search = search { respond(content = "", status = HttpStatusCode.InternalServerError) }

        val error = assertFailsWith<NetworkError.HttpError> { search.placeAt(GeoPoint(latitude = 49.9, longitude = 14.2)) }

        assertEquals(500, error.statusCode)
    }

    @Test
    fun `no connection on reverse lookup is thrown as offline`() = runTest {
        val search = search { throw IOException("Network is unreachable") }

        assertFailsWith<OfflineException> { search.placeAt(GeoPoint(latitude = 49.9, longitude = 14.2)) }
    }

    private fun search(handler: MockRequestHandleScope.(HttpRequestData) -> HttpResponseData): ApiPlaceSearch {
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
        return ApiPlaceSearch(api)
    }

    private fun MockRequestHandleScope.respondJson(body: String) = respond(
        content = body,
        status = HttpStatusCode.OK,
        headers = headersOf(HttpHeaders.ContentType to listOf("application/json")),
    )

    private companion object {
        const val RECORDED_REVERSE_RESPONSE =
            """{"location":{"lat":49.9394,"lon":14.1882},"name":"Karlštejn","area":"Budňany, Středočeský kraj","covered":true,"kind":"POI"}"""

        const val PROBLEM_NOT_FOUND =
            """{"title":"Not found","status":404,"code":"NOT_FOUND","detail":"No place near this point.","type":"about:blank"}"""

        // Recorded from the local backend, trimmed to two items
        const val RECORDED_RESPONSE =
            """{"items":[{"location":{"lat":49.933809,"lon":14.1832917},"name":"Karlštejn","area":"okres Beroun, Central Bohemian Region","covered":true,"kind":"TOWN"},{"location":{"lat":50.0552885,"lon":14.3641761},"name":"Karlštejnská","area":"Prague","covered":true,"kind":"STREET"}]}"""
    }
}
