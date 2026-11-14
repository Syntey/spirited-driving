package com.syntey.spiriteddriving.data.network.plugin

import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import org.koin.core.annotation.Single
import com.syntey.spiriteddriving.data.network.tools.Constants

@Single
internal class LoggingPlugin : HttpClientPlugin {

    override fun install(config: HttpClientConfig<*>) {
        config.install(Logging) {
            this.level = Constants.Http.LOG_LEVEL
            this.logger = KtorKermitLogger()
        }
    }

    /**
     * Custom Ktor logger that uses Kermit to log the logs 🪵.
     */
    private class KtorKermitLogger : Logger {

        private val logger = co.touchlab.kermit.Logger.withTag("Ktor")

        override fun log(message: String) = logger.d { message }
    }
}
