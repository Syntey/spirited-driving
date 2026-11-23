package com.syntey.spiriteddriving.data.network.plugin

import com.syntey.spiriteddriving.core.platform.binding.Platform
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.http.HttpHeaders
import org.koin.core.annotation.Single

/**
 * Sends the phone's preferred languages as `Accept-Language` on every call, so place names come in the user's language.
 * They're read for each request, as the user can change them while the app runs.
 */
@Single
internal class AcceptLanguagePlugin(private val platform: Platform) : HttpClientPlugin {

    override fun install(config: HttpClientConfig<*>) {
        config.install(
            createClientPlugin("AcceptLanguage") {
                onRequest { request, _ ->
                    acceptLanguage(platform.preferredLanguages)?.let { request.headers[HttpHeaders.AcceptLanguage] = it }
                }
            },
        )
    }
}

/** [tags] weighted in their order, such as `cs-CZ, en-US;q=0.9, de;q=0.8`; null without any. At most 10, so every weight stays above 0. */
internal fun acceptLanguage(tags: List<String>): String? =
    tags.take(MAX_LANGUAGES)
        .mapIndexed { index, tag -> if (index == 0) tag else "$tag;q=0.${MAX_LANGUAGES - index}" }
        .joinToString(", ")
        .ifEmpty { null }

private const val MAX_LANGUAGES = 10
