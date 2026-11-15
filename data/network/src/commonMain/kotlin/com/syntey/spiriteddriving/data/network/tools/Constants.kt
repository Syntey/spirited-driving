package com.syntey.spiriteddriving.data.network.tools

import io.ktor.client.plugins.logging.LogLevel
import kotlin.time.Duration.Companion.seconds

internal object Constants {

    object Http {
        val CONNECT_TIMEOUT = 10.seconds
        val REQUEST_TIMEOUT = 15.seconds
        val SOCKET_TIMEOUT = 10.seconds
        val LOG_LEVEL = LogLevel.ALL
    }

    /** The `code` of a Problem response that maps to a domain error. */
    object ProblemCode {
        // NO_ROUTE is left a generic failure: the UI doesn't explain it differently yet
        const val OUTSIDE_COVERAGE = "OUTSIDE_COVERAGE"
    }

    object RouteKindCode {
        const val FASTEST = "FASTEST"
    }

    object AuthHeaders {
        const val APP_CHECK = "X-Firebase-AppCheck"
        const val BEARER_PREFIX = "Bearer "
    }

    object PlaceKindCode {
        const val TOWN = "TOWN"
        const val STREET = "STREET"
        const val ADDRESS = "ADDRESS"
        const val NATURE = "NATURE"
    }
}
