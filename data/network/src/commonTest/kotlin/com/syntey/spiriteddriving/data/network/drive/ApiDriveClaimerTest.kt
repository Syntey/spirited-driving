package com.syntey.spiriteddriving.data.network.drive

import com.syntey.spiriteddriving.core.domain.error.OfflineException
import com.syntey.spiriteddriving.data.network.api.createSpiritedDrivingApi
import com.syntey.spiriteddriving.data.network.injection.NetworkModule
import com.syntey.spiriteddriving.data.network.plugin.ContentNegotiationPlugin
import com.syntey.spiriteddriving.data.network.result.NetworkErrorParser
import com.syntey.spiriteddriving.data.network.result.NetworkResultConverterFactory
import de.jensklingenberg.ktorfit.Ktorfit
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import kotlinx.coroutines.test.runTest
import kotlinx.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ApiDriveClaimerTest {

    private var lastRequest: HttpRequestData? = null

    @Test
    fun `claim is posted to the claim path under the API version`() = runTest {
        val claimer = claimer { respond(content = "", status = HttpStatusCode.NoContent) }

        claimer.claim(listOf("a", "b"))

        assertEquals(HttpMethod.Post, lastRequest?.method)
        assertTrue(lastRequest?.url?.encodedPath?.endsWith("/v1/me/drives/claim") == true, lastRequest?.url.toString())
    }

    @Test
    fun `request body carries the drive IDs`() = runTest {
        val claimer = claimer { respond(content = "", status = HttpStatusCode.NoContent) }

        claimer.claim(listOf("a", "b"))

        val body = (lastRequest?.body as? TextContent)?.text ?: error("The request body is not text")
        assertEquals("""{"driveIds":["a","b"]}""", body)
    }

    @Test
    fun `a no content response is a successful claim`() = runTest {
        val claimer = claimer { respond(content = "", status = HttpStatusCode.NoContent) }

        claimer.claim(listOf("a"))
    }

    @Test
    fun `no connection is thrown as offline`() = runTest {
        val claimer = claimer { throw IOException("Network is unreachable") }

        assertFailsWith<OfflineException> { claimer.claim(listOf("a")) }
    }

    private fun claimer(handler: MockRequestHandleScope.(HttpRequestData) -> HttpResponseData): ApiDriveClaimer {
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
        return ApiDriveClaimer(api)
    }
}
