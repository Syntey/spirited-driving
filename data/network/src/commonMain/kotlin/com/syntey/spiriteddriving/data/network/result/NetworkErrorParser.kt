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
     * Parses provided [code] as [NetworkError.HttpError]
     */
    fun parse(code: HttpStatusCode): NetworkError = if (!code.isSuccess()) {
        NetworkError.HttpError(statusCode = code)
    } else {
        error("The provided code: $code is successful and cannot be parsed as error")
    }
}
