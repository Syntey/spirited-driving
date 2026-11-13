package com.syntey.spiriteddriving.data.network.feedback

import com.syntey.spiriteddriving.core.domain.error.OfflineException
import com.syntey.spiriteddriving.core.domain.feedback.Feedback
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
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ApiFeedbackSenderTest {

    private var lastRequest: HttpRequestData? = null

    @Test
    fun `accepted feedback is posted to the feedback path with its fields`() = runTest {
        val sender = sender { respond(content = "", status = HttpStatusCode.NoContent) }

        sender.send(feedback())

        assertEquals(HttpMethod.Post, lastRequest?.method)
        assertTrue(lastRequest?.url?.encodedPath?.endsWith("/v1/feedback") == true)
        val body = (lastRequest?.body as? TextContent)?.text ?: error("The request body is not text")
        assertTrue("\"message\":\"The map froze\"" in body, body)
        assertTrue("\"appBuild\":\"42\"" in body, body)
    }

    @Test
    fun `rate limited feedback is thrown as the HTTP error with its problem code`() = runTest {
        val sender = sender {
            respond(
                content = """{"title":"Rate limited","status":429,"code":"RATE_LIMITED","type":"about:blank"}""",
                status = HttpStatusCode.TooManyRequests,
                headers = headersOf(HttpHeaders.ContentType to listOf("application/problem+json")),
            )
        }

        val error = assertFailsWith<NetworkError.HttpError> { sender.send(feedback()) }

        assertEquals(429, error.statusCode)
        assertEquals("RATE_LIMITED", error.code)
    }

    @Test
    fun `rejected message is thrown as the HTTP error with its problem code`() = runTest {
        val sender = sender {
            respond(
                content = """{"title":"Validation failed","status":400,"code":"VALIDATION_FAILED","type":"about:blank"}""",
                status = HttpStatusCode.BadRequest,
                headers = headersOf(HttpHeaders.ContentType to listOf("application/problem+json")),
            )
        }

        val error = assertFailsWith<NetworkError.HttpError> { sender.send(feedback()) }

        assertEquals(400, error.statusCode)
        assertEquals("VALIDATION_FAILED", error.code)
    }

    @Test
    fun `server error is thrown as the HTTP error without a code`() = runTest {
        val sender = sender { respond(content = "", status = HttpStatusCode.InternalServerError) }

        val error = assertFailsWith<NetworkError.HttpError> { sender.send(feedback()) }

        assertEquals(500, error.statusCode)
        assertNull(error.code)
    }

    @Test
    fun `no connection is thrown as offline`() = runTest {
        val sender = sender { throw IOException("Network is unreachable") }

        assertFailsWith<OfflineException> { sender.send(feedback()) }
    }

    private fun feedback() = Feedback(
        message = "The map froze",
        appVersion = "1.0.0",
        appBuild = "42",
        os = "iOS 26.1",
        device = "iPhone 17 Pro",
    )

    private fun sender(handler: MockRequestHandleScope.(HttpRequestData) -> HttpResponseData): ApiFeedbackSender {
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
        return ApiFeedbackSender(api)
    }
}
