package com.syntey.spiriteddriving.data.network.result

import com.syntey.spiriteddriving.core.domain.error.OfflineException
import io.ktor.http.HttpStatusCode
import kotlinx.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DomainErrorsTest {

    @Test
    fun `a connection error is thrown as offline, keeping the network error as its cause`() {
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
}
