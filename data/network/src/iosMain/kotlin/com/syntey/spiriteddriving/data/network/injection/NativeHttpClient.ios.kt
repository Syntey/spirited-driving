package com.syntey.spiriteddriving.data.network.injection

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.darwin.Darwin

/**
 * Returns platform-native HTTP client for Ktor framework.
 */
actual fun getNativeHttpClient(configure: HttpClientConfig<*>.() -> Unit): HttpClient = HttpClient(Darwin) {
    configure()
}
