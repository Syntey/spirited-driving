package com.syntey.spiriteddriving.data.network.place

import com.syntey.spiriteddriving.core.domain.geo.GeoPoint
import com.syntey.spiriteddriving.core.domain.place.Place
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
            ),
            suggestions.first(),
        )
        assertEquals("Karlštejnská", suggestions.last().place.name)
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
        // Recorded from the local backend, trimmed to two items
        const val RECORDED_RESPONSE =
            """{"items":[{"location":{"lat":49.933809,"lon":14.1832917},"name":"Karlštejn","area":"okres Beroun, Central Bohemian Region","covered":true},{"location":{"lat":50.0552885,"lon":14.3641761},"name":"Karlštejnská","area":"Prague","covered":true}]}"""
    }
}
