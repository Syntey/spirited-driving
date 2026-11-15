package com.syntey.spiriteddriving.feature.profile.domain

import com.syntey.spiriteddriving.core.platform.binding.Platform
import com.syntey.spiriteddriving.feature.profile.model.Licence
import com.syntey.spiriteddriving.feature.profile.tools.Constants
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetLicencesUseCaseTest {

    @Test
    fun `the Phosphor id returns its MIT licence with the bundled text`() = runTest {
        val licences = GetLicencesUseCase(FakePlatform(LIBRARIES_JSON)).build(GetLicencesUseCase.Args(Constants.Licences.PHOSPHOR_ID))

        assertEquals(
            listOf(
                Licence(
                    name = "MIT License",
                    text = Constants.Licences.PHOSPHOR_LICENCE_TEXT,
                    url = "https://github.com/phosphor-icons/core/blob/main/LICENSE",
                ),
            ),
            licences,
        )
        assertTrue(licences.single().text.orEmpty().startsWith("MIT License\n\nCopyright (c) 2023 Phosphor Icons"))
    }

    @Test
    fun `a known library returns its licences`() = runTest {
        val licences = GetLicencesUseCase(FakePlatform(LIBRARIES_JSON)).build(GetLicencesUseCase.Args("io.insert-koin:koin-core"))

        assertEquals(
            listOf(
                Licence(
                    name = "Apache License 2.0",
                    text = "Apache License\nVersion 2.0",
                    url = "https://spdx.org/licenses/Apache-2.0.html",
                ),
            ),
            licences,
        )
    }

    @Test
    fun `an unknown library returns no licences`() = runTest {
        val licences = GetLicencesUseCase(FakePlatform(LIBRARIES_JSON)).build(GetLicencesUseCase.Args("com.example:missing"))

        assertEquals(persistentListOf<Licence>(), licences)
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
        const val LIBRARIES_JSON = """
            {
              "libraries": [{"uniqueId": "io.insert-koin:koin-core", "name": "Koin", "licenses": ["Apache-2.0"]}],
              "licenses": {"Apache-2.0": {"name": "Apache License 2.0", "url": "https://spdx.org/licenses/Apache-2.0.html", "content": "Apache License\nVersion 2.0"}}
            }
        """
    }
}
