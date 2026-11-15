package com.syntey.spiriteddriving.feature.profile.domain

import com.syntey.spiriteddriving.core.platform.binding.Platform
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class GetOpenSourceLibrariesUseCaseTest {

    @Test
    fun `returns the libraries in the platform's json`() = runTest {
        val libraries = GetOpenSourceLibrariesUseCase(FakePlatform(LIBRARY_JSON)).build(Unit)

        assertEquals(listOf("io.insert-koin:koin-core"), libraries.map { it.id })
    }

    private class FakePlatform(private val json: String) : Platform {
        override val appName = "Spirited Driving"
        override val appVersionName = "1.0.0"
        override val appBuildNumber = "42"
        override val applicationId = "app.id"
        override val osNameWithVersion = "iOS 26.1"
        override val deviceModel = "iPhone 17 Pro"
        override val storeReviewUrl = "https://store.test/app"
        override val preferredLanguages = listOf("en")
        override val regionCode: String? = null

        override fun countryName(code: String): String? = null

        override fun openSourceLibrariesJson(): String = json
    }

    private companion object {
        const val LIBRARY_JSON = """
            {
              "libraries": [{"uniqueId": "io.insert-koin:koin-core", "name": "Koin", "licenses": ["Apache-2.0"]}],
              "licenses": {"Apache-2.0": {"name": "Apache License 2.0", "content": "Apache License"}}
            }
        """
    }
}
