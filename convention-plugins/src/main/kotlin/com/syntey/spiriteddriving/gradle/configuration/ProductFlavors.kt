package com.syntey.spiriteddriving.gradle.configuration

/**
 * Defines all flavor-specific values provided to KMP during compilation.
 */
interface ProductFlavor {
    val name: String
    val restApiUrl: String
}

object ProductFlavors {

    val DEFAULT = Dev

    object Dev : ProductFlavor {
        override val name: String = "dev"
        override val restApiUrl: String = "https://swapi.info/api/"
    }

    object Prod : ProductFlavor {
        override val name: String = "prod"
        override val restApiUrl: String = "https://swapi.info/api/"
    }
}
