package com.syntey.spiriteddriving.data.network.result

import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import io.ktor.util.network.UnresolvedAddressException
import kotlinx.io.IOException
import kotlinx.serialization.SerializationException
import org.koin.core.annotation.Single
import kotlin.coroutines.cancellation.CancellationException

/**
 * Class responsible for converting [Throwable]s into meaningful [NetworkError]s.
 */
@Single
internal class NetworkErrorParser {

    /**
     * Parses provided [throwable] into [NetworkError]. A [CancellationException] is rethrown, since it is the standard way
     * of cancelling a coroutine.
     */
    fun parse(throwable: Throwable): NetworkError = when (throwable) {
        is CancellationException -> throw throwable
        is SerializationException -> NetworkError.SerializationError(throwable)
        is IOException, is UnresolvedAddressException -> NetworkError.ConnectionError(throwable)
        else -> NetworkError.UnknownError(throwable)
    }

    /**
     * Parses provided [status] and the Problem [problemCode] of its body, if any, as [NetworkError.HttpError]
     */
    fun parse(status: HttpStatusCode, problemCode: String? = null): NetworkError = if (!status.isSuccess()) {
        NetworkError.HttpError(statusCode = status, code = problemCode)
    } else {
        error("The provided status: $status is successful and cannot be parsed as error")
    }
}
