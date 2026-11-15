package com.syntey.spiriteddriving.data.network.plugin

import com.syntey.spiriteddriving.core.platform.binding.AppCheck
import com.syntey.spiriteddriving.core.platform.binding.Auth
import com.syntey.spiriteddriving.core.platform.binding.AuthListenerRegistration
import com.syntey.spiriteddriving.core.platform.binding.AuthUser
import com.syntey.spiriteddriving.core.platform.binding.AuthUserListener
import com.syntey.spiriteddriving.core.platform.binding.PlatformAppCheck
import com.syntey.spiriteddriving.core.platform.binding.PlatformAuth
import com.syntey.spiriteddriving.core.platform.binding.SignInListener
import com.syntey.spiriteddriving.core.platform.binding.SignInProvider
import com.syntey.spiriteddriving.core.platform.binding.TokenListener
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respondOk
import io.ktor.client.request.get
import io.ktor.http.HttpHeaders
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AuthTokensPluginTest {

    @Test
    fun `calls carry the app check token and the bearer id token`() = runTest {
        val sent = sendRequest(appCheckToken = "app-check", idToken = "id-token")

        assertEquals("app-check", sent["X-Firebase-AppCheck"])
        assertEquals("Bearer id-token", sent[HttpHeaders.Authorization])
    }

    @Test
    fun `calls carry no app check header without a token`() = runTest {
        val sent = sendRequest(appCheckToken = null, idToken = "id-token")

        assertNull(sent["X-Firebase-AppCheck"])
        assertEquals("Bearer id-token", sent[HttpHeaders.Authorization])
    }

    @Test
    fun `calls carry no authorization header when signed out`() = runTest {
        val sent = sendRequest(appCheckToken = "app-check", idToken = null)

        assertEquals("app-check", sent["X-Firebase-AppCheck"])
        assertNull(sent[HttpHeaders.Authorization])
    }

    private suspend fun sendRequest(appCheckToken: String?, idToken: String?): Map<String, String?> {
        var sent: Map<String, String?>? = null
        val engine = MockEngine { request ->
            sent = mapOf(
                "X-Firebase-AppCheck" to request.headers["X-Firebase-AppCheck"],
                HttpHeaders.Authorization to request.headers[HttpHeaders.Authorization],
            )
            respondOk()
        }
        val client = HttpClient(engine) {
            AuthTokensPlugin(
                auth = Auth(FakePlatformAuth(idToken)),
                appCheck = AppCheck(FakePlatformAppCheck(appCheckToken)),
            ).install(this)
        }

        client.get("https://example.com/")

        return checkNotNull(sent)
    }
}

private class FakePlatformAuth(private val idToken: String?) : PlatformAuth {
    override fun currentUser(): AuthUser? = null
    override fun addUserListener(listener: AuthUserListener): AuthListenerRegistration = AuthListenerRegistration { }
    override fun signIn(provider: SignInProvider, listener: SignInListener) = Unit
    override fun signOut() = Unit
    override fun idToken(listener: TokenListener) = listener.onToken(idToken)
}

private class FakePlatformAppCheck(private val token: String?) : PlatformAppCheck {
    override fun token(listener: TokenListener) = listener.onToken(token)
}
