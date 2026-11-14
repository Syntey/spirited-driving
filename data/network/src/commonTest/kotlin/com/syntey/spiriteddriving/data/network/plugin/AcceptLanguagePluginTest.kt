package com.syntey.spiriteddriving.data.network.plugin

import com.syntey.spiriteddriving.core.platform.binding.Platform
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respondOk
import io.ktor.client.request.get
import io.ktor.http.HttpHeaders
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AcceptLanguagePluginTest {

    @Test
    fun `languages are weighted in their order`() {
        assertEquals("cs-CZ, en-US;q=0.9, de;q=0.8", acceptLanguage(listOf("cs-CZ", "en-US", "de")))
    }

    @Test
    fun `no languages sends no header value`() {
        assertNull(acceptLanguage(emptyList()))
    }

    @Test
    fun `only the first ten languages are sent`() {
        val header = checkNotNull(acceptLanguage(List(12) { "lang$it" }))

        val entries = header.split(", ")
        assertEquals(10, entries.size)
        assertTrue(entries.last().endsWith(";q=0.1"))
    }

    @Test
    fun `every call carries the languages`() = runTest {
        var sentHeader: String? = null
        val engine = MockEngine { request ->
            sentHeader = request.headers[HttpHeaders.AcceptLanguage]
            respondOk()
        }
        val client = HttpClient(engine) {
            AcceptLanguagePlugin(FakePlatform).install(this)
        }

        client.get("https://example.com/")

        assertEquals("cs-CZ, en;q=0.9", sentHeader)
    }
}

private object FakePlatform : Platform {
    override val appName = "App"
    override val appVersionName = "1.0"
    override val appBuildNumber = "1"
    override val applicationId = "app.id"
    override val osNameWithVersion = "OS 1"
    override val deviceModel = "Model"
    override val preferredLanguages = listOf("cs-CZ", "en")
    override val regionCode: String? = "CZ"
}
