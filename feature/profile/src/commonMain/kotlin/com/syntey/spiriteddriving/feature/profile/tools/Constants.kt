package com.syntey.spiriteddriving.feature.profile.tools

import com.syntey.spiriteddriving.core.domain.settings.CountryCode

internal object Constants {

    object Vignettes {
        // Asked about until the covered area has loaded, which lists the countries the app covers
        val BUILT_IN: List<CountryCode> = listOf(CountryCode.Czechia)
    }

    object Account {
        // Apple's Hide My Email addresses end in this domain; they mean nothing to the user, so the row names the provider
        const val APPLE_PRIVATE_RELAY_DOMAIN = "privaterelay.appleid.com"
    }

    object Feedback {
        // The backend rejects a message longer than this after trimming
        const val MESSAGE_MAX_LENGTH = 2000
    }

    object Licences {
        // The attribution's page, which the OpenStreetMap licence asks users to be able to reach
        const val OPENSTREETMAP_COPYRIGHT_URL = "https://www.openstreetmap.org/copyright"

        // Phosphor isn't in the generated libraries JSON, its icons are bundled as SVGs
        const val PHOSPHOR_ID = "phosphor-icons"
        const val PHOSPHOR_LICENCE_URL = "https://github.com/phosphor-icons/core/blob/main/LICENSE"

        val PHOSPHOR_LICENCE_TEXT: String = """
            MIT License

            Copyright (c) 2023 Phosphor Icons

            Permission is hereby granted, free of charge, to any person obtaining a copy
            of this software and associated documentation files (the "Software"), to deal
            in the Software without restriction, including without limitation the rights
            to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
            copies of the Software, and to permit persons to whom the Software is
            furnished to do so, subject to the following conditions:

            The above copyright notice and this permission notice shall be included in all
            copies or substantial portions of the Software.

            THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
            IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
            FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
            AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
            LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
            OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
            SOFTWARE.
        """.trimIndent()
    }
}
