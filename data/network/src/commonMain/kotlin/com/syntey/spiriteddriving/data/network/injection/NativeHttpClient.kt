package com.syntey.spiriteddriving.data.network.injection

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig

/**
 * Returns platform-native HTTP client for Ktor framework.
 */
expect fun getNativeHttpClient(configure: HttpClientConfig<*>.() -> Unit): HttpClient
