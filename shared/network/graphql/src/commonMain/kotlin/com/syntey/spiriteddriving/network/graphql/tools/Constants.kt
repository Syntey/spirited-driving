package com.syntey.spiriteddriving.network.graphql.tools

import com.syntey.spiriteddriving.network.graphql.FlavorConstants
import kotlin.time.Duration.Companion.seconds

internal object Constants {

    object Configuration {
        val API_URL = FlavorConstants.apiUrl
        val REQUEST_TIMEOUT = 20.seconds
    }
}
