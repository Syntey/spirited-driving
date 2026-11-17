package com.syntey.spiriteddriving.data.network.drive

import com.syntey.spiriteddriving.core.domain.drive.Drive
import com.syntey.spiriteddriving.core.domain.drive.DriveNotRecordableException
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
import io.ktor.http.content.TextContent
import kotlinx.coroutines.test.runTest
import kotlinx.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

class ApiDriveRecorderTest {

    private var lastRequest: HttpRequestData? = null

    @Test
    fun `drive is put to its ID's path under the API version`() = runTest {
        val recorder = recorder { respond(content = "", status = HttpStatusCode.NoContent) }

        recorder.record(drive())

        assertEquals(HttpMethod.Put, lastRequest?.method)
        assertEquals(true, lastRequest?.url?.encodedPath?.endsWith("/v1/drives/$DRIVE_ID"), lastRequest?.url.toString())
    }

    @Test
    fun `request body carries the route ID and the start time`() = runTest {
        val recorder = recorder { respond(content = "", status = HttpStatusCode.NoContent) }

        recorder.record(drive())

        val body = requestBody()
        assertTrue("\"routeId\":\"$ROUTE_ID\"" in body, body)
        assertTrue("\"startedAt\":\"2026-11-13T10:15:30Z\"" in body, body)
    }

    @Test
    fun `a no content response is a recorded drive`() = runTest {
        val recorder = recorder { respond(content = "", status = HttpStatusCode.NoContent) }

        recorder.record(drive())
    }

    @Test
    fun `route not found is thrown as not recordable with the HTTP error as its cause`() = runTest {
        val recorder = recorder { respond(content = "", status = HttpStatusCode.NotFound) }

        val error = assertFailsWith<DriveNotRecordableException> { recorder.record(drive()) }

        val cause = assertIs<NetworkError.HttpError>(error.cause)
        assertEquals(404, cause.statusCode)
    }

    @Test
    fun `drive ID of another route is thrown as not recordable`() = runTest {
        val recorder = recorder { respond(content = "", status = HttpStatusCode.Conflict) }

        val error = assertFailsWith<DriveNotRecordableException> { recorder.record(drive()) }

        val cause = assertIs<NetworkError.HttpError>(error.cause)
        assertEquals(409, cause.statusCode)
    }

    @Test
    fun `server error is thrown as the HTTP error and not as not recordable`() = runTest {
        val recorder = recorder { respond(content = "", status = HttpStatusCode.TooManyRequests) }

        val error = assertFailsWith<NetworkError.HttpError> { recorder.record(drive()) }

        assertEquals(429, error.statusCode)
    }

    @Test
    fun `no connection is thrown as offline`() = runTest {
        val recorder = recorder { throw IOException("Network is unreachable") }

        assertFailsWith<OfflineException> { recorder.record(drive()) }
    }

    @Test
    fun `removing a drive sends DELETE to its ID's path under the API version`() = runTest {
        val recorder = recorder { respond(content = "", status = HttpStatusCode.NoContent) }

        recorder.remove(DRIVE_ID)

        assertEquals(HttpMethod.Delete, lastRequest?.method)
        assertEquals(true, lastRequest?.url?.encodedPath?.endsWith("/v1/drives/$DRIVE_ID"), lastRequest?.url.toString())
    }

    @Test
    fun `a no content response is a removed drive`() = runTest {
        val recorder = recorder { respond(content = "", status = HttpStatusCode.NoContent) }

        recorder.remove(DRIVE_ID)
    }

    @Test
    fun `a drive of another user counts as removed`() = runTest {
        val recorder = recorder { respond(content = "", status = HttpStatusCode.Forbidden) }

        recorder.remove(DRIVE_ID)
    }

    @Test
    fun `removing without a connection is thrown as offline`() = runTest {
        val recorder = recorder { throw IOException("Network is unreachable") }

        assertFailsWith<OfflineException> { recorder.remove(DRIVE_ID) }
    }

    private fun drive() = Drive(
        id = DRIVE_ID,
        routeId = ROUTE_ID,
        startedAt = Instant.parse("2026-11-13T10:15:30Z"),
        plannedDuration = 3900.seconds,
    )

    private fun requestBody(): String = (lastRequest?.body as? TextContent)?.text ?: error("The request body is not text")

    private fun recorder(handler: MockRequestHandleScope.(HttpRequestData) -> HttpResponseData): ApiDriveRecorder {
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
        return ApiDriveRecorder(api)
    }

    private companion object {
        const val DRIVE_ID = "3f2b8c1e-5d4a-4e7b-9a6c-1d2e3f4a5b6c"
        const val ROUTE_ID = "9c4e7a2d-1b3f-4c8e-8d5a-6f7b2e9c0a1d"
    }
}
