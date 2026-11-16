package com.syntey.spiriteddriving.feature.profile.tools

import com.syntey.spiriteddriving.feature.profile.model.Licence
import com.syntey.spiriteddriving.feature.profile.model.OpenSourceLibrary
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** Reads the JSON the AboutLibraries Gradle plugin generates from the app's dependencies at build time. */
internal object OpenSourceLibrariesParser {

    // The generated file carries more fields than the app shows, and newer plugin versions add more
    private val format = Json { ignoreUnknownKeys = true }

    /** Blank JSON, which a build without the generated file has, gives no libraries. */
    fun parse(json: String): ImmutableList<OpenSourceLibrary> {
        if (json.isBlank()) return persistentListOf()

        val document = format.decodeFromString<LibrariesDto>(json)
        return document.libraries
            .map { library ->
                OpenSourceLibrary(
                    id = library.uniqueId,
                    name = library.name,
                    // A licence key missing from the map has no text or url to show, so it's skipped
                    licences = library.licenses.mapNotNull { key ->
                        document.licenses[key]?.let { licence ->
                            Licence(
                                name = licence.name,
                                text = licence.content?.takeIf(String::isNotBlank),
                                url = licence.url,
                            )
                        }
                    }.toImmutableList(),
                )
            }
            .sortedWith(compareBy<OpenSourceLibrary> { it.name.lowercase() }.thenBy { it.id })
            .toImmutableList()
    }

    @Serializable
    private class LibrariesDto(val libraries: List<LibraryDto> = emptyList(), val licenses: Map<String, LicenceDto> = emptyMap())

    @Serializable
    private class LibraryDto(val uniqueId: String, val name: String, val licenses: List<String> = emptyList())

    @Serializable
    private class LicenceDto(val name: String, val url: String? = null, val content: String? = null)
}
