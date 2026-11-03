package com.syntey.spiriteddriving.network.rest.ext

import com.syntey.spiriteddriving.network.rest.result.NetworkError

/**
 * Checks whether given [Throwable] is [NetworkError.ConnectionError]
 */
fun Throwable.isConnectionError(): Boolean = this is NetworkError.ConnectionError
