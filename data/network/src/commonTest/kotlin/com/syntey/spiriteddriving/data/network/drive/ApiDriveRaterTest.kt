package com.syntey.spiriteddriving.data.network.drive

import com.syntey.spiriteddriving.core.domain.drive.DriveNotRatableException
import com.syntey.spiriteddriving.core.domain.drive.Rating
import com.syntey.spiriteddriving.core.domain.drive.RatingReason
import com.syntey.spiriteddriving.core.domain.drive.Thumb
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

class ApiDriveRaterTest {

    private var lastRequest: HttpRequestData? = null

    @Test
    fun `rating is put to the rating path of its drive under the API version`() = runTest {
        val rater = rater { respond(content = "", status = HttpStatusCode.NoContent) }

        rater.rate("d1", Rating(Thumb.Up))

        assertEquals(HttpMethod.Put, lastRequest?.method)
        assertEquals(true, lastRequest?.url?.encodedPath?.endsWith("/v1/drives/d1/rating"), lastRequest?.url.toString())
    }

    @Test
    fun `request body lists the reasons in the order the app shows them`() = runTest {
        val rater = rater { respond(content = "", status = HttpStatusCode.NoContent) }

        rater.rate("d1", Rating(Thumb.Up, setOf(RatingReason.NiceViews, RatingReason.CurvyRoads)))

        assertEquals("""{"thumb":"UP","reasons":["CURVY_ROADS","NICE_VIEWS"]}""", requestBody())
    }

    @Test
    fun `a no content response is a saved rating`() = runTest {
        val rater = rater { respond(content = "", status = HttpStatusCode.NoContent) }

        rater.rate("d1", Rating(Thumb.Down))
    }

    @Test
    fun `drive not found is thrown as not ratable with the HTTP error as its cause`() = runTest {
        val rater = rater { respond(content = "", status = HttpStatusCode.NotFound) }

        val error = assertFailsWith<DriveNotRatableException> { rater.rate("d1", Rating(Thumb.Up)) }

        assertEquals(404, (error.cause as? NetworkError.HttpError)?.statusCode)
    }

    @Test
    fun `drive of another user is thrown as not ratable`() = runTest {
        val rater = rater { respond(content = "", status = HttpStatusCode.Forbidden) }

        assertFailsWith<DriveNotRatableException> { rater.rate("d1", Rating(Thumb.Up)) }
    }

    @Test
    fun `no connection is thrown as offline`() = runTest {
        val rater = rater { throw IOException("Network is unreachable") }

        assertFailsWith<OfflineException> { rater.rate("d1", Rating(Thumb.Up)) }
    }

    private fun requestBody(): String = (lastRequest?.body as? TextContent)?.text ?: error("The request body is not text")

    private fun rater(handler: MockRequestHandleScope.(HttpRequestData) -> HttpResponseData): ApiDriveRater {
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
        return ApiDriveRater(api)
    }
}
