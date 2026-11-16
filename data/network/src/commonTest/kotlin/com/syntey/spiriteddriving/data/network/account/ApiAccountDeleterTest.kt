package com.syntey.spiriteddriving.data.network.account

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
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.HttpHeaders
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlinx.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ApiAccountDeleterTest {

    private var lastRequest: HttpRequestData? = null

    @Test
    fun `delete is sent to the me path under the API version`() = runTest {
        val deleter = deleter { respond(content = "", status = HttpStatusCode.NoContent) }

        deleter.delete()

        assertEquals(HttpMethod.Delete, lastRequest?.method)
        assertTrue(lastRequest?.url?.encodedPath?.endsWith("/v1/me") == true, lastRequest?.url.toString())
    }

    @Test
    fun `a no content response is a successful deletion`() = runTest {
        val deleter = deleter { respond(content = "", status = HttpStatusCode.NoContent) }

        deleter.delete()
    }

    @Test
    fun `an invalid ID token problem means the account is already deleted`() = runTest {
        val deleter = deleter {
            respond(
                content = """{"type":"about:blank","title":"Invalid ID token","status":401,"code":"ID_TOKEN_INVALID"}""",
                status = HttpStatusCode.Unauthorized,
                headers = headersOf(HttpHeaders.ContentType to listOf("application/problem+json")),
            )
        }

        deleter.delete()
    }

    @Test
    fun `an unauthorized problem with another code is thrown`() = runTest {
        val deleter = deleter {
            respond(
                content = """{"type":"about:blank","title":"Unauthorized","status":401,"code":"APP_CHECK_INVALID"}""",
                status = HttpStatusCode.Unauthorized,
                headers = headersOf(HttpHeaders.ContentType to listOf("application/problem+json")),
            )
        }

        val error = assertFailsWith<NetworkError.HttpError> { deleter.delete() }

        assertEquals(401, error.statusCode)
        assertEquals("APP_CHECK_INVALID", error.code)
    }

    @Test
    fun `service unavailable is thrown as the HTTP error with its problem code`() = runTest {
        val deleter = deleter {
            respond(
                content = """{"type":"about:blank","title":"Unavailable","status":503,"code":"UNAVAILABLE"}""",
                status = HttpStatusCode.ServiceUnavailable,
                headers = headersOf(HttpHeaders.ContentType to listOf("application/problem+json")),
            )
        }

        val error = assertFailsWith<NetworkError.HttpError> { deleter.delete() }

        assertEquals(503, error.statusCode)
        assertEquals("UNAVAILABLE", error.code)
    }

    @Test
    fun `no connection is thrown as offline`() = runTest {
        val deleter = deleter { throw IOException("Network is unreachable") }

        assertFailsWith<OfflineException> { deleter.delete() }
    }

    private fun deleter(handler: MockRequestHandleScope.(HttpRequestData) -> HttpResponseData): ApiAccountDeleter {
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
        return ApiAccountDeleter(api)
    }
}
