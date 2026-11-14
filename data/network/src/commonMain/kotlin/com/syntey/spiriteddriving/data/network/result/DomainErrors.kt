package com.syntey.spiriteddriving.data.network.result

import com.syntey.spiriteddriving.core.domain.error.OfflineException
import com.syntey.spiriteddriving.data.network.result.NetworkResult.Failure
import com.syntey.spiriteddriving.data.network.result.NetworkResult.Success

/**
 * Returns the encapsulated value like [getOrThrow], but a [NetworkError.ConnectionError] is thrown as [OfflineException], so
 * features can tell "no connection" apart from other failures. An HTTP error whose Problem code [problemError] knows is thrown
 * as the caller's domain error. Other errors are thrown as they are.
 */
internal inline fun <reified T> NetworkResult<T>.getOrThrowDomainError(
    problemError: (NetworkError.HttpError) -> Throwable? = { null },
): T = when (this) {
    is Success -> data

    is Failure -> throw when (val networkError = error) {
        is NetworkError.ConnectionError -> OfflineException(networkError)
        is NetworkError.HttpError -> networkError.code?.let { problemError(networkError) } ?: networkError
        else -> networkError
    }
}
