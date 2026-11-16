package com.syntey.spiriteddriving.feature.profile.tools

import com.syntey.spiriteddriving.feature.profile.model.Licence
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class OpenSourceLibrariesParserTest {

    @Test
    fun `libraries are sorted by name, ignoring case`() {
        val libraries = OpenSourceLibrariesParser.parse(LIBRARIES_JSON)

        assertEquals(listOf("Activity", "collections", "Koin"), libraries.map { it.name })
    }

    @Test
    fun `a library keeps its id and its licence's name, text and url`() {
        val libraries = OpenSourceLibrariesParser.parse(LIBRARIES_JSON)

        val koin = libraries.single { it.name == "Koin" }
        assertEquals("io.insert-koin:koin-core", koin.id)
        assertEquals(
            listOf(
                Licence(
                    name = "Apache License 2.0",
                    text = "Apache License\nVersion 2.0",
                    url = "https://spdx.org/licenses/Apache-2.0.html",
                ),
            ),
            koin.licences,
        )
    }

    @Test
    fun `a licence without content has no text`() {
        val libraries = OpenSourceLibrariesParser.parse(LIBRARIES_JSON)

        val licence = libraries.single { it.name == "Activity" }.licences.single()
        assertNull(licence.text)
        assertEquals("https://developer.android.com/studio/terms.html", licence.url)
    }

    @Test
    fun `a licence with blank content has no text`() {
        val json = """
            {
              "libraries": [{"uniqueId": "a:b", "name": "Blank", "licenses": ["MIT"]}],
              "licenses": {"MIT": {"name": "MIT License", "content": "   "}}
            }
        """.trimIndent()

        val licence = OpenSourceLibrariesParser.parse(json).single().licences.single()

        assertNull(licence.text)
        assertNull(licence.url)
    }

    @Test
    fun `a licence key missing from the licences map is skipped`() {
        val json = """
            {
              "libraries": [{"uniqueId": "a:b", "name": "Partial", "licenses": ["Unknown-1.0", "MIT"]}],
              "licenses": {"MIT": {"name": "MIT License", "content": "Permission is granted"}}
            }
        """.trimIndent()

        val licences = OpenSourceLibrariesParser.parse(json).single().licences

        assertEquals(listOf(Licence(name = "MIT License", text = "Permission is granted", url = null)), licences)
    }

    @Test
    fun `fields the parser does not know are ignored`() {
        val json = """
            {
              "generator": "aboutlibraries",
              "libraries": [{"uniqueId": "a:b", "artifactVersion": "1.0", "name": "Known", "licenses": ["MIT"], "funding": {}}],
              "licenses": {"MIT": {"name": "MIT License", "hash": "MIT", "content": "Text"}}
            }
        """.trimIndent()

        val libraries = OpenSourceLibrariesParser.parse(json)

        assertEquals(
            listOf("Known"),
            libraries.map { it.name },
        )
        assertEquals("Text", libraries.single().licences.single().text)
    }

    @Test
    fun `a blank json has no libraries`() {
        assertEquals(emptyList(), OpenSourceLibrariesParser.parse("").toList())
    }

    private companion object {
        val LIBRARIES_JSON = """
            {
              "libraries": [
                {"uniqueId": "org.jetbrains.kotlinx:kotlinx-collections-immutable", "artifactVersion": "0.4.0", "name": "collections", "licenses": ["Apache-2.0"]},
                {"uniqueId": "androidx.activity:activity", "artifactVersion": "1.13.0", "name": "Activity", "licenses": ["ASDKL"]},
                {"uniqueId": "io.insert-koin:koin-core", "artifactVersion": "4.1.0", "name": "Koin", "licenses": ["Apache-2.0"]}
              ],
              "licenses": {
                "Apache-2.0": {"name": "Apache License 2.0", "url": "https://spdx.org/licenses/Apache-2.0.html", "content": "Apache License\nVersion 2.0", "hash": "Apache-2.0"},
                "ASDKL": {"name": "Android Software Development Kit License", "url": "https://developer.android.com/studio/terms.html", "hash": "ASDKL"}
              }
            }
        """.trimIndent()
    }
}
