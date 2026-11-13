package com.syntey.spiriteddriving.data.network.plugin

import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.HttpTimeout
import org.koin.core.annotation.Single
import com.syntey.spiriteddriving.data.network.tools.Constants

@Single
internal class HttpTimeoutPlugin : HttpClientPlugin {

    override fun install(config: HttpClientConfig<*>) {
        config.install(HttpTimeout) {
            connectTimeoutMillis = Constants.Http.CONNECT_TIMEOUT.inWholeMilliseconds
            requestTimeoutMillis = Constants.Http.REQUEST_TIMEOUT.inWholeMilliseconds
            socketTimeoutMillis = Constants.Http.SOCKET_TIMEOUT.inWholeMilliseconds
        }
    }
}
