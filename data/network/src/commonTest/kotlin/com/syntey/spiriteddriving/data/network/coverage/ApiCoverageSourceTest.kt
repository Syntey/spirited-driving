package com.syntey.spiriteddriving.data.network.coverage

import com.syntey.spiriteddriving.core.domain.geo.GeoPoint
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
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ApiCoverageSourceTest {

    private var lastRequest: HttpRequestData? = null

    @Test
    fun `recorded response decodes and maps with its ETag as the version`() = runTest {
        val source = source { respondJson(RECORDED_RESPONSE, etag = RECORDED_ETAG) }

        val versioned = checkNotNull(source.fetch(knownVersion = null))

        assertEquals(RECORDED_ETAG, versioned.version)
        assertEquals(
            listOf(CountryCode("CZ"), CountryCode("SK"), CountryCode("AT"), CountryCode("HU"), CountryCode("SI")),
            versioned.coverage.vignetteCountries,
        )
        assertEquals(1, versioned.coverage.area.size)
        assertEquals(GeoPoint(latitude = 50.25032, longitude = 12.09287), versioned.coverage.area.single().outline.first())
        assertEquals(4, versioned.coverage.area.single().outline.size)
    }

    @Test
    fun `known version is sent as If-None-Match`() = runTest {
        val source = source { respondJson(RECORDED_RESPONSE, etag = RECORDED_ETAG) }

        source.fetch(knownVersion = RECORDED_ETAG)

        assertEquals(RECORDED_ETAG, lastRequest?.headers?.get(HttpHeaders.IfNoneMatch))
    }

    @Test
    fun `no known version sends no If-None-Match`() = runTest {
        val source = source { respondJson(RECORDED_RESPONSE, etag = RECORDED_ETAG) }

        source.fetch(knownVersion = null)

        assertNull(lastRequest?.headers?.get(HttpHeaders.IfNoneMatch))
    }

    @Test
    fun `request goes to the coverage path under the API version`() = runTest {
        val source = source { respondJson(RECORDED_RESPONSE, etag = RECORDED_ETAG) }

        source.fetch(knownVersion = null)

        assertTrue(lastRequest?.url?.encodedPath?.endsWith("/v1/coverage") == true)
    }

    @Test
    fun `304 means the known copy is current`() = runTest {
        val source = source { respond(content = "", status = HttpStatusCode.NotModified) }

        assertNull(source.fetch(knownVersion = RECORDED_ETAG))
    }

    @Test
    fun `server error is thrown as an HTTP error with its status`() = runTest {
        val source = source { respond(content = "", status = HttpStatusCode.InternalServerError) }

        val error = assertFailsWith<NetworkError.HttpError> { source.fetch(knownVersion = null) }

        assertEquals(500, error.statusCode)
    }

    private fun source(handler: MockRequestHandleScope.(HttpRequestData) -> HttpResponseData): ApiCoverageSource {
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
        return ApiCoverageSource(api)
    }

    private fun MockRequestHandleScope.respondJson(body: String, etag: String) = respond(
        content = body,
        status = HttpStatusCode.OK,
        headers = headersOf(
            HttpHeaders.ContentType to listOf("application/json"),
            HttpHeaders.ETag to listOf(etag),
        ),
    )

    private companion object {
        const val RECORDED_ETAG = "\"92f8a1c1ec35b7c24f45adc5\""

        // Recorded from the local backend, trimmed to the first polygon
        const val RECORDED_RESPONSE =
            """{"area":{"coordinates":[[[[12.09287,50.25032],[12.19335,50.19997],[12.21484,50.16399],[12.09287,50.25032]]]],"type":"MultiPolygon"},"vignetteCountries":["CZ","SK","AT","HU","SI"]}"""
    }
}
