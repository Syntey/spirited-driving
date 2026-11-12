package com.syntey.spiriteddriving.gradle.configuration

/**
 * Defines all flavor-specific values provided to KMP during compilation.
 */
interface ProductFlavor {
    val name: String
    val restApiUrl: String

    /** The URL as the Android emulator reaches it, when it differs from [restApiUrl]. */
    val androidRestApiUrl: String get() = restApiUrl
}

object ProductFlavors {

    val DEFAULT = Dev

    object Dev : ProductFlavor {
        override val name: String = "dev"

        // The iOS simulator reaches the Mac as localhost
        override val restApiUrl: String = "http://localhost:8080/v1/"

        // The emulator reaches the Mac at 10.0.2.2
        override val androidRestApiUrl: String = "http://10.0.2.2:8080/v1/"
    }

    object Prod : ProductFlavor {
        override val name: String = "prod"
        override val restApiUrl: String = "https://swapi.info/api/"
    }
}
