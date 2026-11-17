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
        const val ID_TOKEN_INVALID = "ID_TOKEN_INVALID"
    }

    object ThumbCode {
        const val UP = "UP"
        const val DOWN = "DOWN"
    }

    object RatingReasonCode {
        // Positive
        const val CURVY_ROADS = "CURVY_ROADS"
        const val NICE_VIEWS = "NICE_VIEWS"
        const val LITTLE_TRAFFIC = "LITTLE_TRAFFIC"
        const val SMOOTH_SURFACE = "SMOOTH_SURFACE"
        const val WORTH_THE_EXTRA_TIME = "WORTH_THE_EXTRA_TIME"

        // Negative
        const val BORING_ROADS = "BORING_ROADS"
        const val TOO_MANY_TOWNS = "TOO_MANY_TOWNS"
        const val TOO_NARROW = "TOO_NARROW"
        const val ROUGH_SURFACE = "ROUGH_SURFACE"
        const val HEAVY_TRAFFIC = "HEAVY_TRAFFIC"
        const val ROADWORKS = "ROADWORKS"
        const val TOOK_TOO_LONG = "TOOK_TOO_LONG"
        const val MAPS_APP_WENT_ANOTHER_WAY = "MAPS_APP_WENT_ANOTHER_WAY"
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
