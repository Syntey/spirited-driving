package com.syntey.spiriteddriving.data.network.plugin

import com.syntey.spiriteddriving.core.platform.binding.AppCheck
import com.syntey.spiriteddriving.core.platform.binding.Auth
import com.syntey.spiriteddriving.data.network.tools.Constants
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.http.HttpHeaders
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single

/**
 * Sends the Firebase App Check token and the signed-in user's Firebase ID token with every call. Both are read for each
 * request, as they are refreshed by the SDK and the user can sign in or out while the app runs. A header is left out
 * when its token is null.
 */
@Single
internal class AuthTokensPlugin(@Provided private val auth: Auth, @Provided private val appCheck: AppCheck) : HttpClientPlugin {

    override fun install(config: HttpClientConfig<*>) {
        config.install(
            createClientPlugin("AuthTokens") {
                onRequest { request, _ ->
                    appCheck.token()?.let { request.headers[Constants.AuthHeaders.APP_CHECK] = it }
                    auth.idToken()?.let { request.headers[HttpHeaders.Authorization] = Constants.AuthHeaders.BEARER_PREFIX + it }
                }
            },
        )
    }
}
