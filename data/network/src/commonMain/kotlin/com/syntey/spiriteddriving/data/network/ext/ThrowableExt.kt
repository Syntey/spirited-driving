package com.syntey.spiriteddriving.data.network.ext

import com.syntey.spiriteddriving.data.network.result.NetworkError

/**
 * Checks whether given [Throwable] is [NetworkError.ConnectionError]
 */
fun Throwable.isConnectionError(): Boolean = this is NetworkError.ConnectionError
