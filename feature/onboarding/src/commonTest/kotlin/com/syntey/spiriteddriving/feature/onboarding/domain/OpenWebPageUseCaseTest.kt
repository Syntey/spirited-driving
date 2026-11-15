package com.syntey.spiriteddriving.feature.onboarding.domain

import com.syntey.spiriteddriving.core.platform.binding.PlatformWebBrowser
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class OpenWebPageUseCaseTest {

    @Test
    fun `opens the page in the in-app browser`() = runTest {
        val browser = FakeWebBrowser()

        OpenWebPageUseCase(browser).build(OpenWebPageUseCase.Args(url = "https://example.com/privacy"))

        assertEquals(listOf("https://example.com/privacy"), browser.openedUrls)
    }

    private class FakeWebBrowser : PlatformWebBrowser {
        val openedUrls = mutableListOf<String>()

        override fun open(url: String) {
            openedUrls += url
        }
    }
}
