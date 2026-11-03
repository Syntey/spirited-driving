package com.syntey.spiriteddriving.network.rest.injection

import com.syntey.spiriteddriving.network.rest.FlavorConstants
import com.syntey.spiriteddriving.network.rest.api.StarWarsApi
import com.syntey.spiriteddriving.network.rest.api.createStarWarsApi
import com.syntey.spiriteddriving.network.rest.plugin.ContentNegotiationPlugin
import com.syntey.spiriteddriving.network.rest.plugin.HttpTimeoutPlugin
import com.syntey.spiriteddriving.network.rest.plugin.LoggingPlugin
import com.syntey.spiriteddriving.network.rest.plugin.UserAgentPlugin
import com.syntey.spiriteddriving.network.rest.result.NetworkResultConverterFactory
import de.jensklingenberg.ktorfit.Ktorfit
import io.ktor.client.HttpClient
import kotlinx.serialization.json.Json
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@Module
@ComponentScan("com.syntey.spiriteddriving.network.rest")
@Configuration
class NetworkRestModule {

    @Single
    @Named("apiUrl")
    internal fun apiUrl(): String = FlavorConstants.apiUrl

    @Single
    @Named("restApiJson")
    internal fun restApiJson(): Json = Json {
        isLenient = true
        prettyPrint = false
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    @Single
    internal fun httpClient(
        contentNegotiationPlugin: ContentNegotiationPlugin,
        httpTimeoutPlugin: HttpTimeoutPlugin,
        userAgentPlugin: UserAgentPlugin,
        loggingPlugin: LoggingPlugin,
    ): HttpClient = getNativeHttpClient {
        contentNegotiationPlugin.install(this)
        httpTimeoutPlugin.install(this)
        userAgentPlugin.install(this)
        loggingPlugin.install(this)
    }

    @Single
    internal fun ktorFit(
        @Named("apiUrl") apiUrl: String,
        httpClient: HttpClient,
        converterFactory: NetworkResultConverterFactory,
    ): Ktorfit = Ktorfit.Builder()
        .baseUrl(apiUrl)
        .httpClient(httpClient)
        .converterFactories(converterFactory)
        .build()

    @Single
    internal fun starWarsApi(ktorFit: Ktorfit): StarWarsApi = ktorFit.createStarWarsApi()
}
