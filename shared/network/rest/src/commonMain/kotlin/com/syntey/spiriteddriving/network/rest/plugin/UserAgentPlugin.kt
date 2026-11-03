package com.syntey.spiriteddriving.network.rest.plugin

import com.syntey.spiriteddriving.network.rest.FlavorConstants
import com.syntey.spiriteddriving.platform.binding.Platform
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.UserAgent
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single

@Single
internal class UserAgentPlugin(@Provided platform: Platform) : HttpClientPlugin {

    private val userAgentString = with(platform) {
        listOf(
            "$appName/$appVersionName",
            "($applicationId; build:$appBuildNumber; $osNameWithVersion; Model:$deviceModel)",
            "ktor-client/${FlavorConstants.ktorUserAgentVersion}",
        )
    }.joinToString(separator = " ")

    override fun install(config: HttpClientConfig<*>) {
        config.install(UserAgent) {
            agent = userAgentString
        }
    }
}
