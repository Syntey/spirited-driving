package com.syntey.spiriteddriving.data.network.result

import com.syntey.spiriteddriving.core.domain.error.OfflineException
import com.syntey.spiriteddriving.core.domain.route.OutsideCoverageException
import io.ktor.http.HttpStatusCode
import kotlinx.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DomainErrorsTest {

    @Test
    fun `a connection error is thrown as offline with the network error as its cause`() {
        val connectionError = NetworkError.ConnectionError(IOException("Socket closed"))

        val error = assertFailsWith<OfflineException> {
            NetworkResult.error(connectionError).getOrThrowDomainError()
        }

        assertEquals(connectionError, error.cause)
    }

    @Test
    fun `an HTTP error is thrown as it is`() {
        val result = NetworkResult.error(NetworkError.HttpError(HttpStatusCode.ServiceUnavailable))

        val error = assertFailsWith<NetworkError.HttpError> { result.getOrThrowDomainError() }

        assertEquals(503, error.statusCode)
    }

    @Test
    fun `an HTTP error whose problem code the caller maps is thrown as the mapped error`() {
        val result = NetworkResult.error(NetworkError.HttpError(HttpStatusCode.UnprocessableEntity, code = "OUTSIDE_COVERAGE"))

        val error = assertFailsWith<OutsideCoverageException> {
            result.getOrThrowDomainError { OutsideCoverageException(cause = it) }
        }

        assertEquals("OUTSIDE_COVERAGE", (error.cause as NetworkError.HttpError).code)
    }

    @Test
    fun `an HTTP error whose problem code the caller doesn't map is thrown as the HTTP error`() {
        val result = NetworkResult.error(NetworkError.HttpError(HttpStatusCode.UnprocessableEntity, code = "NO_ROUTE"))

        val error = assertFailsWith<NetworkError.HttpError> {
            result.getOrThrowDomainError { null }
        }

        assertEquals("NO_ROUTE", error.code)
    }
}
