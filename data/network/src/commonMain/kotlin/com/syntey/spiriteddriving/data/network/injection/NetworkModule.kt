package com.syntey.spiriteddriving.data.network.injection

import com.syntey.spiriteddriving.data.network.FlavorConstants
import com.syntey.spiriteddriving.data.network.api.SpiritedDrivingApi
import com.syntey.spiriteddriving.data.network.api.createSpiritedDrivingApi
import com.syntey.spiriteddriving.data.network.plugin.AcceptLanguagePlugin
import com.syntey.spiriteddriving.data.network.plugin.ContentNegotiationPlugin
import com.syntey.spiriteddriving.data.network.plugin.HttpTimeoutPlugin
import com.syntey.spiriteddriving.data.network.plugin.LoggingPlugin
import com.syntey.spiriteddriving.data.network.plugin.UserAgentPlugin
import com.syntey.spiriteddriving.data.network.result.NetworkResultConverterFactory
import de.jensklingenberg.ktorfit.Ktorfit
import io.ktor.client.HttpClient
import kotlinx.serialization.json.Json
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@Module
@ComponentScan("com.syntey.spiriteddriving.data.network")
@Configuration
class NetworkModule {

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
        acceptLanguagePlugin: AcceptLanguagePlugin,
        loggingPlugin: LoggingPlugin,
    ): HttpClient = getNativeHttpClient {
        contentNegotiationPlugin.install(this)
        httpTimeoutPlugin.install(this)
        userAgentPlugin.install(this)
        acceptLanguagePlugin.install(this)
        loggingPlugin.install(this)
    }

    @Single
    internal fun spiritedDrivingApi(ktorFit: Ktorfit): SpiritedDrivingApi = ktorFit.createSpiritedDrivingApi()

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
}
